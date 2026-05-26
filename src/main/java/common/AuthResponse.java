package common;

import common.NetworkMessage;

/**
 * DTO sent from Server to Client in response to a LoginRequest.
 * Communicates whether authentication was successful and the user's permissions.
 */
public class AuthResponse implements NetworkMessage {
    // Encapsulation: Private fields to store the state of the response
    private final boolean success;
    private final String message;
    private final String userRole; // Uses roles like BIDDER, SELLER, or ADMIN

    /**
     * Constructor for a complete authentication response[cite: 1].
     *
     * @param success True if credentials are valid, false otherwise.
     * @param message A descriptive message (e.g., "Login successful" or "Invalid password")[cite: 1].
     * @param userRole The role assigned to the user from AppConstants[cite: 1].
     */
    public AuthResponse(boolean success, String message, String userRole) {
        this.success = success;
        this.message = message;
        this.userRole = userRole;
    }

    // --- Getters for the Client to process the response[cite: 1] ---

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUserRole() {
        return userRole;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", role='" + userRole + '\'' +
                '}';
    }
}