package com.auction.models;

import com.auction.server.factory.UserRole;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    // Create a new Admin instance
    public Admin(String username, String password) {
        super(UserRole.ADMIN, username, password);
    }

    // Create an Admin instance from an existed Admin in DB
    public Admin(String id, String username, String password, double balance) {
        super(id, UserRole.ADMIN, username, password, balance);
    }
}
