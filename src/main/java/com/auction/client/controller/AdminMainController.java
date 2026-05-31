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

/**
 * Main Controller for the Admin Dashboard.
 * Manages view switching and session for administrator users.
 */
public class AdminMainController {

    private static final Logger logger = LoggerFactory.getLogger(AdminMainController.class);

    @FXML
    private BorderPane mainBorderPane;

    private Consumer<Object> responseListener;
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    @FXML
    public void initialize() {
        mainBorderPane.getProperties().put("controller", this);

        // Register listener for server responses (Logout, etc.)
        responseListener = msg -> {
            if (msg instanceof AuthResponse response) {
                Platform.runLater(() -> handleAuthResponse(response));
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);

        // Initial view load
        handleShowAvailableAuctions(null);
    }

    @FXML
    private void handleShowAvailableAuctions(ActionEvent event) {
        loadView("AuctionList.fxml", false);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (currentUser != null) {
            LogoutRequest logoutRequest = new LogoutRequest(currentUser.getUsername());
            ClientManager.getInstance().sendRequest(logoutRequest);
        } else {
            // Safe fallback if session is lost
            SessionManager.getInstance().clearSession();
            Stage stage = (Stage) mainBorderPane.getScene().getWindow();
            ControllerUtils.changeScene(stage, "Login.fxml");
        }
    }

    public void loadView(String fxmlFile, boolean myAuctionsMode) {
        try {
            logger.info("Admin loading view: {}", fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/" + fxmlFile));
            Parent view = loader.load();
            
            if (fxmlFile.equals("AuctionList.fxml")) {
                AuctionListController controller = loader.getController();
                if (controller != null) {
                    controller.setMyAuctionsMode(myAuctionsMode);
                    // CRUCIAL: Pass the Admin's selection handler to the list
                    controller.setOnAuctionSelected(this::handleAuctionSelected);
                    logger.info("Admin selection handler attached to AuctionListController");
                }
            }
            
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            logger.error("Error loading view: {}", fxmlFile, e);
            ControllerUtils.showAlert("Lỗi khi tải giao diện: " + fxmlFile);
        }
    }

    private void handleAuctionSelected(Auction auction) {
        logger.info("Admin selected auction: {}", auction.getId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/AuctionDetail.fxml"));
            Parent view = loader.load();
            
            AuctionDetailController controller = loader.getController();
            if (controller != null) {
                controller.setAuction(auction);
                mainBorderPane.setCenter(view);
                logger.info("Admin navigating to AuctionDetail for auction: {}", auction.getId());
            }
        } catch (IOException e) {
            logger.error("Error loading Auction Detail View", e);
            ControllerUtils.showAlert("Lỗi khi tải Chi tiết phiên đấu giá");
        }
    }

    private void handleAuthResponse(AuthResponse response) {
        // A success AuthResponse with a null user indicates a logout success
        if (response.isSuccess() && response.getUser() == null) {
            logger.info("[LOGOUT] Admin logout success: {}", response.getMessage());

            ClientManager.getInstance().removeMessageListener(responseListener);
            SessionManager.getInstance().clearSession();

            if (mainBorderPane.getScene() != null && mainBorderPane.getScene().getWindow() != null) {
                Stage stage = (Stage) mainBorderPane.getScene().getWindow();
                ControllerUtils.changeScene(stage, "Login.fxml");
            }
        } else if (!response.isSuccess()) {
            ControllerUtils.showAlert(response.getMessage());
        }
    }
}
