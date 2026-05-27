package com.auction.exceptions;

/**
 * Base exception for authentication-related errors.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
