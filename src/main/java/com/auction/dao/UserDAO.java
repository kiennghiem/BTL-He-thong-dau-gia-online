package main.java.com.auction.dao; // Ensure this matches your folder structure

import main.java.com.auction.models.User; // Import the specific User model

public interface UserDAO {
    /**
     * Finds a user by their username and password.
     */
    User authenticate(String username, String password);

    /**
     * Saves a new user to the database.
     */
    boolean registerUser(User user);
}