package com.auction.server.database.dao;

import com.auction.models.Item;
import java.util.List;

/**
 * Interface for Item Data Access.
 * Defines how the system interacts with polymorphic items (Electronic, Art, Vehicle).
 * This supports the "Quản lý sản phẩm" requirement in the project PDF.
 */
public interface ItemDAO {

    /**
     * Retrieves all items from the database.
     * Uses polymorphism to return the correct subclasses.
     * @return List of all auction items.
     */
    List<Item> getAllItems();

    /**
     * Adds a new item to the auction catalog.
     * @param item The Item object to persist (Electronic, Art, or Vehicle).
     * @return true if the item was successfully saved.
     */
    boolean addItem(Item item);

    /**
     * Updates the status or details of an item (e.g., changing from OPEN to RUNNING).
     * @param item The item to update.
     * @return true if successful.
     */
    boolean updateItem(Item item);

    /**
     * Finds a specific item by its unique ID.
     * @param id The item ID.
     * @return The Item object if found, null otherwise.
     */
    Item findById(String id);

    /**
     * Deletes an item from the database.
     * @param id The item ID.
     * @return true if successful.
     */
    boolean deleteItem(String id);
}
