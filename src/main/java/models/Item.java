package main.java.models;

import main.java.common.commonException.*;

public abstract class Item extends Entity {
    private static final long serialVersionUID = 1L;
    private String itemName;
    private String description;
    private float startingPrice;

    public Item(String itemName, String description, float startingPrice) {
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
    public float getStartingPrice() {
        return startingPrice;
    }
    public void setStartingPrice(float startingPrice) {
        this.startingPrice = startingPrice;
    }


    abstract public String getInfo();
}

