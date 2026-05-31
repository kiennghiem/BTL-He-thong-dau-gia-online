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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * UserHandler manages authentication and user-profile communication.
 * Standardizes security checks and session tracking for each connection.
 */
public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
    private final ObjectOutputStream out;
    private final UserService userService;
    private String currentUsername;

    public UserHandler(ObjectOutputStream out) {
        this.out = out;
        this.userService = new UserService();
    }

    /**
     * Entry point for messages identified as user-related.
     */
    public boolean handleMessage(NetworkMessage message) {
        if (message instanceof LoginRequest loginReq) {
            handleLogin(loginReq);
            return true;
        } else if (message instanceof LogoutRequest logoutReq) {
            handleLogout(logoutReq);
            return true;
        } else if (message instanceof RegisterRequest registerReq) {
            handleRegister(registerReq);
            return true;
        }
        return false;
    }

    private void handleLogin(LoginRequest req) {
        try {
            User user = userService.login(req.getUsername(), req.getPassword());
            this.currentUsername = user.getUsername();
            
            AuthResponse response = new AuthResponse(true, "Đăng nhập thành công!", user);
            sendResponse(response);
            
            logger.info("Login successful for: {}", currentUsername);
        } catch (AuthenticationException e) {
            logger.warn("Login failed for {}: {}", req.getUsername(), e.getMessage());
            sendResponse(new AuthResponse(false, e.getMessage(), null));
        }
    }

    private void handleLogout(LogoutRequest req) {
        userService.logout(req.getUsername());
        if (req.getUsername().equals(currentUsername)) {
            currentUsername = null;
        }
        sendResponse(new AuthResponse(true, "Đăng xuất thành công", null));
        logger.info("User logged out: {}", req.getUsername());
    }

    private void handleRegister(RegisterRequest req) {
        try {
            // 1. Convert role string from request to UserRole enum
            UserRole role;
            try {
                role = UserRole.valueOf(req.getRole().toUpperCase());
            } catch (Exception e) {
                role = UserRole.BIDDER; // Safe default
            }

            // 2. Delegate creation and persistence to the Orchestrator Service
            User newUser = userService.register(role, req.getUsername(), req.getPassword());
            
            // 3. Success response
            sendResponse(new AuthResponse(true, "Đăng ký tài khoản thành công!", newUser));

            logger.info("Registration successful: {}", newUser.getUsername());
        } catch (ValidationException e) {
            // User-friendly validation error
            logger.warn("Registration failed for {}: {}", req.getUsername(), e.getMessage());
            sendResponse(new AuthResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            // System error
            logger.error("System error during registration for {}: {}", req.getUsername(), e.getMessage(), e);
            sendResponse(new AuthResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
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
            synchronized (out) {
                out.writeObject(response);
                out.flush();
                out.reset(); // Crucial: prevents object caching issues in persistent streams
            }
        } catch (IOException e) {
            logger.error("Connection error while sending response: {}", e.getMessage(), e);
        }
    }
}
