package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.User;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.LogoutRequest;
import com.mysql.cj.xdevapi.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class SellerMainController {

    private static final Logger logger = LoggerFactory.getLogger(SellerMainController.class);

    @FXML
    private BorderPane mainBorderPane;

    private Consumer<Object> responseListener;
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    @FXML
    public void initialize() {
        // Show Available Auctions by default
        handleShowAvailableAuctions(null);

        // Register listener for server responses
        responseListener = msg -> {
            if (msg instanceof AuthResponse response) {
                Platform.runLater(() -> handleAuthResponse(response));
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);
    }

    @FXML
    private void handleShowAvailableAuctions(ActionEvent event) {
        loadView("AuctionList.fxml", false);
    }

    @FXML
    private void handleShowMyAuctions(ActionEvent event) {
        loadView("AuctionList.fxml", true);
    }

    @FXML
    private void handleShowCreateAuction(ActionEvent event) {
        loadView("SellerCreateAuction.fxml", false);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LogoutRequest logoutRequest = new LogoutRequest(currentUser.getUsername());
        ClientManager.getInstance().sendRequest(logoutRequest);
    }

    private void loadView(String fxmlFile, boolean myAuctionsMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/" + fxmlFile));
            Parent view = loader.load();
            
            // If it's the AuctionList, set the mode
            if (fxmlFile.equals("AuctionList.fxml")) {
                AuctionListController controller = loader.getController();
                controller.setMyAuctionsMode(myAuctionsMode);
            }
            
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            logger.error("Error loading view: " + fxmlFile, e);
            ControllerUtils.showAlert("Error loading view: " + fxmlFile);
        }
    }

    private void handleAuthResponse(AuthResponse response) {
        if (response.isSuccess()) {
            logger.info("[LOGOUT] Success: " + response.getMessage());

            // Clean up listener and clear session with the current user
            ClientManager.getInstance().removeMessageListener(responseListener);
            SessionManager.getInstance().clearSession();

            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            ControllerUtils.changeScene(stage, "Login.fxml");
        } else {
            ControllerUtils.showAlert(response.getMessage());
        }
    }
}
