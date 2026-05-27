package com.auction.server.database.dao.impl;

import com.auction.models.BidTransaction;
import common.AppConstants;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.BidDAO;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDAOImpl extends BaseDAO implements BidDAO {

    @Override
    public boolean placeBid(BidTransaction bid) {
        String checkPriceSQL = "SELECT currentPrice, status FROM items WHERE id = ? FOR UPDATE";
        String updateItemSQL = "UPDATE items SET currentPrice = ?, current_bidder_id = ? WHERE id = ?";
        String insertBidSQL = "INSERT INTO bids (id, itemId, bidderId, bidAmount, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPriceSQL);
                 PreparedStatement upStmt = conn.prepareStatement(updateItemSQL);
                 PreparedStatement inStmt = conn.prepareStatement(insertBidSQL)) {
                
                // 1. Atomic verification stage
                checkStmt.setString(1, bid.getItemId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal currentPrice = rs.getBigDecimal("currentPrice");
                        String status = rs.getString("status");

                        // Validate price and open auction state
                        if (bid.getBidAmount().compareTo(currentPrice) <= 0 || !AppConstants.STATUS_RUNNING.equalsIgnoreCase(status)) {
                            conn.rollback();
                            return false;
                        }
                    } else {
                        conn.rollback();
                        return false;
                    }
                }

                // 2. Update the item's live price tracking and current bidder
                upStmt.setBigDecimal(1, bid.getBidAmount());
                upStmt.setString(2, bid.getBidderId());
                upStmt.setString(3, bid.getItemId());
                upStmt.executeUpdate();

                // 3. Log the history record transaction
                inStmt.setString(1, bid.getId());
                inStmt.setString(2, bid.getItemId());
                inStmt.setString(3, bid.getBidderId());
                inStmt.setBigDecimal(4, bid.getBidAmount());
                inStmt.setTimestamp(5, Timestamp.valueOf(bid.getTimestamp()));
                inStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<BidTransaction> getHistoryByItem(String itemId) {
        List<BidTransaction> history = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE itemId = ? ORDER BY timestamp ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new BidTransaction(
                            rs.getString("id"), rs.getString("itemId"),
                            rs.getString("bidderId"), rs.getBigDecimal("bidAmount"),
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
