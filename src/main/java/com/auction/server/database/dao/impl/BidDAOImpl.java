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
        // Corrected SQL: Use camelCase column names and target 'auctions' table for price verification
        String checkPriceSQL = "SELECT currentPrice, status FROM auctions WHERE id = ? FOR UPDATE";
        String updateAuctionSQL = "UPDATE auctions SET currentPrice = ?, highestBidderId = ? WHERE id = ?";
        String insertBidSQL = "INSERT INTO bids (id, auctionId, bidderId, bidAmount, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPriceSQL);
                 PreparedStatement upStmt = conn.prepareStatement(updateAuctionSQL);
                 PreparedStatement inStmt = conn.prepareStatement(insertBidSQL)) {

                // 1. Atomic verification stage
                checkStmt.setString(1, bid.getItemId()); // Note: bid.getItemId() returns the auction ID in this context
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal currentPrice = rs.getBigDecimal("currentPrice");
                        String status = rs.getString("status");

                        // Validate bid amount and auction status
                        if (bid.getBidAmount().compareTo(currentPrice) <= 0 || 
                            !"RUNNING".equalsIgnoreCase(status)) {
                            conn.rollback();
                            return false;
                        }
                    } else {
                        conn.rollback();
                        return false;
                    }
                }

                // 2. Update the auction's live price and highest bidder
                upStmt.setBigDecimal(1, bid.getBidAmount());
                upStmt.setString(2, bid.getBidderId());
                upStmt.setString(3, bid.getItemId());
                upStmt.executeUpdate();

                // 3. Log the bid in the bids history table
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
        String sql = "SELECT b.*, u.username as bidderName FROM bids b " +
                     "JOIN users u ON b.bidderId = u.id " +
                     "WHERE b.auctionId = ? ORDER BY b.timestamp DESC"; // DESC as requested

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new BidTransaction(
                            rs.getString("id"), 
                            rs.getString("auctionId"),
                            rs.getString("bidderId"), 
                            rs.getString("bidderName"),
                            rs.getBigDecimal("bidAmount"),
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