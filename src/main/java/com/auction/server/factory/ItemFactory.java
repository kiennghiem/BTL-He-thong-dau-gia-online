package com.auction.server.factory;

import com.auction.models.*;
import java.math.BigDecimal;

/**
 * This class is used to create an Item object based on the specified ItemType.
 */
public class ItemFactory {
    public static Item createNewItem(ItemType type, String name, String description, BigDecimal startingPrice,
                                  String BrandOrArtist, Seller owner) throws IllegalArgumentException {
        switch (type) {
            case ELECTRONICS -> {
                return new Electronics(name, description, startingPrice, BrandOrArtist, owner);
            }
            case ART -> {
                return new Art(name, description, startingPrice, BrandOrArtist, owner);
            }
            case VEHICLE -> {
                return new Vehicle(name, description, startingPrice, BrandOrArtist, owner);
            }
            default -> throw new IllegalArgumentException("Invalid item type");
        }
    }

    public static Item createItemFromDB(String id, ItemType type, String name, String description, BigDecimal startingPrice,
                                        String BrandOrArtist, Seller owner) throws IllegalArgumentException {
        switch (type) {
            case ELECTRONICS -> {
                return new Electronics(id, name, description, startingPrice, BrandOrArtist, owner);
            }
            case ART -> {
                return new Art(id, name, description, startingPrice, BrandOrArtist, owner);
            }
            case VEHICLE -> {
                return new Vehicle(id, name, description, startingPrice, BrandOrArtist, owner);
            }
            default -> throw new IllegalArgumentException("Invalid item type");
        }
    }
}