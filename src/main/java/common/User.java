package main.java.common;

public abstract class User extends Entity {
    private static final long serialVersionUID = 1L;
    private String userName;
    private String password;
    private String role;

    public User() {
        super();
    }

    public User(String userName, String password) {
        super();
        this.userName = userName;
        this.password = password;
        this.role = " ";
    }

    public String getUsername() { return userName; }
    public void setUsername ( String userName) {  this.userName = userName;}
    public String getPassword() { return password;}
    public void setPassword( String password) {  this.password = password;}
    public String getRole() { return role;}
    public void setRole(String role) { this.role = role; }

}