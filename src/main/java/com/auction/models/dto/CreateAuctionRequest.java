package com.auction.models.dto;

import com.auction.server.factory.ItemType;

import java.time.LocalDateTime;

/**
 * Request sent by a Seller to create a new auction.
 */
public class CreateAuctionRequest implements NetworkMessage {
    private final String sellerUsername;
    private final ItemType itemType;
    private final String itemName;
    private final String itemDescription;
    private final double startingPrice;
    private final double minIncrement;
    private final String specificAttribute; // e.g., Brand for Electronics, Artist for Art, Mileage for Vehicle
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public CreateAuctionRequest(String sellerUsername, ItemType itemType, String itemName, 
                                String itemDescription, double startingPrice, double minIncrement, 
                                String specificAttribute, LocalDateTime startTime, LocalDateTime endTime) {
        this.sellerUsername = sellerUsername;
        this.itemType = itemType;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.startingPrice = startingPrice;
        this.minIncrement = minIncrement;
        this.specificAttribute = specificAttribute;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getMinIncrement() {
        return minIncrement;
    }

    public String getSpecificAttribute() {
        return specificAttribute;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
