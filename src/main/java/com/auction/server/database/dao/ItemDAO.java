package com.auction.server.database.dao;

import com.auction.models.Item;

import java.util.List;

public interface ItemDAO {

    Item findById(String id);

    List<Item> findByOwnerId(String ownerId); // Used by Seller see what items they have made

    void addItem(Item item); //Used by Seller to add an item
}
