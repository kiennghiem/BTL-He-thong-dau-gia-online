package com.auction.server.database.dao.impl;

import com.auction.models.User;
import com.auction.server.factory.UserRole;
import com.auction.server.factory.UserFactory;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.UserDAO;
import com.auction.exceptions.DatabaseException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl extends BaseDAO implements UserDAO {

    @Override
    public User findByUsername(String username) throws DatabaseException {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by username: " + username, e);
        }
        return null;
    }

    @Override
    public User findById(String id) throws DatabaseException {
        String query = "SELECT * FROM users WHERE id = ?";

        if (id == null) return null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by ID: " + id, e);
        }
        return null;
    }

    @Override
    public void addUser(User user) {
        String query = "INSERT INTO users (id, role, username, password, balance) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getRole().toString());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getPassword());
            pstmt.setBigDecimal(5, user.getBalance());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add user: " + user.getUsername(), e);
        }
    }

    @Override
    public void updateUser(User user) {
        String query = "UPDATE users SET role = ?, username = ?, password = ?, balance = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getRole().toString());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setBigDecimal(4, user.getBalance());
            pstmt.setString(5, user.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user: " + user.getUsername(), e);
        }
    }

    /**
     * Maps a database record row to the correct polymorphic User type.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String roleStr = rs.getString("role");
        String username = rs.getString("username");
        String password = rs.getString("password");
        BigDecimal balance = rs.getBigDecimal("balance");

        UserRole role;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            role = UserRole.BIDDER; // Safe fallback
        }

        try {
            return UserFactory.createUserFromDB(id, role, username, password, balance);
        } catch (IllegalArgumentException e) {
            throw new DatabaseException("Failed to create user from database record", e);
        }
    }
}