package com.auction.models.dto;

/**
 * Request sent by a Client to subscribe to real-time updates for a specific auction.
 */
public class SubscribeRequest implements NetworkMessage {
    private final String auctionId;

    public SubscribeRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
