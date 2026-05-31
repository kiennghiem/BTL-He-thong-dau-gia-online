package com.auction.models.dto;

import com.auction.server.factory.ItemType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testLoginRequest() {
        LoginRequest req = new LoginRequest("user", "pass");
        assertEquals("user", req.getUsername());
        assertEquals("pass", req.getPassword());
    }

    @Test
    void testBidRequest() {
        BidRequest req = new BidRequest("auction1", "bidder1", new BigDecimal("150"));
        assertEquals("bidder1", req.getBidderId());
        assertEquals("auction1", req.getAuctionId());
        assertEquals(new BigDecimal("150"), req.getAmount());
    }

    @Test
    void testCreateAuctionRequest() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        CreateAuctionRequest req = new CreateAuctionRequest(null, ItemType.ART, "Painting", "Desc", new BigDecimal("100"), "Picasso", start, end);
        
        assertEquals(ItemType.ART, req.getItemType());
        assertEquals("Painting", req.getItemName());
        assertEquals(new BigDecimal("100"), req.getStartingPrice());
    }
}
