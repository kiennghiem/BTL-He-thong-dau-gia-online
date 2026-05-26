package com.auction.server.network;

import com.auction.models.Notification;
import com.auction.server.manager.AuctionManager;
import com.auction.server.observer.AuctionObserver;
import com.auction.server.service.AuctionService;
import main.java.common.BidRequest;
import common.NetworkMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * AuctionHandler manages auction-related communication for a specific client connection.
 * It implements AuctionObserver to push real-time updates directly to the client.
 */
public class AuctionHandler implements AuctionObserver {
    private final ObjectOutputStream out;
    private final AuctionService auctionService;
    private final AuctionManager auctionManager;

    public AuctionHandler(ObjectOutputStream out) {
        this.out = out;
        this.auctionService = new AuctionService();
        this.auctionManager = AuctionManager.getInstance();
    }

    /**
     * Entry point for messages identified as auction-related.
     */
    public void handleMessage(NetworkMessage message) {
        if (message instanceof BidRequest bidReq) {
            handleBid(bidReq);
        }
        // Future: Handle ViewAuctionRequest, CancelAuctionRequest, etc.
    }

    private void handleBid(BidRequest req) {
        try {
            // 1. Process via Service (Memory + DB)
            auctionService.placeBid(req.getAuctionId(), req.getBidderId(), req.getAmount());
            
            // 2. Automatically subscribe this client to real-time updates for this auction
            auctionManager.subscribe(req.getAuctionId(), this);
            
            System.out.println("[AuctionHandler] Bid successful: " + req.getBidderId() + " on " + req.getAuctionId());
        } catch (Exception e) {
            sendResponse("ERROR: " + e.getMessage());
        }
    }

    /**
     * Pushes notifications from AuctionManager to the Client.
     * Part of the Observer pattern implementation.
     */
    @Override
    public void update(Notification notification) {
        sendResponse(notification);
    }

    private void sendResponse(Object response) {
        try {
            synchronized (out) {
                out.writeObject(response);
                out.flush();
                out.reset(); // Clear object cache to ensure fresh state is sent
            }
        } catch (IOException e) {
            System.err.println("[AuctionHandler] Connection error while sending update: " + e.getMessage());
        }
    }
}
