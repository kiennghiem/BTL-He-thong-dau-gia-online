package com.auction.client.network;

import com.auction.models.dto.NetworkMessage;
import com.auction.models.dto.AppConstants;
import com.auction.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Singleton class to manage the Socket connection on the client side.
 * It handles connecting, sending requests, and receiving messages from the server using JSON.
 */
public class ClientManager {

    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

    private static ClientManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running = false;
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
                String[] hostsToTry = {"localhost", AppConstants.SERVER_HOST};
                
                for (String host : hostsToTry) {
                    try {
                        logger.info("[CLIENT] Attempting to connect to: " + host);
                        socket = new Socket();
                        socket.connect(new java.net.InetSocketAddress(host, AppConstants.SERVER_PORT), 2000);
                        
                        out = new PrintWriter(socket.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        running = true;

                        Thread listenerThread = new Thread(() -> listen(), "ClientListenerThread");
                        listenerThread.setDaemon(true);
                        listenerThread.start();
                        
                        logger.info("[CLIENT] Connected successfully to " + host);
                        return;
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
     * Continuous loop to read JSON strings from the server.
     */
    private void listen() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                Object msg = JsonUtil.fromJson(line);
                if (msg != null) {
                    notifyListeners(msg);
                }
            }
        } catch (IOException e) {
            if (running) {
                logger.error("[CLIENT] Connection lost or error", e);
                running = false;
            }
        } finally {
            close();
        }
    }

    /**
     * Sends a request to the server as a JSON string.
     */
    public void sendRequest(NetworkMessage request) {
        try {
            if (out == null || socket == null || socket.isClosed()) {
                connect();
            }
            
            String json = JsonUtil.toJson(request);
            if (json != null) {
                synchronized (this) {
                    out.println(json);
                }
            }
        } catch (Exception e) {
            logger.error("[CLIENT] Failed to send request", e);
            javafx.application.Platform.runLater(() -> {
                com.auction.client.controller.ControllerUtils.showAlert(
                    "Không thể gửi yêu cầu tới máy chủ! Chi tiết: " + e.getMessage()
                );
            });
        }
    }

    public void addMessageListener(Consumer<Object> listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(Consumer<Object> listener) {
        messageListeners.remove(listener);
    }

    private void notifyListeners(Object msg) {
        if (msg instanceof com.auction.models.Notification notification) {
            if (notification.getType() == com.auction.models.Notification.Type.STATUS_CHANGED) {
                com.auction.models.dto.AuctionUpdateDTO update = notification.getData();
                if ("CANCELED".equalsIgnoreCase(update.getStatus())) {
                    javafx.application.Platform.runLater(() -> {
                        com.auction.client.controller.ControllerUtils.showWarning("Phiên đấu giá đã bị hủy", 
                            "Phiên đấu giá '" + update.getAuctionTitle() + "' đã bị quản trị viên hủy.");
                    });
                }
            }
        }
        for (Consumer<Object> listener : messageListeners) {
            listener.accept(msg);
        }
    }

    public void close() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("[CLIENT] Error closing connection", e);
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && running;
    }
}
