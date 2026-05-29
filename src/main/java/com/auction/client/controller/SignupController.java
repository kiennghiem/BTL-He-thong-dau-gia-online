package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.RegisterRequest;
import com.auction.server.factory.UserRole;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.function.Consumer;

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

    private Consumer<Object> responseListener;

    public void initialize() {
        // Register listener for server responses
        responseListener = msg -> {
            if (msg instanceof AuthResponse response) {
                Platform.runLater(() -> handleAuthResponse(response));
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);
    }

    public void handleSignup(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText().trim();
        ToggleGroup roleGroup = rbBidder.getToggleGroup();
        RadioButton selectedButton = (RadioButton)roleGroup.getSelectedToggle();
        
        if (username.isEmpty() || password.isEmpty() || selectedButton == null) {
            ControllerUtils.showAlert("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        String role = selectedButton.getText().toUpperCase();

        // Send register request to server
        RegisterRequest registerRequest = new RegisterRequest(username, password, role);
        ClientManager.getInstance().sendRequest(registerRequest);
    }

    private void handleAuthResponse(AuthResponse response) {
        if (response.isSuccess()) {
            System.out.println("[SIGNUP] Success: " + response.getMessage());
            
            // Navigate to Auction List (or Login)
            javafx.stage.Stage stage = (javafx.stage.Stage) buttonSignUp.getScene().getWindow();
            ControllerUtils.changeScene(stage, "AuctionList.fxml");

            // Cleanup listener
            ClientManager.getInstance().removeMessageListener(responseListener);
        } else {
            ControllerUtils.showAlert(response.getMessage());
        }
    }

    // Click on "Log in" button will take user to the Login screen.
    public void handleLogin(ActionEvent event) {
        ClientManager.getInstance().removeMessageListener(responseListener);
        ControllerUtils.changeScene(event, "Login.fxml");
    }
}
