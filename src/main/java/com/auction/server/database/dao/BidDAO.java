package com.auction.server.database.dao;

import main.java.com.auction.models.BidTransaction;
import java.util.List;

/**
 * Interface for Bid Data Access.
 * Defines the contract for placing bids and retrieving history.
 * Essential for "Concurrent Bidding" and "Price Curve Visualization".
 */
public interface BidDAO {

    /**
     * Places a bid using a transaction to ensure thread safety.
     * Prevents "Lost Updates" and race conditions.
     * @param bid The BidTransaction object containing bidder and amount.
     * @return true if the bid was valid and successfully placed.
     */
    boolean placeBid(BidTransaction bid);

    /**
     * Retrieves the history of all successful bids for a specific item.
     * Used for the JavaFX LineChart visualization.
     * @param itemId The ID of the auction item.
     * @return A list of BidTransactions ordered by timestamp.
     */
    List<BidTransaction> getHistoryByItem(String itemId);
}