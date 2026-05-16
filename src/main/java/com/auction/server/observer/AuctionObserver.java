package com.auction.server.observer;

import com.auction.models.BidTransaction;

public interface AuctionObserver {
    void update(BidTransaction highestBid);

}