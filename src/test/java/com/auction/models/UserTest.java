package com.auction.models;

import com.auction.exceptions.InvalidDepositException;
import com.auction.exceptions.InvalidWithdrawException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        // Using a concrete implementation (Bidder) to test abstract User logic
        user = new Bidder("testUser", "password");
    }

    @Test
    void testInitialBalanceIsZero() {
        assertEquals(BigDecimal.ZERO, user.getBalance());
    }

    @Test
    void testDepositValidAmount() throws Exception {
        user.deposit(new BigDecimal("100.50"));
        assertEquals(new BigDecimal("100.50"), user.getBalance());
    }

    @Test
    void testDepositInvalidAmount() {
        assertThrows(InvalidDepositException.class, () -> user.deposit(new BigDecimal("-10")));
        assertThrows(InvalidDepositException.class, () -> user.deposit(BigDecimal.ZERO));
    }

    @Test
    void testWithdrawValidAmount() throws Exception {
        user.setBalance(new BigDecimal("200"));
        user.withdraw(new BigDecimal("50"));
        assertEquals(new BigDecimal("150"), user.getBalance());
    }

    @Test
    void testWithdrawInsufficientBalance() {
        user.setBalance(new BigDecimal("20"));
        assertThrows(InvalidWithdrawException.class, () -> user.withdraw(new BigDecimal("50")));
    }

    @Test
    void testWithdrawInvalidAmount() {
        assertThrows(InvalidWithdrawException.class, () -> user.withdraw(new BigDecimal("-5")));
    }
}
