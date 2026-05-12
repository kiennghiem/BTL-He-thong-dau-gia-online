package main.java.com.auction.dao;

import main.java.com.auction.dao.impl.*;

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