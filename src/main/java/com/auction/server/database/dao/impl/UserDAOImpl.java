package com.auction.server.database.dao.impl;

import com.auction.exceptions.DatabaseException;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.DatabaseConnection;
import com.auction.server.factory.UserFactory;

import java.sql.*;

public class UserDAOImpl implements UserDAO {

    @Override
    public User authenticate(String username) throws DatabaseException {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Neu username da ton tai: resultSet has 1 row, and next() returns true, then moves the cursor to the
                // next row in the result set table.
                if (rs.next()) {
                    String retrievedPassword = rs.getString("password");
                    String retrievedRole = rs.getString("role");
                    // Using constructor from User.java
                    return UserFactory.createUser(username, retrievedPassword, retrievedRole);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error authenticating user, please try again");
        }
    }

    @Override
    public void registerUser(User user) throws DatabaseException {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getUser_Name());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());

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
}