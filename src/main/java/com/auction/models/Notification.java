package com.auction.models;

import com.auction.models.dto.NetworkMessage;

import java.io.Serializable;

/**
 * Carrier object for real-time updates sent to observers.
 * Standardizes communication between Server logic and Network handlers.
 */
public class Notification implements NetworkMessage {
    private static final long serialVersionUID = 1L;

    public enum Type {
        BID_PLACED,
        TIME_EXTENDED,
        STATUS_CHANGED
    }

    private final Type type;
    private final String auctionId;
    private final Object data;

    public Notification(Type type, String auctionId, Object data) {
        this.type = type;
        this.auctionId = auctionId;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "type=" + type +
                ", auctionId='" + auctionId + '\'' +
                ", data=" + data +
                '}';
    }
}
