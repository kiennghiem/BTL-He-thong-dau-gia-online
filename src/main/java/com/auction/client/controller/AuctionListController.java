package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.User;
import com.auction.models.dto.GetActiveAuctionsRequest;
import com.auction.server.factory.UserRole;
import com.auction.server.observer.AuctionStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AuctionListController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionListController.class);

    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, String> colTitle;
    @FXML private TableColumn<Auction, BigDecimal> colPrice;
    @FXML private TableColumn<Auction, String> colStatus;
    @FXML private TableColumn<Auction, LocalDateTime> colEndTime;

    private boolean myAuctionsMode = false;
    private Consumer<Auction> onAuctionSelected;
    private Consumer<Object> messageListener;

    public void setMyAuctionsMode(boolean myAuctionsMode) {
        this.myAuctionsMode = myAuctionsMode;
        logger.info("AuctionList mode set to: myAuctionsMode={}", myAuctionsMode);
        refreshAuctionList();
    }

    public void setOnAuctionSelected(Consumer<Auction> onAuctionSelected) {
        this.onAuctionSelected = onAuctionSelected;
        logger.info("Selection handler attached to AuctionListController");
    }

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // Set the Row Factory ONCE during initialization
        auctionTable.setRowFactory(tv -> {
            TableRow<Auction> row = new TableRow<>() {
                @Override
                protected void updateItem(Auction item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        User currentUser = SessionManager.getInstance().getCurrentUser();
                        if (currentUser != null && myAuctionsMode) {
                            String currentUserId = currentUser.getId();
                            if (currentUser.getRole() == UserRole.SELLER) {
                                if ("CANCELED".equalsIgnoreCase(item.getStatusAsString())) {
                                    setStyle("-fx-background-color: #e0e0e0; -fx-opacity: 0.6;");
                                } else if (item.getHighestBidderId() != null && !"None".equalsIgnoreCase(item.getHighestBidderId())) {
                                    setStyle("-fx-background-color: #d4edda;");
                                } else {
                                    setStyle("");
                                }
                            } else if (currentUser.getRole() == UserRole.BIDDER) {
                                if (currentUserId.equals(item.getHighestBidderId())) {
                                    setStyle("-fx-background-color: #d4edda;");
                                } else {
                                    setStyle("-fx-background-color: #f8d7da;");
                                }
                            }
                        } else {
                            setStyle("");
                        }
                    }
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Auction selectedAuction = row.getItem();
                    logger.info("Row double-clicked: {}", selectedAuction.getId());
                    if (onAuctionSelected != null) {
                        onAuctionSelected.accept(selectedAuction);
                    } else {
                        logger.warn("No selection handler defined for AuctionListController!");
                    }
                }
            });
            return row;
        });

        messageListener = msg -> {
            if (msg instanceof List<?> list) {
                if (!list.isEmpty() && list.get(0) instanceof Auction) {
                    @SuppressWarnings("unchecked")
                    List<Auction> auctions = (List<Auction>) list;
                    Platform.runLater(() -> updateTable(auctions));
                } else if (list.isEmpty()) {
                    Platform.runLater(() -> updateTable(List.of()));
                }
            } else if (msg instanceof com.auction.models.Notification) {
                Platform.runLater(this::refreshAuctionList);
            }
        };
        ClientManager.getInstance().addMessageListener(messageListener);

        refreshAuctionList();
    }

    public void refreshAuctionList() {
        logger.info("Requesting fresh auction list from server...");
        ClientManager.getInstance().sendRequest(new GetActiveAuctionsRequest());
    }

    private void updateTable(List<Auction> allData) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        String currentUserId = currentUser.getId();

        List<Auction> displayList;
        if (myAuctionsMode) {
            if (currentUser.getRole() == UserRole.SELLER) {
                // Show ALL auctions I created
                displayList = allData.stream()
                        .filter(a -> a.getSeller() != null && currentUserId.equals(a.getSeller().getId()))
                        .collect(Collectors.toList());
            } else {
                // Show ALL auctions I participated in (even if finished/paid)
                displayList = allData.stream()
                        .filter(a -> a.getBidHistory() != null && 
                                     a.getBidHistory().stream().anyMatch(b -> b.getBidderId().equals(currentUserId)))
                        .collect(Collectors.toList());
            }
        } else {
            // Public mode: only show OPEN or RUNNING
            displayList = allData.stream().filter(a -> {
                AuctionStatus status = a.getStatus();
                return status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING;
            }).collect(Collectors.toList());
        }
        
        logger.info("Updating table with {} items", displayList.size());
        auctionTable.setItems(FXCollections.observableArrayList(displayList));
        // No need to call setRowFactory here anymore, it's already set in initialize()
    }
}
