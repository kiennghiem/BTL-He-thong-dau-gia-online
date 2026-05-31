package com.auction.server.database.dao.impl;

import com.auction.models.*;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.factory.ItemFactory;
import com.auction.server.factory.ItemType;
import com.auction.server.factory.UserRole;
import com.auction.server.factory.UserFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Optimized ItemDAO using JOINs to avoid nested connection calls.
 */
public class ItemDAOImpl extends BaseDAO implements ItemDAO {

    // Optimized JOIN to get item and owner in one go
    private static final String SELECT_ITEM_JOIN = 
        "SELECT i.*, u.username as ownerName, u.role as ownerRole, u.balance as ownerBalance, " +
        "b.username as buyerName, b.role as buyerRole, b.balance as buyerBalance " +
        "FROM items i " +
        "JOIN users u ON i.ownerId = u.id " +
        "LEFT JOIN users b ON i.buyerId = b.id";

    @Override
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ITEM_JOIN);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapFullRowToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public Item findById(String id) {
        String sql = SELECT_ITEM_JOIN + " WHERE i.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapFullRowToItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Maps a joined result set row to a complete Item object with Owner and Buyer.
     * NO NESTED DATABASE CALLS HERE.
     */
    private Item mapFullRowToItem(ResultSet rs) throws SQLException {
        // 1. Create Owner (Seller)
        Seller owner = (Seller) UserFactory.createUserFromDB(
            rs.getString("ownerId"),
            UserRole.valueOf(rs.getString("ownerRole").toUpperCase()),
            rs.getString("ownerName"),
            "HIDDEN",
            rs.getBigDecimal("ownerBalance")
        );

        // 2. Create Buyer (Bidder) if exists
        Bidder buyer = null;
        String bId = rs.getString("buyerId");
        if (bId != null) {
            buyer = (Bidder) UserFactory.createUserFromDB(
                bId,
                UserRole.valueOf(rs.getString("buyerRole").toUpperCase()),
                rs.getString("buyerName"),
                "HIDDEN",
                rs.getBigDecimal("buyerBalance")
            );
        }

        // 3. Create Item
        return ItemFactory.createItemFromDB(
            rs.getString("id"),
            ItemType.valueOf(rs.getString("itemType").toUpperCase()),
            rs.getString("itemName"),
            rs.getString("description"),
            rs.getBigDecimal("startingPrice"),
            rs.getBigDecimal("currentPrice"),
            rs.getString("specialAttribute"),
            owner,
            buyer
        );
    }

    @Override
    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (id, itemType, itemName, description, startingPrice, currentPrice, specialAttribute, ownerId, buyerId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getTypeAsString());
            pstmt.setString(3, item.getItemName());
            pstmt.setString(4, item.getDescription());
            pstmt.setBigDecimal(5, item.getStartingPrice());
            pstmt.setBigDecimal(6, item.getCurrentPrice());
            pstmt.setString(7, item.getSpecialAttribute());
            pstmt.setString(8, item.getOwner().getId());
            pstmt.setString(9, item.getBuyer() != null ? item.getBuyer().getId() : null);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new com.auction.exceptions.DatabaseException("Error adding item", e);
        }
    }

    @Override
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET itemType=?, itemName=?, description=?, startingPrice=?, currentPrice=?, specialAttribute=?, ownerId=?, buyerId=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getTypeAsString());
            pstmt.setString(2, item.getItemName());
            pstmt.setString(3, item.getDescription());
            pstmt.setBigDecimal(4, item.getStartingPrice());
            pstmt.setBigDecimal(5, item.getCurrentPrice());
            pstmt.setString(6, item.getSpecialAttribute());
            pstmt.setString(7, item.getOwner().getId());
            pstmt.setString(8, item.getBuyer() != null ? item.getBuyer().getId() : null);
            pstmt.setString(9, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new com.auction.exceptions.DatabaseException("Error updating item", e);
        }
    }
}
