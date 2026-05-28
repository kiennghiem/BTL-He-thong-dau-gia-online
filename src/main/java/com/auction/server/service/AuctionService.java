package com.auction.server.service;

import com.auction.exceptions.AuctionNotFoundException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.*;
import com.auction.models.dto.AppConstants;
import com.auction.server.database.dao.BidDAO;
import com.auction.server.database.dao.DAOFactory;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.database.dao.AuctionDAO;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.manager.AuctionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * AuctionService acts as the orchestrator between the Network Handlers,
 * the Real-time AuctionManager, and the Database DAOs.
 */
public class AuctionService {
    private final AuctionManager auctionManager;
    private final ItemDAO itemDAO;
    private final BidDAO bidDAO;
    private final AuctionDAO auctionDAO;
    private final UserDAO userDAO;

    // Background thread pool for non-blocking database persistence
    private final ExecutorService persistenceExecutor;

    public AuctionService() {
        this.auctionManager = AuctionManager.getInstance();
        this.itemDAO = DAOFactory.getItemDAO();
        this.bidDAO = DAOFactory.getBidDAO();
        this.auctionDAO = DAOFactory.getAuctionDAO();
        this.userDAO = DAOFactory.getUserDAO();
        this.persistenceExecutor = Executors.newFixedThreadPool(4); // Adjust based on load
    }

