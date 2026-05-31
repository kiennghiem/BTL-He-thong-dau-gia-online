package com.auction.server.network;

import com.auction.models.Notification;
import com.auction.models.Seller;
import com.auction.models.Auction;
import com.auction.server.factory.ItemType;
import com.auction.server.manager.AuctionManager;
import com.auction.server.observer.AuctionObserver;
import com.auction.server.observer.AuctionStatus;
import com.auction.server.service.AuctionService;
import com.auction.models.dto.*;

import java.math.BigDecimal;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AuctionHandler manages auction-related communication for a specific client connection.
 * It implements AuctionObserver to push real-time updates directly to the client.
 */
public class AuctionHandler implements AuctionObserver {
    private final ObjectOutputStream out;
    private final AuctionService auctionService;
    private final AuctionManager auctionManager;
    private com.auction.models.User currentUser;

    public AuctionHandler(ObjectOutputStream out) {
        this.out = out;
        this.auctionService = new AuctionService();
        this.auctionManager = AuctionManager.getInstance();
        
        // Register as a Global Observer immediately
        this.auctionManager.addObserver(this);
    }

    public void setCurrentUser(com.auction.models.User user) {
        this.currentUser = user;
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
        if (message instanceof EndAuctionEarlyRequest endEarlyReq) {
            handleEndAuctionEarly(endEarlyReq);
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

    private void handleEndAuctionEarly(EndAuctionEarlyRequest req) {
        try {
            Auction auction = auctionManager.getAuction(req.getAuctionId());
            if (auction == null) {
                sendResponse(new GenericResponse(false, "Không tìm thấy phiên đấu giá!"));
                return;
            }

            // Check if requester is the seller
            if (currentUser == null || !currentUser.getId().equals(auction.getSellerId())) {
                sendResponse(new GenericResponse(false, "Bạn không có quyền kết thúc sớm phiên này!"));
                return;
            }

            auctionService.endAuctionEarly(req.getAuctionId(), req.getSellerId());
            sendResponse(new GenericResponse(true, "Kết thúc sớm phiên đấu giá thành công!"));
            System.out.println("[AuctionHandler] Auction ended early: " + req.getAuctionId() + " by " + req.getSellerId());
        } catch (Exception e) {
            sendResponse(new GenericResponse(false, "Lỗi kết thúc sớm: " + e.getMessage()));
        }
    }

    private void handleBid(BidRequest req) {
        try {
            // Server-side safety: Prevent sellers from bidding on anything (or their own items)
            if (currentUser != null && currentUser.getRole() == com.auction.server.factory.UserRole.SELLER) {
                sendResponse(new GenericResponse(false, "Người bán không được phép đặt giá!"));
                return;
            }

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
                // Fetch updated user to send back balance
                com.auction.models.User updatedUser = com.auction.server.database.dao.DAOFactory.getUserDAO().findById(req.getBidderId());
                this.currentUser = updatedUser;
                sendResponse(new AuthResponse(true, "Thanh toán thành công! Chúc mừng bạn đã sở hữu món đồ.", updatedUser));
            } else {
                sendResponse(new GenericResponse(false, "Thanh toán thất bại."));
            }
        } catch (Exception e) {
            sendResponse(new GenericResponse(false, "Lỗi thanh toán: " + e.getMessage()));
        }
    }

    private void handleSubscribe(SubscribeRequest req) {
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
            Auction auction = auctionManager.getAuction(req.getAuctionId());
            if (auction == null) {
                sendResponse(new GenericResponse(false, "Không tìm thấy phiên đấu giá!"));
                return;
            }

            // Allow if Admin OR if Seller of the auction
            boolean isAdmin = currentUser != null && currentUser.getRole() == com.auction.server.factory.UserRole.ADMIN;
            boolean isOwner = currentUser != null && currentUser.getId().equals(auction.getSellerId());

            if (!isAdmin && !isOwner) {
                sendResponse(new GenericResponse(false, "Bạn không có quyền hủy phiên này!"));
                return;
            }

            auctionService.cancelAuction(req.getAuctionId(), currentUser.getId());
            sendResponse(new GenericResponse(true, "Hủy phiên đấu giá thành công!"));
            System.out.println("[AuctionHandler] Auction canceled: " + req.getAuctionId() + " by " + currentUser.getId());
        } catch (Exception e) {
            sendResponse(new GenericResponse(false, "Lỗi hủy đấu giá: " + e.getMessage()));
        }
    }

    private void handleCreateAuction(CreateAuctionRequest req) {
        try {
            System.out.println("[AuctionHandler] Creating auction for user: " + req.getSeller().getUsername());
            ItemType type = req.getItemType();
            
            auctionService.createAuction(
                type, req.getItemName(), req.getItemDescription(),
                req.getStartingPrice(), req.getSpecificAttribute(),
                req.getSeller().getUsername(),
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
        List<Auction> allAuctions = auctionService.getAllActiveAuctions();
        
        // ADMIN can see everything
        if (currentUser != null && currentUser.getRole() == com.auction.server.factory.UserRole.ADMIN) {
            sendResponse(allAuctions);
            return;
        }

        String currentUserId = (currentUser != null) ? currentUser.getId() : "";
        
        // VISIBILITY RULES:
        // 1. OPEN / RUNNING: Visible to everyone
        // 2. FINISHED / PAID / CANCELED: Only visible to Seller OR any Bidder who participated
        List<Auction> filtered = allAuctions.stream().filter(a -> {
            AuctionStatus status = a.getStatus();
            if (status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING) {
                return true;
            }
            
            // For completed/canceled auctions:
            boolean isSeller = (a.getSeller() != null && currentUserId.equals(a.getSeller().getId()));
            boolean isParticipant = (a.getBidHistory() != null && 
                                    a.getBidHistory().stream().anyMatch(b -> b.getBidderId().equals(currentUserId)));
            
            return isSeller || isParticipant;
        }).collect(Collectors.toList());

        sendResponse(filtered);
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
