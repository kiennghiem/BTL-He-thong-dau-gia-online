package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.User;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.LoginRequest;
import com.auction.server.factory.UserRole;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class LoginController {

    @FXML
    TextField tfUsername;
    @FXML
    TextField tfPassword;
    @FXML
    Button buttonLogin;
    @FXML
    Button buttonSignUp;

    private Consumer<Object> responseListener;

    @FXML
    public void initialize() {
        // Register listener for server responses
        responseListener = msg -> {
            if (msg instanceof AuthResponse response) {
                Platform.runLater(() -> handleAuthResponse(response));
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            ControllerUtils.showAlert("Tên đăng nhập và mật khẩu không được để trống!");
            return;
        }

        // Send login request to server
        LoginRequest loginRequest = new LoginRequest(username, password);
        ClientManager.getInstance().sendRequest(loginRequest);
    }

    private void handleAuthResponse(AuthResponse response) {
        if (response.isSuccess()) {
            System.out.println("[LOGIN] Success: " + response.getMessage());
            
            // Save user to session
            SessionManager.getInstance().setCurrentUser(response.getUser());
            
            Stage stage = (Stage) buttonLogin.getScene().getWindow();
            
            if (response.getUser().getRole() == UserRole.SELLER) {
                ControllerUtils.changeScene(stage, "SellerMainView.fxml");
            } else if (response.getUser().getRole() == UserRole.BIDDER) {
                ControllerUtils.changeScene(stage, "BidderMainView.fxml");
            } else if (response.getUser().getRole() == UserRole.ADMIN) {
                ControllerUtils.changeScene(stage, "AdminMainView.fxml");
            }

            // Cleanup listener when leaving
            ClientManager.getInstance().removeMessageListener(responseListener);
        } else {
            ControllerUtils.showAlert(response.getMessage());
        }
    }

    @FXML
    // Click on "Sign up" button will take user to the Sign up screen.
    public void handleSignup(ActionEvent event) {
        ClientManager.getInstance().removeMessageListener(responseListener);
        ControllerUtils.changeScene(event, "Signup.fxml");
    }
}

