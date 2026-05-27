package com.auction.client.controller;

import com.auction.exceptions.InvalidSignupException;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.dao.impl.UserDAOImpl;
import com.auction.server.factory.UserFactory;
import com.auction.server.factory.UserRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignupController {

    @FXML
    TextField tfUsername;
    @FXML
    TextField tfPassword;
    @FXML
    Button buttonSignUp;
    @FXML
    RadioButton rbBidder;
    @FXML
    RadioButton rbSeller;
    @FXML
    RadioButton rbAdmin;
    @FXML
    Button buttonLogin;

    private UserDAO userDao = new UserDAOImpl();

    @FXML
    public void handleSignup(ActionEvent event) {
        try {
            String username = tfUsername.getText().trim();
            String password = tfPassword.getText().trim();
            ToggleGroup roleGroup = rbBidder.getToggleGroup();
            RadioButton selectedButton = (RadioButton)roleGroup.getSelectedToggle();
            UserRole roleEnum = null;
            if (selectedButton != null) {
                String role = selectedButton.getText();
                roleEnum = UserRole.valueOf(role.toUpperCase());
            }

            // Check if all information has been filled.
            if (username.isEmpty() || password.isEmpty() || roleEnum == null) {
                throw new InvalidSignupException("Please fill in all information to log in!");
            }

            User existedUser = userDao.findByUsername(username); // Can throw DatabaseException

            if (existedUser != null) {
                throw new InvalidSignupException("You cannot use this username");
            }
            // Create a new user from given information
            User newUser = UserFactory.createNewUser(roleEnum, username, password);
            userDao.addUser(newUser); // Can throw DatabaseException
        // Catch all custom exceptions
        } catch (RuntimeException e) {
            ControllerUtils.showAlert(e.getMessage());
        }
    }

    @FXML
    // Click on "Log in" button will take user to the Login screen.
    public void handleLogin(ActionEvent event) {
        ControllerUtils.changeScene(event, "Login.fxml");
    }
}
