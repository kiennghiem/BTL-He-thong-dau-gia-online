package main.java.server.observer;

import main.java.models.BidTransaction;

public interface AuctionObserver {
    void update(BidTransaction highestBid);

}
