package com.auction.server.network;

import com.auction.exceptions.AuthenticationException;
import com.auction.exceptions.ValidationException;
import com.auction.models.User;
import com.auction.models.dto.*;
import com.auction.server.service.UserService;
import com.auction.server.factory.UserRole;
import com.auction.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * UserHandler manages authentication and user-profile communication via JSON.
 */
public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
    private final PrintWriter out;
    private final UserService userService;
    private String currentUsername;

    private User currentUser;

    public UserHandler(PrintWriter out) {
        this.out = out;
        this.userService = new UserService();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User handleMessage(NetworkMessage message) {
        if (message instanceof LoginRequest loginReq) {
            return handleLogin(loginReq);
        } else if (message instanceof LogoutRequest logoutReq) {
            handleLogout(logoutReq);
        } else if (message instanceof RegisterRequest registerReq) {
            handleRegister(registerReq);
        } else if (message instanceof DepositRequest depositReq) {
            handleDeposit(depositReq);
        }
        return null;
    }

    private void handleDeposit(DepositRequest req) {
        try {
            userService.deposit(req.getUsername(), req.getAmount());
            com.auction.server.database.dao.UserDAO userDAO = com.auction.server.database.dao.DAOFactory.getUserDAO();
            User updatedUser = userDAO.findByUsername(req.getUsername());
            this.currentUser = updatedUser; 
            
            sendResponse(new AuthResponse(true, "DEPOSIT_SUCCESS: " + updatedUser.getBalance(), updatedUser));
            logger.info("Virtual deposit successful for {}: +${}", req.getUsername(), req.getAmount());
        } catch (Exception e) {
            logger.error("Deposit failed for user {}", req.getUsername(), e);
            sendResponse(new AuthResponse(false, "Lỗi nạp tiền: " + e.getMessage(), null));
        }
    }

    private User handleLogin(LoginRequest req) {
        try {
            User user = userService.login(req.getUsername(), req.getPassword());
            this.currentUser = user;
            this.currentUsername = user.getUsername();
            
            AuthResponse response = new AuthResponse(true, "Đăng nhập thành công!", user);
            sendResponse(response);
            
            logger.info("Login successful for: {}", currentUsername);
            return user;
        } catch (AuthenticationException e) {
            logger.warn("Login failed for {}: {}", req.getUsername(), e.getMessage());
            sendResponse(new AuthResponse(false, e.getMessage(), null));
            return null;
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
            UserRole role;
            try {
                role = UserRole.valueOf(req.getRole().toUpperCase());
            } catch (Exception e) {
                role = UserRole.BIDDER;
            }

            User newUser = userService.register(role, req.getUsername(), req.getPassword());
            sendResponse(new AuthResponse(true, "Đăng ký tài khoản thành công!", newUser));
            logger.info("Registration successful: {}", newUser.getUsername());
        } catch (ValidationException e) {
            logger.warn("Registration failed for {}: {}", req.getUsername(), e.getMessage());
            sendResponse(new AuthResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("System error during registration for {}: {}", req.getUsername(), e.getMessage(), e);
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
        String json = JsonUtil.toJson(response);
        if (json != null) {
            synchronized (out) {
                out.println(json);
            }
        }
    }
}
