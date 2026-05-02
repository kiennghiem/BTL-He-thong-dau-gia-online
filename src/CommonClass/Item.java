package CommonClass;

import java.time.LocalDateTime;
import SharedException.InvalidBidException;
import SharedException.InvalidStatusException;

public abstract class Item extends Entity{
    private static final long serialVersionUID = 1L;
    private String itemName;
    private String description;
    private double currentPrice;
    private double startingPrice;
    private LocalDateTime startingTime;
    private LocalDateTime closingTime;
    private ItemStatus status;
    private User owner;
    private User currentBidder;
    private User buyer;

    public Item(String itemName, String description, double startingPrice, LocalDateTime startingTime, LocalDateTime closingTime, User owner) {
        super();
        this.itemName = itemName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.startingTime = startingTime;
        this.closingTime = closingTime;
        this.status = ItemStatus.PENDING;
        this.owner = owner;
    }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }  
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getCurrentPrice() { return currentPrice; }    
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    public LocalDateTime getStartingTime() { return startingTime; }
    public void setStartingTime(LocalDateTime startingTime) { this.startingTime = startingTime; }
    public void setClosingTime(LocalDateTime closingTime) { this.closingTime = closingTime; }
    public LocalDateTime getClosingTime() { return closingTime; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public String getOwner() { return owner.getUser_Name(); }
    public void setOwner(User owner) { this.owner = owner; }
    public String getCurrentBidder() { return currentBidder.getUser_Name(); }
    public void setCurrentBidder(User currentBidder) { this.currentBidder = currentBidder; }
    public String getBuyer() { return buyer.getUser_Name(); }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public boolean IsAutioning(){
        LocalDateTime now = LocalDateTime.now();
        if ( now.isBefore(startingTime)) return false;
        else if (closingTime == null || now.isAfter(closingTime)) return false;
        else return "RUNNING".equals(status);
    }

    public void UpdateBidder(double price, User Bidder) throws InvalidBidException
    {
        if(price< currentPrice) {throw new InvalidBidException("Price must be higher than current price.");}
        if(!this.IsAutioning()){throw new InvalidBidException("Aution is currently not active.");}
        currentBidder = Bidder;
        currentPrice = price;
    }

    public void UpdateStatus(ItemStatus newStatus) throws InvalidStatusException
    {
        if(this.status == newStatus){return;}
        switch(newStatus){
            case OPEN -> {
                if(this.status != ItemStatus.PENDING){throw new InvalidStatusException("Cannot set this status to OPEN");}
                this.status = newStatus;
            }
            case RUNNING -> {
                if(this.status != ItemStatus.OPEN){throw new InvalidStatusException("Cannot set this status to RUNNING");}
                this.status = newStatus;
            }
            case FINISHED -> {
                if(this.status != ItemStatus.RUNNING){throw new InvalidStatusException("Cannot set this status to FINISHED");}
                this.status = newStatus;
            }
            case PAID -> {
                if(this.status != ItemStatus.FINISHED){throw new InvalidStatusException("Cannot set this status to PAID");}
                this.status = newStatus;
            }
            case CANCELED -> {
                this.status = newStatus;
            }
        }

    }

    abstract public String getInfo();
}

