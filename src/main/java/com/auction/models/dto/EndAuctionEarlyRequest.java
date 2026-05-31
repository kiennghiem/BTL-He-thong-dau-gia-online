package com.auction.models.dto;

import java.io.Serializable;

/**
 * Request to end an auction early.
 * Can be sent by the Seller of the auction.
 */
public class EndAuctionEarlyRequest implements Serializable, NetworkMessage {
    private static final long serialVersionUID = 1L;
    private final String auctionId;
    private final String sellerId;

    public EndAuctionEarlyRequest(String auctionId, String sellerId) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getSellerId() {
        return sellerId;
    }
}
