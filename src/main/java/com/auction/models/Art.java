package com.auction.models;

import java.time.LocalDateTime;

public class Art extends Item {
    private static final long serialVersionUID = 1L;
    private String artist;

    public Art(String itemName, String description, float startingPrice, String artist){
        super(itemName,description,startingPrice);
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
