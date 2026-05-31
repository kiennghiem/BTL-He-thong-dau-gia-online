package com.auction.server.manager;

import com.auction.models.*;
import com.auction.server.observer.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuctionManagerTest {

    private AuctionManager auctionManager;
    private Auction auction;
    private Seller seller;

    @BeforeEach
    void setUp() {
        auctionManager = AuctionManager.getInstance();
        
        // Use correct Seller constructor
        seller = new Seller("testSeller", "password");

        Item item = new Electronics("Laptop", "Gaming laptop", new BigDecimal("1000"), "ASUS", seller);
        
        auction = new Auction();
        auction.setId(UUID.randomUUID().toString());
        auction.setItem(item);
        auction.setSeller(seller);
        auction.setTitle("Gaming Laptop");
        auction.setStartingPrice(new BigDecimal("1000"));
        auction.setCurrentPrice(new BigDecimal("1000"));
        // Set times to ensure it's in RUNNING state
        auction.setStartTime(LocalDateTime.now().minusHours(1));
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        auction.setStatus(AuctionStatus.RUNNING);

        auctionManager.addAuction(auction);
    }

    @Test
    void testProcessValidBid() throws Exception {
        BidTransaction bid = new BidTransaction(auction.getId(), "bidder1", "Bidder One", new BigDecimal("1100"));
        auctionManager.processBid(auction.getId(), bid);

        assertEquals(new BigDecimal("1100"), auction.getCurrentPrice());
        assertEquals("bidder1", auction.getHighestBidderId());
    }

    @Test
    void testAntiSnipingExtension() throws Exception {
        // Set end time to 1 minute from now (within the 5-minute snipe window)
        LocalDateTime nearEnd = LocalDateTime.now().plusMinutes(1);
        auction.setEndTime(nearEnd);

        BidTransaction bid = new BidTransaction(auction.getId(), "bidder1", "Bidder One", new BigDecimal("1200"));
        auctionManager.processBid(auction.getId(), bid);

        // End time should have been extended by 5 minutes (300 seconds)
        // Check if it's at least nearEnd + 5 minutes
        assertTrue(auction.getEndTime().isAfter(nearEnd.plusMinutes(4)));
    }
}
