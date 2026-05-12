package main.java.com.auction.dao.impl;

import main.java.common.*;
import main.java.com.auction.dao.BaseDAO;
import main.java.com.auction.dao.ItemDAO;
import main.java.com.auction.dao.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl extends BaseDAO implements ItemDAO {

    @Override
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM items";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String type = rs.getString("item_type");
                Item item = createItemByType(type, rs);
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
        String sql = "INSERT INTO items (id, itemName, currentPrice, status, item_type) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getItemName());
            pstmt.setDouble(3, item.getCurrentPrice());

            // FIX: Add .name() to convert the ItemStatus enum to a String
            pstmt.setString(4, item.getStatus().name());

            pstmt.setString(5, item.getClass().getSimpleName().toUpperCase());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Item createItemByType(String type, ResultSet rs) throws SQLException {
        if ("ELECTRONIC".equalsIgnoreCase(type)) {
            Electronic e = new Electronic();
            e.setBrand(rs.getString("brand"));
            return e;
        } else if ("ART".equalsIgnoreCase(type)) {
            Art a = new Art();
            a.setArtist(rs.getString("artist_name"));
            return a;
        } else if ("VEHICLE".equalsIgnoreCase(type)) {
            Vehicle v = new Vehicle();
            v.setBrand(rs.getString("brand"));
            return v;
        }
        return null;
    }

    private void mapCommonFields(ResultSet rs, Item item) throws SQLException {
        item.setId(rs.getString("id"));
        item.setItemName(rs.getString("itemName"));
        item.setCurrentPrice(rs.getDouble("currentPrice"));
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            item.setStatus(ItemStatus.valueOf(statusStr));
        }
    }

    @Override
    public boolean updateItem(Item item) { return false; } // Implement as needed

    @Override
    public Item findById(String id) { return null; } // Implement as needed
}