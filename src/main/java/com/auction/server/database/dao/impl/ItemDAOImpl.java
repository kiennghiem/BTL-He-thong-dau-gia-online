package com.auction.server.database.dao.impl;

import common.*;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.ItemDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAOImpl extends BaseDAO implements ItemDAO {

    @Override
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
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
        String sql = "INSERT INTO items (id, itemName, description, currentPrice, startingPrice, startingTime, closingTime, status, owner_id, current_bidder_id, buyer_id, item_type, brand, artist_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getItemName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getCurrentPrice());
            pstmt.setDouble(5, item.getStartingPrice());
            pstmt.setTimestamp(6, item.getStartingTime() != null ? Timestamp.valueOf(item.getStartingTime()) : null);
            pstmt.setTimestamp(7, item.getClosingTime() != null ? Timestamp.valueOf(item.getClosingTime()) : null);
            pstmt.setString(8, item.getStatus().name());
            
            // For now, we use usernames/IDs as strings from the User objects
            pstmt.setString(9, item.getOwner()); 
            pstmt.setString(10, item.getCurrentBidder());
            pstmt.setString(11, item.getBuyer());

            if (item instanceof Electronic) {
                pstmt.setString(12, "ELECTRONICS");
                pstmt.setString(13, ((Electronic) item).getBrand());
                pstmt.setNull(14, Types.VARCHAR);
            } else if (item instanceof Vehicle) {
                pstmt.setString(12, "VEHICLE");
                pstmt.setString(13, ((Vehicle) item).getBrand());
                pstmt.setNull(14, Types.VARCHAR);
            } else if (item instanceof Art) {
                pstmt.setString(12, "ART");
                pstmt.setNull(13, Types.VARCHAR);
                pstmt.setString(14, ((Art) item).getArtist());
            } else {
                pstmt.setNull(12, Types.VARCHAR);
                pstmt.setNull(13, Types.VARCHAR);
                pstmt.setNull(14, Types.VARCHAR);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET itemName = ?, description = ?, currentPrice = ?, startingPrice = ?, startingTime = ?, closingTime = ?, status = ?, owner_id = ?, current_bidder_id = ?, buyer_id = ?, brand = ?, artist_name = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getCurrentPrice());
            pstmt.setDouble(4, item.getStartingPrice());
            pstmt.setTimestamp(5, item.getStartingTime() != null ? Timestamp.valueOf(item.getStartingTime()) : null);
            pstmt.setTimestamp(6, item.getClosingTime() != null ? Timestamp.valueOf(item.getClosingTime()) : null);
            pstmt.setString(7, item.getStatus().name());
            pstmt.setString(8, item.getOwner());
            pstmt.setString(9, item.getCurrentBidder());
            pstmt.setString(10, item.getBuyer());

            if (item instanceof Electronic) {
                pstmt.setString(11, ((Electronic) item).getBrand());
                pstmt.setNull(12, Types.VARCHAR);
            } else if (item instanceof Vehicle) {
                pstmt.setString(11, ((Vehicle) item).getBrand());
                pstmt.setNull(12, Types.VARCHAR);
            } else if (item instanceof Art) {
                pstmt.setNull(11, Types.VARCHAR);
                pstmt.setString(12, ((Art) item).getArtist());
            } else {
                pstmt.setNull(11, Types.VARCHAR);
                pstmt.setNull(12, Types.VARCHAR);
            }

            pstmt.setString(13, item.getId());

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

    private Item createItemByType(String type, ResultSet rs) throws SQLException {
        if ("ELECTRONICS".equalsIgnoreCase(type)) {
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
        item.setDescription(rs.getString("description"));
        item.setCurrentPrice(rs.getDouble("currentPrice"));
        item.setStartingPrice(rs.getDouble("startingPrice"));
        
        Timestamp startTs = rs.getTimestamp("startingTime");
        if (startTs != null) item.setStartingTime(startTs.toLocalDateTime());
        
        Timestamp closeTs = rs.getTimestamp("closingTime");
        if (closeTs != null) item.setClosingTime(closeTs.toLocalDateTime());

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                item.setStatus(ItemStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        // Handle User objects by creating stubs if necessary
        // In a real application, you might use a UserDAO to fetch full objects
        String ownerId = rs.getString("owner_id");
        if (ownerId != null) {
            Seller owner = new Seller();
            owner.setUsername(ownerId);
            item.setOwner(owner);
        }

        String bidderId = rs.getString("current_bidder_id");
        if (bidderId != null) {
            Bidder bidder = new Bidder();
            bidder.setUsername(bidderId);
            item.setCurrentBidder(bidder);
        }

        String buyerId = rs.getString("buyer_id");
        if (buyerId != null) {
            Bidder buyer = new Bidder();
            buyer.setUsername(buyerId);
            item.setBuyer(buyer);
        }
    }
}
