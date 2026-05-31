package com.auction.models;

import com.auction.server.factory.ItemType;
import java.math.BigDecimal;

public class Vehicle extends Item {
    private static final long serialVersionUID = 1L;

    // Create a new Vehicle instance
    public Vehicle(String itemName, String description, BigDecimal startingPrice, String specialAttribute, Seller owner) {
        super(ItemType.VEHICLE, itemName, description, startingPrice, specialAttribute, owner);
    }

    // Create a Vehicle instance from the database
    public Vehicle(String id, String itemName, String description, BigDecimal startingPrice,
                   BigDecimal currentPrice, String specialAttribute, Seller owner, Bidder buyer) {
        super(id, ItemType.VEHICLE, itemName, description, startingPrice, currentPrice, specialAttribute, owner, buyer);
    }

    @Override
    public String getInfo(){
        return "Name: " + this.getItemName()
                +"\nDescription: "+ this.getDescription()
                +"\nBrand: " + this.getSpecialAttribute();
    }
}