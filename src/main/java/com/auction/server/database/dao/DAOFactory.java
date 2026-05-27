package com.auction.server.database.dao;

import com.auction.server.database.dao.impl.*;

/**
 * Lớp Nhà Máy (DAOFactory) quản lý tập trung việc khởi tạo các đối tượng DAO.
 * Áp dụng cơ chế Caching Singleton để tối ưu bộ nhớ, tránh việc tạo lại
 * đối tượng lặp đi lặp lại trong môi trường đa luồng (Socket Server).
 */
public final class DAOFactory {

    // Khởi tạo trước các instance duy nhất (Singleton Pattern) cho từng lớp DAO
    private static final UserDAO userDAO = new UserDAOImpl();
    private static final ItemDAO itemDAO = new ItemDAOImpl();
    private static final BidDAO bidDAO = new BidDAOImpl();
    private static final AuctionDAO auctionDAO = new AuctionDAOImpl();

    // Khóa Constructor không cho khởi tạo thực thể Factory bên ngoài
    private DAOFactory() {}

    /**
     * Lấy instance duy nhất quản lý dữ liệu người dùng (User).
     */
    public static UserDAO getUserDAO() {
        return userDAO;
    }

    /**
     * Lấy instance duy nhất quản lý dữ liệu sản phẩm (Item).
     */
    public static ItemDAO getItemDAO() {
        return itemDAO;
    }

    /**
     * Lấy instance duy nhất quản lý dữ liệu lịch sử đặt giá (Bid).
     */
    public static BidDAO getBidDAO() {
        return bidDAO;
    }

    /**
     * Lấy instance duy nhất quản lý dữ liệu phiên đấu giá (Auction).
     * Phục vụ đắc lực cho tầng Service xử lý Concurrent Bidding và Realtime Update.
     */
    public static AuctionDAO getAuctionDAO() {
        return auctionDAO;
    }
}