package com.auction.exceptions;

public class InvalidSignupException extends RuntimeException {
    public InvalidSignupException(String message) {
        super(message);
    }
}
