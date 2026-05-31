package com.auction.exceptions;

/**
 * Thrown when login credentials (username/password) are incorrect.
 */
public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
