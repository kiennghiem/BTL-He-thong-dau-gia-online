package com.auction.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.auction.exceptions.*;
import com.auction.server.observer.AuctionStatus;

public class Auction extends Entity {

    private static final long serialVersionUID = 1L;
    private Item item;
    private AuctionStatus status;
    private Seller seller;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BidTransaction highestBid;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;

    // History of all proper bids
    private List<BidTransaction> bidHistory;

    // Constructors trống dành cho DAO RowMapper khởi tạo thực thể
    public Auction() {
        this.bidHistory = new ArrayList<>();
    }

    public Auction(Item item, LocalDateTime startTime, LocalDateTime endTime) {
        this.item = item;
        this.status = AuctionStatus.OPEN;
        this.seller = item.getOwner();
        this.title = item.getItemName();
        this.description = item.getDescription();
        this.startTime = startTime;
        this.endTime = endTime;
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

    public Item getItem() {
        return item;
    }

    public String getItemId() {
        return item.getId();
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Seller getSeller() {
        if (seller == null && item != null) {
            return item.getOwner();
        }
        return seller;
    }

    public String getSellerId() {
        Seller s = getSeller();
        return s != null ? s.getId() : null;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public BidTransaction getHighestBid() {
        return highestBid;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
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

    public AuctionStatus getStatus() {
        return status;
    }

    public String getStatusAsString() {
        return status != null ? status.toString() : "OPEN";
    }

    public void setStatus(AuctionStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public void addBid(BidTransaction bid) throws InvalidBidException {
        if (bid.getBidAmount().compareTo(this.currentPrice) <= 0) {
            throw new InvalidBidException("Bid must be higher than current price");
        }
        this.highestBid = bid;
        this.currentPrice = bid.getBidAmount();
        this.bidHistory.add(bid);
    }

    public long getClosingTimeMillis() {
        if (endTime == null) return 0;
        return endTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Các hàm Setter bổ sung cho việc tái thiết lập Object từ DB
    public void setHighestBidderId(String bidderId) {
        if (bidderId != null) {
            this.highestBid = new BidTransaction(this.getItemId(), bidderId, this.currentPrice);
        }
    }
}