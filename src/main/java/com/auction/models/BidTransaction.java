package main.java.com.auction.models;

public class BidTransaction extends Entity {

    private static final long serialVersionUID = 1L;
    private Bidder bidder;
    private int bidPrice;

    // Constructors
    public BidTransaction(Bidder bidder, int bidPrice) {
        this.bidder = bidder;
        this.bidPrice = bidPrice;
    }

    // Getters
    public Bidder getBidder() {
        return this.bidder;
    }

    public int getBidPrice() {
        return bidPrice;
    }
}

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    @Override
    public String toString() {
        return String.format("[%s] Bidder %s bid $%.2f on Item %s",
                timestamp, bidderId, bidAmount, itemId);
    }
}