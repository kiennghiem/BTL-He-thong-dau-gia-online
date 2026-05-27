package com.auction.client.controller;

import com.auction.exceptions.DatabaseException;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.dao.impl.UserDAOImpl;
import com.auction.service.LoginSignupService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

    public void handleLogin(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText().trim();

        try {
            User existedUser = LoginSignupService.login(username, password); // Can throw custom exceptions
            ControllerUtils.changeScene(event, "AuctionList.fxml");

            // Catch all custom exceptions
        } catch (RuntimeException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    // Click on "Sign up" button will take user to the Sign up screen.
    public void handleSignup(ActionEvent event) {
        ControllerUtils.changeScene(event, "Signup.fxml");
    }
}

