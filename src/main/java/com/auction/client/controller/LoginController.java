package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    TextField usernameLogin;

    @FXML
    PasswordField passwordLogin;

    private Parent root;
    private Scene scene;
    private Stage stage;

    // Handle the texts typed in Username and Password when "Log in" button is clicked
    // This is UNFINISHED until a database with Users is made
    public void handleLogin(ActionEvent event) {
        String username = usernameLogin.getText();
        String password = passwordLogin.getText();


    }

    public void switchToSignup(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/auction/view/Signup.fxml"));
        root = fxmlLoader.load();

        scene = new Scene(root);
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }


}
