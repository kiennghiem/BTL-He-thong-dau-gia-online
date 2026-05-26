package com.auction.models;


public class Bidder extends User {
    private static final long serialVersionUID = 1L;
    
    private double balance;
    private double lockedAmount; // Money temporarily held during active bids

    public Bidder(String username, String password) {
        super(username, password);
        this.setRole("BIDDER");
        this.balance = 0.0;
        this.lockedAmount = 0.0;
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public double getLockedAmount() { return lockedAmount; }
    public void setLockedAmount(double lockedAmount) { this.lockedAmount = lockedAmount; }

    /**
     * Checks if the bidder has enough funds for a new bid.
     */
    public boolean canAfford(double amount) {
        return (balance - lockedAmount) >= amount;
    }
}
