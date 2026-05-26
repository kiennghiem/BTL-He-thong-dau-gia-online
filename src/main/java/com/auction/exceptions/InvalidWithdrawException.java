package com.auction.exceptions;

public class InvalidWithdrawException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public InvalidWithdrawException(String message) {
        super(message);
    }
}
