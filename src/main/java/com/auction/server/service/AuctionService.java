package com.auction.server.service;

import com.auction.exceptions.AuctionNotFoundException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.*;
import com.auction.models.dto.*;
import com.auction.server.database.dao.BidDAO;
import com.auction.server.database.dao.DAOFactory;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.database.dao.AuctionDAO;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.factory.ItemFactory;
import com.auction.server.manager.AuctionManager;
import com.auction.server.factory.ItemType;
import com.auction.server.observer.AuctionStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        this(AuctionManager.getInstance(),
             DAOFactory.getItemDAO(),
             DAOFactory.getBidDAO(),
             DAOFactory.getAuctionDAO(),
             DAOFactory.getUserDAO());
    }

    /**
     * Constructor for dependency injection (used in tests).
     */
    public AuctionService(AuctionManager auctionManager, ItemDAO itemDAO, BidDAO bidDAO, AuctionDAO auctionDAO, UserDAO userDAO) {
        this.auctionManager = auctionManager;
        this.itemDAO = itemDAO;
        this.bidDAO = bidDAO;
        this.auctionDAO = auctionDAO;
        this.userDAO = userDAO;
        this.persistenceExecutor = Executors.newFixedThreadPool(4);
    }

    /**
     * Finalizes an auction in the database.
     */
    public void finishAuction(String auctionId) {
        Auction auction = auctionManager.getAuction(auctionId);
        if (auction != null) {
            persistenceExecutor.submit(() -> {
                try {
                    boolean updated = auctionDAO.updateStatus(auctionId, AuctionStatus.FINISHED);
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
    public void placeBid(String auctionId, String bidderId, BigDecimal amount)
            throws InvalidBidException, AuctionNotFoundException {

        User bidder = userDAO.findById(bidderId);
        String bidderName = (bidder != null) ? bidder.getUsername() : bidderId;

        BidTransaction bid = new BidTransaction(auctionId, bidderId, bidderName, amount);
        auctionManager.processBid(auctionId, bid);

        persistenceExecutor.submit(() -> {
            boolean saved = bidDAO.placeBid(bid);
            if (!saved) {
                System.err.println("[AuctionService-Async] CRITICAL: Bid for " + bidderId +
                        " on " + auctionId + " failed to save to DB via BidDAO!");
            }
        });
    }

    /**
     * Creates a new auction (Seller functionality).
     */
    public boolean createAuction(ItemType type, String name, String desc, BigDecimal startingPrice,
                                 String specAttr, String sellerUsername, LocalDateTime start,
                                 LocalDateTime end) {

        User user = userDAO.findByUsername(sellerUsername);
        if (user == null) {
            throw new RuntimeException("User not found: " + sellerUsername);
        }
        if (!(user instanceof Seller seller)) {
            throw new RuntimeException("User " + sellerUsername + " is not a seller (Role: " + user.getRoleAsString() + ")");
        }

        Item item = ItemFactory.createNewItem(type, name, desc, startingPrice, specAttr, seller);

        // Create Auction object
        Auction auction = new Auction();
        auction.setId(UUID.randomUUID().toString());
        auction.setItem(item);
        auction.setSeller(seller);
        auction.setTitle(name);
        auction.setDescription(desc);
        auction.setStartingPrice(startingPrice);
        auction.setCurrentPrice(startingPrice);
        auction.setStartTime(start);
        auction.setEndTime(end);
        
        // ALL new auctions start as PENDING and require Admin approval
        auction.setStatus(AuctionStatus.PENDING);

        // Persist both sequentially to Database
        boolean itemSaved = itemDAO.addItem(item);
        if (!itemSaved) {
            throw new RuntimeException("Failed to save Item to database.");
        }

        boolean auctionSaved = false;
        try {
            auctionSaved = auctionDAO.insert(auction);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save Auction to database: " + e.getMessage(), e);
        }

        if (auctionSaved) {
            // Add to Real-time Manager
            auctionManager.addAuction(auction);
            System.out.println("[AuctionService] New auction created: " + auction.getId());
            return true;
        }

        throw new RuntimeException("Auction DAO insert returned false without error.");
    }

    /**
     * Approves a pending auction (Admin functionality).
     */
    public void approveAuction(String auctionId, String adminId) throws AuctionNotFoundException {
        // 1. Update real-time state
        auctionManager.approveAuction(auctionId);

        // 2. Persist status change to database
        persistenceExecutor.submit(() -> {
            try {
                boolean updated = auctionDAO.updateStatus(auctionId, AuctionStatus.OPEN);
                if (updated) {
                    System.out.println("[AuctionService-Async] Auction " + auctionId + " approved in DB by admin " + adminId);
                }
            } catch (SQLException e) {
                System.err.println("[AuctionService-Async] Error approving auction in DB: " + e.getMessage());
            }
        });
    }

    /**
     * Ends an auction early (Seller functionality).
     */
    public void endAuctionEarly(String auctionId, String sellerId) throws AuctionNotFoundException {
        // 1. Update real-time state
        auctionManager.endAuctionEarly(auctionId);

        // 2. Persist status and endTime change to database
        persistenceExecutor.submit(() -> {
            try {
                Auction auction = auctionManager.getAuction(auctionId);
                if (auction != null) {
                    auctionDAO.update(auction); // Updates endTime
                    auctionDAO.updateStatus(auctionId, AuctionStatus.FINISHED);
                    System.out.println("[AuctionService-Async] Auction " + auctionId + " ended early in DB by seller " + sellerId);
                }
            } catch (SQLException e) {
                System.err.println("[AuctionService-Async] Error ending auction early in DB: " + e.getMessage());
            }
        });
    }

    /**
     * Cancels an auction (Admin/Seller functionality).
     */
    public void cancelAuction(String auctionId, String requesterId, String reason) throws AuctionNotFoundException {
        // 1. Update real-time state
        auctionManager.cancelAuction(auctionId);

        // 2. Persist status change to database
        persistenceExecutor.submit(() -> {
            try {
                boolean updated = auctionDAO.updateStatus(auctionId, AuctionStatus.CANCELED);
                if (updated) {
                    System.out.println("[AuctionService-Async] Auction " + auctionId + " canceled in DB by " + requesterId + ". Reason: " + reason);
                }
            } catch (SQLException e) {
                System.err.println("[AuctionService-Async] Error canceling auction in DB: " + e.getMessage());
            }
        });
    }

    /**
     * Initializes the AuctionManager with data from the Database.
     * Should be called when the Server starts.
     */
    public void loadActiveAuctions() {
        System.out.println("[AuctionService] Initializing: Loading all auctions from database (Optimized JOIN)...");
        try {
            // Load ALL auctions from DB into memory (Optimized query prevents deadlocks)
            List<Auction> allAuctions = auctionDAO.findAll();
            for (Auction auction : allAuctions) {
                auctionManager.addAuction(auction);
            }
            System.out.println("[AuctionService] Successfully loaded " + allAuctions.size() + " auctions (including historical).");
        } catch (SQLException e) {
            System.err.println("[AuctionService] Failed to load auctions from DB: " + e.getMessage());
        }
    }

    public List<Auction> getAllActiveAuctions() {
        return auctionManager.getAllAuctions();
    }

    /**
     * Processes payment for a won auction.
     */
    public boolean processPayment(String auctionId, String bidderId, BigDecimal amount) {
        Auction auction = auctionManager.getAuction(auctionId);
        if (auction == null) {
            throw new RuntimeException("Auction not found: " + auctionId);
        }

        if (auction.getStatus() != AuctionStatus.FINISHED) {
            throw new RuntimeException("Auction is not in FINISHED state.");
        }

        // --- 24-HOUR PAYMENT RULE ---
        LocalDateTime now = LocalDateTime.now();
        if (auction.getEndTime() != null && now.isAfter(auction.getEndTime().plusHours(24))) {
            throw new RuntimeException("Payment deadline (24 hours) has passed. You can no longer pay for this item.");
        }

        if (auction.getHighestBidderId() == null || !auction.getHighestBidderId().equals(bidderId)) {
            throw new RuntimeException("User is not the winner of this auction.");
        }

        User bidder = userDAO.findById(bidderId);
        if (bidder == null) {
            throw new RuntimeException("Bidder user not found.");
        }

        if (bidder.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance to pay for item.");
        }

        // --- ATOMIC PAYMENT PROCESS ---
        try {
            // 1. Deduct from bidder
            bidder.withdraw(amount);
            userDAO.updateUser(bidder);

            // 2. Add to seller (optional but good)
            if (auction.getSeller() != null) {
                User seller = userDAO.findById(auction.getSellerId());
                if (seller != null) {
                    seller.deposit(amount);
                    userDAO.updateUser(seller);
                }
            }

            // 3. Update Auction Status to PAID
            auction.updateStatus(AuctionStatus.PAID);
            auctionDAO.updateStatus(auctionId, AuctionStatus.PAID);

            // 4. Update Item ownership/status if needed
            Item item = auction.getItem();
            if (item != null) {
                item.setBuyer((com.auction.models.Bidder) bidder);
                itemDAO.updateItem(item);
            }

            // 5. Notify all observers about the status change
            auctionManager.addAuction(auction); // Force update in manager map and notify
            
            System.out.println("[AuctionService] Payment successful for auction: " + auctionId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }
}
