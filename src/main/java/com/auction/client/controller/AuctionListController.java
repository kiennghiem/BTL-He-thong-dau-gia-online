package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.User;
import com.auction.models.dto.GetActiveAuctionsRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

    // Used to check if
    private boolean myAuctionsMode = false;
    private Consumer<Auction> onAuctionSelected;
    private Consumer<Object> messageListener;

    public void setMyAuctionsMode(boolean myAuctionsMode) {
        this.myAuctionsMode = myAuctionsMode;
        refreshAuctionList();
    }

    public void setOnAuctionSelected(Consumer<Auction> onAuctionSelected) {
        this.onAuctionSelected = onAuctionSelected;
    }

    // This method runs automatically when the screen loads
    @FXML
    public void initialize() {
        // 1. Tell the columns which variables to look for in your Auction model
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // 2. Add row selection listener
        auctionTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Auction> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Auction rowData = row.getItem();
                    if (onAuctionSelected != null) {
                        onAuctionSelected.accept(rowData);
                    }
                }
            });
            return row;
        });

        // 3. Register network listener
        messageListener = msg -> {
            if (msg instanceof List<?> list) {
                // Check if it's a list of Auctions
                if (!list.isEmpty() && list.get(0) instanceof Auction) {
                    @SuppressWarnings("unchecked")
                    List<Auction> auctions = (List<Auction>) list;
                    Platform.runLater(() -> updateTable(auctions));
                } else if (list.isEmpty()) {
                    Platform.runLater(() -> updateTable(List.of()));
                }
            }
        };
        ClientManager.getInstance().addMessageListener(messageListener);

        // 4. Load the data
        refreshAuctionList();
    }

    public void refreshAuctionList() {
        // Send a network request to your server asking for all active auctions
        ClientManager.getInstance().sendRequest(new GetActiveAuctionsRequest());
    }

    private void updateTable(List<Auction> allData) {
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
            auctionTable.setItems(FXCollections.observableArrayList(allData));
        }
    }
}
