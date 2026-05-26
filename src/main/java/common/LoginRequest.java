package common;

/**
 * DTO sent from Client to Server to initiate authentication.
 * Implements NetworkMessage for serialization over Sockets.
 */
public class LoginRequest implements NetworkMessage {
    // Encapsulation: private fields
    private final String username;
    private final String password;

    /**
     * Constructor to initialize login credentials.
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- Getters for the Server to access the data ---

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Overriding toString for easier debugging in logs.
     * We do not print the password for security reasons.
     */
    @Override
    public String toString() {
        return "LoginRequest{username='" + username + "'}";
    }
}