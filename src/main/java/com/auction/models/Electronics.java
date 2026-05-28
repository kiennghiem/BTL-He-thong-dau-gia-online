package com.auction.models;

import com.auction.server.factory.ItemType;

public class Electronics extends Item {
    private static final long serialVersionUID = 1L;
    private String brand;

    public Electronics() {
        super();
        this.setType(ItemType.ELECTRONICS);
    }

    // Create a new Electronics instance
    public Electronics(String itemName, String description, double startingPrice, String Brand, Seller owner){
        super(ItemType.ELECTRONICS, itemName,description,startingPrice, owner);
        this.brand = Brand;
    }

    // Create an Electronics instance from the database
    public Electronics(String id, String itemName, String description, double startingPrice, String Brand, Seller owner){
        super(id, ItemType.ELECTRONICS, itemName,description,startingPrice, owner);
        this.brand = Brand;
    }

    public String getBrand(){return brand;}
    public void setBrand(String brand){this.brand = brand;}

    @Override
    public String getInfo(){
        return "Name: " + this.getItemName()
                +"\nDescription: "+ this.getDescription()
                +"\nBrand: " + this.getBrand();
    }
}