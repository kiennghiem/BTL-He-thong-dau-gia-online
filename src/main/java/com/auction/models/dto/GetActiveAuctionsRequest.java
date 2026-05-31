package com.auction.models.dto;

/**
 * DTO sent from Client to Server to request a list of all currently active auctions.
 * Used by Bidders to browse available items.
 */
public class GetActiveAuctionsRequest implements NetworkMessage {
    // Can be expanded later to include filters (e.g., category, search term)
    public GetActiveAuctionsRequest() {
    }
}
