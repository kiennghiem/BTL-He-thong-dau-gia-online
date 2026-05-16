package com.auction.models;

public abstract class User extends Entity {
    private static final long serialVersionUID = 1L;
    private String userName;
    private String password;
    private String role;

    public User(String userName, String password) {
        super();
        this.userName = userName;
        this.password = password;
        this.role = " ";
    }

    public String getUser_Name() { return userName; }
    public void setUser_Name ( String user_Name) {  userName = user_Name;}
    public String getPassword() { return password;}
    public void setPassword( String password) {  this.password = password;}
    public String getRole() { return role;}
    public void setRole(String role) { this.role = role; }

}