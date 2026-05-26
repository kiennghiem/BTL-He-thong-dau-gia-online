package com.auction.models;

import com.auction.server.factory.UserRole;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String userName, String password) {
        super(UserRole.ADMIN, userName, password);
    }


}
