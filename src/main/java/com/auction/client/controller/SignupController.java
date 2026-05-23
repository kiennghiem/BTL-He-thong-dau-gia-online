package com.auction.client.controller;

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

public class SignupController implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buttonSignUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = tfUsername.getText().trim();
                String password = tfPassword.getText().trim();
                ToggleGroup roleGroup = rbBidder.getToggleGroup();
                RadioButton selectedRole = (RadioButton)roleGroup.getSelectedToggle();

                // Check if all information has been filled.
                if (!username.isEmpty() && !password.isEmpty() && selectedRole != null) {
                    String role = selectedRole.getText();

                    User existedUser = userDao.authenticate(username, password);

                    if (existedUser == null) {
                        User newUser = UserFactory.createUser(username, password, role);
                        userDao.registerUser(newUser);
                        ControllerUtils.changeScene(event, "AuctionList.fxml");
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("You can not use this username");
                        alert.show();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Please fill in all information to sign up!");
                    alert.show();
                }
            }
        });

        // Click on "Log in" button will take user to the Login screen.
        buttonLogin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ControllerUtils.changeScene(event, "Login.fxml");
            }
        });
    }
}
