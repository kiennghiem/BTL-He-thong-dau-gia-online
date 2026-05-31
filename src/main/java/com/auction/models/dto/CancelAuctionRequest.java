package com.auction.models.dto;

public class CancelAuctionRequest implements NetworkMessage {
    private final String auctionId;
    private final String adminId;

    public CancelAuctionRequest(String auctionId, String adminId) {
        this.auctionId = auctionId;
        this.adminId = adminId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getAdminId() {
        return adminId;
    }
}
