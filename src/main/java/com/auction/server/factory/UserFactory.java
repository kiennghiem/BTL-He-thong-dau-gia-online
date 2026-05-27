package com.auction.server.factory;

import com.auction.models.*;
import java.math.BigDecimal;

public class UserFactory {
    public static User createNewUser(UserRole role, String username, String password) throws IllegalArgumentException {
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

    public static User createUserFromDB(String id, UserRole role, String username, String password, BigDecimal balance)
            throws IllegalArgumentException {
        switch (role) {
            case BIDDER -> {
                return new Bidder(id, username, password, balance);
            }
            case SELLER -> {
                return new Seller(id, username, password, balance);
            }
            case ADMIN -> {
                return new Admin(id, username, password, balance);
            }
            default -> throw new IllegalArgumentException("Invalid user role");
        }
    }
}
