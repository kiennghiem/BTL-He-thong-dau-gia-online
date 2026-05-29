package com.auction.client.controller;

import com.auction.client.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SellerMainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        // Show Available Auctions by default
        handleShowAvailableAuctions(null);
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
        SessionManager.getInstance().clearSession();
        ControllerUtils.changeScene((Stage) mainBorderPane.getScene().getWindow(), "Login.fxml");
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
            e.printStackTrace();
            ControllerUtils.showAlert("Error loading view: " + fxmlFile);
        }
    }
}
