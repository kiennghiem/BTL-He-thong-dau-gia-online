package com.auction.server.service;

import com.auction.exceptions.AuctionNotFoundException;
import com.auction.exceptions.InvalidBidException;
import com.auction.models.*;
import com.auction.server.database.DBConnection;
import com.auction.server.database.dao.BidDAO;
import com.auction.server.database.dao.DAOFactory;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.factory.ItemFactory;
import com.auction.server.manager.AuctionManager;

import java.sql.*;
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
    private final com.auction.server.database.dao.AuctionDAO auctionDAO;
    
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
            // ASYNC: Do not block the status checker loop
            persistenceExecutor.submit(() -> {
                boolean updated = auctionDAO.updateAuctionStatus(auction);
                if (updated) {
                    System.out.println("[AuctionService-Async] Auction " + auctionId + " finalized in DB.");
                }
            });
        }
    }

    /**
     * Places a bid, updates real-time state, and persists to DB.
     */
    public void placeBid(String auctionId, String bidderId, double amount) 
            throws InvalidBidException, AuctionNotFoundException {
        
        // 1. Create the transaction object
        BidTransaction bid = new BidTransaction(auctionId, bidderId, amount);

        // 2. Process in Real-time Manager (IMMEDIATE FEEDBACK)
        // This is synchronized inside AuctionManager, ensuring data integrity in memory.
        auctionManager.processBid(auctionId, bid);

        // 3. ASYNC PERSISTENCE: Offload DB write to background thread
        // The Client will receive success notification as soon as step 2 finishes.
        persistenceExecutor.submit(() -> {
            boolean saved = bidDAO.placeBid(bid);
            if (!saved) {
                System.err.println("[AuctionService-Async] CRITICAL: Bid for " + bidderId + 
                                   " on " + auctionId + " failed to save to DB!");
            }
        });
    }

    /**
     * Creates a new auction (Seller functionality).
     */
    public boolean createAuction(ItemType type, String name, String desc, double startingPrice, 
                                 String specAttr, Seller seller, LocalDateTime start, 
                                 LocalDateTime end, double minIncrement) {
        
        // 1. Create Item via Factory
        Item item = ItemFactory.createItem(type, name, desc, startingPrice, specAttr);
        item.setOwner(seller);
        // Use a simple UUID or similar if id is null
        if (item.getId() == null) item.setId(java.util.UUID.randomUUID().toString());

        // 2. Create Auction object
        Auction auction = new Auction(item, seller, start, end, minIncrement);
        auction.setId(item.getId());

        // 3. Persist both to Database
        boolean saved = itemDAO.addItemWithAuction(item, auction);
        
        if (saved) {
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
        List<Auction> activeAuctions = auctionDAO.getAllActiveAuctions();
        
        for (Auction auction : activeAuctions) {
            auctionManager.addAuction(auction);
        }
        
        System.out.println("[AuctionService] Successfully loaded " + activeAuctions.size() + " auctions into memory.");
    }

    public List<Auction> getAllActiveAuctions() {
        return auctionManager.getAllAuctions();
    }
}
