package com.auction.server.database.dao.impl;

import com.auction.models.*;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.factory.ItemFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ItemDAO.
 * Refactored to align with the new model where auction-specific logic 
 * (price, status, timing) has been moved to the Auction class.
 */
public class ItemDAOImpl extends BaseDAO implements ItemDAO {

    @Override
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM items";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String typeStr = rs.getString("item_type");
                Item item = createItemFromResultSet(rs, typeStr);
                if (item != null) {
                    mapCommonFields(rs, item);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public boolean addItem(Item item) {
        return addItemWithAuction(item, null);
    }

    @Override
    public boolean addItemWithAuction(Item item, Auction auction) {
        String sql = "INSERT INTO items (id, itemName, description, startingPrice, currentPrice, startingTime, closingTime, status, owner_id, item_type, brand, artist_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getItemName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getStartingPrice());
            
            if (auction != null) {
                pstmt.setDouble(5, auction.getCurrentPrice());
                pstmt.setTimestamp(6, Timestamp.valueOf(auction.getStartingTime()));
                pstmt.setTimestamp(7, Timestamp.valueOf(auction.getClosingTime()));
                pstmt.setString(8, auction.getStatus().name());
            } else {
                pstmt.setDouble(5, item.getStartingPrice());
                pstmt.setNull(6, Types.TIMESTAMP);
                pstmt.setNull(7, Types.TIMESTAMP);
                pstmt.setString(8, "PENDING");
            }

            pstmt.setString(9, item.getOwner() != null ? item.getOwner().getUsername() : null);
            
            if (item instanceof Electronics) {
                pstmt.setString(10, "ELECTRONICS");
                pstmt.setString(11, ((Electronics) item).getBrand());
                pstmt.setNull(12, Types.VARCHAR);
            } else if (item instanceof Vehicle) {
                pstmt.setString(10, "VEHICLE");
                pstmt.setString(11, ((Vehicle) item).getBrand());
                pstmt.setNull(12, Types.VARCHAR);
            } else if (item instanceof Art) {
                pstmt.setString(10, "ART");
                pstmt.setNull(11, Types.VARCHAR);
                pstmt.setString(12, ((Art) item).getArtist());
            } else {
                pstmt.setNull(10, Types.VARCHAR);
                pstmt.setNull(11, Types.VARCHAR);
                pstmt.setNull(12, Types.VARCHAR);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET itemName = ?, description = ?, startingPrice = ?, owner_id = ?, brand = ?, artist_name = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getStartingPrice());
            pstmt.setString(4, item.getOwner() != null ? item.getOwner().getUsername() : null);

            if (item instanceof Electronics) {
                pstmt.setString(5, ((Electronics) item).getBrand());
                pstmt.setNull(6, Types.VARCHAR);
            } else if (item instanceof Vehicle) {
                pstmt.setString(5, ((Vehicle) item).getBrand());
                pstmt.setNull(6, Types.VARCHAR);
            } else if (item instanceof Art) {
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setString(6, ((Art) item).getArtist());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
            }

            pstmt.setString(7, item.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Item findById(String id) {
        String query = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String typeStr = rs.getString("item_type");
                    Item item = createItemFromResultSet(rs, typeStr);
                    if (item != null) {
                        mapCommonFields(rs, item);
                        return item;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Item createItemFromResultSet(ResultSet rs, String typeStr) throws SQLException {
        String name = rs.getString("itemName");
        String desc = rs.getString("description");
        double startPrice = rs.getDouble("startingPrice");
        
        try {
            ItemType type = ItemType.valueOf(typeStr.toUpperCase());
            String specificAttr = "";
            if (type == ItemType.ELECTRONICS || type == ItemType.VEHICLE) {
                specificAttr = rs.getString("brand");
            } else if (type == ItemType.ART) {
                specificAttr = rs.getString("artist_name");
            }
            return ItemFactory.createItem(type, name, desc, startPrice, specificAttr);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private void mapCommonFields(ResultSet rs, Item item) throws SQLException {
        item.setId(rs.getString("id"));
        item.setItemName(rs.getString("itemName"));
        item.setDescription(rs.getString("description"));
        item.setStartingPrice(rs.getDouble("startingPrice"));
        
        // Owner handling - Seller requires (username, password) in constructor
        String ownerId = rs.getString("owner_id");
        if (ownerId != null) {
            Seller owner = new Seller(ownerId, ""); 
            item.setOwner(owner);
        }
        
        // Note: imageUrls are currently not handled in the simple DB schema.
    }
}
