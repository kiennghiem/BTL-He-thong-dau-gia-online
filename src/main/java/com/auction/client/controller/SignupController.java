package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SignupController {

    private Parent root;
    private Scene scene;
    private Stage stage;

    // Handle the texts typed in Username, Password and the Role chosen when "Sign up" button is clicked
    // This is UNFINISHED until a database with Users is made
    public void handleSignup(ActionEvent event) {


    }

    public void switchToLogin(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/auction/view/Login.fxml"));
        root = fxmlLoader.load();

        scene = new Scene(root);
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
