package com.auction.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BidTransactionTest {

    @Test
    void testBidTransactionCreation() {
        String itemId = "item-123";
        String bidderId = "bidder-456";
        String bidderName = "John Doe";
        BigDecimal amount = new BigDecimal("250.00");

        BidTransaction bid = new BidTransaction(itemId, bidderId, bidderName, amount);

        assertNotNull(bid.getId());
        assertEquals(itemId, bid.getItemId());
        assertEquals(bidderId, bid.getBidderId());
        assertEquals(bidderName, bid.getBidderName());
        assertEquals(amount, bid.getBidAmount());
        assertNotNull(bid.getTimestamp());
        assertTrue(bid.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testOverloadedConstructor() {
        String id = "fixed-id";
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        BigDecimal amount = new BigDecimal("500");
        
        BidTransaction bid = new BidTransaction(id, "item", "bidder", "Name", amount, time);
        
        assertEquals(id, bid.getId());
        assertEquals(time, bid.getTimestamp());
        assertEquals(amount, bid.getBidAmount());
    }
}
