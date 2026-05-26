package com.auction.models;

import com.auction.server.factory.UserRole;

public class Bidder extends User {
    private static final long serialVersionUID = 1L;

    // Create a new Bidder instance
    public Bidder(String username, String password) {
        super(UserRole.BIDDER, username, password);
    }

    // Create a Bidder instance from an existed Bidder in DB
    public Bidder(String id, String username, String password, double balance) {
        super(id, UserRole.BIDDER, username, password, balance);
    }
}
