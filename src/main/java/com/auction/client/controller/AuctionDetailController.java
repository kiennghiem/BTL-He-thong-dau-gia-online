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
import javafx.stage.Stage;

public class AuctionDetailController {

    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblPrice;
    @FXML private Label lblStatus;
    @FXML private Label lblEndTime;
    @FXML private Button btnCancel;

    private Auction currentAuction;

    public void setAuction(Auction auction) {
        this.currentAuction = auction;
        lblTitle.setText(auction.getTitle());
        lblDescription.setText(auction.getDescription());
        lblPrice.setText(auction.getCurrentPrice().toString());
        lblStatus.setText(auction.getStatusAsString());
        lblEndTime.setText(auction.getEndTime().toString());
        lblPrice.setText("Current Price: $" + auction.getCurrentPrice().toString());
        lblStatus.setText("Status: " + auction.getStatusAsString());
        lblEndTime.setText("End Time: " + auction.getEndTime().toString());

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            btnCancel.setVisible(true);
        }
    }

    @FXML
    private void handleCancelAuction(ActionEvent event) {
        if (currentAuction == null) return;
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            CancelAuctionRequest request = new CancelAuctionRequest(currentAuction.getId(), currentUser.getId());
            ClientManager.getInstance().sendRequest(request);
            
            // Optionally update UI immediately or wait for broadcast
            lblStatus.setText(AuctionStatus.CANCELED.toString());
            // UI Feedback
            lblStatus.setText("Status: CANCELED");
            btnCancel.setDisable(true);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            javafx.scene.layout.BorderPane mainPane = (javafx.scene.layout.BorderPane) lblTitle.getScene().lookup("#mainBorderPane");
            if (mainPane != null) {
                Object controller = mainPane.getProperties().get("controller");
                if (controller instanceof AdminMainController) {
                    ((AdminMainController) controller).loadView("AuctionList.fxml");
                } else if (controller instanceof SellerMainController) {
                    ((SellerMainController) controller).loadView("AuctionList.fxml", false);
                } else if (controller instanceof BidderMainController) {
                    ((BidderMainController) controller).loadView("AuctionList.fxml", false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            if (currentUser.getRole() == UserRole.ADMIN) {
                ControllerUtils.changeScene(stage, "AdminMainView.fxml");
            } else if (currentUser.getRole() == UserRole.SELLER) {
                ControllerUtils.changeScene(stage, "SellerMainView.fxml");
            } else {
                ControllerUtils.changeScene(stage, "AuctionList.fxml");
            }
        }
    }
}
