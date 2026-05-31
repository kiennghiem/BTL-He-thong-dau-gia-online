package com.auction.server.network;

import com.auction.models.dto.NetworkMessage;
import com.auction.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * Manages a persistent session with one specific client using JSON for communication.
 */
public class ClientSession implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private final Socket socket;

    public ClientSession(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        UserHandler userHandler = null;
        AuctionHandler auctionHandler = null;
        
        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Initialize specialized handlers for this session
            // Handlers now take PrintWriter for JSON communication
            userHandler = new UserHandler(out);
            auctionHandler = new AuctionHandler(out);

            logger.info("Handlers initialized for {}", socket.getInetAddress());

            String line;
            while ((line = in.readLine()) != null) {
                try {
                    Object received = JsonUtil.fromJson(line);
                    if (received instanceof NetworkMessage msg) {
                        // Routing logic:
                        // 1. Try UserHandler first
                        com.auction.models.User authenticatedUser = userHandler.handleMessage(msg);
                        boolean handled = (authenticatedUser != null);
                        
                        // 2. Synchronize user state if login occurred
                        if (authenticatedUser != null) {
                            auctionHandler.setCurrentUser(authenticatedUser);
                        }

                        if (!handled) {
                            if (msg instanceof com.auction.models.dto.LogoutRequest || 
                                msg instanceof com.auction.models.dto.RegisterRequest ||
                                msg instanceof com.auction.models.dto.DepositRequest) {
                                handled = true; 
                            }
                            
                            if (!handled) {
                                handled = auctionHandler.handleMessage(msg);
                            }
                        }
                        
                        if (!handled) {
                            logger.warn("Unhandled message type in JSON: {}", msg.getClass().getSimpleName());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing JSON line: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("IO Error in client session: {}", e.getMessage());
        } finally {
            logger.info("Client disconnected: {}", socket.getInetAddress());
            if (userHandler != null) {
                userHandler.cleanUp();
            }
            if (auctionHandler != null) {
                auctionHandler.cleanUp();
            }
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error closing socket: {}", e.getMessage(), e);
            }
        }
    }
}
