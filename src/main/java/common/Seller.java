package main.java.common;

public class Seller extends User{
    private static final long serialVersionUID = 1L;
    
    public Seller() {
        super();
        this.setRole("SELLER");
    }

    public Seller(String userName, String password) {
        super(userName,password);
        this.setRole("SELLER");
    }
}
