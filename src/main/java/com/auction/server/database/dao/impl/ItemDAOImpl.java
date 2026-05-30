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
        String sql = "INSERT INTO items (id, item_type, item_name, description, startingPrice, currentPrice," +
                     "special_attribute, owner_id, buyer_id" +
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
            pstmt.setString(9, item.getBuyer() != null? item.getBuyer().getId() : null);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET item_name = ?, description = ?, startingPrice = ?, currentPrice = ?," +
                     "special_attribute, owner_id = ?, buyer_id = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getDescription());
            pstmt.setBigDecimal(3, item.getStartingPrice());
            pstmt.setBigDecimal(4, item.getCurrentPrice());
            pstmt.setString(5, item.getSpecialAttribute());
            pstmt.setString(6, item.getOwner().getId());
            pstmt.setString(7, item.getBuyer() != null? item.getBuyer().getId() : null);

            pstmt.setString(8, item.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
        String typeStr = rs.getString("item_type");
        ItemType type = ItemType.valueOf(typeStr.toUpperCase());
        String name = rs.getString("item_name");
        String description = rs.getString("description");
        BigDecimal startingPrice = rs.getBigDecimal("starting_price");
        BigDecimal currentPrice = rs.getBigDecimal("current_price");
        String specialAttribute = rs.getString("special_attribute");
        String ownerId = rs.getString("owner_id");
        String buyerId = rs.getString("buyer_id");

        User owner = userDAO.findById(ownerId);

        User buyer = userDAO.findById(buyerId);

        return ItemFactory.createItemFromDB(id, type, name, description, startingPrice, currentPrice,
                specialAttribute, (Seller) owner, (Bidder) buyer);
    }
}
