package com.auction.models.dto;

import com.auction.models.Auction;
import java.util.List;

/**
 * Response from Server containing the list of auctions.
 */
public class AuctionListResponse implements NetworkMessage {
    private static final long serialVersionUID = 1L;
    private final List<Auction> auctions;

    public AuctionListResponse(List<Auction> auctions) {
        this.auctions = auctions;
    }

    public List<Auction> getAuctions() {
        return auctions;
    }
}
