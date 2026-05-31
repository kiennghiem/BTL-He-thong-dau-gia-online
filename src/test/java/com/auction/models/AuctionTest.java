package com.auction.models;

import com.auction.exceptions.InvalidStatusException;
import com.auction.server.observer.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    private Auction auction;
    private Seller seller;

    @BeforeEach
    void setUp() {
        seller = new Seller("seller", "pass");
        Item item = new OtherItem("Antique Vase", "Old vase", new BigDecimal("50"), "None", seller);
        auction = new Auction(item, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
    }

    @Test
    void testInitialState() {
        assertEquals(AuctionStatus.OPEN, auction.getStatus());
        assertEquals(new BigDecimal("50"), auction.getCurrentPrice());
    }

    @Test
    void testStatusTransitions() throws Exception {
        // OPEN -> RUNNING
        auction.updateStatus(AuctionStatus.RUNNING);
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());

        // RUNNING -> FINISHED
        auction.updateStatus(AuctionStatus.FINISHED);
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());

        // FINISHED -> PAID
        auction.updateStatus(AuctionStatus.PAID);
        assertEquals(AuctionStatus.PAID, auction.getStatus());
    }

    @Test
    void testInvalidStatusTransition() {
        // Cannot go from OPEN directly to PAID
        assertThrows(InvalidStatusException.class, () -> auction.updateStatus(AuctionStatus.PAID));
    }

    @Test
    void testAddBidUpdatesPrice() throws Exception {
        BidTransaction bid = new BidTransaction("item1", "bidder1", "Bidder", new BigDecimal("100"));
        auction.addBid(bid);
        assertEquals(new BigDecimal("100"), auction.getCurrentPrice());
        assertEquals("bidder1", auction.getHighestBidderId());
    }
}
