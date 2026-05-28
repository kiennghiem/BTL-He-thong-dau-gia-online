package com.auction.server.database.dao.impl;

import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.DepositDAO;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DepositDAOImpl extends BaseDAO implements DepositDAO {

    @Override
    public boolean addDeposit(String auctionId, String username, double amount) throws SQLException {
        String sql = "INSERT INTO bidder_deposits (auction_id, username, amount) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            pstmt.setString(2, username);
            pstmt.setDouble(3, amount);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public double getDeposit(String auctionId, String username) throws SQLException {
        String sql = "SELECT amount FROM bidder_deposits WHERE auction_id = ? AND username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            pstmt.setString(2, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("amount");
            }
        }
        return 0;
    }

    @Override
    public Map<String, Double> getAllDepositsForAuction(String auctionId) throws SQLException {
        Map<String, Double> deposits = new HashMap<>();
        String sql = "SELECT username, amount FROM bidder_deposits WHERE auction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    deposits.put(rs.getString("username"), rs.getDouble("amount"));
                }
            }
        }
        return deposits;
    }

    @Override
    public boolean removeDeposit(String auctionId, String username) throws SQLException {
        String sql = "DELETE FROM bidder_deposits WHERE auction_id = ? AND username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeAllForAuction(String auctionId) throws SQLException {
        String sql = "DELETE FROM bidder_deposits WHERE auction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auctionId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
