package com.auction.models;

import com.auction.server.factory.ItemType;
import java.math.BigDecimal;

public class Electronics extends Item {
    private static final long serialVersionUID = 1L;

    // Create a new Electronics instance
    public Electronics(String itemName, String description, BigDecimal startingPrice, String specialAttribute, Seller owner){
        super(ItemType.ELECTRONICS, itemName, description, startingPrice, specialAttribute, owner);
    }

    // Create an Electronics instance from the database
    public Electronics(String id, String itemName, String description, BigDecimal startingPrice, String specialAttribute, Seller owner){
        super(id, ItemType.ELECTRONICS, itemName, description, startingPrice, specialAttribute, owner);
    }

    @Override
    public String getInfo(){
        return "Name: " + this.getItemName()
                +"\nDescription: "+ this.getDescription()
                +"\nBrand: " + this.getSpecialAttribute();
    }
}