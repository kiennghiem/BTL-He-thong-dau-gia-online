package com.auction.models;

import com.auction.server.factory.UserRole;

public class Bidder extends User {
    private static final long serialVersionUID = 1L;
    private double balance;

    public Bidder(String userName, String password) {
        super(UserRole.BIDDER, userName, password);
    }
    
}
