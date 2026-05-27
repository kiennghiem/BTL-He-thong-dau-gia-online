package com.auction.models;

import com.auction.server.factory.ItemType;

public abstract class Item extends Entity {

    private static final long serialVersionUID = 1L;
    private ItemType type;
    private String itemName;
    private String description;
    private double startingPrice;
    private Seller owner;

    // Create an instance of a new item
    public Item(ItemType type, String itemName, String description, double startingPrice, Seller owner) {
        super();
        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.owner = owner;
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
    public ItemType getType() {
        return type;
    }
    public void setType(ItemType type) {
        this.type = type;
    }


    abstract public String getInfo();
}