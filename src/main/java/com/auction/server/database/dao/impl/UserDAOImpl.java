package com.auction.server.database.dao.impl;

import com.auction.exceptions.DatabaseException;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.DatabaseConnection;
import com.auction.server.factory.UserFactory;
import com.auction.server.factory.UserRole;

import java.math.BigDecimal;
import java.sql.*;

public class UserDAOImpl implements UserDAO {

    @Override
    public User findByUsername(String username) throws DatabaseException {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                // If username exists in the database, then result set has 1 row, and next() returns true
                if (rs.next()) {
                    return createUserFromRow(rs);
                } else {
                    return null; // Username doesn't exist
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by username");
        }
    }

    @Override
    public User findById(String id) throws DatabaseException {
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                // If a user with id exists in the database, then result set has 1 row, and next() returns true
                if (rs.next()) {
                    return createUserFromRow(rs);
                } else {
                    return null; // No user with given id exists
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by username");
        }
    }

    @Override
    public void addUser(User user) throws DatabaseException {
        String query = "INSERT INTO users (id, username, password, role, balance) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole().toString());
            pstmt.setBigDecimal(5, user.getBalance());

            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DatabaseException("Error registering user, please try again");
        }
    }

    private User createUserFromRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String role = rs.getString("role");
        UserRole roleEnum = UserRole.valueOf(role.toUpperCase());
        BigDecimal balance = rs.getBigDecimal("balance");

        return UserFactory.createUserFromDB(id, roleEnum, username, password, balance);
    }
}
