package com.auction.models;

import com.auction.server.factory.ItemType;
import java.math.BigDecimal;

public class Art extends Item {
    private static final long serialVersionUID = 1L;

    // Create a new Art instance
    public Art(String itemName, String description, BigDecimal startingPrice, String specialAttribute, Seller owner){
        super(ItemType.ART, itemName, description, startingPrice, specialAttribute, owner);
    }

    // Create an Art instance from the database
    public Art(String id, String itemName, String description, BigDecimal startingPrice,
               BigDecimal currentPrice, String specialAttribute, Seller owner, Bidder buyer) {
        super(id, ItemType.ART, itemName, description, startingPrice, currentPrice, specialAttribute, owner, buyer);
    }

    @Override
    public String getInfo(){
        return "Name: " + this.getItemName()
                +"\nDescription: "+ this.getDescription()
                +"\nArtist: " + this.getSpecialAttribute();
    }
}