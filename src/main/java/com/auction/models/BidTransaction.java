package com.auction.models;

import java.math.BigDecimal;

public class BidTransaction
{
    private static final long serialVersionUID = 1L;
    private Bidder bidder;
    private BigDecimal bidPrice;

    // Constructors
    public BidTransaction(Bidder bidder, BigDecimal bidPrice) {
        this.bidder = bidder;
        this.bidPrice = bidPrice;
    }

    // Getters
    public Bidder getBidder() {
        return this.bidder;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }
}
