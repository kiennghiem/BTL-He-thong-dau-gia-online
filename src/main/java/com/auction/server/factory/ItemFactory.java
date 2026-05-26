package com.auction.server.factory;

import com.auction.models.*;

public class ItemFactory {
    public static Item createItem(ItemType type, String name, String description, double startingPrice, String SpeAttr){
        switch (type) {
            case ELECTRONICS -> {
                return new Electronics(name, description, startingPrice, SpeAttr);
            }
            case ART -> {
                return new Art(name, description, startingPrice, SpeAttr);
            }
            case VEHICLE -> {
                return new Vehicle(name, description, startingPrice, SpeAttr);
            }
            default -> throw new IllegalArgumentException("Invalid item type");
        }
    }
}