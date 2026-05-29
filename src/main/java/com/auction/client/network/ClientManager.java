package com.auction.client.network;

import com.auction.models.dto.NetworkMessage;
import com.auction.models.dto.AppConstants;

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
     * Establishes a connection to the server using constants from AppConstants.
     */
    public void connect() throws IOException {
        if (socket != null && !socket.isClosed()) return;

        socket = new Socket(AppConstants.SERVER_HOST, AppConstants.SERVER_PORT);
        // Important: Create 'out' before 'in' to avoid deadlock when server does the same (the OutputStream
        // constructor will send a Stream Header to the other side, but the InputStream constructor will wait
        // until it receives a Stream Header, so declaring 'out' before 'in' ensures the Stream Header will always
        // get sent first, allowing the InputStream of the other side to finish and terminate).
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        running = true;

        Thread listenerThread = new Thread(() -> listen(), "ClientListenerThread");
        listenerThread.setDaemon(true); // Allow app to exit if this thread is still running
        listenerThread.start();
        System.out.println("[CLIENT] Connected to server at " + AppConstants.SERVER_HOST + ":" + AppConstants.SERVER_PORT);
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
                System.err.println("[CLIENT] Connection lost or error: " + e.getMessage());
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
            System.err.println("[CLIENT] Failed to send request: " + e.getMessage());
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
            System.err.println("[CLIENT] Error closing connection: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && running;
    }
}
