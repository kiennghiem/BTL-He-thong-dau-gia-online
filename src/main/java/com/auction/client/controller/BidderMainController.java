package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.Notification;
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

public class BidderMainController {

    @FXML
    private BorderPane mainBorderPane;

    private Consumer<Object> responseListener;
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    @FXML
    public void initialize() {
        mainBorderPane.getProperties().put("controller", this);
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
    private void handleShowMyBids(ActionEvent event) {
        // For now, reuse AuctionList but maybe with a different filter
        // In the future, "My Bids" could show auctions where user has active bids
        loadView("AuctionList.fxml", true); 
    }

    @FXML
    private void handleShowDeposit(ActionEvent event) {
        loadView("DepositView.fxml", false);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (currentUser != null) {
            LogoutRequest logoutRequest = new LogoutRequest(currentUser.getUsername());
            ClientManager.getInstance().sendRequest(logoutRequest);
        } else {
            // If session is already lost, just go to login
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            ControllerUtils.changeScene(stage, "Login.fxml");
        }
    }

    public void loadView(String fxmlFile, boolean myMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/" + fxmlFile));
            Parent view = loader.load();
            
            if (fxmlFile.equals("AuctionList.fxml")) {
                AuctionListController controller = loader.getController();
                controller.setMyAuctionsMode(myMode);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/AuctionBidView.fxml"));
            Parent view = loader.load();
            
            AuctionBidController controller = loader.getController();
            controller.setAuction(auction);
            
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            ControllerUtils.showAlert("Error loading Bid View");
        }
    }

    private void handleAuthResponse(AuthResponse response) {
        if (response.isSuccess() && response.getMessage() != null && response.getMessage().contains("Đăng xuất")) {
            ClientManager.getInstance().removeMessageListener(responseListener);
            SessionManager.getInstance().clearSession();

            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            ControllerUtils.changeScene(stage, "Login.fxml");
        } else if (!response.isSuccess()) {
            ControllerUtils.showAlert(response.getMessage());
        }
    }
}
