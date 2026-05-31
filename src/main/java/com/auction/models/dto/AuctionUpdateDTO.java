package com.auction.models.dto;

import java.math.BigDecimal;

/**
 * DTO sent from Server to all Clients to broadcast changes in an auction.
 * This facilitates "Realtime Update" and "Anti-sniping" visibility.
 */
public class AuctionUpdateDTO implements NetworkMessage {
    // Encapsulation: Private fields for the updated auction state
    private final String auctionId;
    private final BigDecimal currentHighestBid;
    private final String leadingBidderId;
    private final String leadingBidderName;
    private final long endTimeMillis; // Updated end time (crucial for Anti-sniping)
    private final String status;      // e.g., RUNNING, FINISHED

    /**
     * Constructor for the broadcast message.
     */
    public AuctionUpdateDTO(String auctionId, BigDecimal currentHighestBid,
                            String leadingBidderId, String leadingBidderName,
                            long endTimeMillis, String status) {
        this.auctionId = auctionId;
        this.currentHighestBid = currentHighestBid;
        this.leadingBidderId = leadingBidderId;
        this.leadingBidderName = leadingBidderName;
        this.endTimeMillis = endTimeMillis;
        this.status = status;
    }

    // --- Getters for JavaFX UI Binding ---

    public String getAuctionId() {
        return auctionId;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public String getLeadingBidderId() {
        return leadingBidderId;
    }

    public String getLeadingBidderName() {
        return leadingBidderName;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("Update[ID: %s, Price: %.2f, Leader: %s, Ends: %d]",
                auctionId, currentHighestBid, leadingBidderName, endTimeMillis);
    }
}