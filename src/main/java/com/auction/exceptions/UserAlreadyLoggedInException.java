package com.auction.exceptions;

/**
 * Thrown when a user attempts to log in but is already online.
 */
public class UserAlreadyLoggedInException extends AuthenticationException {
    public UserAlreadyLoggedInException(String message) {
        super(message);
    }
}
