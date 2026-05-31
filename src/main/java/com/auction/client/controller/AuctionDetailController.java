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
    @FXML private Button btnApprove;

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
                // Admin can Cancel any auction that is not already canceled/finished
                btnCancel.setVisible(true);
                btnCancel.setManaged(true);
                btnCancel.setDisable(auction.getStatus() == AuctionStatus.CANCELED || auction.getStatus() == AuctionStatus.FINISHED);

                // Admin can Approve only PENDING auctions
                boolean isPending = auction.getStatus() == AuctionStatus.PENDING;
                btnApprove.setVisible(isPending);
                btnApprove.setManaged(isPending);
            } else {
                btnCancel.setVisible(false);
                btnCancel.setManaged(false);
                btnApprove.setVisible(false);
                btnApprove.setManaged(false);
            }
        } catch (Exception e) {
            logger.error("Error updating Detail UI", e);
        }
    }

    @FXML
    private void handleApproveAuction(ActionEvent event) {
        if (currentAuction == null) return;

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            com.auction.models.dto.ApproveAuctionRequest request = 
                new com.auction.models.dto.ApproveAuctionRequest(currentAuction.getId(), currentUser.getId());
            ClientManager.getInstance().sendRequest(request);

            // Immediate local feedback
            lblStatus.setText("Status: OPEN (Approved)");
            btnApprove.setVisible(false);
            btnApprove.setManaged(false);
            logger.info("Admin approved auction: {}", currentAuction.getId());
        }
    }

    @FXML
    private void handleCancelAuction(ActionEvent event) {
        if (currentAuction == null) return;
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            // Show dialog to input reason
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Hủy phiên đấu giá");
            dialog.setHeaderText("Lý do hủy phiên đấu giá: " + currentAuction.getTitle());
            dialog.setContentText("Vui lòng nhập lý do:");

            dialog.showAndWait().ifPresent(reason -> {
                if (reason.trim().isEmpty()) {
                    ControllerUtils.showError("Lỗi", "Lý do không được để trống!");
                    return;
                }

                CancelAuctionRequest request = new CancelAuctionRequest(currentAuction.getId(), currentUser.getId(), reason);
                ClientManager.getInstance().sendRequest(request);
                
                // Immediate local feedback
                lblStatus.setText("Status: CANCELED");
                btnCancel.setDisable(true);
                logger.info("Admin canceled auction: {} for reason: {}", currentAuction.getId(), reason);
            });
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
