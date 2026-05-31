package com.auction.server.network;

import com.auction.server.service.AuctionService;
import com.auction.models.dto.NetworkMessage;
import com.auction.models.dto.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main Server class for the Online Auction System.
 * Orchestrates client connections and delegates tasks to Handlers.
 */
public class AuctionServer {
    private static final Logger logger = LoggerFactory.getLogger(AuctionServer.class);
    private final AuctionService auctionService;

    public AuctionServer() {
        try {
            this.auctionService = new AuctionService();
            // Link Service with Manager for end-of-auction persistence
            com.auction.server.manager.AuctionManager.getInstance().setAuctionService(this.auctionService);
        } catch (Exception e) {
            logger.error("Fatal Error during initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Server could not start due to initialization failure.", e);
        }
    }

    public void start() {
        // 1. Initialize data from database
        auctionService.loadActiveAuctions();

        logger.info("Auction System starting on port {}...", AppConstants.SERVER_PORT);
        
        try (ServerSocket serverSocket = new ServerSocket(AppConstants.SERVER_PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Connection accepted from: {}", clientSocket.getInetAddress());
                
                // Spawn a new session thread for each client
                new Thread(new ClientSession(clientSocket)).start();
            }
        } catch (IOException e) {
            logger.error("Fatal Error: {}", e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        new AuctionServer().start();
    }
}
