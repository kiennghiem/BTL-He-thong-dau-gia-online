package main.java.com.auction.models;

public class Electronics extends Item {
    private static final long serialVersionUID = 1L;
    private String brand;

    public Electronics(String itemName, String description, float startingPrice, String Brand){
        super(itemName,description,startingPrice);
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