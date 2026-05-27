package com.auction.server.network;

import com.auction.exceptions.AuthenticationException;
import com.auction.exceptions.ValidationException;
import com.auction.models.*;
import com.auction.server.service.UserService;
import com.auction.models.dto.LoginRequest;
import com.auction.models.dto.LogoutRequest;
import com.auction.models.dto.RegisterRequest;
import com.auction.models.dto.AppConstants;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.NetworkMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * UserHandler manages authentication and user-profile communication.
 */
public class UserHandler {
    private final ObjectOutputStream out;
    private final UserService userService;
    private String currentUsername;

    public UserHandler(ObjectOutputStream out) {
        this.out = out;
        this.userService = new UserService();
    }

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
            AuthResponse response = new AuthResponse(true, "Đăng nhập thành công!", user.getRole());
            sendResponse(response);
        } catch (AuthenticationException e) {
            sendResponse(new AuthResponse(false, e.getMessage(), null));
        }
    }

    private void handleLogout(LogoutRequest req) {
        userService.logout(req.getUsername());
        if (req.getUsername().equals(currentUsername)) {
            currentUsername = null;
        }
        sendResponse(new AuthResponse(true, "Đăng xuất thành công", null));
    }

    private void handleRegister(RegisterRequest req) {
        try {
            // Determine user type based on role in request
            User newUser;
            String role = req.getRole();
            if (AppConstants.ROLE_SELLER.equalsIgnoreCase(role)) {
                newUser = new Seller(req.getUsername(), req.getPassword());
            } else if (AppConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                newUser = new Admin(req.getUsername(), req.getPassword());
            } else {
                newUser = new Bidder(req.getUsername(), req.getPassword());
            }

            // Call the Orchestrator Service
            userService.register(newUser, req.getPassword());
            
            // Success response
            sendResponse(new AuthResponse(true, "Đăng ký tài khoản thành công!", newUser.getRole()));

        } catch (ValidationException e) {
            // User-friendly validation error
            sendResponse(new AuthResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            // System error
            sendResponse(new AuthResponse(false, "Lỗi hệ thống: " + e.getMessage(), null));
        }
    }

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
            }
        } catch (IOException e) {
            System.err.println("[UserHandler] Connection error: " + e.getMessage());
        }
    }
}
