package com.auction.models;
import com.auction.models.dto.NetworkMessage;

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
    private final double bidAmount;
    private final LocalDateTime timestamp;

    /**
     * Constructor used by the Server after a BidRequest is validated.
     */
    public BidTransaction(String itemId, String bidderId, double bidAmount) {
        this.id = UUID.randomUUID().toString();
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Overloaded constructor for the DAO when loading from the Database.
     */
    public BidTransaction(String id, String itemId, String bidderId, double bidAmount, LocalDateTime timestamp) {
        this.id = id;
        this.itemId = itemId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

    //Getters
    public String getId() {return id;}
    public String getItemId() {return itemId;}
    public String getBidderId() {return bidderId;}
    public double getBidAmount() {return bidAmount;}
    public LocalDateTime getTimestamp() {return timestamp;}

    @Override
    public String toString() {
        return String.format("[%s] Bidder %s bid $%.2f on Item %s",
                timestamp, bidderId, bidAmount, itemId);
    }
}