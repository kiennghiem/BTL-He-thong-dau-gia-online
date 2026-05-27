package com.auction.service;

import com.auction.exceptions.DatabaseException;
import com.auction.exceptions.InvalidLoginException;
import com.auction.exceptions.InvalidSignupException;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.dao.impl.UserDAOImpl;
import com.auction.server.factory.UserFactory;
import com.auction.server.factory.UserRole;

/**
 * This class had methods for handling login and sign up.
 */
public class LoginSignupService {

    private static UserDAO userDao = new UserDAOImpl();

    private LoginSignupService() {}

    /**
     * Method to handle login event.
     * @param username
     * @param password
     * @return a User instance of the user with matching information from the DB
     * @throws InvalidLoginException if username or password is empty, user not found, or wrong password
     * @throws DatabaseException if connection failure occcurs
     */
    public static User login(String  username, String password) throws InvalidLoginException, DatabaseException {

        // Check if all information has been filled.
        if (!username.isEmpty() && !password.isEmpty()) {

            User existedUser = userDao.findByUsername(username); // Can throw DatabaseException

            if (existedUser != null) {
                // Check if password is correct
                if (password.equals(existedUser.getPassword())) {
                    return existedUser;
                } else {
                    throw new InvalidLoginException("Provided credentials are incorrect");
                }
            } else {
                throw new InvalidLoginException("Provided credentials are incorrect");
            }
        } else {
            throw new InvalidLoginException("Please fill in all information to log in!");
        }
    }

    /**
     * Method to handle sign up event, use synchronized to prevent duplicate username in DB if two people
     * sign up at the same time with the same username.
     * @param username
     * @param password
     * @param roleEnum
     * @throws InvalidSignupException if username or password or role is empty, or username already existed
     * @throws DatabaseException if connection failure occcurs
     */
    public static synchronized void registerUser(String username, String password, UserRole roleEnum) throws InvalidSignupException, DatabaseException {

        // Check if all information has been filled.
        if (!username.isEmpty() && !password.isEmpty() && roleEnum != null) {

            User existedUser = userDao.findByUsername(username); // Can throw DatabaseException

            if (existedUser == null) {
                // Create a new user from given information
                User newUser = UserFactory.createNewUser(roleEnum, username, password);
                userDao.addUser(newUser); // Can throw DatabaseException
            } else {
                throw new InvalidSignupException("You cannot use this username");
            }
        } else {
            throw new InvalidSignupException("Please fill in all information to log in!");
        }
    }
}
