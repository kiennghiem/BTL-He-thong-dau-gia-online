package com.auction.models.dto;
/**
 * Global constants for the Online Auction System.
 * This ensures consistency across the Client and Server modules.
 */
public final class AppConstants {
    // Prevent instantiation of this utility class
    private AppConstants() {}
    // --- Networking Configuration ---
    public static final String SERVER_HOST = "localhost"; // Change to Server IP for real network
    public static final int SERVER_PORT = 8080;           // Port for Socket communication
    // --- Auction Statuses (Lifecycle Management) ---
    // Matches the states: OPEN -> RUNNING -> FINISHED -> PAID/CANCELED
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_FINISHED = "FINISHED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELED = "CANCELED";
    // --- User Roles ---
    // Specific roles as defined in Section 3.1.1
    public static final String ROLE_BIDDER = "BIDDER";
    public static final String ROLE_SELLER = "SELLER";
    public static final String ROLE_ADMIN = "ADMIN";
    // --- Anti-Sniping (Gia hạn phiên) ---
    // If a bid is placed in the last 10 seconds, extend by 30 seconds
    public static final int SNIPE_WINDOW_SECONDS = 10;
    public static final int EXTENSION_TIME_SECONDS = 30;
    // --- Error Messages ---
    public static final String ERR_LOW_BID = "Giá đặt phải cao hơn giá hiện tại!";
    public static final String ERR_AUCTION_CLOSED = "Phiên đấu giá đã kết thúc!";
}