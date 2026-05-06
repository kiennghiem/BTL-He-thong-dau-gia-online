package main.java.common.Class;

public interface AuctionObserver {
    void onPriceUpdated(double newPrice, String lastBidder);
    void onStatusChanged(ItemStatus newStatus);
}
