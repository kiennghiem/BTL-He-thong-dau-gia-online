package main.java.com.auction.server.observer;

import main.java.com.auction.models.BidTransaction;

public interface AuctionObserver {
    void update(BidTransaction highestBid);

}