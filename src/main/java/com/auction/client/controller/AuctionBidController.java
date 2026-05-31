package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.BidTransaction;
import com.auction.models.Notification;
import com.auction.models.User;
import com.auction.models.dto.AuctionUpdateDTO;
import com.auction.models.dto.BidRequest;
import com.auction.server.observer.AuctionStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class AuctionBidController {

    @FXML private Label lblItemName;
    @FXML private Label lblStatus;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblHighestBidder;
    @FXML private Label lblTimeRemaining;
    @FXML private TextField tfBidAmount;
    @FXML private Button btnPlaceBid;
    @FXML private VBox bidSection;
    @FXML private LineChart<String, Number> priceChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TableView<BidTransaction> bidTable;
    @FXML private TableColumn<BidTransaction, String> colBidder;
    @FXML private TableColumn<BidTransaction, BigDecimal> colAmount;
    @FXML private TableColumn<BidTransaction, LocalDateTime> colTime;

    private Auction currentAuction;
    private XYChart.Series<String, Number> priceSeries;
    private ObservableList<BidTransaction> bidHistory = FXCollections.observableArrayList();
    private Consumer<Object> messageListener;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void setAuction(Auction auction) {
        this.currentAuction = auction;
        if (auction.getBidHistory() != null) {
            // Sort ascending for the chart, but Table uses bidHistory (descending usually handled by add(0, ...))
            bidHistory.setAll(auction.getBidHistory());
        }
        updateUI();
        setupChart();
    }

    @FXML
    public void initialize() {
        // Setup table columns
        colBidder.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        bidTable.setItems(bidHistory);

        // Optimize Chart Scaling
        yAxis.setForceZeroInRange(false); // Don't force 0, zoom into the price range
        yAxis.setAutoRanging(true);      // Let it adjust automatically
        
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Price Trend ($)");
        priceChart.getData().add(priceSeries);
        priceChart.setCreateSymbols(true); // Show dots on points
        priceChart.setAnimated(false);    // Disable animation for better stability with updates

        messageListener = msg -> {
            if (msg instanceof Notification notification) {
                // Logic updated to be more robust
                if (currentAuction != null && notification.getAuctionId().equals(currentAuction.getId())) {
                    Platform.runLater(() -> handleNotification(notification));
                }
            }
        };
        ClientManager.getInstance().addMessageListener(messageListener);
    }

    private void updateUI() {
        if (currentAuction == null) return;
        lblItemName.setText(currentAuction.getTitle());
        lblStatus.setText(currentAuction.getStatusAsString());
        
        // Update Status Label Color
        switch (currentAuction.getStatus()) {
            case RUNNING -> lblStatus.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-background-radius: 5;");
            case OPEN -> lblStatus.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-background-radius: 5;");
            case FINISHED -> lblStatus.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-background-radius: 5;");
            case CANCELED -> lblStatus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-background-radius: 5;");
        }

        // Hide bidding section if not active
        if (bidSection != null) {
            bidSection.setVisible(currentAuction.getStatus() == AuctionStatus.RUNNING);
            bidSection.setManaged(currentAuction.getStatus() == AuctionStatus.RUNNING);
        }

        lblCurrentPrice.setText("$" + currentAuction.getCurrentPrice().toString());
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getId() : "";
        
        String bidderId = currentAuction.getHighestBidderId();
        
        if (bidderId == null || bidderId.equalsIgnoreCase("None")) {
            lblHighestBidder.setText("Highest Bidder: None");
            lblCurrentPrice.setStyle("-fx-text-fill: black;"); // Neutral
        } else {
            // Check if highest bid object exists to avoid NPE
            String name = (currentAuction.getHighestBid() != null) ? currentAuction.getHighestBid().getBidderName() : bidderId;
            lblHighestBidder.setText("Highest Bidder: " + name);
            
            if (bidderId.equals(currentUserId)) {
                lblCurrentPrice.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green if leading
            } else {
                lblCurrentPrice.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red if outbid
            }
        }
        
        if (currentAuction.getEndTime() != null) {
            lblTimeRemaining.setText("End Time: " + currentAuction.getEndTime().format(TIME_FORMATTER));
        } else {
            lblTimeRemaining.setText("End Time: N/A");
        }
    }

    private void setupChart() {
        if (currentAuction == null) return;
        priceSeries.getData().clear();

        // 1. Add Starting Price as first point
        if (currentAuction.getStartTime() != null) {
            priceSeries.getData().add(new XYChart.Data<>(
                currentAuction.getStartTime().format(TIME_FORMATTER), 
                currentAuction.getStartingPrice()
            ));
        }

        // 2. Add all bids from history (already sorted in chronological order from DB usually)
        // If bidHistory is descending for the table, we need to sort it ascending here
        java.util.List<BidTransaction> sortedHistory = new java.util.ArrayList<>(bidHistory);
        sortedHistory.sort(java.util.Comparator.comparing(BidTransaction::getTimestamp));

        for (BidTransaction bid : sortedHistory) {
            priceSeries.getData().add(new XYChart.Data<>(
                bid.getTimestamp().format(TIME_FORMATTER), 
                bid.getBidAmount()
            ));
        }

        // 3. Ensure current price is shown if it's different from last bid
        if (priceSeries.getData().size() == 1) { // only starting price
             // Initial state
        }
    }

    @FXML
    private void handlePlaceBid(ActionEvent event) {
        if (currentAuction == null) return;
        
        if (currentAuction.getStatus() != AuctionStatus.RUNNING) {
            ControllerUtils.showAlert("This auction is not active!");
            return;
        }

        try {
            String bidText = tfBidAmount.getText().trim();
            if (bidText.isEmpty()) {
                ControllerUtils.showAlert("Please enter a bid amount!");
                return;
            }

            BigDecimal amount = new BigDecimal(bidText);
            if (amount.compareTo(currentAuction.getCurrentPrice()) <= 0) {
                ControllerUtils.showAlert("Bid must be higher than current price ($" + currentAuction.getCurrentPrice() + ")!");
                return;
            }

            User user = SessionManager.getInstance().getCurrentUser();
            if (user == null) {
                ControllerUtils.showAlert("You must be logged in to place a bid!");
                return;
            }
            BidRequest bidRequest = new BidRequest(currentAuction.getId(), user.getId(), amount);
            ClientManager.getInstance().sendRequest(bidRequest);
            
            tfBidAmount.clear();
        } catch (NumberFormatException e) {
            ControllerUtils.showAlert("Invalid amount! Please enter a numeric value.");
        }
    }

    private void handleNotification(Notification notification) {
        if (notification.getData() instanceof AuctionUpdateDTO update) {
            if (currentAuction == null) return;

            // 1. Sync local auction model state
            currentAuction.setCurrentPrice(update.getCurrentHighestBid());
            currentAuction.setHighestBidderId(update.getLeadingBidderId());
            currentAuction.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(update.getEndTimeMillis()), ZoneId.systemDefault()));
            
            // Re-create the highest bid object so updateUI can access leading username
            BidTransaction latestBid = new BidTransaction(
                update.getAuctionId(), 
                update.getLeadingBidderId(), 
                update.getLeadingBidderName(), 
                update.getCurrentHighestBid()
            );
            currentAuction.setHighestBid(latestBid);
            
            try {
                currentAuction.updateStatus(AuctionStatus.valueOf(update.getStatus()));
            } catch (Exception ignored) {}

            // 2. Refresh UI labels and colors
            updateUI(); 

            // 3. Specifically handle NEW BID event
            if (notification.getType() == Notification.Type.BID_PLACED) {
                // Check if this bid is already in the table to prevent double-insert
                boolean alreadyInList = bidHistory.stream().anyMatch(b -> 
                    b.getBidAmount().compareTo(latestBid.getBidAmount()) == 0 && 
                    b.getBidderId().equals(latestBid.getBidderId()));
                
                if (!alreadyInList) {
                    // Add to UI history list (at the top)
                    bidHistory.add(0, latestBid);

                    // Update trend chart
                    String time = LocalDateTime.now().format(TIME_FORMATTER);
                    priceSeries.getData().add(new XYChart.Data<>(time, update.getCurrentHighestBid()));
                    
                    if (priceSeries.getData().size() > 15) {
                        priceSeries.getData().remove(0);
                    }
                }
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        ClientManager.getInstance().removeMessageListener(messageListener);
        try {
            BorderPane mainPane = (BorderPane) lblItemName.getScene().lookup("#mainBorderPane");
            if (mainPane != null) {
                Object controller = mainPane.getProperties().get("controller");
                if (controller instanceof BidderMainController) {
                    ((BidderMainController) controller).loadView("AuctionList.fxml", false);
                } else if (controller instanceof SellerMainController) {
                    ((SellerMainController) controller).loadView("AuctionList.fxml", false);
                } else if (controller instanceof AdminMainController) {
                    ((AdminMainController) controller).loadView("AuctionList.fxml", false);
                }
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
}
