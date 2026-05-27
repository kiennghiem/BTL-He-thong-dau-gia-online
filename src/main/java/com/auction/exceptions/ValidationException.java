package com.auction.exceptions;

/**
 * Thrown when data validation fails.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
