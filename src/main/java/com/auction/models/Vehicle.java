package main.java.com.auction.models;

import java.time.LocalDateTime;


public class Vehicle extends Item {
    private static final long serialVersionUID = 1L;
    private String brand;

    public Vehicle(String itemName, String description, double startingPrice, LocalDateTime startingTime, LocalDateTime closingTime, User owner, String brand){
        super(itemName,description,startingPrice,startingTime,closingTime,owner);
        this.brand = brand;
    }

    public String getBrand(){return brand;}
    public void setBrand(String brand){this.brand = brand;}

    @Override
    public String getInfo(){
        return "Name: " + this.getItemName()
             +"\nDescription: "+ this.getDescription()
             +"\nOwner: " + this.getOwner()
             +"\nBrand: " + this.getBrand();
    }
}
