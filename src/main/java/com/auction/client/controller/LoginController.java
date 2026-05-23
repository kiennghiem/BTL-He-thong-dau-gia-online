package com.auction.client.controller;

import com.auction.models.User;
import com.auction.server.database.dao.UserDAO;
import com.auction.server.database.dao.impl.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    TextField tfUsername;
    @FXML
    TextField tfPassword;
    @FXML
    Button buttonLogin;
    @FXML
    Button buttonSignUp;

    private UserDAO userDao = new UserDAOImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buttonLogin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = tfUsername.getText().trim();
                String password = tfPassword.getText().trim();

                // Check if all information has been filled.
                if (!username.isEmpty() && !password.isEmpty()) {

                    User loggedInUser = userDao.authenticate(username, password);

                    if (loggedInUser != null) {
                        ControllerUtils.changeScene(event, "AuctionList.fxml");
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Provided credentials are incorrect");
                        alert.show();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Please fill in all information to log in!");
                    alert.show();
                }
            }
        });

        // Click on "Sign up" button will take user to the Sign up screen.
        buttonSignUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ControllerUtils.changeScene(event, "Signup.fxml");
            }
        });
    }
}
