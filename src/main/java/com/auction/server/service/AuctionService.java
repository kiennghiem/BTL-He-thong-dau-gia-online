package com.auction.server.service;

import com.auction.exceptions.AuctionNotFoundException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.*;
import com.auction.server.database.dao.BidDAO;
import com.auction.server.database.dao.DAOFactory;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.database.dao.AuctionDAO;
import com.auction.server.manager.AuctionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
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

    // Background thread pool for non-blocking database persistence
    private final ExecutorService persistenceExecutor;

    public AuctionService() {
        this.auctionManager = AuctionManager.getInstance();
        this.itemDAO = DAOFactory.getItemDAO();
        this.bidDAO = DAOFactory.getBidDAO();
        this.auctionDAO = DAOFactory.getAuctionDAO();
        this.persistenceExecutor = Executors.newFixedThreadPool(4); // Adjust based on load
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
     * Creates a new auction (Seller functionality).
     */
    public boolean createAuction(ItemType type, String name, String desc, double startingPrice,
                                 String specAttr, Seller seller, LocalDateTime start,
                                 LocalDateTime end, double minIncrement) {

        // 1. RESOLVED: Manually construct the correct common.Item subclass to resolve package conflicts
        common.Item item;
        switch (type.name()) {
            case "ELECTRONICS":
                common.Electronic e = new common.Electronic();
                e.setBrand(specAttr);
                item = e;
                break;
            case "VEHICLE":
                common.Vehicle v = new common.Vehicle();
                v.setBrand(specAttr);
                item = v;
                break;
            case "ART":
                common.Art a = new common.Art();
                a.setArtist(specAttr);
                item = a;
                break;
            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }

        String itemId = java.util.UUID.randomUUID().toString();
        item.setId(itemId);
        item.setItemName(name);
        item.setDescription(desc);
        item.setStartingPrice(startingPrice);
        item.setCurrentPrice(startingPrice);
        item.setStartingTime(start);
        item.setClosingTime(end);

        try {
            item.setStatus(common.ItemStatus.RUNNING);
        } catch (Exception e) {
            // Fallback block if your enum defaults to a different open status name
        }

        // RESOLVED: Convert com.auction.models.Seller to common.Seller expected by common.Item
        common.Seller commonSeller = new common.Seller();
        commonSeller.setUsername(seller.getUsername());
        item.setOwner(commonSeller);

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