package com.auction.client.controller;

import com.auction.exceptions.InvalidLoginException;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.dao.impl.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    TextField tfUsername;
    @FXML
    TextField tfPassword;
    @FXML
    Button buttonLogin;
    @FXML
    Button buttonSignUp;

    private UserDAO userDao = new UserDAOImpl();

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            String username = tfUsername.getText().trim();
            String password = tfPassword.getText().trim();

            // Check if all information has been filled.
            if (username.isEmpty() || password.isEmpty()) {
                throw new InvalidLoginException("Please fill in all information to log in!");
            }

            User existedUser = userDao.findByUsername(username); // Can throw DatabaseException

            if (existedUser == null) {
                throw new InvalidLoginException("Provided credentials are incorrect");
            }
            // Check if password is correct
            if (!password.equals(existedUser.getPassword())) {
                throw new InvalidLoginException("Provided credentials are incorrect");
            }
            ControllerUtils.changeScene(event, "AuctionList.fxml");
        // Catch all custom exceptions
        } catch (RuntimeException e) {
            ControllerUtils.showAlert(e.getMessage());
        }
    }

    @FXML
    // Click on "Sign up" button will take user to the Sign up screen.
    public void handleSignup(ActionEvent event) {
        ControllerUtils.changeScene(event, "Signup.fxml");
    }
}

