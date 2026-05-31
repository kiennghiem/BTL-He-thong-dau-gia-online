package com.auction.server.observer;

import com.auction.models.Notification;

public interface AuctionObserver {
    void update(Notification notification);
}