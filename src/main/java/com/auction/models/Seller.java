package com.auction.models;

import com.auction.server.factory.UserRole;

public class Seller extends User {
    private static final long serialVersionUID = 1L;

    // Create a new Seller instance
    public Seller(String userName, String password) {
        super(UserRole.SELLER, userName, password);
    }

    // Create a Seller instance from an existed Seller in DB
    public Seller(String id, String userName, String password, double balance) {
        super(id, UserRole.SELLER, userName, password, balance);
    }
}