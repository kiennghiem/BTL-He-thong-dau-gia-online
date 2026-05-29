package com.auction;

import com.auction.server.ServerApp;
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

        // Run server to initialize server socket connection
        ServerApp.main(args);
    }
}