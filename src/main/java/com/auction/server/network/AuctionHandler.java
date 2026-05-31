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

import java.math.BigDecimal;
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

    public AuctionHandler(ObjectOutputStream out) {
        this.out = out;
        this.auctionService = new AuctionService();
        this.auctionManager = AuctionManager.getInstance();
        
        // Register as a Global Observer immediately
        this.auctionManager.addObserver(this);
    }

    /**
     * Entry point for messages identified as auction-related.
     */
    public boolean handleMessage(NetworkMessage message) {
        if (message instanceof BidRequest bidReq) {
            handleBid(bidReq);
            return true;
        }
        if (message instanceof PayRequest payReq) {
            handlePay(payReq);
            return true;
        }
        if (message instanceof SubscribeRequest subReq) {
            handleSubscribe(subReq);
            return true;
        }
        if (message instanceof UnsubscribeRequest unsubReq) {
            // No-op in global mode, but kept for protocol compatibility
            return true;
        }
        if (message instanceof CancelAuctionRequest cancelReq) {
            handleCancelAuction(cancelReq);
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
        if (message instanceof CancelAuctionRequest req) {
            handleCancelAuction(req);
            return true;
        }

        return false;
    }

    private void handleCancelAuction(CancelAuctionRequest req) {
        try {
            auctionService.cancelAuction(req.getAuctionId(), req.getAdminId());
            sendResponse(new GenericResponse(true, "Hủy phiên đấu giá thành công!"));
            System.out.println("[AuctionHandler] Auction canceled: " + req.getAuctionId() + " by " + req.getAdminId());
        } catch (Exception e) {
            sendResponse(new GenericResponse(false, "Lỗi hủy đấu giá: " + e.getMessage()));
        }
    }

    private void handleBid(BidRequest req) {
        try {
            auctionService.placeBid(req.getAuctionId(), req.getBidderId(), req.getAmount());
            sendResponse(new GenericResponse(true, "Đặt giá thành công!"));
            System.out.println("[AuctionHandler] Bid successful: " + req.getBidderId() + " on " + req.getAuctionId());
        } catch (Exception e) {
            sendResponse(new GenericResponse(false, "Lỗi đặt giá: " + e.getMessage()));
        }
    }

    private void handlePay(PayRequest req) {
        try {
            boolean success = auctionService.processPayment(req.getAuctionId(), req.getBidderId(), req.getAmount());
            if (success) {
                sendResponse(new GenericResponse(true, "Thanh toán thành công! Chúc mừng bạn đã sở hữu món đồ."));
            } else {
                sendResponse(new GenericResponse(false, "Thanh toán thất bại."));
            }
        } catch (Exception e) {
            sendResponse(new GenericResponse(false, "Lỗi thanh toán: " + e.getMessage()));
        }
    }

    private void handleSubscribe(SubscribeRequest req) {
        // In Global Mode, we already receive all updates. 
        // We just send back the CURRENT state of THIS auction for initial sync.
        System.out.println("[AuctionHandler] Sync request for auction: " + req.getAuctionId());
        Auction auction = auctionManager.getAuction(req.getAuctionId());
        if (auction != null) {
            sendResponse(new Notification(
                Notification.Type.STATUS_CHANGED, 
                auction.getId(), 
                createUpdateDTO(auction)
            ));
        }
    }

    private void handleCancelAuction(CancelAuctionRequest req) {
        try {
            auctionService.cancelAuction(req.getAuctionId(), req.getAdminId());
            sendResponse(new GenericResponse(true, "Phiên đấu giá đã được hủy."));
        } catch (Exception e) {
            sendResponse(new GenericResponse(false, "Lỗi hủy đấu giá: " + e.getMessage()));
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
            e.printStackTrace();
            sendResponse(new GenericResponse(false, "Lỗi tạo đấu giá: " + e.getMessage()));
        }
    }

    private AuctionUpdateDTO createUpdateDTO(Auction auction) {
        String bidderId = "None";
        String bidderName = "None";
        if (auction.getHighestBid() != null) {
            bidderId = auction.getHighestBid().getBidderId();
            bidderName = auction.getHighestBid().getBidderName();
        }
        BigDecimal currentPrice = (auction.getCurrentPrice() != null) ? auction.getCurrentPrice() : BigDecimal.ZERO;
        return new AuctionUpdateDTO(
                auction.getId(), currentPrice, bidderId, bidderName,
                auction.getClosingTimeMillis(),
                auction.getStatus() != null ? auction.getStatusAsString() : "OPEN"
        );
    }

    public void handleGetActiveAuctions(GetActiveAuctionsRequest req) {
        List<Auction> activeAuctions = auctionService.getAllActiveAuctions();
        sendResponse(activeAuctions);
    }

    public void cleanUp() {
        auctionManager.removeObserver(this);
        System.out.println("[AuctionHandler] Unregistered global observer.");
    }

    @Override
    public void update(Notification notification) {
        sendResponse(notification);
    }

    private void sendResponse(Object response) {
        try {
            synchronized (out) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("[AuctionHandler] Connection error: " + e.getMessage());
        }
    }
}
