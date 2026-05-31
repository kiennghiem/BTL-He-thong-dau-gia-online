package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.User;
import com.auction.models.dto.CancelAuctionRequest;
import com.auction.server.factory.UserRole;
import com.auction.server.observer.AuctionStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Auction Detail View (primarily for Admins).
 * Provides information and management tools like cancellation.
 */
public class AuctionDetailController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionDetailController.class);

    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblPrice;
    @FXML private Label lblStatus;
    @FXML private Label lblEndTime;
    @FXML private Button btnCancel;

    private Auction currentAuction;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void setAuction(Auction auction) {
        if (auction == null) {
            logger.warn("setAuction called with null auction object.");
            return;
        }
        this.currentAuction = auction;
        
        try {
            lblTitle.setText(auction.getTitle() != null ? auction.getTitle() : "N/A");
            lblDescription.setText(auction.getDescription() != null ? auction.getDescription() : "No description.");
            
            BigDecimal price = auction.getCurrentPrice() != null ? auction.getCurrentPrice() : BigDecimal.ZERO;
            lblPrice.setText("Current Price: $" + price);
            
            lblStatus.setText("Status: " + auction.getStatusAsString());
            
            if (auction.getEndTime() != null) {
                lblEndTime.setText("End Time: " + auction.getEndTime().format(FORMATTER));
            } else {
                lblEndTime.setText("End Time: N/A");
            }

            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
                btnCancel.setVisible(true);
                btnCancel.setManaged(true);
                btnCancel.setDisable(auction.getStatus() == AuctionStatus.CANCELED);
            } else {
                btnCancel.setVisible(false);
                btnCancel.setManaged(false);
            }
        } catch (Exception e) {
            logger.error("Error updating Detail UI", e);
        }
    }

    @FXML
    private void handleCancelAuction(ActionEvent event) {
        if (currentAuction == null) return;
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            CancelAuctionRequest request = new CancelAuctionRequest(currentAuction.getId(), currentUser.getId());
            ClientManager.getInstance().sendRequest(request);
            
            // Immediate local feedback
            lblStatus.setText("Status: CANCELED");
            btnCancel.setDisable(true);
            logger.info("Admin canceled auction: {}", currentAuction.getId());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Find the main dashboard container using CSS ID
            BorderPane mainPane = (BorderPane) lblTitle.getScene().lookup("#mainBorderPane");
            if (mainPane != null) {
                Object controller = mainPane.getProperties().get("controller");
                if (controller instanceof AdminMainController) {
                    ((AdminMainController) controller).loadView("AuctionList.fxml", false);
                } else if (controller instanceof SellerMainController) {
                    ((SellerMainController) controller).loadView("AuctionList.fxml", false);
                } else if (controller instanceof BidderMainController) {
                    ((BidderMainController) controller).loadView("AuctionList.fxml", false);
                }
            } else {
                logger.warn("Navigation back failed: #mainBorderPane not found.");
            }
        } catch (Exception e) {
            logger.error("Error during back navigation", e);
        }
    }
}
