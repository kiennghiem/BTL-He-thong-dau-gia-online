package com.auction.server.database.dao;

import com.auction.exceptions.InvalidLoginException;
import com.auction.models.User;

import java.sql.SQLException;

public interface UserDAO {
    /**
     * Finds a user by their username and password.
     */
    User authenticate(String username, String password);

    /**
     * Saves a new user to the database.
     */
    boolean registerUser(User user);
}