package com.auction.server.database.dao;

import com.auction.exceptions.DatabaseException;
import com.auction.models.User;

import java.sql.SQLException;

public interface UserDAO {

    User findByUsername(String username) throws DatabaseException;

    User findById(String id) throws DatabaseException;

    void addUser(User user) throws DatabaseException;
}