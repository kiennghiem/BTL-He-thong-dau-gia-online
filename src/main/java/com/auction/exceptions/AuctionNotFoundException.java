package com.auction.exceptions;

public class AuctionNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public AuctionNotFoundException(String message) {
        super(message);
    }
}
