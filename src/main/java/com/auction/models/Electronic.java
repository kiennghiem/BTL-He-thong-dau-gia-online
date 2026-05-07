package main.java.com.auction.models;

import java.time.LocalDateTime;

public class Electronic extends Item {
    private static final long serialVersionUID = 1L;
    private String brand;

    public Electronic(String itemName, String description, double startingPrice, LocalDateTime startingTime, LocalDateTime closingTime, User owner, String Brand){
        super(itemName,description,startingPrice,startingTime,closingTime,owner);
        brand  = Brand;
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
