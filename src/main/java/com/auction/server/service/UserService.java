package com.auction.server.service;

import com.auction.exceptions.AuthenticationException;
import com.auction.exceptions.InvalidCredentialsException;
import com.auction.exceptions.UserAlreadyLoggedInException;
import com.auction.exceptions.ValidationException;
import com.auction.exceptions.DatabaseException;
import com.auction.exceptions.InvalidDepositException;
import com.auction.exceptions.InvalidWithdrawException;
import com.auction.models.User;
import com.auction.server.database.dao.DAOFactory;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.factory.UserFactory;
import com.auction.server.factory.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserService handles user-related business logic like authentication and session management.
 * Implements the "Orchestrator" pattern to protect the DAO layer.
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    // Set of usernames currently online
    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public UserService() {
        this(DAOFactory.getUserDAO());
    }

    /**
     * Constructor for dependency injection (used in tests).
     */
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user and starts a session.
     */
    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Tên đăng nhập không được để trống.");
        }

        if (onlineUsers.contains(username)) {
            throw new UserAlreadyLoggedInException("Tài khoản " + username + " hiện đang trực tuyến ở nơi khác.");
        }

        try {
            User user = userDAO.findByUsername(username);

            // Verify that the user exists and the password credentials match up
            if (user == null || !password.equals(user.getPassword())) {
                throw new InvalidCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác.");
            }

            onlineUsers.add(username);
            logger.info("User logged in: {}", username);
            return user;
        } catch (DatabaseException e) {
            logger.error("Database access error during login for {}: {}", username, e.getMessage(), e);
            throw new AuthenticationException("Lỗi truy cập cơ sở dữ liệu khi đăng nhập.", e);
        }
    }

    /**
     * Registers a new user with strict validation rules.
     */
    public User register(UserRole role, String username, String password) throws ValidationException {
        // 1. Validation: Không để trống tên đăng nhập
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập không được để trống.");
        }

        // 2. Validation: Mật khẩu tối thiểu 6 ký tự
        if (password == null || password.trim().length() < 6) {
            throw new ValidationException("Mật khẩu phải có tối thiểu 6 ký tự.");
        }

        // 3. Validation: Check if the username already exists
        try {
            if (userDAO.findByUsername(username) != null) {
                throw new ValidationException("Tên đăng nhập đã tồn tại trong hệ thống.");
            }

            // 4. Create the domain model via Factory
            User newUser = UserFactory.createNewUser(role, username, password);

            // 5. Execute registration via DAO
            userDAO.addUser(newUser);
            
            logger.info("User registered successfully: {}", username);
            return newUser;
        } catch (DatabaseException e) {
            logger.error("Database error during registration for {}: {}", username, e.getMessage(), e);
            throw new ValidationException("Lỗi cơ sở dữ liệu khi đăng ký: " + e.getMessage(), e);
        }
    }

    /**
     * Deposits money into a user's account and persists the change.
     */
    public void deposit(String username, BigDecimal amount) throws DatabaseException, InvalidDepositException {
        User user = userDAO.findByUsername(username);
        if (user != null) {
            user.deposit(amount);
            userDAO.updateUser(user);
            logger.info("User {} deposited: {}", username, amount);
        } else {
            logger.warn("User not found for deposit: {}", username);
            throw new DatabaseException("User not found: " + username);
        }
    }

    /**
     * Withdraws money from a user's account and persists the change.
     */
    public void withdraw(String username, BigDecimal amount) throws DatabaseException, InvalidWithdrawException {
        User user = userDAO.findByUsername(username);
        if (user != null) {
            user.withdraw(amount);
            userDAO.updateUser(user);
            logger.info("User {} withdrew: {}", username, amount);
        } else {
            logger.warn("User not found for withdraw: {}", username);
            throw new DatabaseException("User not found: " + username);
        }
    }

    /**
     * Ends a user session.
     */
    public void logout(String username) {
        if (username != null && onlineUsers.remove(username)) {
            logger.info("User logged out: {}", username);
        }
    }

    /**
     * Forcefully ends a user session (e.g., on connection loss).
     */
    public void forceLogout(String username) {
        logout(username);
    }

    public boolean isUserOnline(String username) {
        if (username == null) return false;
        return onlineUsers.contains(username);
    }
}