package com.auction.server.service;

import com.auction.exceptions.AuctionNotFoundException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.*;
import com.auction.models.dto.*;
import com.auction.server.database.dao.BidDAO;
import com.auction.server.database.dao.DAOFactory;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.database.dao.AuctionDAO;
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

        User bidder = DAOFactory.getUserDAO().findById(bidderId);
        String bidderName = (bidder != null) ? bidder.getUsername() : bidderId;

        BidTransaction bid = new BidTransaction(auctionId, bidderId, bidderName, amount);
        auctionManager.processBid(auctionId, bid);

        persistenceExecutor.submit(() -> {
            boolean saved = bidDAO.placeBid(bid);
            if (!saved) {
                System.err.println("[AuctionService-Async] CRITICAL: Bid for " + bidderId +
                        " on " + auctionId + " failed to save to DB via BidDAO!");
            }

            try {
                boolean auctionUpdated = auctionDAO.placeBid(auctionId, bidderId, amount);
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
    public boolean createAuction(ItemType type, String name, String desc, BigDecimal startingPrice,
                                 String specAttr, String sellerUsername, LocalDateTime start,
                                 LocalDateTime end) {

        User user = DAOFactory.getUserDAO().findByUsername(sellerUsername);
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
        
        // Decide status based on time
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(start) && now.isBefore(end)) {
            auction.setStatus(AuctionStatus.RUNNING);
        } else if (now.isBefore(start)) {
            auction.setStatus(AuctionStatus.OPEN);
        } else {
            auction.setStatus(AuctionStatus.FINISHED);
        }

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
     * Initializes the AuctionManager with data from the Database.
     * Should be called when the Server starts.
     */
    public void loadActiveAuctions() {
        System.out.println("[AuctionService] Loading active and upcoming auctions from database...");
        try {
            // Load both RUNNING and OPEN auctions
            List<Auction> running = auctionDAO.findByStatus(AuctionStatus.RUNNING);
            List<Auction> upcoming = auctionDAO.findByStatus(AuctionStatus.OPEN);
            
            for (Auction auction : running) {
                auctionManager.addAuction(auction);
            }
            for (Auction auction : upcoming) {
                auctionManager.addAuction(auction);
            }
            
            System.out.println("[AuctionService] Successfully loaded " + (running.size() + upcoming.size()) + " auctions into memory.");
        } catch (SQLException e) {
            System.err.println("[AuctionService] Failed to load active auctions from DB: " + e.getMessage());
        }
    }

    public List<Auction> getAllActiveAuctions() {
        return auctionManager.getAllAuctions();
    }
}