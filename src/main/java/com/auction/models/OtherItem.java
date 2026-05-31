package com.auction.models;

import com.auction.server.factory.ItemType;
import java.math.BigDecimal;

public class OtherItem extends Item {
    private static final long serialVersionUID = 1L;

    // Create a new OtherItem instance
    public OtherItem(String itemName, String description, BigDecimal startingPrice, String specialAttribute, Seller owner){
        super(ItemType.OTHERS, itemName, description, startingPrice, specialAttribute, owner);
    }

    // Create an OtherItem instance from the database
    public OtherItem(String id, String itemName, String description, BigDecimal startingPrice,
                       BigDecimal currentPrice, String specialAttribute, Seller owner, Bidder buyer){
        super(id, ItemType.OTHERS, itemName, description, startingPrice, currentPrice, specialAttribute, owner, buyer);
    }

    @Override
    public String getInfo(){
        return "Name: " + this.getItemName()
                +"\nDescription: "+ this.getDescription()
                +"\nNote: " + this.getSpecialAttribute();
    }
}
