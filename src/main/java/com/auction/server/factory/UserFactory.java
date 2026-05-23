package com.auction.server.factory;

import com.auction.models.*;

public class UserFactory {
    public static User createUser(String username, String password, String role) throws IllegalArgumentException {
        switch (role) {
            case "Bidder" -> {
                return new Bidder(username, password);
            }
            case "Seller" -> {
                return new Seller(username, password);
            }
            case "Admin" -> {
                return new Admin(username, password);
            }
            default -> throw new IllegalArgumentException("Invalid user role");
        }
    }
}
