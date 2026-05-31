package com.auction.models.dto;

/**
 * Request sent by a Client to unsubscribe from real-time updates for a specific auction.
 */
public class UnsubscribeRequest implements NetworkMessage {
    private final String auctionId;

    public UnsubscribeRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