    /**
     * Returns all auctions created by a specific seller.
     */
    public List<Auction> getAuctionsBySeller(String sellerId) {
        return auctionManager.getAllAuctions().stream()
                .filter(a -> a.getSeller() != null && sellerId.equals(a.getSeller().getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Processes payment for a finished auction.
     */
    public boolean processPayment(String auctionId, String bidderId) {
        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null || auction.getAuctionStatus() != AuctionStatus.FINISHED) {
            return false;
        }

        // Kiểm tra xem người yêu cầu thanh toán có phải người thắng không
        if (!bidderId.equals(auction.getHighestBidderId())) {
            return false;
        }

        double amount = auction.getCurrentPrice().doubleValue();
        
        try {
            User winner = userDAO.findByUsername(bidderId);
            if (winner == null || winner.getBalance() < amount) {
                return false;
            }

            // Thực hiện trừ tiền và cập nhật trạng thái
            winner.withdraw(amount);
            userDAO.updateUser(winner);
            
            auction.updateStatus(AuctionStatus.PAID);
            auctionDAO.updateStatus(auctionId, "PAID");
            
            System.out.println("[AuctionService] Payment successful for auction: " + auctionId);
            return true;
        } catch (Exception e) {
            System.err.println("[AuctionService] Payment error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks for auctions that were finished more than X days ago and marks them as CANCELED.
     */
    public void expireUnpaidAuctions() {
        LocalDateTime limit = LocalDateTime.now().minusDays(AppConstants.PAYMENT_WINDOW_DAYS);
        try {
            List<Auction> finishedAuctions = auctionDAO.findByStatus("FINISHED");
            for (Auction auction : finishedAuctions) {
                // Giả sử endTime là thời điểm kết thúc đấu giá
                if (auction.getEndTime() != null && auction.getEndTime().isBefore(limit)) {
                    System.out.println("[AuctionService] Expiring unpaid auction: " + auction.getId());
                    auction.updateStatus(AuctionStatus.CANCELED);
                    auctionDAO.updateStatus(auction.getId(), "CANCELED");
                    
                    // Cập nhật trạng thái trong manager nếu đang có trong memory
                    Auction liveAuction = auctionManager.getAuction(auction.getId());
                    if (liveAuction != null) {
                        liveAuction.updateStatus(AuctionStatus.CANCELED);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AuctionService] Error expiring auctions: " + e.getMessage());
        }
    }

    /**
     * Finalizes an auction in the database.
     */
    public void finishAuction(String auctionId) {
        Auction auction = auctionManager.getAuction(auctionId);
        if (auction != null) {
            persistenceExecutor.submit(() -> {
                try {
                    boolean updated = auctionDAO.updateStatus(auctionId, "FINISHED");
                    if (updated) {
                        System.out.println("[AuctionService-Async] Auction " + auctionId + " finalized in DB.");
                    }
                } catch (SQLException e) {
                    System.err.println("[AuctionService-Async] Error finalizing auction status in DB: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Places a bid, updates real-time state, and persists to DB.
     */
    public void placeBid(String auctionId, String bidderId, double amount)
            throws InvalidBidException, AuctionNotFoundException {

        // 1. Kiểm tra số dư người dùng trước khi đặt giá
        User bidder = userDAO.findByUsername(bidderId);
        if (bidder == null) {
            throw new AuctionNotFoundException("User not found: " + bidderId);
        }
        
        if (bidder.getBalance() < amount) {
            throw new InvalidBidException("Số dư tài khoản không đủ để thực hiện đặt giá này!");
        }

        BidTransaction bid = new BidTransaction(auctionId, bidderId, BigDecimal.valueOf(amount));
        auctionManager.processBid(auctionId, bid);

        persistenceExecutor.submit(() -> {
            boolean saved = bidDAO.placeBid(bid);
            if (!saved) {
                System.err.println("[AuctionService-Async] CRITICAL: Bid for " + bidderId +
                        " on " + auctionId + " failed to save to DB via BidDAO!");
            }

            try {
                boolean auctionUpdated = auctionDAO.placeBid(auctionId, bidderId, BigDecimal.valueOf(amount));
                if (!auctionUpdated) {
                    System.err.println("[AuctionService-Async] CRITICAL: Auction atomic bid constraints rejected the value for: " + auctionId);
                }
            } catch (SQLException e) {
                System.err.println("[AuctionService-Async] CRITICAL: Database error updating auction current balance: " + e.getMessage());
            }
        });
    }

    /**
     * Fetches the bid history for a specific auction to be used in charts.
     */
    public List<BidTransaction> getBidHistory(String auctionId) {
        return bidDAO.getHistoryByItem(auctionId);
    }

    /**
     * Updates an existing auction (Seller functionality).
     * Only allowed if the auction status is PENDING or OPEN.
     */
    public boolean updateAuction(String auctionId, String sellerId, String newName, String newDesc, double newStartingPrice) {
        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) return false;

        // 1. Kiểm tra quyền sở hữu và trạng thái
        if (!auction.getSeller().getUsername().equals(sellerId)) return false;
        
        AuctionStatus status = auction.getAuctionStatus();
        if (status != AuctionStatus.PENDING && status != AuctionStatus.OPEN) {
            System.err.println("[AuctionService] Cannot update auction that is already RUNNING or FINISHED.");
            return false;
        }

        // 2. Cập nhật trong Memory
        auction.setTitle(newName);
        auction.setDescription(newDesc);
        auction.setStartingPrice(BigDecimal.valueOf(newStartingPrice));
        if (auction.getHighestBid() == null) {
            auction.setCurrentPrice(BigDecimal.valueOf(newStartingPrice));
        }

        // 3. Cập nhật trong Database (Async)
        persistenceExecutor.submit(() -> {
            try {
                auctionDAO.update(auction);
                // Đồng bộ cả bảng items
                com.auction.models.Item item = itemDAO.findById(auction.getItemId());
                if (item != null) {
                    item.setItemName(newName);
                    item.setDescription(newDesc);
                    item.setStartingPrice(newStartingPrice);
                    itemDAO.updateItem(item);
                }
                System.out.println("[AuctionService] Auction " + auctionId + " updated successfully.");
            } catch (SQLException e) {
                System.err.println("[AuctionService] Failed to update auction in DB: " + e.getMessage());
            }
        });

        return true;
    }

    /**
     * Deletes an auction (Seller functionality).
     * Only allowed if the auction hasn't started or has no bids.
     */
    public boolean deleteAuction(String auctionId, String sellerId) {
        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) return false;

        // 1. Kiểm tra quyền sở hữu và trạng thái (Chỉ được xóa khi chưa có ai đặt giá)
        if (!auction.getSeller().getUsername().equals(sellerId)) return false;
        if (auction.getHighestBid() != null) {
            System.err.println("[AuctionService] Cannot delete auction with active bids.");
            return false;
        }

        // 2. Xóa khỏi Memory
        auctionManager.removeAuction(auctionId);

        // 3. Xóa khỏi Database (Async)
        persistenceExecutor.submit(() -> {
            try {
                boolean auctionDeleted = auctionDAO.delete(auctionId); 
                boolean itemDeleted = itemDAO.deleteItem(auction.getItemId());
                if (auctionDeleted && itemDeleted) {
                    System.out.println("[AuctionService] Auction " + auctionId + " deleted successfully.");
                }
            } catch (Exception e) {
                System.err.println("[AuctionService] Failed to delete auction from DB: " + e.getMessage());
            }
        });

        return true;
    }

    /**
     * Creates a new auction (Seller functionality).
     */
    public boolean createAuction(ItemType type, String name, String desc, double startingPrice,
                                 String specAttr, Seller seller, LocalDateTime start,
                                 LocalDateTime end, double minIncrement) {

        // 1. RESOLVED: Manually construct the correct com.auction.models.Item subclass to resolve package conflicts
        com.auction.models.Item item;
        switch (type.name()) {
            case "ELECTRONICS":
                com.auction.models.Electronics e = new com.auction.models.Electronics(name, desc, startingPrice, specAttr, seller);
                item = e;
                break;
            case "VEHICLE":
                com.auction.models.Vehicle v = new com.auction.models.Vehicle(name, desc, startingPrice, specAttr, seller);
                item = v;
                break;
            case "ART":
                com.auction.models.Art a = new com.auction.models.Art(name, desc, startingPrice, specAttr, seller);
                item = a;
                break;
            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }

        String itemId = java.util.UUID.randomUUID().toString();
        item.setId(itemId);

        // 2. Create Auction object
        Auction auction = new Auction();
        auction.setId(itemId);
        auction.setItemId(itemId);
        auction.setTitle(name);
        auction.setDescription(desc);
        auction.setStartingPrice(BigDecimal.valueOf(startingPrice));
        auction.setCurrentPrice(BigDecimal.valueOf(startingPrice));
        auction.setStartTime(start);
        auction.setEndTime(end);
        auction.setStatus("RUNNING");

        // 3. Persist both sequentially to Database
        boolean itemSaved = itemDAO.addItem(item); // Compiles perfectly with common.Item
        boolean auctionSaved = false;

        if (itemSaved) {
            try {
                auctionSaved = auctionDAO.insert(auction);
            } catch (SQLException e) {
                System.err.println("[AuctionService] Failed to insert auction entity mapping: " + e.getMessage());
            }
        }

        if (itemSaved && auctionSaved) {
            // 4. Add to Real-time Manager
            auctionManager.addAuction(auction);
            System.out.println("[AuctionService] New auction created: " + auction.getId());
            return true;
        }

        return false;
    }

    /**
     * Initializes the AuctionManager with data from the Database.
     * Should be called when the Server starts.
     */
    public void loadActiveAuctions() {
        System.out.println("[AuctionService] Loading active auctions from database...");
        try {
            List<Auction> activeAuctions = auctionDAO.findByStatus("RUNNING");
            for (Auction auction : activeAuctions) {
                auctionManager.addAuction(auction);
            }
            System.out.println("[AuctionService] Successfully loaded " + activeAuctions.size() + " auctions into memory.");
        } catch (SQLException e) {
            System.err.println("[AuctionService] Failed to load active auctions from DB: " + e.getMessage());
        }
    }

    public List<Auction> getAllActiveAuctions() {
        return auctionManager.getAllAuctions();
    }
}
