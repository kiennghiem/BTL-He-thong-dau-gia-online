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
        if (currentUser == null) return;
        String currentUserId = currentUser.getId();

        if (myAuctionsMode) {
            if (currentUser.getRole() == com.auction.server.factory.UserRole.SELLER) {
                // "My Auctions" Mode for Sellers: Show ALL auctions I created, including CANCELED
                List<Auction> myOwn = allData.stream()
                        .filter(a -> a.getSeller() != null && currentUserId.equals(a.getSeller().getId()))
                        .collect(Collectors.toList());
                
                auctionTable.setItems(FXCollections.observableArrayList(myOwn));
                
                // Row Factory for Sellers
                auctionTable.setRowFactory(tv -> {
                    javafx.scene.control.TableRow<Auction> row = new javafx.scene.control.TableRow<>() {
                        @Override
                        protected void updateItem(Auction item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setStyle("");
                            } else {
                                if ("CANCELED".equalsIgnoreCase(item.getStatusAsString())) {
                                    setStyle("-fx-background-color: #e0e0e0; -fx-opacity: 0.6;"); // Gray out for canceled
                                } else {
                                    boolean hasBids = (item.getHighestBidderId() != null && !item.getHighestBidderId().equalsIgnoreCase("None"));
                                    if (hasBids) {
                                        setStyle("-fx-background-color: #d4edda;"); // Green: Active interest
                                    } else {
                                        // Check if it's ending in less than 1 hour
                                        java.time.Duration remaining = java.time.Duration.between(java.time.LocalDateTime.now(), item.getEndTime());
                                        if (!remaining.isNegative() && remaining.toHours() < 1) {
                                            setStyle("-fx-background-color: #f8d7da;"); // Red: Ending soon with no bids
                                        } else {
                                            setStyle(""); // Neutral: Normal state
                                        }
                                    }
                                }
                            }
                        }
                    };
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && (!row.isEmpty())) {
                            if (onAuctionSelected != null) onAuctionSelected.accept(row.getItem());
                        }
                    });
                    return row;
                });
            } else {
                // "My Bids" Mode for Bidders: Show auctions I participated in
                List<Auction> participated = allData.stream()
                        .filter(a -> a.getBidHistory() != null && 
                                     a.getBidHistory().stream().anyMatch(b -> b.getBidderId().equals(currentUserId)))
                        .collect(Collectors.toList());
                
                auctionTable.setItems(FXCollections.observableArrayList(participated));
                
                // Row Factory for Bidders: Green if leading, Red if outbid
                auctionTable.setRowFactory(tv -> {
                    javafx.scene.control.TableRow<Auction> row = new javafx.scene.control.TableRow<>() {
                        @Override
                        protected void updateItem(Auction item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setStyle("");
                            } else {
                                String leaderId = item.getHighestBidderId();
                                if (currentUserId.equals(leaderId)) {
                                    setStyle("-fx-background-color: #d4edda;"); // Light Green for leading
                                } else {
                                    setStyle("-fx-background-color: #f8d7da;"); // Light Red for outbid
                                }
                            }
                        }
                    };
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && (!row.isEmpty())) {
                            if (onAuctionSelected != null) onAuctionSelected.accept(row.getItem());
                        }
                    });
                    return row;
                });
            }
        } else {
            // Default Mode: Show all active/upcoming auctions
            auctionTable.setItems(FXCollections.observableArrayList(allData));
            auctionTable.setRowFactory(tv -> {
                javafx.scene.control.TableRow<Auction> row = new javafx.scene.control.TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        if (onAuctionSelected != null) onAuctionSelected.accept(row.getItem());
                    }
                });
                return row;
            });
        }
    }
}
