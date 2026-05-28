package com.auction.models;

import com.auction.server.factory.UserRole;

import java.math.BigDecimal;

public class Seller extends User {
    private static final long serialVersionUID = 1L;

    public Seller() {
        super();
        this.setRole(UserRole.SELLER);
    }

    // Create a new Seller instance
    public Seller(String username, String password) {
        super(UserRole.SELLER, username, password);
    }

    // Create a Seller instance from an existed Seller in DB
    public Seller(String id, String username, String password, BigDecimal balance) {
        super(id, UserRole.SELLER, username, password, balance);
    }
}