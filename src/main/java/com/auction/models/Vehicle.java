package com.auction.models;

import com.auction.server.factory.ItemType;

public class Vehicle extends Item {
    private static final long serialVersionUID = 1L;
    private String brand;

    public Vehicle() {
        super();
        this.setType(ItemType.VEHICLE);
    }

    // Create a new Vehicle instance
    public Vehicle(String itemName, String description, double startingPrice, String brand, Seller owner) {
        super(ItemType.VEHICLE, itemName, description,startingPrice, owner);
        this.brand = brand;
    }

    // Create a Vehicle instance from the database
    public Vehicle(String id, String itemName, String description, double startingPrice, String Brand, Seller owner){
        super(id, ItemType.VEHICLE, itemName,description,startingPrice, owner);
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