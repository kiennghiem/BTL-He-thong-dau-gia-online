package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.User;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.LogoutRequest;
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

public class AdminMainController {

    private static final Logger logger = LoggerFactory.getLogger(AdminMainController.class);

    @FXML
    private BorderPane mainBorderPane;

    private Consumer<Object> responseListener;
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    @FXML
    public void initialize() {
        mainBorderPane.getProperties().put("controller", this);

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
    private void handleLogout(ActionEvent event) {
        LogoutRequest logoutRequest = new LogoutRequest(currentUser.getUsername());
        ClientManager.getInstance().sendRequest(logoutRequest);
    }

    public void loadView(String fxmlFile, boolean myAuctionsMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/" + fxmlFile));
            Parent view = loader.load();
            
            if (fxmlFile.equals("AuctionList.fxml")) {
                AuctionListController controller = loader.getController();
                controller.setMyAuctionsMode(myAuctionsMode);
                controller.setOnAuctionSelected(this::handleAuctionSelected);
            }
            
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            logger.error("Error loading view: " + fxmlFile, e);
            ControllerUtils.showAlert("Error loading view: " + fxmlFile);
        }
    }

    private void handleAuctionSelected(Auction auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/AuctionDetail.fxml"));
            Parent view = loader.load();
            
            AuctionDetailController controller = loader.getController();
            controller.setAuction(auction);
            
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            logger.error("Error loading Auction Detail View", e);
            ControllerUtils.showAlert("Error loading Auction Detail View");
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
