package com.auction.server.network;

import com.auction.models.dto.NetworkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Manages a persistent session with one specific client.
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
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            // Initialize specialized handlers for this session
            userHandler = new UserHandler(out);
            auctionHandler = new AuctionHandler(out);

            logger.info("Handlers initialized for {}", socket.getInetAddress());

            // Continuous listening loop
            while (true) {
                try {
                    Object received = in.readObject();
                    if (received instanceof NetworkMessage msg) {
                        // Routing logic:
                        // 1. Try UserHandler first
                        com.auction.models.User authenticatedUser = userHandler.handleMessage(msg);
                        boolean handled = (authenticatedUser != null);
                        
                        // 2. Synchronize user state if login occurred
                        if (authenticatedUser != null) {
                            auctionHandler.setCurrentUser(authenticatedUser);
                        }

                        // 3. If UserHandler didn't "handle" it as a successful login, 
                        // it might still have been a logout or a failed login (which UserHandler already responded to).
                        // If it's still not handled, try AuctionHandler.
                        if (!handled) {
                            // Check if UserHandler still wants it (e.g. LogoutRequest, RegisterRequest)
                            // We need to re-check handled status or just trust the short-circuiting.
                            // To be safe and clean:
                            if (msg instanceof com.auction.models.dto.LogoutRequest || 
                                msg instanceof com.auction.models.dto.RegisterRequest) {
                                handled = true; // UserHandler handled it but returned null User
                            }
                            
                            if (!handled) {
                                handled = auctionHandler.handleMessage(msg);
                            }
                        }
                        
                        if (!handled) {
                            logger.warn("Unhandled message type: {}", msg.getClass().getSimpleName());
                        }
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("Unknown data received: {}", e.getMessage(), e);
                } catch (IOException e) {
                    // Connection closed by client
                    break;
                }
            }
        } catch (IOException e) {
            // IO Error (e.g. broken pipe)
            logger.error("IO Error in client session: {}", e.getMessage());
        } finally {
            logger.info("Client disconnected: {}", socket.getInetAddress());
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
                logger.error("Error closing socket: {}", e.getMessage(), e);
            }
        }
    }
}
