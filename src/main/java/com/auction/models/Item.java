package com.auction.models;

import com.auction.server.factory.ItemType;
import java.math.BigDecimal;

public abstract class Item extends Entity {

    private static final long serialVersionUID = 1L;
    private ItemType type;
    private String itemName;
    private String description;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private String specialAttribute; // brand or artist
    private Seller owner;
    private Bidder buyer;

    // Create an instance of a new item
    public Item(ItemType type, String itemName, String description, BigDecimal startingPrice, String specialAttribute, Seller owner) {
        super();
        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.specialAttribute = specialAttribute;
        this.owner = owner;
        this.buyer = null;
    }

    // Create an instance of an existed item from the database
    public Item(String id, ItemType type, String itemName, String description, BigDecimal startingPrice,
                BigDecimal currentPrice, String specialAttribute, Seller owner, Bidder buyer) {
        super(id);
        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.specialAttribute = specialAttribute;
        this.owner = owner;
        this.buyer = buyer;
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
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    public ItemType getType() {
        return type;
    }
    public void setType(ItemType type) {
        this.type = type;
    }
    public String getSpecialAttribute() {
        return specialAttribute;
    }
    public void setSpecialAttribute(String specialAttribute) {
        this.specialAttribute = specialAttribute;
    }
    public Seller getOwner() {
        return owner;
    }
    public void setOwner(Seller owner) {
        this.owner = owner;
    }
    public Bidder getBuyer() {
        return buyer;
    }
    public void setBuyer(Bidder buyer) {
        this.buyer = buyer;
    }

    public static void main(String[] args) {
        Bidder buyer = null;
        buyer.getId();
    }

    abstract public String getInfo();
}