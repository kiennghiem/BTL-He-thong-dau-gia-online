package com.auction.server.database.dao.impl;

import com.auction.models.User;
import com.auction.server.factory.UserRole;
import com.auction.server.factory.UserFactory;
import com.auction.server.database.dao.BaseDAO;
import com.auction.server.database.dao.UserDAO;
import com.auction.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl extends BaseDAO implements UserDAO {

    @Override
    public User findByUsername(String username) {
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
    public User findById(String id) {
        String query = "SELECT * FROM users WHERE id = ?";

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
        String query = "INSERT INTO users (id, username, password, role, balance) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole().name());
            pstmt.setDouble(5, user.getBalance());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add user: " + user.getUsername(), e);
        }
    }

    @Override
    public void updateUser(User user) {
        String query = "UPDATE users SET username = ?, password = ?, role = ?, balance = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole().name());
            pstmt.setDouble(4, user.getBalance());
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
        String username = rs.getString("username");
        String password = rs.getString("password");
        String roleStr = rs.getString("role");
        double balance = rs.getDouble("balance");

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