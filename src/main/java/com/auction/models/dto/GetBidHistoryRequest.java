package com.auction.models.dto;

/**
 * DTO sent from Client to Server to request the bid history of a specific auction.
 * Essential for the "Bid History Visualization" (Price Curve) feature.
 */
public class GetBidHistoryRequest implements NetworkMessage {
    private final String auctionId;

    public GetBidHistoryRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
