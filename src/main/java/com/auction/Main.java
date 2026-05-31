package com.auction;

import com.auction.server.ServerApp;
import com.auction.util.DatabaseSetup;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // 1. Initialize Database
        try {
            DatabaseSetup dbSetup = new DatabaseSetup();
            dbSetup.initDatabase();
        } catch (Exception e) {
            logger.error("Error during database initialization", e);
        }

        Thread serverThread = new Thread(() -> ServerApp.main(args));
        serverThread.setDaemon(true); // The JVM will kill this thread when the UI closes
        serverThread.start();

        Application.launch(AuctionApp.class, args);
    }
}



