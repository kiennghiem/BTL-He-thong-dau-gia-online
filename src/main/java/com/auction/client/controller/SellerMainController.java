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

import javafx.scene.control.Label;
import java.io.IOException;
import java.util.function.Consumer;

public class SellerMainController {

    private static final Logger logger = LoggerFactory.getLogger(SellerMainController.class);

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label lblBalance;

    private Consumer<Object> responseListener;
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    @FXML
    public void initialize() {
        mainBorderPane.getProperties().put("controller", this);
        // Show Available Auctions by default
        handleShowAvailableAuctions(null);
        updateBalanceDisplay();

        // Register listener for server responses
        responseListener = msg -> {
            if (msg instanceof AuthResponse response) {
                Platform.runLater(() -> handleAuthResponse(response));
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);
    }

    public void updateBalanceDisplay() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null && lblBalance != null) {
            lblBalance.setText(String.format("Balance: $%.2f", user.getBalance()));
        }
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
    private void handleShowDeposit(ActionEvent event) {
        loadView("DepositView.fxml", false);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LogoutRequest logoutRequest = new LogoutRequest(currentUser.getUsername());
        ClientManager.getInstance().sendRequest(logoutRequest);
    }

    public void loadView(String fxmlFile, boolean myAuctionsMode) {
        try {
            updateBalanceDisplay();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/" + fxmlFile));
            Parent view = loader.load();
            
            // If it's the AuctionList, set the mode
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
        // A success AuthResponse with a null user indicates a logout success
        if (response.isSuccess() && response.getUser() == null) {
            logger.info("[LOGOUT] Success: " + response.getMessage());

            // Clean up listener and clear session with the current user
            ClientManager.getInstance().removeMessageListener(responseListener);
            SessionManager.getInstance().clearSession();

            if (mainBorderPane.getScene() != null && mainBorderPane.getScene().getWindow() != null) {
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                ControllerUtils.changeScene(stage, "Login.fxml");
            }
        } else if (response.isSuccess() && response.getUser() != null) {
            // If we get a user back, it might have an updated balance
            SessionManager.getInstance().setCurrentUser(response.getUser());
            updateBalanceDisplay();
        } else if (!response.isSuccess()) {
            ControllerUtils.showAlert(response.getMessage());
        }
    }
}
