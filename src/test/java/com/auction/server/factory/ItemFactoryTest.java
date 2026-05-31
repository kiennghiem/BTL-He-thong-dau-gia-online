package com.auction.server.factory;

import com.auction.models.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemFactoryTest {

    @Test
    void testCreateElectronics() {
        Seller seller = new Seller("s", "p");
        Item item = ItemFactory.createNewItem(ItemType.ELECTRONICS, "Phone", "Desc", new BigDecimal("500"), "Samsung", seller);
        
        assertTrue(item instanceof Electronics);
        assertEquals("Samsung", item.getSpecialAttribute());
    }

    @Test
    void testCreateArt() {
        Seller seller = new Seller("s", "p");
        Item item = ItemFactory.createNewItem(ItemType.ART, "Painting", "Desc", new BigDecimal("1000"), "Picasso", seller);
        
        assertTrue(item instanceof Art);
        assertEquals("Picasso", item.getSpecialAttribute());
    }

    @Test
    void testCreateVehicle() {
        Seller seller = new Seller("s", "p");
        Item item = ItemFactory.createNewItem(ItemType.VEHICLE, "Car", "Desc", new BigDecimal("5000"), "10000km", seller);
        
        assertTrue(item instanceof Vehicle);
        assertEquals("10000km", item.getSpecialAttribute());
    }
}
