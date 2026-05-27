package com.auction.server.service;

import com.auction.exceptions.AuthenticationException;
import com.auction.exceptions.DatabaseException;
import com.auction.exceptions.InvalidLoginException;
import com.auction.exceptions.InvalidSignupException;
import com.auction.exceptions.ValidationException;
import com.auction.models.User;
import com.auction.server.factory.UserRole;

/**
 * Legacy service used by client controllers.
 * Refactored to delegate to the primary UserService for consistency.
 */
public class LoginSignupService {

    private static final UserService userService = new UserService();

    private LoginSignupService() {}

    /**
     * Method to handle login event.
     */
    public static User login(String username, String password) throws InvalidLoginException, DatabaseException {
        try {
            return userService.login(username, password);
        } catch (AuthenticationException e) {
            throw new InvalidLoginException(e.getMessage());
        } catch (Exception e) {
            throw new DatabaseException("Lỗi hệ thống khi đăng nhập: " + e.getMessage());
        }
    }

    /**
     * Method to handle sign up event.
     */
    public static synchronized void registerUser(String username, String password, UserRole roleEnum) 
            throws InvalidSignupException, DatabaseException {
        try {
            userService.register(roleEnum, username, password);
        } catch (ValidationException e) {
            throw new InvalidSignupException(e.getMessage());
        } catch (Exception e) {
            throw new DatabaseException("Lỗi hệ thống khi đăng ký: " + e.getMessage());
        }
    }
}