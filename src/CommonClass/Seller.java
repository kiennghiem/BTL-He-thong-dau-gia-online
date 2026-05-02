package Base;

public class Seller extends User{
    private static final long serialVersionUID = 1L;
    
    public Seller(String userName, String password) {
        super(userName,password);
        this.setRole("SELLER");
    }
}
