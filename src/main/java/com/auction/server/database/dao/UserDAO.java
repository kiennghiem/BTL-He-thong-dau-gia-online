package com.auction.server.database.dao;

import com.auction.exceptions.DatabaseException;
import com.auction.models.User;

import java.sql.SQLException;

public interface UserDAO {

    User findByUsername(String username); // Used for login, sign up

    User findById(String id);

    void addUser(User user); // Used for sign up

    void updateUser(User user);
}