package com.auction.server.database.dao;

import java.sql.SQLException;
import java.util.Map;

public interface DepositDAO {
    boolean addDeposit(String auctionId, String username, double amount) throws SQLException;
    double getDeposit(String auctionId, String username) throws SQLException;
    Map<String, Double> getAllDepositsForAuction(String auctionId) throws SQLException;
    boolean removeDeposit(String auctionId, String username) throws SQLException;
    boolean removeAllForAuction(String auctionId) throws SQLException;
}
