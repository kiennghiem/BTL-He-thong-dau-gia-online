package com.auction.models;

import com.auction.server.factory.ItemType;
import java.math.BigDecimal;

public class Art extends Item {
    private static final long serialVersionUID = 1L;
    private String artist;

    // Create a new Art instance
    public Art(String itemName, String description, BigDecimal startingPrice, String artist, Seller owner){
        super(ItemType.ART, itemName, description, startingPrice, owner);
        this.artist = artist;
    }

    // Create an Art instance from the database
    public Art(String id, String itemName, String description, BigDecimal startingPrice, String artist, Seller owner){
        super(id, ItemType.ART, itemName,description, startingPrice, owner);
        this.artist = artist;
    }
    
    public void setArtist(String name){this.artist = name;}
    public String getArtist(){return artist;}

    @Override
    public String getInfo(){
        return "Name: " + this.getItemName()
                +"\nDescription: "+ this.getDescription()
                +"\nArtist: " + this.getArtist();
    }
}