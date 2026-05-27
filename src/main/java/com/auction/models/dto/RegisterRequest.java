package com.auction.models.dto;

/**
 * DTO sent from Client to Server to register a new user.
 */
public class RegisterRequest implements NetworkMessage {
    private final String username;
    private final String password;
    private final String role;

    public RegisterRequest(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
