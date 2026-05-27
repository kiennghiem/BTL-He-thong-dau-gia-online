package com.auction.server.observer;

import com.auction.models.Notification;

/**
 * Interface for the Observer pattern.
 * Receives standardized Notification objects for real-time updates.
 */
public interface AuctionObserver {
    void update(Notification notification);
}
