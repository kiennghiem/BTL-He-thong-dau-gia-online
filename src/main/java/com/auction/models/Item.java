package com.auction.models;

import com.auction.server.factory.ItemType;
import java.math.BigDecimal;

public abstract class Item extends Entity {

    private static final long serialVersionUID = 1L;
    private ItemType type;
    private String itemName;
    private String description;
    private BigDecimal startingPrice;
    private String specialAttribute; // brand or artist
    private Seller owner;

    // Create an instance of a new item
    public Item(ItemType type, String itemName, String description, BigDecimal startingPrice, String specialAttribute, Seller owner) {
        super();
        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.specialAttribute = specialAttribute;
        this.owner = owner;
    }

    // Create an instance of an existed item from the database
    public Item(String id, ItemType type, String itemName, String description, BigDecimal startingPrice, String specialAttribute, Seller owner) {
        super(id);
        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.specialAttribute = specialAttribute;
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
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
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

    abstract public String getInfo();
}