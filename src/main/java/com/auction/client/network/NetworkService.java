package com.auction.client.network;

import com.auction.models.dto.NetworkMessage;
import com.auction.models.dto.PacketType;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.GenericResponse;
import com.auction.util.LocalDateTimeAdapter;
import com.google.gson.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * NetworkService manages the socket connection on the Client side.
 * Uses GSON for JSON communication with the Server.
 */
public class NetworkService {
    private static NetworkService instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;
    private boolean running;
    private Consumer<Object> onNotificationReceived;

    private NetworkService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public static synchronized NetworkService getInstance() {
        if (instance == null) {
            instance = new NetworkService();
        }
        return instance;
    }

    public void connect(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.running = true;
        startListening();
    }

    /**
     * Sends a request to the server as a JSON string.
     */
    public void sendRequest(NetworkMessage request) {
        if (out != null) {
            String json = gson.toJson(request);
            out.println(json);
        }
    }

    /**
     * Starts a background thread to listen for responses and notifications from the server.
     */
    private void startListening() {
        new Thread(() -> {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    handleIncomingMessage(line);
                }
            } catch (IOException e) {
                System.err.println("[Client] Connection lost: " + e.getMessage());
            } finally {
                disconnect();
            }
        }).start();
    }

    private void handleIncomingMessage(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            // Logic to determine type and notify UI controllers...
            // For now, we can pass the raw JSON or use a callback mechanism
            System.out.println("[Client] Received: " + json);
            
            if (onNotificationReceived != null) {
                // Determine the class based on content or a 'type' field if added to responses
                // This is where you'd parse back to AuthResponse, GenericResponse, or Notification
                onNotificationReceived.accept(json); 
            }
        } catch (Exception e) {
            System.err.println("[Client] Error parsing incoming message: " + e.getMessage());
        }
    }

    public void setOnNotificationReceived(Consumer<Object> callback) {
        this.onNotificationReceived = callback;
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
