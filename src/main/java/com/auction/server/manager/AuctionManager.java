package com.auction.server.manager;

import com.auction.models.*;
import com.auction.exceptions.InvalidBidException;
import com.auction.exceptions.AuctionNotFoundException;
import com.auction.server.observer.AuctionObserver;
import com.auction.server.observer.AuctionStatus;
import com.auction.server.service.AuctionService;
import com.auction.models.dto.AppConstants;
import com.auction.models.dto.AuctionUpdateDTO;
import com.auction.models.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
    private static final Logger logger = LoggerFactory.getLogger(AuctionManager.class);
    private static AuctionManager instance;
    private final Map<String, Auction> activeAuctions;
    private final List<AuctionObserver> observers; // Consolidated list (Global Observer)
    private final ScheduledExecutorService scheduler;
    private AuctionService auctionService;

    private AuctionManager() {
        activeAuctions = new ConcurrentHashMap<>();
        observers = new CopyOnWriteArrayList<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startStatusChecker();
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
        // Notify observers about NEW auction
        broadcastNotification(new Notification(
                Notification.Type.STATUS_CHANGED,
                auction.getId(),
                createUpdateDTO(auction)
        ));
        logger.info("[AuctionManager] New auction added to memory: {}", auction.getId());
    }

    public Auction getAuction(String auctionId) {
        return activeAuctions.get(auctionId);
    }

    public List<Auction> getAllAuctions() {
        return new ArrayList<>(activeAuctions.values());
    }

    public void addObserver(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
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
            LocalDateTime now = LocalDateTime.now();
            
            // Critical Check: Status must be RUNNING AND time must not be up
            if (auction.getStatus() != AuctionStatus.RUNNING || now.isAfter(auction.getEndTime())) {
                throw new InvalidBidException(AppConstants.ERR_AUCTION_CLOSED);
            }

            // Anti-sniping logic
            long remainingSeconds = Duration.between(now, auction.getEndTime()).getSeconds();
            if (remainingSeconds >= 0 && remainingSeconds <= AppConstants.SNIPE_WINDOW_SECONDS) {
                auction.setEndTime(auction.getEndTime().plusSeconds(AppConstants.EXTENSION_TIME_SECONDS));
                logger.info("[INFO] Anti-sniping: Extended auction {}", auctionId);
                
                // Notify about time extension immediately so UI can update timer
                broadcastNotification(new Notification(
                        Notification.Type.TIME_EXTENDED,
                        auctionId,
                        createUpdateDTO(auction)
                ));
            }

            // auction.addBid throws InvalidBidException if bid is too low
            auction.addBid(bid);
            
            logger.info("[INFO] Bid placed successfully on {}: {}", auctionId, bid.getBidAmount());

            // Notify about new bid
            broadcastNotification(new Notification(
                    Notification.Type.BID_PLACED,
                    auctionId,
                    createUpdateDTO(auction)
            ));
        }
    }

    /**
     * Approves a pending auction (Admin functionality).
     */
    public void approveAuction(String auctionId) throws AuctionNotFoundException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException("Auction with ID " + auctionId + " not found.");
        }

        synchronized (auction) {
            try {
                auction.updateStatus(AuctionStatus.OPEN);
                logger.info("[INFO] Auction {} approved by Admin.", auctionId);

                // Notify about status change
                broadcastNotification(new Notification(
                        Notification.Type.STATUS_CHANGED,
                        auctionId,
                        createUpdateDTO(auction)
                ));
            } catch (Exception e) {
                logger.error("[ERROR] Failed to approve auction {}: {}", auctionId, e.getMessage(), e);
            }
        }
    }

    /**
     * Ends an auction early (Seller functionality).
     */
    public void endAuctionEarly(String auctionId) throws AuctionNotFoundException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException("Auction with ID " + auctionId + " not found.");
        }

        synchronized (auction) {
            try {
                auction.updateStatus(AuctionStatus.FINISHED);
                auction.setEndTime(LocalDateTime.now());
                logger.info("[INFO] Auction {} ended early by Seller.", auctionId);

                // Notify about status and time change
                broadcastNotification(new Notification(
                        Notification.Type.STATUS_CHANGED,
                        auctionId,
                        createUpdateDTO(auction)
                ));
            } catch (Exception e) {
                logger.error("[ERROR] Failed to end auction early {}: {}", auctionId, e.getMessage(), e);
            }
        }
    }

    /**
     * Cancels an auction (Admin functionality).
     */
    public void cancelAuction(String auctionId) throws AuctionNotFoundException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException("Auction with ID " + auctionId + " not found.");
        }

        synchronized (auction) {
            try {
                auction.setStatus(AuctionStatus.CANCELED);
                logger.info("[INFO] Auction {} canceled by Admin.", auctionId);

                // Notify about status change
                broadcastNotification(new Notification(
                        Notification.Type.STATUS_CHANGED,
                        auctionId,
                        createUpdateDTO(auction)
                ));
            } catch (Exception e) {
                logger.error("[ERROR] Failed to cancel auction {}: {}", auctionId, e.getMessage(), e);
            }
        }
    }

    private void broadcastNotification(Notification notification) {
        for (AuctionObserver observer : observers) {
            observer.update(notification);
        }
    }

    private void startStatusChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            for (Auction auction : activeAuctions.values()) {
                Notification notification = null;
                synchronized (auction) {
                    try {
                        AuctionStatus oldStatus = auction.getStatus();
                        if (oldStatus == AuctionStatus.PENDING) {
                            continue; // Skip pending auctions until admin approval
                        }
                        if (oldStatus == AuctionStatus.OPEN && now.isAfter(auction.getStartTime())) {
                            auction.updateStatus(AuctionStatus.RUNNING);
                            broadcastNotification(new Notification(
                                    Notification.Type.STATUS_CHANGED,
                                    auction.getId(),
                                    createUpdateDTO(auction)
                            ));
                            logger.info("[AuctionManager] Auction {} started (RUNNING).", auction.getId());
                        } 
                        else if (oldStatus == AuctionStatus.RUNNING && now.isAfter(auction.getEndTime())) {
                            auction.updateStatus(AuctionStatus.FINISHED);
                            logger.info("[INFO] Auction {} finished.", auction.getId());
                            
                            // PERSISTENCE: Ensure DB reflects finished state
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
                        logger.error("[ERROR] Status checker error for auction {}: {}", auction.getId(), e.getMessage(), e);
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Helper to create a standardized DTO for network transmission.
     */
    private AuctionUpdateDTO createUpdateDTO(Auction auction) {
        String bidderId = "None";
        String bidderName = "None";
        
        if (auction.getHighestBid() != null) {
            bidderId = auction.getHighestBid().getBidderId();
            bidderName = auction.getHighestBid().getBidderName();
        }
        
        BigDecimal currentPrice = (auction.getCurrentPrice() != null) ? auction.getCurrentPrice() : BigDecimal.ZERO;
        
        return new AuctionUpdateDTO(
                auction.getId(),
                auction.getTitle(),
                currentPrice,
                bidderId,
                bidderName,
                auction.getClosingTimeMillis(),
                auction.getStatus() != null ? auction.getStatusAsString() : AuctionStatus.OPEN.toString()
        );
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
