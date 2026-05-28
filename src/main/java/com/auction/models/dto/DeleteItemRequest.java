package com.auction.models.dto;

/**
 * DTO sent from Client to Server to delete an item.
 */
public class DeleteItemRequest implements NetworkMessage {
    private final String itemId;
    private final String sellerId; // For security validation

    public DeleteItemRequest(String itemId, String sellerId) {
        this.itemId = itemId;
        this.sellerId = sellerId;
    }

    public String getItemId() { return itemId; }
    public String getSellerId() { return sellerId; }
}
