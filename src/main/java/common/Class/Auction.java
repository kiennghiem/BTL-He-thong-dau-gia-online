package main.java.common.Class;

import main.java.common.commonException.InvalidBidException;
import main.java.common.commonException.InvalidStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedList;

public class Auction
{
    private static final long serialVersionUID = 1L;
    private Item item;
    private Seller seller;
    private AuctionStatus status;
    private LocalDateTime startingTime;
    private LocalDateTime closingTime;
    private BidTransaction highestBid;
    private float currentPrice;
    private List<BidTransaction> BidHistory = new LinkedList<>();
    // Lock
    // Add lock here

    // Constructors
    public Auction(Item item, Seller seller, LocalDateTime staringTime, LocalDateTime closingTime) {
        this.item = item;
        this.seller = seller;
        status = AuctionStatus.PENDING;
        this.closingTime = closingTime;
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
    public LocalDateTime getStartingTime() {
        return startingTime;
    }
    public void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }
    public void setClosingTime(LocalDateTime closingTime) {
        this.closingTime = closingTime;
    }
    public LocalDateTime getClosingTime() {
        return closingTime;
    }
    public AuctionStatus getStatus() {
        return status;
    }

    public boolean IsAuctioning(){
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startingTime)) return false;
        else if (closingTime == null || now.isAfter(closingTime)) return false;
        else return "RUNNING".equals(status);
    }

    // Method for getting highestBid

    public BidTransaction getHighestBid() {
        highestBid = BidHistory.get(0);
        for (BidTransaction i : BidHistory) {
            if (i.getBidPrice() > highestBid.getBidPrice()) {
                highestBid = i;
            }
        }
        return highestBid;
    }

    // Add a bid
    public synchronized void AddBid(BidTransaction bid) throws InvalidBidException
    {
        if (!this.IsAuctioning()) {
            throw new InvalidBidException("Auction is currently not active.");
        }
        else if (BidHistory.isEmpty()) {
            BidHistory.add(bid);
            highestBid = this.getHighestBid();
            currentPrice = highestBid.getBidPrice();
        }
        else {
            highestBid = this.getHighestBid();
            currentPrice = highestBid.getBidPrice();
            if (bid.getBidPrice() < currentPrice) {
                throw new InvalidBidException("Price must be higher than current price.");
            }
            else {
                BidHistory.add(bid);
                highestBid = this.getHighestBid();
                currentPrice = highestBid.getBidPrice();
            }
        }
    }

    public void UpdateStatus(AuctionStatus newStatus) throws InvalidStatusException
    {
        if(this.status == newStatus){return;}
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

    }

}
