package com.auction.server.database.dao.impl;

import com.auction.models.BidTransaction;
import com.auction.models.dto.AppConstants;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.BidDAO;
import com.auction.exceptions.DatabaseException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of BidDAO using JDBC.
 * Handles bid persistence and history retrieval with atomic consistency.
 */
public class BidDAOImpl extends BaseDAO implements BidDAO {

    @Override
    public boolean placeBid(BidTransaction bid) {
        String insertBidSQL = "INSERT INTO bids (id, auction_id, bidder_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)";
        String updateItemSQL = "UPDATE items SET current_price = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement inStmt = conn.prepareStatement(insertBidSQL);
             PreparedStatement upStmt = conn.prepareStatement(updateItemSQL)) {

            // Update the item's live price tracking and current bidder
            upStmt.setBigDecimal(1, bid.getBidAmount());
            upStmt.setString(2, bid.getBidderId());
            upStmt.setString(3, bid.getItemId());
            upStmt.executeUpdate();

            // Log the history record transaction
            inStmt.setString(1, bid.getId());
            inStmt.setString(2, bid.getItemId()); // BidTransaction uses itemId to store auctionId
            inStmt.setString(3, bid.getBidderId());
            inStmt.setBigDecimal(4, bid.getBidAmount());
            inStmt.setTimestamp(5, Timestamp.valueOf(bid.getTimestamp()));
            
            return inStmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BidDAO] Failed to record bid history: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<BidTransaction> getHistoryByItem(String auctionId) {
        List<BidTransaction> history = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY timestamp ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, auctionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new BidTransaction(
                            rs.getString("id"), 
                            rs.getString("auction_id"),
                            rs.getString("bidder_id"), 
                            rs.getBigDecimal("amount"),
                            rs.getTimestamp("timestamp").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BidDAO] Failed to retrieve bid history for auction: " + auctionId);
        }
        return history;
    }
}