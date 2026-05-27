package com.auction;

import com.auction.util.DatabaseSetup;

public class Main {
    public static void main(String[] args) {
        // Initialize Database and Tables
        try {
            DatabaseSetup dbSetup = new DatabaseSetup();
            dbSetup.initDatabase();
        } catch (Exception e) {
            System.err.println("Warning: Database initialization failed. Ensure MySQL is running.");
            e.printStackTrace();
        }

        // Redirect to JavaFX app
        AuctionApp.main(args);
    }
}