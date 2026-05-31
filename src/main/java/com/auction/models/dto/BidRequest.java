package com.auction.models.dto;

import java.math.BigDecimal;

/**
 * DTO sent from Client to Server when a user places a bid.
 * This facilitates the "Tham gia đấu giá" core functionality.
 */
public class BidRequest implements NetworkMessage {
    // Encapsulation: Private fields to protect the bid data
    private final String auctionId;
    private final String bidderId;
    private final BigDecimal amount;
    private final long timestamp; // Useful for resolving "Lost Updates" and concurrent ties

    /**
     * Constructor for creating a new bid intent.
     *
     * @param auctionId The unique ID of the auction being bid on.
     * @param bidderId The ID of the user placing the bid.
     * @param amount The price the user is offering.
     */
    public BidRequest(String auctionId, String bidderId, BigDecimal amount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    // --- Getters for Server-side logic processing ---

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("BidRequest[User: %s, Auction: %s, Amount: %.2f]",
                bidderId, auctionId, amount);
    }
}