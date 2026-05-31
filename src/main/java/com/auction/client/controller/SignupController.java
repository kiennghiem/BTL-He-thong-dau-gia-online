package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.RegisterRequest;
import com.auction.server.factory.UserRole;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            alert.showAndWait();

            // Redirect back to Login screen
            Stage stage = (Stage) buttonSignUp.getScene().getWindow();
            ControllerUtils.changeScene(stage, "Login.fxml");

            // Cleanup listener when leaving
            ClientManager.getInstance().removeMessageListener(responseListener);
        } else {
            ControllerUtils.showAlert(response.getMessage());
        }
    }

    @FXML
    // Click on "Log in" button will take user to the Login screen.
    public void handleLogin(ActionEvent event) {
        ClientManager.getInstance().removeMessageListener(responseListener);
        ControllerUtils.changeScene(event, "Login.fxml");
    }
}
