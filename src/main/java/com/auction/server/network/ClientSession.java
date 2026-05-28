package com.auction.server.network;

import com.auction.models.dto.NetworkMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Manages a persistent session with one specific client.
 */
public class ClientSession implements Runnable {
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
