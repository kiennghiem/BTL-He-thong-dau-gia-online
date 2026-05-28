package com.auction.client.controller;

import com.auction.models.Auction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionListController {

    @FXML private Button buttonLogout;
    @FXML private Button btnCreateAuction;

    // Table UI Elements
    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, String> colTitle;
    @FXML private TableColumn<Auction, BigDecimal> colPrice;
    @FXML private TableColumn<Auction, String> colStatus;
    @FXML private TableColumn<Auction, LocalDateTime> colEndTime;

    // This method runs automatically when the screen loads
    @FXML
    public void initialize() {
        // 1. Tell the columns which variables to look for in your Auction model
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // 2. Load the data
        refreshAuctionList();
    }

    public void refreshAuctionList() {
        // TODO: Send a network request to your server asking for all active auctions.
        // For example: NetworkClient.send(new NetworkMessage(AppConstants.GET_ALL_AUCTIONS));
        // List<Auction> auctionsFromServer = ... (receive from server)

        // --- TEMPORARY DUMMY DATA SO YOU CAN SEE THE UI WORK ---
        ObservableList<Auction> dummyData = FXCollections.observableArrayList();

        Auction dummy1 = new Auction();
        dummy1.setTitle("Vintage Rolex Watch");
        dummy1.setCurrentPrice(new BigDecimal("1500.00"));
        dummy1.setStatus("RUNNING");
        dummy1.setEndTime(LocalDateTime.now().plusDays(2));

        dummyData.add(dummy1);

        // Put the data into the table
        auctionTable.setItems(dummyData);
    }

    public void handleLogout(ActionEvent event) {
        ControllerUtils.changeScene(event, "Login.fxml");
    }

    public void handleGoToSellerDashboard(ActionEvent event) {
        ControllerUtils.changeScene(event, "SellerDashboard.fxml");
    }
}