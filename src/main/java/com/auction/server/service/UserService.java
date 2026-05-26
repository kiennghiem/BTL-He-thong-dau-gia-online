package com.auction.server.service;

import com.auction.exceptions.AuthenticationException;
import com.auction.exceptions.InvalidCredentialsException;
import com.auction.exceptions.UserAlreadyLoggedInException;
import com.auction.exceptions.ValidationException;
import com.auction.models.User;
import com.auction.server.database.dao.DAOFactory;
import com.auction.server.database.dao.UserDAO;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserService handles user-related business logic like authentication and session management.
 * Implements the "Orchestrator" pattern to protect the DAO layer.
 */
public class UserService {
    private final UserDAO userDAO;
    
    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public UserService() {
        this.userDAO = DAOFactory.getUserDAO();
    }

    public User login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Tên đăng nhập không được để trống.");
        }

        if (onlineUsers.contains(username)) {
            throw new UserAlreadyLoggedInException("Tài khoản " + username + " hiện đang trực tuyến ở nơi khác.");
        }

        User user = userDAO.login(username, password);
        
        if (user == null) {
            throw new InvalidCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        onlineUsers.add(username);
        System.out.println("[UserService] User logged in: " + username);
        return user;
    }

    /**
     * Registers a new user with strict validation rules.
     * Following the "Nhạc trưởng" pattern as requested.
     */
    public void register(User user, String password) throws ValidationException, Exception {
        // 1. Validation: Không để trống tên đăng nhập
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new ValidationException("Tên đăng nhập không được để trống.");
        }

        // 2. Validation: Mật khẩu tối thiểu 6 ký tự
        if (password == null || password.trim().length() < 6) {
            throw new ValidationException("Mật khẩu phải có tối thiểu 6 ký tự.");
        }

        // 3. Validation: Kiểm tra trùng lặp username
        if (userDAO.checkUserExists(user.getUsername())) {
            throw new ValidationException("Tên đăng nhập đã tồn tại trong hệ thống.");
        }

        // 4. Khởi tạo đối tượng và lưu trữ bền vững (Gọi tầng DAO)
        boolean success = userDAO.register(user, password);
        if (!success) {
            throw new Exception("Lỗi hệ thống khi đăng ký. Vui lòng thử lại sau.");
        }
        
        System.out.println("[UserService] User registered successfully: " + user.getUsername());
    }

    public void logout(String username) {
        if (username != null && onlineUsers.remove(username)) {
            System.out.println("[UserService] User logged out: " + username);
        }
    }

    public void forceLogout(String username) {
        logout(username);
    }
    
    public boolean isUserOnline(String username) {
        if (username == null) return false;
        return onlineUsers.contains(username);
    }
}
