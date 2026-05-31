package com.auction.models.dto;

import com.auction.models.User;

/**
 * DTO sent from Server to Client in response to a LoginRequest.
 * Communicates whether authentication was successful and the user's permissions.
 */
public class AuthResponse implements NetworkMessage {
    // Encapsulation: Private fields to store the state of the response
    private final boolean success;
    private final String message;
    private final User user;

    /**
     * Constructor for a complete authentication response.
     *
     * @param success True if credentials are valid, false otherwise.
     * @param message A descriptive message (e.g., "Login successful" or "Invalid password").
     * @param user the user object returned from UserDAO's findByUsername.
     */
    public AuthResponse(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    // --- Getters for the Client to process the response ---

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", User role='" + user.getRole().toString() + '\'' +
                '}';
    }
}