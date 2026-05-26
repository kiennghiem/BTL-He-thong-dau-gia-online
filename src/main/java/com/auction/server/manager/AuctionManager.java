package com.auction.server.manager;

import com.auction.models.Auction;
import com.auction.models.BidTransaction;
import com.auction.exceptions.InvalidBidException;
import com.auction.exceptions.AuctionNotFoundException;
import com.auction.models.AuctionStatus;
import com.auction.server.observer.AuctionObserver;
import main.java.common.AppConstants;

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

    private AuctionManager() {
        activeAuctions = new ConcurrentHashMap<>();
        observers = new ConcurrentHashMap<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startStatusChecker();
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
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new InvalidBidException(AppConstants.ERR_AUCTION_CLOSED);
            }

            // Anti-sniping logic
            long remainingSeconds = Duration.between(LocalDateTime.now(), auction.getClosingTime()).getSeconds();
            if (remainingSeconds > 0 && remainingSeconds <= AppConstants.SNIPE_WINDOW_SECONDS) {
                auction.setClosingTime(auction.getClosingTime().plusSeconds(AppConstants.EXTENSION_TIME_SECONDS));
                System.out.println("[INFO] Anti-sniping triggered: Extended auction " + auctionId + " by " + AppConstants.EXTENSION_TIME_SECONDS + " seconds.");
            }

            // auction.AddBid throws InvalidBidException if bid is too low
            auction.AddBid(bid);
            
            // Note: DB persistence of the new bid would typically happen here via a Service or DAO call
            // e.g., DAOFactory.getBidDAO().placeBid(bid);
            System.out.println("[INFO] Bid placed successfully on auction " + auctionId + ": " + bid.getBidAmount());

            notifyObservers(auctionId, auction);
        }
    }

    public void notifyObservers(String auctionId, Auction auction) {
        List<AuctionObserver> subs = observers.get(auctionId);
        if (subs != null) {
            for (AuctionObserver observer : subs) {
                observer.update(auction);
            }
        }
    }

    private void startStatusChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            for (Auction auction : activeAuctions.values()) {
                synchronized (auction) {
                    try {
                        AuctionStatus oldStatus = auction.getStatus();
                        
                        if (oldStatus == AuctionStatus.OPEN && now.isAfter(auction.getStartingTime())) {
                            auction.UpdateStatus(AuctionStatus.RUNNING);
                            System.out.println("[INFO] Auction " + auction.getId() + " is now RUNNING.");
                            notifyObservers(auction.getId(), auction);
                        } 
                        else if (oldStatus == AuctionStatus.RUNNING && now.isAfter(auction.getClosingTime())) {
                            auction.UpdateStatus(AuctionStatus.FINISHED);
                            
                            // Determine Winner
                            if (auction.getHighestBid() != null) {
                                // Real-world logic: fetch Bidder object from UserDAO using bidderId
                                String winnerId = auction.getHighestBid().getBidderId();
                                // auction.setWinner(winner); 
                                System.out.println("[INFO] Auction " + auction.getId() + " finished. Winner: " + winnerId);
                                
                                // TODO: Call ItemDAO or AuctionDAO to update the DB status to FINISHED and save winner
                                // e.g., DAOFactory.getItemDAO().updateItemStatus(auction.getItem().getId(), AppConstants.STATUS_FINISHED);
                            } else {
                                System.out.println("[INFO] Auction " + auction.getId() + " finished with no bids.");
                            }
                            
                            notifyObservers(auction.getId(), auction);
                        }
                    } catch (Exception e) {
                        System.err.println("[ERROR] Error updating status for auction " + auction.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
