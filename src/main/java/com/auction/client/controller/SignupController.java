package com.auction.client.controller;

import com.auction.server.database.DBLoginSignupUtils;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buttonSignUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = tfUsername.getText().trim();
                String password = tfPassword.getText().trim();
                ToggleGroup toggle = rbBidder.getToggleGroup();
                RadioButton selectedRole = (RadioButton)toggle.getSelectedToggle();

                // Check if all information has been filled.
                if (!username.isEmpty() && !password.isEmpty() && selectedRole != null) {
                    String role = selectedRole.getText();
                    DBLoginSignupUtils.signupUser(event, username, password, role);
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
                DBLoginSignupUtils.changeScene(event, "Login.fxml");
            }
        });
    }
}
