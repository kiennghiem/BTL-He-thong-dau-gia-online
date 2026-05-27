package com.auction.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

import com.auction.exceptions.*;
import com.auction.server.observer.*;
import java.math.BigDecimal;

public class Auction extends Entity {

    private static final long serialVersionUID = 1L;
    private Item item;
    private Seller seller;
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime closeTime;
    private BidTransaction highestBid;
    private BigDecimal currentPrice;
    // History of all proper bids
    private List<BidTransaction> bidHistory;
    // List of observers
    private List<AuctionObserver> observers;
    private ScheduledExecutorService scheduler;

    // Constructors
    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime closingTime) {
        this.item = item;
        this.seller = seller;
        this.status = AuctionStatus.PENDING;
        this.startTime = startTime;
        this.closeTime = closingTime;
        // At first, the current price is the item's starting price
        this.currentPrice = item.getStartingPrice();
        this.bidHistory = new ArrayList<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    // Method to check if Auction is running (WILL CHANGE cuz will make a seperate timer class)
    public boolean IsAuctioning(){
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return false;
        }
        else if (closeTime == null || now.isAfter(closeTime)) {
            return false;
        }
        else return AuctionStatus.RUNNING.equals(status);
    }

    // Method to add a bid ONLY if there is no bid, or the bid is higher than highestBid
    public synchronized void AddBid(BidTransaction bid) throws InvalidBidException
    {
        if (!this.IsAuctioning()) {
            throw new InvalidBidException("Auction is currently not active.");
        }
        if (bid.getBidPrice() == null || bid.getBidPrice().compareTo(currentPrice) <= 0) {
            throw new InvalidBidException("Price must be higher than current price.");
        }
        bidHistory.add(bid);
        highestBid = bid;
        currentPrice = highestBid.getBidPrice();

        notifyObservers();
    }

    // Control observers (every class implementing AuctionObserver can see the latest highestBid)
    public void addObserver(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (AuctionObserver observer : observers) {
            observer.update(highestBid);
        }
    }

    // MIGHT change this method
    public void UpdateStatus(AuctionStatus newStatus) throws InvalidStatusException
    {
        if (this.status == newStatus) {
            return;
        }
        switch(newStatus){
            case OPEN -> {
                if(this.status != AuctionStatus.PENDING){throw new InvalidStatusException("Cannot set this status to OPEN");}
                this.status = newStatus;
            }
            case RUNNING -> {
                if(this.status != AuctionStatus.OPEN){throw new InvalidStatusException("Cannot set this status to RUNNING");}
                this.status = newStatus;
            }
            case FINISHED -> {
                if(this.status != AuctionStatus.RUNNING){throw new InvalidStatusException("Cannot set this status to FINISHED");}
                this.status = newStatus;
            }
            case PAID -> {
                if(this.status != AuctionStatus.FINISHED){throw new InvalidStatusException("Cannot set this status to PAID");}
                this.status = newStatus;
            }
            case CANCELED -> {
                this.status = newStatus;
            }
        }

        // MORE METHODS
    }

    // Getters and Setters
    public Item getItem() {
        return this.item;
    }
    public void setItem(Item item) {
        this.item = item;
    }
    public Seller getSeller() {
        return this.seller;
    }
    public void setSeller(Seller seller) {
        this.seller = seller;
    }
    public AuctionStatus getStatus() {
        return status;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public void setCloseTime(LocalDateTime closeTime) {
        this.closeTime = closeTime;
    }
    public LocalDateTime getCloseTime() {
        return closeTime;
    }
    public BidTransaction getHighestBid() {
        if (bidHistory.isEmpty()) {
            return null;
        }
        else {
            return highestBid;
        }
    }
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
}
