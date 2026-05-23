package com.auction.server.database.dao.impl;

import com.auction.models.User;
import com.auction.models.Bidder;
import com.auction.models.Seller;
import com.auction.models.Admin;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.DatabaseConnection2;
import com.auction.server.factory.UserFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {

    @Override
    public User authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection2.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Neu username da ton tai: resultSet has 1 row, and next() returns true, then moves the cursor to the
                // next row in the result set table.
                if (!rs.next()) {
                    return null;
                } else {
                    String retrievedPassword = rs.getString("password");
                    String retrievedRole = rs.getString("role");
                    // Using constructor from User.java
                    return UserFactory.createUser(username, retrievedPassword, retrievedRole);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean registerUser(User user) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection2.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getUser_Name());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}