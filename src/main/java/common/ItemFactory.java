package main.java.common;

import java.time.LocalDateTime;

public class ItemFactory{
    public static Item createItem(ItemType type, String name, String description, double startingPrice, LocalDateTime startingTime, LocalDateTime closingTime, User owner, String SpeAttr){
        switch (type) {
            case ELECTRONICS -> {
                return new Electronic(name, description, startingPrice, startingTime, closingTime, owner,SpeAttr);
            }
            case ART -> {
                return new Art(name, description, startingPrice, startingTime, closingTime, owner, SpeAttr);
            }
            case VEHICLE -> {
                return new Vehicle(name, description, startingPrice, startingTime, closingTime, owner,SpeAttr);
            }
            default -> throw new IllegalArgumentException("Invalid item type");
        }
    }
}