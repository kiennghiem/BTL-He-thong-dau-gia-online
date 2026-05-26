package com.auction.models;

import com.auction.server.factory.UserRole;

public class Bidder extends User {
    private static final long serialVersionUID = 1L;

    // Create a new Bidder instance
    public Bidder(String userName, String password) {
        super(UserRole.BIDDER, userName, password);
    }

    // Create a Bidder instance from an existed Bidder in DB
    public Bidder(String id, String userName, String password, double balance) {
        super(id, UserRole.BIDDER, userName, password, balance);
    }
}
