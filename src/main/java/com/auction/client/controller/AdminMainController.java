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

import java.io.IOException;
import java.util.function.Consumer;

public class AdminMainController {

    @FXML
    private BorderPane mainBorderPane;

    private Consumer<Object> responseListener;
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    @FXML
    public void initialize() {
        mainBorderPane.getProperties().put("controller", this);
        // Show Auctions by default
        handleShowAuctions(null);

        // Register listener for server responses
        responseListener = msg -> {
            if (msg instanceof AuthResponse response) {
                Platform.runLater(() -> handleAuthResponse(response));
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);
    }

    @FXML
    private void handleShowAuctions(ActionEvent event) {
        loadView("AuctionList.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LogoutRequest logoutRequest = new LogoutRequest(currentUser.getUsername());
        ClientManager.getInstance().sendRequest(logoutRequest);
    }

    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/" + fxmlFile));
            Parent view = loader.load();
            
            if (fxmlFile.equals("AuctionList.fxml")) {
                AuctionListController controller = loader.getController();
                controller.setMyAuctionsMode(false);
                controller.setOnAuctionSelected(this::handleAuctionSelected);
            }
            
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            ControllerUtils.showAlert("Error loading Detail View");
        }
    }

    private void handleAuthResponse(AuthResponse response) {
        if (response.isSuccess()) {
            ClientManager.getInstance().removeMessageListener(responseListener);
            SessionManager.getInstance().clearSession();

            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            ControllerUtils.changeScene(stage, "Login.fxml");
        } else {
            ControllerUtils.showAlert(response.getMessage());
        }
    }
}
