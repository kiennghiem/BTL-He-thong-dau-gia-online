package com.auction.models.dto;

import java.math.BigDecimal;

/**
 * DTO for virtual account recharge.
 */
public class DepositRequest implements NetworkMessage {
    private final String username;
    private final BigDecimal amount;

    public DepositRequest(String username, BigDecimal amount) {
        this.username = username;
        this.amount = amount;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
