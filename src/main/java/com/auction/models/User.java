package com.auction.models;

import com.auction.exceptions.InvalidDepositException;
import com.auction.exceptions.InvalidWithdrawException;
import com.auction.server.factory.UserRole;

import java.math.BigDecimal;

public abstract class User extends Entity {
    private static final long serialVersionUID = 1L;
    private UserRole role;
    private String username;
    private String password;
    private BigDecimal balance;

    public User() {
        super();
    }

    // Create an instance of a new user
    public User(UserRole role, String username, String password) {
        super();
        this.role = role;
        this.username = username;
        this.password = password;
        this.balance = BigDecimal.ZERO;
    }

    // Create an instance of an existed user from the DB
    public User(String id, UserRole role, String username, String password, BigDecimal balance) {
        super(id);
        this.role = role;
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    // Deposit money to balance
    public void deposit(BigDecimal amount) throws InvalidDepositException {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(amount);
        } else {
            throw new InvalidDepositException("Deposit amount must be higher than 0");
        }
    }

    // Withdraw money from balance (to pay for an item from an auction)
    public void withdraw(BigDecimal amount) throws InvalidWithdrawException {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (amount.compareTo(balance) <= 0) {
                balance = balance.subtract(amount);
            } else {
                throw new InvalidWithdrawException("Balance is not enough to withdraw from");
            }
        } else {
            throw new InvalidWithdrawException("Withdraw amount must be higher than 0");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername ( String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword( String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public String getRoleAsString() {
        return role.toString();
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}