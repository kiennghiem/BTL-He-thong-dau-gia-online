package com.auction.server.database.dao.impl;
import com.auction.models.Item;
import com.auction.server.database.dao.*;
import com.auction.models.Auction;
import com.auction.server.observer.AuctionStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai các tác vụ truy vấn dữ liệu SQL thực tế cho thực thể Auction.
 * Tận dụng cơ chế Connection Pooling của BaseDAO và áp dụng kỹ thuật chặn Race Condition.
 */
public class AuctionDAOImpl extends BaseDAO implements AuctionDAO {

    // =========================================================================
    // QUẢN LÝ TẬP TRUNG TẤT CẢ CÁC CÂU LỆNH SQL CONSTANTS
    // =========================================================================
    private static final String SELECT_BASE =
            "SELECT id, status, title, description, startingPrice, currentPrice, startTime, endTime," +
            "itemId, highestBidderId FROM auctions";

    private static final String SELECT_BY_ID = SELECT_BASE + " WHERE id = ?;";
    private static final String SELECT_BY_ITEM_ID = SELECT_BASE + " WHERE itemId = ?;";
    private static final String SELECT_ALL = SELECT_BASE + ";";
    private static final String SELECT_BY_STATUS = SELECT_BASE + " WHERE status = ? ORDER BY endTime ASC;";

    private static final String INSERT_AUCTION =
            "INSERT INTO auctions (id, status, title, description, startingPrice, currentPrice," +
            "startTime, endTime, itemId, highestBidderId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String UPDATE_AUCTION =
            "UPDATE auctions SET title = ?, description = ?, startTime = ?, endTime = ? WHERE id = ?;";

    private static final String UPDATE_STATUS =
            "UPDATE auctions SET status = ? WHERE id = ?;";

    /**
     * GIẢI PHÁP ĐỒNG THỜI (CONCURRENT BIDDING):
     * Sử dụng mệnh đề WHERE có tính điều kiện nghiêm ngặt để kiểm tra trạng thái nguyên tử (Atomic).
     * Chỉ chấp nhận cập nhật giá mới cao hơn giá hiện tại, trạng thái đang chạy và thời gian chưa kết thúc.
     */
    private static final String PLACE_BID_ATOMIC =
            "UPDATE auctions SET currentPrice = ?, highestBidderId = ? WHERE id = ? AND status = 'RUNNING' AND endTime > ? AND ? > currentPrice;";

    // =========================================================================
    // IMPLEMENTATION METHODS
    // =========================================================================

    @Override
    public Auction findById(String id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ID);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToAuction(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    @Override
    public Auction findByItemId(String itemId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_ITEM_ID);
            stmt.setString(1, itemId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToAuction(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    @Override
    public List<Auction> findAll() throws SQLException {
        List<Auction> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_ALL);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAuction(rs));
            }
            return list;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) throws SQLException {
        List<Auction> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SELECT_BY_STATUS);
            stmt.setString(1, status.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToAuction(rs));
            }
            return list;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    @Override
    public boolean insert(Auction auction) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(INSERT_AUCTION);
            stmt.setString(1, auction.getId());
            stmt.setString(2, auction.getStatusAsString());
            stmt.setString(3, auction.getTitle());
            stmt.setString(4, auction.getDescription());
            stmt.setBigDecimal(5, auction.getStartingPrice());
            stmt.setBigDecimal(6, auction.getCurrentPrice());
            stmt.setTimestamp(7, Timestamp.valueOf(auction.getStartTime()));
            stmt.setTimestamp(8, Timestamp.valueOf(auction.getEndTime()));
            stmt.setString(9, auction.getItemId());
            stmt.setString(10, auction.getHighestBidderId());

            return stmt.executeUpdate() > 0;
        } finally {
            closeResources(stmt, conn);
        }
    }

    @Override
    public boolean update(Auction auction) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_AUCTION);
            stmt.setString(1, auction.getTitle());
            stmt.setString(2, auction.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(auction.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(auction.getEndTime()));
            stmt.setString(5, auction.getId());
            return stmt.executeUpdate() > 0;
        } finally {
            closeResources(stmt, conn);
        }
    }

    @Override
    public boolean updateStatus(String auctionId, AuctionStatus newStatus) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(UPDATE_STATUS);
            stmt.setString(1, newStatus.toString());
            stmt.setString(2, auctionId);
            return stmt.executeUpdate() > 0;
        } finally {
            closeResources(stmt, conn);
        }
    }

    @Override
    public boolean placeBid(String auctionId, String bidderId, BigDecimal newBidPrice) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(PLACE_BID_ATOMIC);

            LocalDateTime now = LocalDateTime.now();

            // Tham số gán cho câu lệnh UPDATE ... SET ... WHERE ...
            stmt.setBigDecimal(1, newBidPrice);     // SET currentPrice = ?
            stmt.setString(2, bidderId);           // SET highestBidderId = ?
            stmt.setString(3, auctionId);          // WHERE id = ?
            stmt.setTimestamp(4, Timestamp.valueOf(now)); // AND endTime > ? (Kiểm tra hết hạn thực tế)
            stmt.setBigDecimal(5, newBidPrice);     // AND ? > currentPrice (Chặn đứng mức giá cũ lỗi thời)

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Đưa ra cảnh báo hệ thống hoặc log chi tiết thay vì để lỗi nuốt chửng âm thầm
                System.err.println("[AuctionDAO] Thao tác đặt giá thất bại cho AuctionID: " + auctionId
                        + " do xung đột giá thấp hơn hiện tại hoặc phiên đấu giá đã kết thúc/đóng.");
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[AuctionDAO-Error] Lỗi nghiêm trọng xảy ra khi thực thi luồng đặt giá: " + e.getMessage());
            throw e; // Ném ngược Exception ra ngoài để tầng dịch vụ (Service) thực hiện Rollback/Xử lý UI nếu cần
        } finally {
            closeResources(stmt, conn);
        }
    }

    // =========================================================================
    // HELPER ROW MAPPER METHOD
    // =========================================================================
    /**
     * Bản đồ ánh xạ dữ liệu chuyển dòng bản ghi dạng ResultSet của MySQL sang Object Java mô hình.
     */
    private Auction mapRowToAuction(ResultSet rs) throws SQLException {
        ItemDAO itemDao = new ItemDAOImpl();
        BidDAO bidDao = new BidDAOImpl();

        Auction auction = new Auction();
        auction.setId(rs.getString("id"));
        Item item = itemDao.findById(rs.getString("itemId"));
        auction.setItem(item);
        if (item != null) {
            auction.setSeller(item.getOwner());
        }
        String statusStr = rs.getString("status");
        auction.setStatus(AuctionStatus.valueOf(statusStr));
        auction.setTitle(rs.getString("title"));
        auction.setDescription(rs.getString("description"));
        auction.setStartingPrice(rs.getBigDecimal("startingPrice"));
        auction.setCurrentPrice(rs.getBigDecimal("currentPrice"));
        auction.setHighestBidderId(rs.getString("highestBidderId"));

        // Load full bid history
        auction.setBidHistory(bidDao.getHistoryByItem(auction.getId()));

        // Chuyển đổi dữ liệu an toàn từ SQL Timestamp sang LocalDateTime của Java 8+
        Timestamp startTs = rs.getTimestamp("startTime");
        if (startTs != null) {
            auction.setStartTime(startTs.toLocalDateTime());
        }
        Timestamp endTs = rs.getTimestamp("endTime");
        if (endTs != null) {
            auction.setEndTime(endTs.toLocalDateTime());
        }
        return auction;
    }
}
