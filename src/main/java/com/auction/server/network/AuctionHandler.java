package com.auction.server.network;

import com.auction.models.Notification;
import com.auction.models.Seller;
import com.auction.models.Auction;
import com.auction.server.factory.ItemType;
import com.auction.server.manager.AuctionManager;
import com.auction.server.observer.AuctionObserver;
import com.auction.server.service.AuctionService;
import com.auction.models.dto.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * AuctionHandler manages auction-related communication for a specific client connection.
 * It implements AuctionObserver to push real-time updates directly to the client.
 */
public class AuctionHandler implements AuctionObserver {
    private final ObjectOutputStream out;
    private final AuctionService auctionService;
    private final AuctionManager auctionManager;
    private final java.util.Set<String> subscribedAuctions;

    public AuctionHandler(ObjectOutputStream out) {
        this.out = out;
        this.auctionService = new AuctionService();
        this.auctionManager = AuctionManager.getInstance();
        this.subscribedAuctions = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    }

    /**
     * Entry point for messages identified as auction-related.
     */
    public boolean handleMessage(NetworkMessage message) {
        if (message instanceof BidRequest bidReq) {
            handleBid(bidReq);
            return true;
        }
        if (message instanceof CreateAuctionRequest req) {
            handleCreateAuction(req);
            return true;
        }
        if (message instanceof GetActiveAuctionsRequest req) {
            handleGetActiveAuctions(req);
            return true;
        }

        return false;
    }

    private void handleBid(BidRequest req) {
        try {
            // 1. Process via Service (Memory + DB)
            auctionService.placeBid(req.getAuctionId(), req.getBidderId(), req.getAmount());
            
            // 2. Automatically subscribe this client to real-time updates for this auction
            if (subscribedAuctions.add(req.getAuctionId())) {
                auctionManager.subscribe(req.getAuctionId(), this);
            }
            sendResponse(new GenericResponse(true, "Đặt giá thành công!"));
            System.out.println("[AuctionHandler] Bid successful: " + req.getBidderId() + " on " + req.getAuctionId());
        } catch (Exception e) {
            sendResponse(new GenericResponse(false, "Lỗi đặt giá: " + e.getMessage()));
        }
    }

    private void handleCreateAuction(CreateAuctionRequest req) {
        try {
            System.out.println("[AuctionHandler] Creating auction for user: " + req.getSellerUsername());
            ItemType type = ItemType.valueOf(req.getItemType().toString());
            
            auctionService.createAuction(
                type, req.getItemName(), req.getItemDescription(),
                req.getStartingPrice(), req.getSpecificAttribute(),
                req.getSellerUsername(),
                req.getStartTime(), req.getEndTime());
            
            sendResponse(new GenericResponse(true, "Tạo phiên đấu giá thành công!"));
            System.out.println("[AuctionHandler] Auction created successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace on server
            sendResponse(new GenericResponse(false, "Lỗi tạo đấu giá: " + e.getMessage()));
        }
    }

    public void handleGetActiveAuctions(GetActiveAuctionsRequest req) {
        List<Auction> activeAuctions = auctionService.getAllActiveAuctions();
        sendResponse(activeAuctions);
    }

    /**
     * Unsubscribes from all auctions when the connection is closed.
     */
    public void cleanUp() {
        synchronized (subscribedAuctions) {
            for (String auctionId : subscribedAuctions) {
                auctionManager.unsubscribe(auctionId, this);
            }
            subscribedAuctions.clear();
        }
        System.out.println("[AuctionHandler] Cleaned up subscriptions.");
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
