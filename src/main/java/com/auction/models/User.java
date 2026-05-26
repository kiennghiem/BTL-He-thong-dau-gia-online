package com.auction.models;

import com.auction.exceptions.InvalidDepositException;
import com.auction.exceptions.InvalidWithdrawException;
import com.auction.server.factory.UserRole;

public abstract class User extends Entity {
    private static final long serialVersionUID = 1L;
    private UserRole role;
    private String userName;
    private String password;
    private double balance;

    // Create an instance of a new user
    public User(UserRole role, String userName, String password) {
        super();
        this.role = role;
        this.userName = userName;
        this.password = password;
        this.balance = 0;
    }

    // Create an instance of an existed user from the DB
    public User(String id, UserRole role, String userName, String password, double balance) {
        super(id);
        this.role = role;
        this.userName = userName;
        this.password = password;
        this.balance = balance;
    }

    // Deposit money to balance
    public void deposit(double amount) throws InvalidDepositException {
        if (amount > 0) {
            balance += amount;
        } else {
            throw new InvalidDepositException("Deposit amount must be higher than 0");
        }
    }

    // Withdraw money from balance (to pay for an item from an auction)
    public void withdraw(double amount) throws InvalidWithdrawException {
        if (amount > 0) {
            if (amount <= balance) {
                balance -= amount;
            } else {
                throw new InvalidWithdrawException("Balance is not enough to withdraw from");
            }
        } else {
            throw new InvalidWithdrawException("Withdraw amount must be higher than 0");
        }
    }

    public String getUserName() { return userName; }
    public void setUserName ( String userName) {  this.userName = userName;}
    public String getPassword() { return password;}
    public void setPassword( String password) {  this.password = password;}
    public UserRole getRole() { return role;}
    public void setRole(UserRole role) { this.role = role; }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }

}