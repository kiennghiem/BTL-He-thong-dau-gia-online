package main.java.com.auction.dao.impl;

import main.java.com.auction.models.User;
import main.java.com.auction.models.Bidder;
import main.java.com.auction.models.Seller;
import main.java.com.auction.models.Admin;
import main.java.common.AppConstants;
import main.java.com.auction.dao.BaseDAO;
import main.java.com.auction.dao.UserDAO;
import main.java.com.auction.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl extends BaseDAO implements UserDAO {

    @Override
    public User authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    // Using constructor from User.java
                    return createUserByRole(username, password, role);
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User createUserByRole(String username, String password, String role) {
        // AppConstants handles role string comparisons
        if (AppConstants.ROLE_BIDDER.equalsIgnoreCase(role)) {
            return new Bidder(username, password); //
        } else if (AppConstants.ROLE_SELLER.equalsIgnoreCase(role)) {
            return new Seller(username, password); //
        } else if (AppConstants.ROLE_ADMIN.equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role)) {
            return new Admin(username, password);  //
        }
        return null;
    }
}