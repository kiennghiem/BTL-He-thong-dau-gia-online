package main.java.com.auction.dao.impl;

import main.java.com.auction.models.BidTransaction;
import main.java.common.AppConstants;
import main.java.com.auction.dao.BaseDAO;
import main.java.com.auction.dao.BidDAO;
import main.java.com.auction.dao.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDAOImpl extends BaseDAO implements BidDAO {

    @Override
    public boolean placeBid(BidTransaction bid) {
        String checkPriceSQL = "SELECT currentPrice, status FROM items WHERE id = ? FOR UPDATE";
        String updateItemSQL = "UPDATE items SET currentPrice = ? WHERE id = ?";
        String insertBidSQL = "INSERT INTO bids (id, itemId, bidderId, bidAmount, timestamp) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start Transaction for Concurrency Control

            try (PreparedStatement checkStmt = conn.prepareStatement(checkPriceSQL)) {
                checkStmt.setString(1, bid.getItemId());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    double currentPrice = rs.getDouble("currentPrice");
                    String status = rs.getString("status");

                    // Atomic validation
                    if (bid.getBidAmount() <= currentPrice || !AppConstants.STATUS_RUNNING.equals(status)) {
                        conn.rollback();
                        return false;
                    }

                    // Update item price and record history in one transaction
                    try (PreparedStatement upStmt = conn.prepareStatement(updateItemSQL);
                         PreparedStatement inStmt = conn.prepareStatement(insertBidSQL)) {

                        upStmt.setDouble(1, bid.getBidAmount());
                        upStmt.setString(2, bid.getItemId());
                        upStmt.executeUpdate();

                        inStmt.setString(1, bid.getId());
                        inStmt.setString(2, bid.getItemId());
                        inStmt.setString(3, bid.getBidderId());
                        inStmt.setDouble(4, bid.getBidAmount());
                        inStmt.setTimestamp(5, Timestamp.valueOf(bid.getTimestamp()));
                        inStmt.executeUpdate();
                    }

                    conn.commit(); // Success
                    return true;
                }
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<BidTransaction> getHistoryByItem(String itemId) {
        List<BidTransaction> history = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE itemId = ? ORDER BY timestamp ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new BidTransaction(
                            rs.getString("id"), rs.getString("itemId"),
                            rs.getString("bidderId"), rs.getDouble("bidAmount"),
                            rs.getTimestamp("timestamp").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}