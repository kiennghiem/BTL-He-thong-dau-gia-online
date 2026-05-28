package com.auction.server.network;

import com.auction.models.Notification;
import com.auction.models.Seller;
import com.auction.models.Auction;
import com.auction.models.BidTransaction;
import com.auction.server.manager.AuctionManager;
import com.auction.server.observer.AuctionObserver;
import com.auction.server.service.AuctionService;
import com.auction.models.dto.*;
import com.auction.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AuctionHandler manages auction-related communication for a specific client connection.
 * It implements AuctionObserver to push real-time updates directly to the client.
 */
public class AuctionHandler implements AuctionObserver {
    private final PrintWriter out;
    private final AuctionService auctionService;
    private final AuctionManager auctionManager;
    private final Gson gson;
    private final java.util.Set<String> subscribedAuctions;

    public AuctionHandler(PrintWriter out) {
        this.out = out;
        this.auctionService = new AuctionService();
        this.auctionManager = AuctionManager.getInstance();
        this.subscribedAuctions = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public void handleBid(NetworkMessage message) {
        if (message instanceof BidRequest req) {
            try {
                auctionService.placeBid(req.getAuctionId(), req.getBidderId(), req.getAmount());
                if (subscribedAuctions.add(req.getAuctionId())) {
                    auctionManager.subscribe(req.getAuctionId(), this);
                }
                sendResponse(new GenericResponse(true, "Đặt giá thành công!"));
                System.out.println("[AuctionHandler] Bid successful: " + req.getBidderId() + " on " + req.getAuctionId());
            } catch (Exception e) {
                sendResponse(new GenericResponse(false, "Lỗi đặt giá: " + e.getMessage()));
            }
        }
    }

    public void handleCreateAuction(NetworkMessage message) {
        if (message instanceof CreateAuctionRequest req) {
            try {
                com.auction.models.ItemType modelType = com.auction.models.ItemType.valueOf(req.getItemType().name());
                boolean success = auctionService.createAuction(
                        modelType, req.getItemName(), req.getItemDescription(),
                        req.getStartingPrice(), req.getSpecificAttribute(),
                        new Seller(req.getSellerUsername(), ""), 
                        req.getStartTime(), req.getEndTime(), req.getMinIncrement()
                );
                if (success) {
                    sendResponse(new GenericResponse(true, "Tạo phiên đấu giá thành công!"));
                } else {
                    sendResponse(new GenericResponse(false, "Lỗi khi tạo phiên đấu giá."));
                }
            } catch (Exception e) {
                sendResponse(new GenericResponse(false, "Lỗi tạo đấu giá: " + e.getMessage()));
            }
        }
    }

    public void handleGetActiveAuctions(NetworkMessage message) {
        if (message instanceof GetActiveAuctionsRequest) {
            List<Auction> activeAuctions = auctionService.getAllActiveAuctions();
            sendResponse(activeAuctions);
        }
    }

    public void handleGetBidHistory(NetworkMessage message) {
        if (message instanceof GetBidHistoryRequest req) {
            List<BidTransaction> history = auctionService.getBidHistory(req.getAuctionId());
            sendResponse(history);
        }
    }

    public void handlePayment(NetworkMessage message) {
        if (message instanceof PaymentRequest req) {
            boolean success = auctionService.processPayment(req.getAuctionId(), req.getBidderId());
            if (success) {
                sendResponse(new GenericResponse(true, "Thanh toán thành công!"));
            } else {
                sendResponse(new GenericResponse(false, "Thanh toán thất bại. Kiểm tra số dư hoặc trạng thái phiên."));
            }
        }
    }

    public void handleDeleteAuction(NetworkMessage message) {
        if (message instanceof DeleteItemRequest req) {
            Auction auction = auctionManager.getAuction(req.getItemId());
            if (auction != null) {
                boolean success = auctionService.deleteAuction(req.getItemId(), auction.getSeller().getUsername());
                if (success) {
                    sendResponse(new GenericResponse(true, "Xóa thành công!"));
                    return;
                }
            }
            sendResponse(new GenericResponse(false, "Xóa thất bại. Phiên đã có người đặt giá hoặc không có quyền."));
        }
    }

    public void handleGetSellerItems(NetworkMessage message) {
        if (message instanceof GetSellerItemsRequest req) {
            List<Auction> auctions = auctionService.getAuctionsBySeller(req.getSellerId());
            sendResponse(auctions);
        }
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
            String json = gson.toJson(response);
            synchronized (out) {
                out.println(json); // Sử dụng println để kết thúc gói tin JSON
            }
        } catch (Exception e) {
            System.err.println("[AuctionHandler] Error sending JSON: " + e.getMessage());
        }
    }
}
