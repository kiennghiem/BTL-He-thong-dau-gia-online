package com.auction.server.network;

import com.auction.models.dto.AppConstants;
import com.auction.server.service.AuctionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main Server class for the Online Auction System.
 * Orchestrates client connections and delegates tasks to Handlers.
 */
public class AuctionServer {
    private final AuctionService auctionService;
    private final ExecutorService clientPool;
    private static final int MAX_CONNECTIONS = 4; // Bảo vệ Server khỏi DoS
    private volatile boolean running = true;

    public AuctionServer() {
        try {
            this.auctionService = new AuctionService();
            com.auction.server.manager.AuctionManager.getInstance().setAuctionService(this.auctionService);
            this.clientPool = Executors.newFixedThreadPool(MAX_CONNECTIONS);
            
            // Đăng ký Shutdown Hook để bắt sự kiện tắt Server (Ctrl+C, kill process)
            Runtime.getRuntime().addShutdownHook(new Thread(this::gracefulShutdown));
            
        } catch (Exception e) {
            System.err.println("[SERVER] Fatal Error during initialization: " + e.getMessage());
            throw new RuntimeException("Server could not start due to initialization failure.", e);
        }
    }

    public void start() {
        auctionService.loadActiveAuctions();
        System.out.println("[SERVER] Auction System starting on port " + AppConstants.SERVER_PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(AppConstants.SERVER_PORT)) {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                if (!running) {
                    clientSocket.close();
                    break;
                }
                System.out.println("[SERVER] Connection accepted from: " + clientSocket.getInetAddress());
                clientPool.execute(new ClientSession(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[SERVER] Fatal Error: " + e.getMessage());
            }
        }
    }

    /**
     * Cơ chế tắt Server an toàn (Graceful Shutdown).
     * Chờ các tác vụ đang chạy hoàn thành (như lưu DB) trước khi đóng hẳn.
     */
    private void gracefulShutdown() {
        System.out.println("\n[SERVER] Initiating graceful shutdown...");
        running = false; // Ngừng nhận kết nối mới
        
        // Tắt AuctionManager (dừng các luồng đếm ngược thời gian)
        com.auction.server.manager.AuctionManager.getInstance().shutdown();
        
        clientPool.shutdown(); // Ngừng nhận task mới vào pool
        try {
            // Cho phép các Client đang xử lý có tối đa 10 giây để hoàn thành
            if (!clientPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("[SERVER] Forced shutdown after waiting 10 seconds.");
                clientPool.shutdownNow();
            } else {
                System.out.println("[SERVER] All client sessions closed safely.");
            }
        } catch (InterruptedException e) {
            clientPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("[SERVER] Shutdown complete.");
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
            MessageRouter router = null;
            
            try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                // Initialize centralized router for this session
                router = new MessageRouter(out);

                System.out.println("[SESSION] Router initialized for " + socket.getInetAddress());

                // Continuous listening loop (JSON based)
                String line;
                while ((line = in.readLine()) != null) {
                    router.handleJson(line);
                }
            } catch (IOException e) {
                // Connection lost
            } finally {
                System.out.println("[SESSION] Client disconnected: " + socket.getInetAddress());
                if (router != null) {
                    router.cleanUp();
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
