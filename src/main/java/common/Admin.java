package common;
public class Admin extends User {
    private static final long serialVersionUID = 1L;
    public Admin() {
        super();
        this.setRole("ADMIN");
    }

    public Admin(String userName, String password) {
        super(userName,password);
        this.setRole("Admin");
    }


}
