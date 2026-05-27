package com.auction.models;

import com.auction.server.factory.ItemType;
import java.math.BigDecimal;

public class Electronics extends Item {
    private static final long serialVersionUID = 1L;
    private String brand;

    // Create a new Electronics instance
    public Electronics(String itemName, String description, BigDecimal startingPrice, String Brand, Seller owner){
        super(ItemType.ELECTRONICS, itemName,description,startingPrice, owner);
        this.brand = Brand;
    }

    // Create an Electronics instance from the database
    public Electronics(String id, String itemName, String description, BigDecimal startingPrice, String Brand, Seller owner){
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