package com.auction.models.dto;

/**
 * DTO sent from Client to Server to request all items owned by a specific Seller.
 */
public class GetSellerItemsRequest implements NetworkMessage {
    private final String sellerId;

    public GetSellerItemsRequest(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerId() { return sellerId; }
}
