package com.auction.models;

import common.NetworkMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a validated and finalized bid in the system.
 * Essential for "Bid History Visualization" and "Realtime Price Curve".
 */
public class BidTransaction implements NetworkMessage {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String itemId;
    private final String bidderId;
    private final BigDecimal bidAmount; // Đổi sang BigDecimal để đồng bộ với DAO
    private final LocalDateTime timestamp;

    /**
     * Constructor used by the Server after a BidRequest is validated.
     */
    public BidTransaction(String itemId, String bidderId, BigDecimal bidAmount) {
        this.id = UUID.randomUUID().toString();
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Overloaded constructor for the DAO when loading from the Database.
     */
    public BidTransaction(String id, String itemId, String bidderId, BigDecimal bidAmount, LocalDateTime timestamp) {
        this.id = id;
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

    // --- Getters for DAO and JavaFX Charts ---

    public String getId() {
        return id;
    }

    public String getItemId() {
        return itemId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "BidTransaction{" +
                "id='" + id + '\'' +
                ", itemId='" + itemId + '\'' +
                ", bidderId='" + bidderId + '\'' +
                ", bidAmount=" + bidAmount +
                ", timestamp=" + timestamp +
                '}';
    }
}