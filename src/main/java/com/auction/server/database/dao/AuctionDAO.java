package com.auction.server.database.dao;

import com.auction.models.Auction;

/**
 * Interface for Auction persistence.
 * Handles updates to auction status, final price, and winner.
 */
public interface AuctionDAO {
    /**
     * Updates the auction state in the database (status, winner, current price).
     */
    boolean updateAuctionStatus(Auction auction);
    
    /**
     * Retrieves all active auctions (OPEN, RUNNING, PENDING) from the database.
     */
    java.util.List<com.auction.models.Auction> getAllActiveAuctions();

    /**
     * Finds an auction by ID.
     */
    Auction findById(String auctionId);
}
