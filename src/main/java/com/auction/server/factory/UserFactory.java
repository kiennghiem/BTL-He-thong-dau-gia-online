package com.auction.server.factory;

import com.auction.models.*;

public class UserFactory {
    public static User createUser(UserRole role, String username, String password) throws IllegalArgumentException {
        switch (role) {
            case BIDDER -> {
                return new Bidder(username, password);
            }
            case SELLER -> {
                return new Seller(username, password);
            }
            case ADMIN -> {
                return new Admin(username, password);
            }
            default -> throw new IllegalArgumentException("Invalid user role");
        }
    }
}
