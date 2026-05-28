package com.auction.server.network;

import com.auction.server.service.AuctionService;
import com.auction.models.dto.NetworkMessage;
import com.auction.models.dto.AppConstants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Server class for the Online Auction System.
 * Orchestrates client connections and delegates tasks to Handlers.
 */
public class AuctionServer {
    private final AuctionService auctionService;
    private final ExecutorService clientPool;

    public AuctionServer() {
        try {
            this.auctionService = new AuctionService();
            // Link Service with Manager for end-of-auction persistence
            com.auction.server.manager.AuctionManager.getInstance().setAuctionService(this.auctionService);
            // Initialize cached thread pool for elastic client connection management
            this.clientPool = Executors.newCachedThreadPool();
        } catch (Exception e) {
            System.err.println("[SERVER] Fatal Error during initialization: " + e.getMessage());
            throw new RuntimeException("Server could not start due to initialization failure.", e);
        }
    }

    public void start() {
        // 1. Initialize data from database
        auctionService.loadActiveAuctions();

        System.out.println("[SERVER] Auction System starting on port " + AppConstants.SERVER_PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(AppConstants.SERVER_PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Connection accepted from: " + clientSocket.getInetAddress());
                
                // Delegate the session to the thread pool
                clientPool.execute(new ClientSession(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Fatal Error: " + e.getMessage());
        } finally {
            clientPool.shutdown();
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
            AuctionHandler auctionHandler = null;
            
            try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
            ) {
                // Initialize specialized handlers for this session
                userHandler = new UserHandler(out);
                auctionHandler = new AuctionHandler(out);

                System.out.println("[SESSION] Handlers initialized for " + socket.getInetAddress());

                // Continuous listening loop
                while (true) {
                    try {
                        Object received = in.readObject();
                        if (received instanceof NetworkMessage msg) {
                            // Routing logic with short-circuiting: 
                            // Try UserHandler first, if not handled, try AuctionHandler
                            boolean handled = userHandler.handleMessage(msg);
                            if (!handled) {
                                handled = auctionHandler.handleMessage(msg);
                            }
                            
                            if (!handled) {
                                System.out.println("[SESSION] Unhandled message: " + msg.getClass().getSimpleName());
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        System.err.println("[SESSION] Unknown data received: " + e.getMessage());
                    } catch (IOException e) {
                        // Connection closed by client
                        break;
                    }
                }
            } catch (IOException e) {
                // IO Error (e.g. broken pipe)
            } finally {
                System.out.println("[SESSION] Client disconnected: " + socket.getInetAddress());
                // Mandatory cleanup to prevent memory leaks and ghost users
                if (userHandler != null) {
                    userHandler.cleanUp();
                }
                if (auctionHandler != null) {
                    auctionHandler.cleanUp();
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
