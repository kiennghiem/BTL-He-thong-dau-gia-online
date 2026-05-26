package com.auction.server.database.dao;

import com.auction.server.database.dao.impl.*;

public class DAOFactory {
    public static UserDAO getUserDAO() {
        return new UserDAOImpl();
    }

    public static ItemDAO getItemDAO() {
        return new ItemDAOImpl();
    }

    public static BidDAO getBidDAO() {
        return new BidDAOImpl();
    }
}