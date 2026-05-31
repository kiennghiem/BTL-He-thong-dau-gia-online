package com.auction.server.service;

import com.auction.models.*;
import com.auction.server.database.dao.AuctionDAO;
import com.auction.server.database.dao.BidDAO;
import com.auction.server.database.dao.ItemDAO;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.factory.ItemType;
import com.auction.server.manager.AuctionManager;
import com.auction.server.observer.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock private AuctionManager auctionManager;
    @Mock private ItemDAO itemDAO;
    @Mock private BidDAO bidDAO;
    @Mock private AuctionDAO auctionDAO;
    @Mock private UserDAO userDAO;

    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        auctionService = new AuctionService(auctionManager, itemDAO, bidDAO, auctionDAO, userDAO);
    }

    @Test
    void testCreateAuctionSuccess() throws Exception {
        String username = "sellerUser";
        Seller seller = new Seller(username, "pass");
        
        when(userDAO.findByUsername(username)).thenReturn(seller);
        when(itemDAO.addItem(any(Item.class))).thenReturn(true);
        when(auctionDAO.insert(any(Auction.class))).thenReturn(true);

        boolean result = auctionService.createAuction(
                ItemType.ELECTRONICS, "Laptop", "High end",
                new BigDecimal("1000"), "ASUS", username,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(5)
        );

        assertTrue(result);
        verify(auctionManager, times(1)).addAuction(any(Auction.class));
    }

    @Test
    void testProcessPaymentSuccess() throws Exception {
        String auctionId = "auc-123";
        String bidderId = "bid-456";
        BigDecimal amount = new BigDecimal("500");

        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus(AuctionStatus.FINISHED);
        auction.setEndTime(LocalDateTime.now().minusHours(1));
        
        // Mock highest bidder
        BidTransaction topBid = new BidTransaction(auctionId, bidderId, "Winner", amount);
        auction.setHighestBid(topBid);

        Bidder bidder = new Bidder(bidderId, "user", "pass", new BigDecimal("1000"));

        when(auctionManager.getAuction(auctionId)).thenReturn(auction);
        when(userDAO.findById(bidderId)).thenReturn(bidder);

        boolean result = auctionService.processPayment(auctionId, bidderId, amount);

        assertTrue(result);
        assertEquals(new BigDecimal("500"), bidder.getBalance());
        assertEquals(AuctionStatus.PAID, auction.getStatus());
        verify(userDAO, times(1)).updateUser(bidder);
    }

    @Test
    void testProcessPaymentInsufficientBalanceFails() {
        String auctionId = "auc-123";
        String bidderId = "bid-456";
        BigDecimal amount = new BigDecimal("500");

        Auction auction = new Auction();
        auction.setId(auctionId);
        auction.setStatus(AuctionStatus.FINISHED);
        auction.setHighestBid(new BidTransaction(auctionId, bidderId, "Winner", amount));

        Bidder bidder = new Bidder(bidderId, "user", "pass", new BigDecimal("100")); // Low balance

        when(auctionManager.getAuction(auctionId)).thenReturn(auction);
        when(userDAO.findById(bidderId)).thenReturn(bidder);

        assertThrows(RuntimeException.class, () -> 
            auctionService.processPayment(auctionId, bidderId, amount)
        );
    }
}
