package com.auction.client.controller;

import com.auction.exceptions.DatabaseException;
import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.dao.impl.UserDAOImpl;
import com.auction.server.factory.UserFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

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
        RadioButton selectedRole = (RadioButton)roleGroup.getSelectedToggle();

        // Check if all information has been filled.
        if (!username.isEmpty() && !password.isEmpty() && selectedRole != null) {
            String role = selectedRole.getText();
            User newUser = UserFactory.createUser(username, password, role);

            try {
                userDao.registerUser(newUser);
                ControllerUtils.changeScene(event, "AuctionList.fxml");
            } catch (DatabaseException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please fill in all information to sign up!");
            alert.show();
        }
    }

    // Click on "Log in" button will take user to the Login screen.
    public void handleLogin(ActionEvent event) {
        ControllerUtils.changeScene(event, "Login.fxml");
    }
}
