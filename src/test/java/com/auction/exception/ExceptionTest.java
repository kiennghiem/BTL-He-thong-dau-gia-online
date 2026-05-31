package com.auction.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testAuctionNotFoundException() {
        AuctionNotFoundException ex = new AuctionNotFoundException("Not found");
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void testInvalidBidException() {
        InvalidBidException ex = new InvalidBidException("Low bid");
        assertEquals("Low bid", ex.getMessage());
    }

    @Test
    void testAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Auth failed");
        assertEquals("Auth failed", ex.getMessage());
    }
}
