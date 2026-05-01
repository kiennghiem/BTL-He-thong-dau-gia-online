package Base;
import java.time.LocalDateTime;


public abstract class Item extends Entity{
    private static final long serialVersionUID = 1L;
    private String itemName;
    private String description;
    private double currentPrice;
    private double startingPrice;
    private LocalDateTime startingTime;
    private LocalDateTime closingTime;
    private String status;
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
        this.status = "PENDING";
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
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
        else if (!"RUNNING".equals(status))return false;
        return true;
    }

    public void UpdateBidder(double price, User Bidder){
        // Them exception sau nua
        currentBidder = Bidder;
        currentPrice = price;
    }

    abstract public String getInfo();
}

