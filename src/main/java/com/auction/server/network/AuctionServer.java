package com.auction.server.network;

import com.auction.server.service.AuctionService;
import common.NetworkMessage;
import main.java.common.AppConstants;

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
    private final AuctionService auctionService;

    public AuctionServer() {
        this.auctionService = new AuctionService();
        // Link Service with Manager for end-of-auction persistence
        com.auction.server.manager.AuctionManager.getInstance().setAuctionService(this.auctionService);
    }

    public void start() {
        // 1. Initialize data from database
        auctionService.loadActiveAuctions();

        System.out.println("[SERVER] Auction System starting on port " + AppConstants.SERVER_PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(AppConstants.SERVER_PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Connection accepted from: " + clientSocket.getInetAddress());
                
                // Spawn a new session thread for each client
                new Thread(new ClientSession(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Fatal Error: " + e.getMessage());
        }
    }

    /**
     * Inner class to manage a persistent session with one specific client.
     */
    private class ClientSession implements Runnable {
        private final Socket socket;

        public ClientSession(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            UserHandler userHandler = null;
            try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
            ) {
                // Initialize specialized handlers for this session
                userHandler = new UserHandler(out);
                AuctionHandler auctionHandler = new AuctionHandler(out);

                System.out.println("[SESSION] Handlers initialized for " + socket.getInetAddress());

                // Continuous listening loop
                while (true) {
                    try {
                        Object received = in.readObject();
                        if (received instanceof NetworkMessage msg) {
                            // Routing logic: Identify the type of message and delegate
                            userHandler.handleMessage(msg);
                            auctionHandler.handleMessage(msg);
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("[SESSION] Unknown data received: " + e.getMessage());
                    } catch (IOException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                // Normal disconnect or network error
            } finally {
                System.out.println("[SESSION] Client disconnected: " + socket.getInetAddress());
                if (userHandler != null) {
                    userHandler.cleanUp();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        new AuctionServer().start();
    }
}
