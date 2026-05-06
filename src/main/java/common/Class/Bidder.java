package main.java.common.Class;

public class Bidder extends User {
    private static final long serialVersionUID = 1L;
    private double balance;

    public Bidder(String userName, String password) {
        super(userName,password);
        this.setRole("BIDDER");
    }
    
}
