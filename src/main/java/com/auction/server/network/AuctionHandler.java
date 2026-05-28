package com.auction.server.network;

import com.auction.models.Notification;
import com.auction.models.Seller;
import com.auction.models.Auction;
import com.auction.models.BidTransaction;
import com.auction.server.manager.AuctionManager;
import com.auction.server.observer.AuctionObserver;
import com.auction.server.service.AuctionService;
import com.auction.models.dto.*;

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
        } else if (message instanceof CreateAuctionRequest createReq) {
            handleCreateAuction(createReq);
            return true;
        } else if (message instanceof GetActiveAuctionsRequest getReq) {
            handleGetActiveAuctions(getReq);
            return true;
        } else if (message instanceof GetBidHistoryRequest historyReq) {
            handleGetBidHistory(historyReq);
            return true;
        } else if (message instanceof PaymentRequest payReq) {
            handlePayment(payReq);
            return true;
        } else if (message instanceof DeleteItemRequest deleteReq) {
            handleDeleteAuction(deleteReq);
            return true;
        } else if (message instanceof GetSellerItemsRequest sellerReq) {
            handleGetSellerItems(sellerReq);
            return true;
        }
        return false;
    }

    private void handleBid(BidRequest req) {
        try {
            // Process via Service (Memory + DB)
            auctionService.placeBid(req.getAuctionId(), req.getBidderId(), req.getAmount());
            
            // Automatically subscribe this client to real-time updates for this auction
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
        // Map ItemType from dto to models
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
    }

    private void handleGetActiveAuctions(GetActiveAuctionsRequest req) {
        List<Auction> activeAuctions = auctionService.getAllActiveAuctions();
        sendResponse(activeAuctions);
    }

    private void handleGetBidHistory(GetBidHistoryRequest req) {
        List<BidTransaction> history = auctionService.getBidHistory(req.getAuctionId());
        sendResponse(history);
    }

    private void handlePayment(PaymentRequest req) {
        boolean success = auctionService.processPayment(req.getAuctionId(), req.getBidderId());
        if (success) {
            sendResponse(new GenericResponse(true, "Thanh toán thành công!"));
        } else {
            sendResponse(new GenericResponse(false, "Thanh toán thất bại. Kiểm tra số dư hoặc trạng thái phiên."));
        }
    }

    private void handleDeleteAuction(DeleteItemRequest req) {
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

    private void handleGetSellerItems(GetSellerItemsRequest req) {
        List<Auction> auctions = auctionService.getAuctionsBySeller(req.getSellerId());
        sendResponse(auctions);
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
