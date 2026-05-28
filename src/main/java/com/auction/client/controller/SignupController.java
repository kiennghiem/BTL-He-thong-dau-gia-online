package com.auction.client.controller;

import com.auction.exceptions.DatabaseException;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.dao.impl.UserDAOImpl;
import com.auction.server.factory.UserFactory;
import com.auction.server.factory.UserRole;
import com.auction.server.service.LoginSignupService;
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

    public void handleSignup(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText().trim();
        ToggleGroup roleGroup = rbBidder.getToggleGroup();
        RadioButton selectedButton = (RadioButton)roleGroup.getSelectedToggle();
        UserRole roleEnum = null;
        if (selectedButton != null) {
            String role = selectedButton.getText();
            roleEnum = UserRole.valueOf(role.toUpperCase());
        }

        try {
            LoginSignupService.registerUser(username, password, roleEnum); // Can throw custom exceptions
            ControllerUtils.changeScene(event, "AuctionList.fxml");

            // Catch all custom exceptions
        } catch (RuntimeException e) {
            ControllerUtils.showAlert(e.getMessage());
        }
    }

    // Click on "Log in" button will take user to the Login screen.
    public void handleLogin(ActionEvent event) {
        ControllerUtils.changeScene(event, "Login.fxml");
    }
}
