package com.auction.server.factory;

import com.auction.models.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserFactoryTest {

    @Test
    void testCreateBidder() {
        User user = UserFactory.createNewUser(UserRole.BIDDER, "bidder", "pass");
        assertTrue(user instanceof Bidder);
        assertEquals(UserRole.BIDDER, user.getRole());
    }

    @Test
    void testCreateSeller() {
        User user = UserFactory.createNewUser(UserRole.SELLER, "seller", "pass");
        assertTrue(user instanceof Seller);
        assertEquals(UserRole.SELLER, user.getRole());
    }

    @Test
    void testCreateAdmin() {
        User user = UserFactory.createNewUser(UserRole.ADMIN, "admin", "pass");
        assertTrue(user instanceof Admin);
        assertEquals(UserRole.ADMIN, user.getRole());
    }
}
