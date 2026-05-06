package main.java.common.Class;

import java.time.LocalDateTime;

import main.java.common.commonException.*;

public abstract class Item extends Entity {
    private static final long serialVersionUID = 1L;
    private String itemName;
    private String description;
    private double startingPrice;

    public Item(String itemName, String description, double startingPrice) {
        super();
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;

    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }


    abstract public String getInfo();
}

