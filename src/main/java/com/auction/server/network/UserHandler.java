package com.auction.server.network;

import com.auction.exceptions.AuthenticationException;
import com.auction.exceptions.ValidationException;
import com.auction.models.User;
import com.auction.server.service.UserService;
import com.auction.server.factory.UserRole;
import com.auction.models.dto.LoginRequest;
import com.auction.models.dto.LogoutRequest;
import com.auction.models.dto.RegisterRequest;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.NetworkMessage;

import com.auction.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * UserHandler manages authentication and user-profile communication.
 * Standardizes security checks and session tracking for each connection.
 */
public class UserHandler {
    private final PrintWriter out;
    private final UserService userService;
    private final Gson gson;
    private String currentUsername;

    public UserHandler(PrintWriter out) {
        this.out = out;
        this.userService = new UserService();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public void handleLogin(NetworkMessage message) {
        if (message instanceof LoginRequest req) {
            try {
                User user = userService.login(req.getUsername(), req.getPassword());
                this.currentUsername = user.getUsername();
                
                AuthResponse response = new AuthResponse(true, "Đăng nhập thành công!", user.getRole().name());
                sendResponse(response);
                
                System.out.println("[UserHandler] Login successful for: " + currentUsername);
            } catch (AuthenticationException e) {
                sendResponse(new AuthResponse(false, e.getMessage(), null));
            }
        }
    }

    public void handleLogout(NetworkMessage message) {
        if (message instanceof LogoutRequest req) {
            userService.logout(req.getUsername());
            if (req.getUsername().equals(currentUsername)) {
                currentUsername = null;
            }
            sendResponse(new AuthResponse(true, "Đăng xuất thành công", null));
        }
    }

    public void handleRegister(NetworkMessage message) {
        if (message instanceof RegisterRequest req) {
            try {
                UserRole role;
                try {
                    role = UserRole.valueOf(req.getRole().toUpperCase());
                } catch (Exception e) {
                    role = UserRole.BIDDER;
                }

                User newUser = userService.register(role, req.getUsername(), req.getPassword());
                sendResponse(new AuthResponse(true, "Đăng ký tài khoản thành công!", newUser.getRole().name()));

                System.out.println("[UserHandler] Registration successful: " + newUser.getUsername());
            } catch (ValidationException e) {
                sendResponse(new AuthResponse(false, e.getMessage(), null));
            } catch (Exception e) {
                sendResponse(new AuthResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
            }
        }
    }

    /**
     * Ensures session cleanup if the connection is lost.
     */
    public void cleanUp() {
        if (currentUsername != null) {
            userService.forceLogout(currentUsername);
            currentUsername = null;
        }
    }

    private void sendResponse(Object response) {
        try {
            String json = gson.toJson(response);
            synchronized (out) {
                out.println(json); // Sử dụng println thay vì writeObject
            }
        } catch (Exception e) {
            System.err.println("[UserHandler] Connection error while sending JSON: " + e.getMessage());
        }
    }
}