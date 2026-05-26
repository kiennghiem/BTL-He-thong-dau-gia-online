package com.auction.models;

import com.auction.server.factory.UserRole;

public class Seller extends User {
    private static final long serialVersionUID = 1L;

    public Seller(String userName, String password) {
        super(UserRole.SELLER, userName, password);
    }
}