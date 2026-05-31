package com.auction.client.util;

import com.auction.models.Bidder;
import com.auction.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();
        sessionManager.clearSession();
    }

    @Test
    void testSetAndGet() {
        User user = new Bidder("user", "pass");
        sessionManager.setCurrentUser(user);
        assertEquals(user, sessionManager.getCurrentUser());
    }

    @Test
    void testClearSession() {
        User user = new Bidder("user", "pass");
        sessionManager.setCurrentUser(user);
        sessionManager.clearSession();
        assertNull(sessionManager.getCurrentUser());
    }
}
