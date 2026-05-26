package com.auction.server.database.dao;

import com.auction.exceptions.DatabaseException;
import com.auction.models.User;

import java.sql.SQLException;

public interface UserDAO {

    User findByUsername(String username) throws DatabaseException;

    User findById(String id) throws DatabaseException;
    /**
     * Finds a user by their username and password.
     *
     * @return User object if found, null otherwise.
     * @throws DatabaseException if failure occurs when connecting to the DB.
     */
    User authenticate(String username, String password) throws DatabaseException;

    /**
     * Register a new user in the database.
     *
     * @param user
     * @throws DatabaseException if the username already exists in the DB, or failure occurs when connecting to the DB.
     */
    void registerUser(User user) throws DatabaseException;
}