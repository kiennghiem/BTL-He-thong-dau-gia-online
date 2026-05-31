package com.auction.models.dto;

import java.math.BigDecimal;

/**
 * DTO sent from Client to Server when a winner pays for an item.
 */
public class PayRequest implements NetworkMessage {
    private final String auctionId;
    private final String bidderId;
    private final BigDecimal amount;

    public PayRequest(String auctionId, String bidderId, BigDecimal amount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
