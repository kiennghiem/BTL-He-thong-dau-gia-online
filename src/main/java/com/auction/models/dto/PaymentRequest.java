package com.auction.models.dto;

import java.io.Serializable;

/**
 * Request sent by a winner to pay for a finished auction.
 */
public class PaymentRequest implements NetworkMessage, Serializable {
    private final String auctionId;
    private final String bidderId;

    public PaymentRequest(String auctionId, String bidderId) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    @Override
    public PacketType getType() {
        return PacketType.PAYMENT;
    }
}
