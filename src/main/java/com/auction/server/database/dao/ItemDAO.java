package com.auction.server.database.dao;

import com.auction.models.Auction;
import com.auction.models.Item;
import java.util.List;

/**
 * Interface for Item Data Access.
 */
public interface ItemDAO {

    List<Item> getAllItems();

    boolean addItem(Item item);

    /**
     * Adds a new item along with its initial auction details.
     * Necessary because the DB schema combines items and auctions into one table.
     */
    boolean addItemWithAuction(Item item, Auction auction);

    boolean updateItem(Item item);

    Item findById(String id);
}
