package com.auction.models.dto;

public class CancelAuctionRequest implements NetworkMessage {
    private final String auctionId;
    private final String adminId;
    private final String reason;

    public CancelAuctionRequest(String auctionId, String adminId) {
        this(auctionId, adminId, "No reason provided.");
    }

    public CancelAuctionRequest(String auctionId, String adminId, String reason) {
        this.auctionId = auctionId;
        this.adminId = adminId;
        this.reason = reason;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getReason() {
        return reason;
    }
}
