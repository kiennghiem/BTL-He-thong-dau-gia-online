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

import javafx.scene.control.Label;
import com.auction.server.observer.AuctionStatus;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class BidderMainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label lblBalance;

    private Consumer<Object> responseListener;
    private User currentUser = SessionManager.getInstance().getCurrentUser();
    private boolean initialWinCheckDone = false;

    @FXML
    public void initialize() {
        mainBorderPane.getProperties().put("controller", this);

        // Register listener for server responses FIRST
        responseListener = msg -> {
            if (msg instanceof AuthResponse response) {
                Platform.runLater(() -> handleAuthResponse(response));
            } else if (msg instanceof List<?> list && !initialWinCheckDone) {
                if (!list.isEmpty() && list.get(0) instanceof Auction) {
                    @SuppressWarnings("unchecked")
                    List<Auction> auctions = (List<Auction>) list;
                    Platform.runLater(() -> checkForWins(auctions));
                }
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);

        // Show Available Auctions by default
        handleShowAvailableAuctions(null);
        updateBalanceDisplay();
    }

    private void checkForWins(List<Auction> auctions) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (initialWinCheckDone || user == null) return;
        
        // Find the first auction where this user is the winner and it's waiting for payment
        Auction wonAuction = auctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.FINISHED && user.getId().equals(a.getHighestBidderId()))
                .findFirst()
                .orElse(null);
                
        if (wonAuction != null) {
            initialWinCheckDone = true;
            handleAuctionSelected(wonAuction);
            ControllerUtils.showSuccess("Chúc mừng!", "Bạn đã thắng cuộc trong phiên đấu giá: " + wonAuction.getTitle() + "\nHãy thực hiện thanh toán ngay.");
        }
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
        } else if (mainBorderPane.getScene() != null && mainBorderPane.getScene().getWindow() != null) {
            // If session is already lost, just go to login
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            ControllerUtils.changeScene(stage, "Login.fxml");
        }
    }

    public void loadView(String fxmlFile, boolean myMode) {
        try {
            updateBalanceDisplay();
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
        // A success AuthResponse with a null user indicates a logout success
        if (response.isSuccess() && response.getUser() == null) {
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
