package com.auction.server.database.dao.impl;

import com.auction.models.*;
import com.auction.server.database.dao.AuctionDAO;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.factory.ItemFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAOImpl extends BaseDAO implements AuctionDAO {

    @Override
    public List<Auction> getAllActiveAuctions() {
        List<Auction> activeAuctions = new ArrayList<>();
        String query = "SELECT * FROM items WHERE status IN ('OPEN', 'RUNNING', 'PENDING')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Auction auction = mapResultSetToAuction(rs);
                if (auction != null) {
                    activeAuctions.add(auction);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeAuctions;
    }

    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
        try {
            String typeStr = rs.getString("item_type");
            if (typeStr == null) return null;

            ItemType type = ItemType.valueOf(typeStr.toUpperCase());
            double startPrice = rs.getDouble("startingPrice");
            Item item = ItemFactory.createItem(
                    type,
                    rs.getString("itemName"),
                    rs.getString("description"),
                    startPrice,
                    (type == ItemType.ART) ? rs.getString("artist_name") : rs.getString("brand")
            );
            item.setId(rs.getString("id"));
            
            Seller seller = new Seller(rs.getString("owner_id"), "");
            item.setOwner(seller);

            Timestamp startTs = rs.getTimestamp("startingTime");
            Timestamp endTs = rs.getTimestamp("closingTime");
            
            if (startTs != null && endTs != null) {
                Auction auction = new Auction(item, seller, startTs.toLocalDateTime(), endTs.toLocalDateTime(), 10.0);
                auction.setId(item.getId());
                
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    auction.updateStatus(AuctionStatus.valueOf(statusStr.toUpperCase()));
                }
                return auction;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateAuctionStatus(Auction auction) {
        // Updates the combined 'items' table with the current auction state
        String sql = "UPDATE items SET status = ?, buyer_id = ?, currentPrice = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auction.getStatus().name());
            pstmt.setString(2, auction.getWinner() != null ? auction.getWinner().getUsername() : 
                              (auction.getHighestBid() != null ? auction.getHighestBid().getBidderId() : null));
            pstmt.setDouble(3, auction.getCurrentPrice());
            pstmt.setString(4, auction.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Auction findById(String auctionId) {
        // Auction objects are currently hydrated via AuctionService.loadActiveAuctions using ItemDAO
        return null; 
    }
}
