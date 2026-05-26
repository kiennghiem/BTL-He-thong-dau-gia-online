package com.auction.models;

import com.auction.server.factory.UserRole;

public abstract class User extends Entity {
    private static final long serialVersionUID = 1L;
    private UserRole role;
    private String userName;
    private String password;

    public User(UserRole role, String userName, String password) {
        super();
        this.role = role;
        this.userName = userName;
        this.password = password;
    }

    public String getUser_Name() { return userName; }
    public void setUser_Name ( String user_Name) {  userName = user_Name;}
    public String getPassword() { return password;}
    public void setPassword( String password) {  this.password = password;}
    public UserRole getRole() { return role;}
    public void setRole(UserRole role) { this.role = role; }

}