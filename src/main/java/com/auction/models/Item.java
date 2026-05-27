package com.auction.models;

import java.util.ArrayList;
import java.util.List;

public abstract class Item extends Entity {

    private static final long serialVersionUID = 1L;
    private String itemName;
    private String description;
    private double startingPrice;
    private Seller owner;
    private List<String> imageUrls;

    public Item(String itemName, String description, double startingPrice) {
        super();
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.imageUrls = new ArrayList<>();
    }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    public Seller getOwner() { return owner; }
    public void setOwner(Seller owner) { this.owner = owner; }
    public List<String> getImageUrls() { return imageUrls; }
    public void addImageUrl(String url) { this.imageUrls.add(url); }

    abstract public String getInfo();
}
