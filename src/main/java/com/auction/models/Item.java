package com.auction.models;

import com.auction.server.factory.ItemType;
import java.time.LocalDateTime;

public abstract class Item extends Entity {

    private static final long serialVersionUID = 1L;
    private ItemType type;
    private String itemName;
    private String description;
    private double startingPrice;
    private double currentPrice;
    private LocalDateTime startingTime;
    private LocalDateTime closingTime;
    private ItemStatus status;
    private Seller owner;
    private Bidder currentBidder;
    private Bidder buyer;

    // No-arg constructor for DAO
    public Item() {
        super();
    }

    // Create an instance of a new item
    public Item(ItemType type, String itemName, String description, double startingPrice, Seller owner) {
        super();
        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.owner = owner;
        this.status = ItemStatus.PENDING;
    }

    // Create an instance of an existed item from the database
    public Item(String id, ItemType type, String itemName, String description, double startingPrice, Seller owner) {
        super(id);
        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.owner = owner;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
    public LocalDateTime getStartingTime() {
        return startingTime;
    }
    public void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }
    public LocalDateTime getClosingTime() {
        return closingTime;
    }
    public void setClosingTime(LocalDateTime closingTime) {
        this.closingTime = closingTime;
    }
    public ItemStatus getStatus() {
        return status;
    }
    public void setStatus(ItemStatus status) {
        this.status = status;
    }
    public ItemType getType() {
        return type;
    }
    public void setType(ItemType type) {
        this.type = type;
    }
    public Seller getOwner() {
        return owner;
    }
    public void setOwner(Seller owner) {
        this.owner = owner;
    }
    public Bidder getCurrentBidder() {
        return currentBidder;
    }
    public void setCurrentBidder(Bidder currentBidder) {
        this.currentBidder = currentBidder;
    }
    public Bidder getBuyer() {
        return buyer;
    }
    public void setBuyer(Bidder buyer) {
        this.buyer = buyer;
    }

    abstract public String getInfo();
}