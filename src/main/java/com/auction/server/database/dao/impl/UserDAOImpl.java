package com.auction.server.database.dao.impl;

import com.auction.exceptions.DatabaseException;
import com.auction.models.Bidder;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.DatabaseConnection;
import com.auction.server.factory.UserFactory;
import com.auction.server.factory.UserRole;

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
    public User authenticate(String username, String password) throws DatabaseException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Neu user voi username va password da ton tai: resultSet has 1 row, and next() returns true,
                // then moves the cursor to the next row in the result set table.
                if (rs.next()) {
                    String retrievedRole = rs.getString("role");
                    UserRole roleEnum = UserRole.valueOf(retrievedRole.toUpperCase());
                    // Using constructor from User.java
                    return UserFactory.createNewUser(roleEnum, username, password);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException("Error authenticating user, please try again");
        }
    }

    @Override
    public void registerUser(User user) throws DatabaseException {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole().toString());

            pstmt.executeUpdate();
        }
        // This exception is thrown when inserting a user into the DB but username already exists, preventing
        // duplicate username in DB if two people sign up at the same time with the same username.
        catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("You cannot use this username");
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
        double balance = rs.getDouble("balance");

        return UserFactory.createUserFromDB(id, roleEnum, username, password, balance);
    }
}