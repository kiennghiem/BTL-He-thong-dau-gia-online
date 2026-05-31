package com.auction.server.factory;

import com.auction.models.*;

import java.math.BigDecimal;

/**
 * This class is used to create an Item object based on the specified ItemType.
 */
public class ItemFactory {
    public static Item createNewItem(ItemType type, String name, String description, BigDecimal startingPrice,
                                  String specialAttribute, Seller owner) throws IllegalArgumentException {
        switch (type) {
            case ELECTRONICS -> {
                return new Electronics(name, description, startingPrice, specialAttribute, owner);
            }
            case ART -> {
                return new Art(name, description, startingPrice, specialAttribute, owner);
            }
            case VEHICLE -> {
                return new Vehicle(name, description, startingPrice, specialAttribute, owner);
            }
            case OTHERS -> {
                return new OtherItem(name, description, startingPrice, specialAttribute, owner);
            }
            default -> throw new IllegalArgumentException("Invalid item type");
        }
    }

    public static Item createItemFromDB(String id, ItemType type, String name, String description,
                                        BigDecimal startingPrice, BigDecimal currentPrice, String specialAttribute,
                                        Seller owner, Bidder buyer) throws IllegalArgumentException {
        switch (type) {
            case ELECTRONICS -> {
                return new Electronics(id, name, description, startingPrice, currentPrice, specialAttribute, owner, buyer);
            }
            case ART -> {
                return new Art(id, name, description, startingPrice, currentPrice, specialAttribute, owner, buyer);
            }
            case VEHICLE -> {
                return new Vehicle(id, name, description, startingPrice, currentPrice, specialAttribute, owner, buyer);
            }
            case OTHERS -> {
                return new OtherItem(id, name, description, startingPrice, currentPrice, specialAttribute, owner, buyer);
            }
            default -> throw new IllegalArgumentException("Invalid item type");
        }
    }
}