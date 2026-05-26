package com.auction.server.database.dao;

import com.auction.models.User;

public interface UserDAO {
    /**
     * Finds a user by their username and password.
     */
    User login(String username, String password);

    /**
     * Saves a new user to the database with its password.
     */
    boolean register(User user, String password);

    /**
     * Checks if a username already exists in the database.
     */
    boolean checkUserExists(String username);
}
