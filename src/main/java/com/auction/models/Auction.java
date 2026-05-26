package com.auction.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneId;

import com.auction.exceptions.*;

public class Auction extends Entity {

    private static final long serialVersionUID = 1L;
    private Item item;
    private Seller seller;
    private AuctionStatus status;
    private LocalDateTime startingTime;
    private LocalDateTime closingTime;
    private BidTransaction highestBid;
    private double currentPrice;
    private double minIncrement;
    private Bidder winner;
    
    private List<BidTransaction> bidHistory;

    public Auction(Item item, Seller seller, LocalDateTime startingTime, LocalDateTime closingTime, double minIncrement) {
        this.item = item;
        this.seller = seller;
        this.status = AuctionStatus.PENDING;
        this.startingTime = startingTime;
        this.closingTime = closingTime;
        this.currentPrice = item.getStartingPrice();
        this.minIncrement = minIncrement;
        this.bidHistory = new ArrayList<>();
    }

    // Getters and Setters
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }
    public AuctionStatus getStatus() { return status; }
    public LocalDateTime getStartingTime() { return startingTime; }
    public void setStartingTime(LocalDateTime startingTime) { this.startingTime = startingTime; }
    public void setClosingTime(LocalDateTime closingTime) { this.closingTime = closingTime; }
    public LocalDateTime getClosingTime() { return closingTime; }
    public BidTransaction getHighestBid() { return highestBid; }
    public double getCurrentPrice() { return currentPrice; }
    public double getMinIncrement() { return minIncrement; }
    public void setMinIncrement(double minIncrement) { this.minIncrement = minIncrement; }
    public Bidder getWinner() { return winner; }
    public void setWinner(Bidder winner) { this.winner = winner; }

    public boolean isAuctioning(){
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startingTime)) return false;
        if (closingTime == null || now.isAfter(closingTime)) return false;
        return AuctionStatus.RUNNING.equals(status);
    }

    public synchronized void addBid(BidTransaction bid) throws InvalidBidException {
        if (bid.getBidAmount() < (currentPrice + minIncrement)) {
            throw new InvalidBidException("Bid must be at least " + (currentPrice + minIncrement));
        }
        bidHistory.add(bid);
        highestBid = bid;
        currentPrice = highestBid.getBidAmount();
    }

    public void updateStatus(AuctionStatus newStatus) throws InvalidStatusException {
        if (this.status == newStatus) return;
        
        switch(newStatus){
            case OPEN -> {
                if(this.status != AuctionStatus.PENDING) throw new InvalidStatusException("Invalid transition to OPEN");
                this.status = newStatus;
            }
            case RUNNING -> {
                if(this.status != AuctionStatus.OPEN) throw new InvalidStatusException("Invalid transition to RUNNING");
                this.status = newStatus;
            }
            case FINISHED -> {
                if(this.status != AuctionStatus.RUNNING) throw new InvalidStatusException("Invalid transition to FINISHED");
                this.status = newStatus;
            }
            case PAID, CANCELED -> this.status = newStatus;
        }
    }

    public long getClosingTimeMillis() {
        return closingTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
