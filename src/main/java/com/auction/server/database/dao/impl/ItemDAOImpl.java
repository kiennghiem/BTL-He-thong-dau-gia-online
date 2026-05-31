package com.auction.server.database.dao.impl;

import com.auction.models.*;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.factory.ItemFactory;
import com.auction.server.factory.ItemType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl extends BaseDAO implements ItemDAO {

    private UserDAO userDAO = new UserDAOImpl();

    @Override
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM items";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Item item = mapRowToItem(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (id, itemType, itemName, description, startingPrice, currentPrice, " +
                     "specialAttribute, ownerId, buyerId) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            throw new com.auction.exceptions.DatabaseException("SQL Error adding Item: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET itemType = ?, itemName = ?, description = ?, startingPrice = ?, currentPrice = ?, " +
                     "specialAttribute = ?, ownerId = ?, buyerId = ? WHERE id = ?";

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
            throw new com.auction.exceptions.DatabaseException("SQL Error updating Item: " + e.getMessage(), e);
        }
    }

    @Override
    public Item findById(String id) {
        String query = "SELECT * FROM items WHERE id = ?";

        if (id == null) return null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String typeStr = rs.getString("itemType");
        ItemType type = ItemType.valueOf(typeStr.toUpperCase());
        String name = rs.getString("itemName");
        String description = rs.getString("description");
        BigDecimal startingPrice = rs.getBigDecimal("startingPrice");
        BigDecimal currentPrice = rs.getBigDecimal("currentPrice");
        String specialAttribute = rs.getString("specialAttribute");
        String ownerId = rs.getString("ownerId");
        String buyerId = rs.getString("buyerId");

        User owner = userDAO.findById(ownerId);

        User buyer = userDAO.findById(buyerId);

        return ItemFactory.createItemFromDB(id, type, name, description, startingPrice, currentPrice,
                specialAttribute, (Seller) owner, (Bidder) buyer);
    }
}
