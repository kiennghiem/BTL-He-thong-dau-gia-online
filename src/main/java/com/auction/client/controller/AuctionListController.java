package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.Seller;
import com.auction.models.User;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.GenericResponse;
import com.auction.models.dto.GetActiveAuctionsRequest;
import com.auction.server.factory.UserFactory;
import com.auction.server.factory.UserRole;
import com.auction.server.observer.AuctionStatus;
import javafx.application.Platform;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AuctionListController {

    // Table UI Elements
    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, String> colTitle;
    @FXML private TableColumn<Auction, BigDecimal> colPrice;
    @FXML private TableColumn<Auction, String> colStatus;
    @FXML private TableColumn<Auction, LocalDateTime> colEndTime;

    private Consumer<Object> responseListener;
    private boolean myAuctionsMode = false;

    public void setMyAuctionsMode(boolean myAuctionsMode) {
        this.myAuctionsMode = myAuctionsMode;
        refreshAuctionList();
    }

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
        if (responseListener == null) {
            responseListener = msg -> {
                if (msg instanceof List<?> list) {
                    try {
                        // Attempt to cast to List<Auction>
                        @SuppressWarnings("unchecked")
                        List<Auction> auctions = (List<Auction>) list;
                        Platform.runLater(() -> updateTableData(auctions));
                    } catch (ClassCastException e) {
                        System.err.println("[AuctionList] Received list was not a List<Auction>");
                    }
                }
            };
            ClientManager.getInstance().addMessageListener(responseListener);
        }

        // Send a network request to your server asking for all active auctions
        ClientManager.getInstance().sendRequest(new GetActiveAuctionsRequest());
    }

    private void updateTableData(List<Auction> auctions) {
        ObservableList<Auction> allData = FXCollections.observableArrayList(auctions);

        // Use SessionManager to get current user's ID
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getId() : "unknown";

        // Filter auctions to be shown from allData based on myAuctionsMode
        if (myAuctionsMode && currentUser != null) {
            List<Auction> filtered = allData.stream()
                    .filter(a -> a.getSeller() != null && currentUserId.equals(a.getSeller().getId()))
                    .filter(a -> !"CANCELED".equalsIgnoreCase(a.getStatusAsString()))
                    .collect(Collectors.toList());
            auctionTable.setItems(FXCollections.observableArrayList(filtered));
        } else {
            auctionTable.setItems(allData);
        }
    }
}