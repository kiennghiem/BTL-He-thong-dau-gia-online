package com.auction.server.manager;

import com.auction.models.*;
import com.auction.exceptions.InvalidBidException;
import com.auction.exceptions.AuctionNotFoundException;
import com.auction.server.observer.AuctionObserver;
import com.auction.server.service.AuctionService;
import com.auction.models.dto.AppConstants;
import com.auction.models.dto.AuctionUpdateDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Singleton class that manages the LIVE state of all auctions.
 * Acts as the "Real-time Engine" and "Mediator" for notifications.
 */
public class AuctionManager {
    private static AuctionManager instance;
    private final Map<String, Auction> activeAuctions;
    private final Map<String, List<AuctionObserver>> observers;
    private final ScheduledExecutorService scheduler;
    private AuctionService auctionService;

    private AuctionManager() {
        activeAuctions = new ConcurrentHashMap<>();
        observers = new ConcurrentHashMap<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startStatusChecker();
        startExpirationChecker();
    }

    private void startExpirationChecker() {
        // Chạy kiểm tra quá hạn thanh toán mỗi 1 giờ
        scheduler.scheduleAtFixedRate(() -> {
            if (auctionService != null) {
                auctionService.expireUnpaidAuctions();
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction);
    }

    public void removeAuction(String auctionId) {
        activeAuctions.remove(auctionId);
    }

    public Auction getAuction(String auctionId) {
        return activeAuctions.get(auctionId);
    }

    public List<Auction> getAllAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }

    public void subscribe(String auctionId, AuctionObserver observer) {
        observers.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>()).add(observer);
    }

    public void unsubscribe(String auctionId, AuctionObserver observer) {
        List<AuctionObserver> subs = observers.get(auctionId);
        if (subs != null) {
            subs.remove(observer);
        }
    }

    /**
     * Processes a new bid with Granular Locking.
     */
    public void processBid(String auctionId, BidTransaction bid) throws InvalidBidException, AuctionNotFoundException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException("Auction with ID " + auctionId + " not found.");
        }

        synchronized (auction) {
            if (auction.getAuctionStatus() != AuctionStatus.RUNNING) {
                throw new InvalidBidException(AppConstants.ERR_AUCTION_CLOSED);
            }

            // Anti-sniping logic
            long remainingSeconds = Duration.between(LocalDateTime.now(), auction.getEndTime()).getSeconds();
            if (remainingSeconds > 0 && remainingSeconds <= AppConstants.SNIPE_WINDOW_SECONDS) {
                auction.setEndTime(auction.getEndTime().plusSeconds(AppConstants.EXTENSION_TIME_SECONDS));
                System.out.println("[INFO] Anti-sniping: Extended auction " + auctionId);
                
                // Notify about time extension immediately so UI can update timer
                broadcastNotification(new Notification(
                        Notification.Type.TIME_EXTENDED,
                        auctionId,
                        createUpdateDTO(auction)
                ));
            }

            // auction.addBid throws InvalidBidException if bid is too low
            auction.addBid(bid);
            
            System.out.println("[INFO] Bid placed successfully on " + auctionId + ": " + bid.getBidAmount());

            // Notify about new bid
            broadcastNotification(new Notification(
                    Notification.Type.BID_PLACED,
                    auctionId,
                    createUpdateDTO(auction)
            ));
        }
    }

    private void broadcastNotification(Notification notification) {
        List<AuctionObserver> subs = observers.get(notification.getAuctionId());
        if (subs != null) {
            for (AuctionObserver observer : subs) {
                observer.update(notification);
            }
        }
    }

    private void startStatusChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            for (Auction auction : activeAuctions.values()) {
                synchronized (auction) {
                    try {
                        AuctionStatus oldStatus = auction.getAuctionStatus();
                        
                        if (oldStatus == AuctionStatus.OPEN && now.isAfter(auction.getStartTime())) {
                            auction.updateStatus(AuctionStatus.RUNNING);
                            broadcastNotification(new Notification(
                                    Notification.Type.STATUS_CHANGED,
                                    auction.getId(),
                                    createUpdateDTO(auction)
                            ));
                        } 
                        else if (oldStatus == AuctionStatus.RUNNING && now.isAfter(auction.getEndTime())) {
                            auction.updateStatus(AuctionStatus.FINISHED);
                            System.out.println("[INFO] Auction " + auction.getId() + " finished.");
                            
                            // PERSISTENCE
                            if (auctionService != null) {
                                auctionService.finishAuction(auction.getId());
                            }

                            broadcastNotification(new Notification(
                                    Notification.Type.STATUS_CHANGED,
                                    auction.getId(),
                                    createUpdateDTO(auction)
                            ));
                        }
                    } catch (Exception e) {
                        System.err.println("[ERROR] Status checker error: " + e.getMessage());
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Helper to create a standardized DTO for network transmission.
     */
    private AuctionUpdateDTO createUpdateDTO(Auction auction) {
        String bidderName = (auction.getHighestBid() != null) ? auction.getHighestBid().getBidderId() : "None";
        double currentPrice = (auction.getCurrentPrice() != null) ? auction.getCurrentPrice().doubleValue() : 0.0;
        
        return new AuctionUpdateDTO(
                auction.getId(),
                currentPrice,
                bidderName,
                auction.getClosingTimeMillis(),
                auction.getAuctionStatus() != null ? auction.getAuctionStatus().name() : "PENDING"
        );
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}