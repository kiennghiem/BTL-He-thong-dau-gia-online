package com.auction.models;

import java.time.LocalDateTime;


public class Vehicle extends Item {
    private static final long serialVersionUID = 1L;
    private String brand;

    public Vehicle(String itemName, String description, float startingPrice, String brand) {
        super(itemName,description,startingPrice);
        this.brand = brand;
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
