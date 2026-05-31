package com.auction.models.dto;

import com.auction.models.Seller;
import com.auction.models.User;
import com.auction.server.factory.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request sent by a Seller to create a new auction.
 */
public class CreateAuctionRequest implements NetworkMessage {
    private User seller;
    private final ItemType itemType;
    private final String itemName;
    private final String itemDescription;
    private final BigDecimal startingPrice;
    private final String specificAttribute; // e.g., Brand for Electronics, Artist for Art, Mileage for Vehicle
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public CreateAuctionRequest(User seller, ItemType itemType, String itemName,
                                String itemDescription, BigDecimal startingPrice,
                                String specificAttribute, LocalDateTime startTime, LocalDateTime endTime) {
        this.seller = seller;
        this.itemType = itemType;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.startingPrice = startingPrice;
        this.specificAttribute = specificAttribute;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public User getSeller() {
        return seller;
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

    public BigDecimal getStartingPrice() {
        return startingPrice;
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
