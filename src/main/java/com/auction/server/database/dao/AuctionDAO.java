package com.auction.server.database.dao;

import com.auction.models.Auction; // Cập nhật đúng package vị trí của Auction.java
import com.auction.server.observer.AuctionStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface định nghĩa các hợp đồng (contract) thao tác dữ liệu cho thực thể Auction.
 */
public interface AuctionDAO {

    Auction findById(String id) throws SQLException;

    Auction findByItemId(String itemId) throws SQLException;

    List<Auction> findAll() throws SQLException;

    List<Auction> findByStatus(AuctionStatus status) throws SQLException;

    boolean insert(Auction auction) throws SQLException;

    boolean update(Auction auction) throws SQLException;

    boolean updateStatus(String auctionId, AuctionStatus newStatus) throws SQLException;

    boolean placeBid(String auctionId, String bidderId, BigDecimal newBidPrice) throws SQLException;
}
