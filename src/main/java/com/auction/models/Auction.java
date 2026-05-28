package com.auction.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.auction.exceptions.*;

public class Auction extends Entity {

    private static final long serialVersionUID = 1L;
    private Item item;
    private Seller seller;
    private AuctionStatus status;
    private LocalDateTime startTime; // Sửa tên thành startTime để khớp DB
    private LocalDateTime endTime;   // Sửa tên thành endTime để khớp DB
    private BidTransaction highestBid;
    private BigDecimal startingPrice; // Bổ sung để khớp với trường thông tin trong DAO
    private BigDecimal currentPrice; // Đổi sang BigDecimal tránh sai số tài chính
    private String title;            // Bổ sung để khớp với trường thông tin trong DAO
    private String description;      // Bổ sung để khớp với trường thông tin trong DAO
    private String itemId;           // Bổ sung để lưu itemId khi item object chưa được load

    // History of all proper bids
    private List<BidTransaction> bidHistory;

    // Constructors trống dành cho DAO RowMapper khởi tạo thực thể
    public Auction() {
        this.bidHistory = new ArrayList<>();
    }

    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime, String title, String description) {
        this.item = item;
        this.seller = seller;
        this.status = AuctionStatus.PENDING;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.description = description;
        // Khởi tạo giá hiện tại bằng giá khởi điểm của mặt hàng
        this.startingPrice = item.getStartingPrice();
        this.currentPrice = this.startingPrice;
        this.bidHistory = new ArrayList<>();
    }

    /**
     * Thay đổi trạng thái phiên đấu giá dựa trên quy tắc chuyển đổi hợp lệ.
     */
    public void updateStatus(AuctionStatus newStatus) throws InvalidStatusException {
        if (this.status == newStatus) {
            return;
        }
        switch(newStatus) {
            case OPEN -> {
                if(this.status != AuctionStatus.PENDING) { throw new InvalidStatusException("Cannot set this status to OPEN"); }
                this.status = newStatus;
            }
            case RUNNING -> {
                if(this.status != AuctionStatus.OPEN) { throw new InvalidStatusException("Cannot set this status to RUNNING"); }
                this.status = newStatus;
            }
            case FINISHED -> {
                if(this.status != AuctionStatus.RUNNING) { throw new InvalidStatusException("Cannot set this status to FINISHED"); }
                this.status = newStatus;
            }
            case PAID -> {
                if(this.status != AuctionStatus.FINISHED) { throw new InvalidStatusException("Cannot set this status to PAID"); }
                this.status = newStatus;
            }
            case CANCELED -> {
                this.status = newStatus;
            }
        }
    }

    // --- Getters & Setters hỗ trợ việc ánh xạ dữ liệu tầng DAO ---

    public String getItemId() {
        if (item != null) return item.getId();
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
        if (this.item != null) this.item.setId(itemId);
    }

    public String getHighestBidderId() {
        return highestBid != null ? highestBid.getBidderId() : null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getStartingPrice() {
        if (startingPrice != null) return startingPrice;
        return item != null ? item.getStartingPrice() : BigDecimal.ZERO;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getAuctionStatus() {
        return status;
    }

    public String getStatus() {
        return status != null ? status.name() : null;
    }

    public void setStatus(String statusStr) {
        if (statusStr != null) {
            try {
                this.status = AuctionStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Fallback or log if status string is invalid
            }
        }
    }

    public void addBid(BidTransaction bid) throws InvalidBidException {
        if (this.currentPrice != null && bid.getBidAmount().compareTo(this.currentPrice) <= 0) {
            throw new InvalidBidException("Bid must be higher than current price");
        }
        this.highestBid = bid;
        this.currentPrice = bid.getBidAmount();
        this.bidHistory.add(bid);
    }

    public BidTransaction getHighestBid() {
        return highestBid;
    }

    public long getClosingTimeMillis() {
        if (endTime == null) return 0;
        return endTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Các hàm Setter bổ sung cho việc tái thiết lập Object từ DB
    public void setHighestBidderId(String bidderId) {
        if (bidderId != null) {
            this.highestBid = new BidTransaction(this.getItemId(), bidderId, this.currentPrice != null ? this.currentPrice : getStartingPrice());
        }
    }
}