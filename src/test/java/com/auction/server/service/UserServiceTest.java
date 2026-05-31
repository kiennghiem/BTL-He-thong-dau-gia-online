package com.auction.server.service;

import com.auction.exceptions.AuthenticationException;
import com.auction.exceptions.ValidationException;
import com.auction.models.Bidder;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.factory.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDAO);
        // Clear online users before each test if possible (it's static)
        // For simplicity in this local project, we'll just use unique names
    }

    @Test
    void testRegisterSuccess() throws Exception {
        String username = "newUser";
        String password = "password123";
        
        when(userDAO.findByUsername(username)).thenReturn(null);

        User result = userService.register(UserRole.BIDDER, username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userDAO, times(1)).addUser(any(User.class));
    }

    @Test
    void testRegisterShortPasswordFails() {
        ValidationException ex = assertThrows(ValidationException.class, () -> 
            userService.register(UserRole.BIDDER, "user", "123")
        );
        assertTrue(ex.getMessage().contains("tối thiểu 6 ký tự"));
    }

    @Test
    void testRegisterDuplicateUsernameFails() {
        String username = "existingUser";
        when(userDAO.findByUsername(username)).thenReturn(new Bidder(username, "pass"));

        ValidationException ex = assertThrows(ValidationException.class, () -> 
            userService.register(UserRole.BIDDER, username, "password123")
        );
        assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        String username = "loginUser" + System.currentTimeMillis(); // Ensure uniqueness
        String password = "password123";
        User mockUser = new Bidder(username, password);
        
        when(userDAO.findByUsername(username)).thenReturn(mockUser);

        User result = userService.login(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertTrue(userService.isUserOnline(username));
    }

    @Test
    void testLoginWrongPasswordFails() {
        String username = "user";
        User mockUser = new Bidder(username, "correctPass");
        
        when(userDAO.findByUsername(username)).thenReturn(mockUser);

        assertThrows(AuthenticationException.class, () -> 
            userService.login(username, "wrongPass")
        );
    }
}
