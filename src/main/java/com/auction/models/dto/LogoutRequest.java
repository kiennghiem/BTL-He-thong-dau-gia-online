package com.auction.models.dto;

/**
 * DTO sent from Client to Server to logout.
 */
public class LogoutRequest implements NetworkMessage {
    private final String username;

    public LogoutRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
