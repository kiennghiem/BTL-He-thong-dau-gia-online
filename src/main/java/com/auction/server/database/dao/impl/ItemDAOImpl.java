package com.auction.server.database.dao.impl;

import com.auction.models.*;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.ItemDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl extends BaseDAO implements ItemDAO {

    @Override
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        // Trong kiến trúc này, Item chỉ lưu thông tin tĩnh
        String query = "SELECT * FROM items";

        try (Connection conn = getConnection();
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
        // Chỉ insert các trường tĩnh thuộc về Item, các trường động (thời gian, trạng thái) thuộc về Auction
        String sql = "INSERT INTO items (id, itemName, description, startingPrice, owner_id, item_type, brand, artist_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getItemName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getStartingPrice());
            pstmt.setString(5, item.getOwner() != null ? item.getOwner().getUsername() : null);

            if (item instanceof Electronics) {
                pstmt.setString(6, "ELECTRONICS");
                pstmt.setString(7, ((Electronics) item).getBrand());
                pstmt.setNull(8, Types.VARCHAR);
            } else if (item instanceof Vehicle) {
                pstmt.setString(6, "VEHICLE");
                pstmt.setString(7, ((Vehicle) item).getBrand());
                pstmt.setNull(8, Types.VARCHAR);
            } else if (item instanceof Art) {
                pstmt.setString(6, "ART");
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setString(8, ((Art) item).getArtist());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR);
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
                    String type = rs.getString("item_type");
                    Item item = createItemByType(type, rs);
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

    @Override
    public boolean deleteItem(String id) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Item createItemByType(String type, ResultSet rs) throws SQLException {
        if ("ELECTRONICS".equalsIgnoreCase(type)) {
            Electronics e = new Electronics("", "", 0, "", null);
            e.setBrand(rs.getString("brand"));
            return e;
        } else if ("ART".equalsIgnoreCase(type)) {
            Art a = new Art("", "", 0, "", null);
            a.setArtist(rs.getString("artist_name"));
            return a;
        } else if ("VEHICLE".equalsIgnoreCase(type)) {
            Vehicle v = new Vehicle("", "", 0, "", null);
            v.setBrand(rs.getString("brand"));
            return v;
        }
        return null;
    }

    private void mapCommonFields(ResultSet rs, Item item) throws SQLException {
        item.setId(rs.getString("id"));
        
        // Kiểm tra xem cột có tồn tại không trước khi get để tránh lỗi nếu schema thiếu
        try {
            item.setItemName(rs.getString("itemName"));
        } catch (SQLException e) {
            try { item.setItemName(rs.getString("title")); } catch (Exception ex) {}
        }
        
        item.setDescription(rs.getString("description"));
        
        try {
            item.setStartingPrice(rs.getDouble("startingPrice"));
        } catch (SQLException e) {
            // Ignored if missing
        }

        String ownerId = rs.getString("owner_id");
        if (ownerId != null) {
            Seller owner = new Seller(ownerId, "");
            item.setOwner(owner);
        }
    }
}
