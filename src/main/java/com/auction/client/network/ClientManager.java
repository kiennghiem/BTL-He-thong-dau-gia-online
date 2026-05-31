package com.auction.client.network;

import com.auction.models.dto.NetworkMessage;
import com.auction.models.dto.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Singleton class to manage the Socket connection on the client side.
 * It handles connecting, sending requests, and receiving messages from the server.
 */
public class ClientManager {

    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

    private static ClientManager instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean running = false;
    // Consumer<Object> is an interface that has one method accept(Object obj), which takes the object and
    // does something with it, and not returning anything. In this case, each Consumer<Object> is a listener
    // for a specific type of messages, and the controllers each have their own Consumer<Object> which handles
    // the message differently.
    private final List<Consumer<Object>> messageListeners = new CopyOnWriteArrayList<>();

    private ClientManager() {}

    public static synchronized ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

    /**
     * Establishes a connection to the server using a non-blocking background task.
     */
    public void connect() {
        if (socket != null && !socket.isClosed()) return;

        new Thread(() -> {
            try {
                // SMART CONNECT: Try localhost first (for the host), then fall back to the Public IP
                String[] hostsToTry = {"localhost", AppConstants.SERVER_HOST};
                
                for (String host : hostsToTry) {
                    try {
                        logger.info("[CLIENT] Attempting to connect to: " + host);
                        socket = new Socket();
                        socket.connect(new java.net.InetSocketAddress(host, AppConstants.SERVER_PORT), 2000); // 2 second timeout
                        
                        out = new ObjectOutputStream(socket.getOutputStream());
                        in = new ObjectInputStream(socket.getInputStream());
                        running = true;

                        Thread listenerThread = new Thread(() -> listen(), "ClientListenerThread");
                        listenerThread.setDaemon(true);
                        listenerThread.start();
                        
                        logger.info("[CLIENT] Connected successfully to " + host);
                        return; // Successfully connected
                    } catch (IOException e) {
                        logger.warn("[CLIENT] Failed to connect to " + host + ". Trying next...");
                    }
                }
                
                logger.error("[CLIENT] All connection attempts failed.");
            } catch (Exception e) {
                logger.error("[CLIENT] Error during connection sequence", e);
            }
        }, "ConnectionThread").start();
    }

    /**
     * Continuous loop to read objects from the server.
     */
    private void listen() {
        try {
            while (running) {
                Object msg = in.readObject();
                if (msg != null) {
                    notifyListeners(msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                logger.error("[CLIENT] Connection lost or error", e);
                running = false;
            }
        } finally {
            close();
        }
    }

    /**
     * Sends a request to the server. Connects if not already connected.
     */
    public void sendRequest(NetworkMessage request) {
        try {
            if (out == null || socket == null || socket.isClosed()) {
                connect();
            }
            synchronized (out) {
                out.writeObject(request);
                out.flush();
                out.reset(); // Clear cache for stateful objects
            }
        } catch (IOException e) {
            logger.error("[CLIENT] Failed to send request", e);
            // Show alert to user so they know why nothing is happening
            javafx.application.Platform.runLater(() -> {
                com.auction.client.controller.ControllerUtils.showAlert(
                    "Không thể kết nối tới máy chủ! Vui lòng kiểm tra lại kết nối mạng hoặc địa chỉ IP.\n" +
                    "Chi tiết: " + e.getMessage()
                );
            });
        }
    }

    /**
     * Registers a listener for any object received from the server.
     */
    public void addMessageListener(Consumer<Object> listener) {
        messageListeners.add(listener);
    }

    /**
     * Removes a previously registered listener.
     */
    public void removeMessageListener(Consumer<Object> listener) {
        messageListeners.remove(listener);
    }

    private void notifyListeners(Object msg) {
        for (Consumer<Object> listener : messageListeners) {
            listener.accept(msg);
        }
    }

    /**
     * Closes the socket and streams.
     */
    public void close() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            // Streams are closed by closing the socket
        } catch (IOException e) {
            logger.error("[CLIENT] Error closing connection", e);
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && running;
    }
}
