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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            bidHistory.setAll(auction.getBidHistory());
        }
        updateUI();
        setupChart();
    }

    @FXML
    public void initialize() {
        // Setup table columns
        colBidder.setCellValueFactory(new PropertyValueFactory<>("bidderId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        bidTable.setItems(bidHistory);

        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Price Trend");
        priceChart.getData().add(priceSeries);

        messageListener = msg -> {
            if (msg instanceof Notification notification) {
                if (notification.getAuctionId().equals(currentAuction.getId())) {
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
        lblCurrentPrice.setText("$" + currentAuction.getCurrentPrice().toString());
        
        String bidder = (currentAuction.getHighestBid() != null) 
                        ? currentAuction.getHighestBid().getBidderId() 
                        : "None";
        lblHighestBidder.setText("Highest Bidder: " + bidder);
        
        lblTimeRemaining.setText("End Time: " + currentAuction.getEndTime().format(TIME_FORMATTER));
    }

    private void setupChart() {
        String now = LocalDateTime.now().format(TIME_FORMATTER);
        priceSeries.getData().add(new XYChart.Data<>(now, currentAuction.getCurrentPrice()));
    }

    @FXML
    private void handlePlaceBid(ActionEvent event) {
        try {
            BigDecimal amount = new BigDecimal(tfBidAmount.getText().trim());
            User user = SessionManager.getInstance().getCurrentUser();
            
            BidRequest bidRequest = new BidRequest(currentAuction.getId(), user.getId(), amount);
            ClientManager.getInstance().sendRequest(bidRequest);
            
            tfBidAmount.clear();
        } catch (NumberFormatException e) {
            ControllerUtils.showAlert("Invalid amount!");
        }
    }

    private void handleNotification(Notification notification) {
        if (notification.getData() instanceof AuctionUpdateDTO update) {
            String currentUserId = SessionManager.getInstance().getCurrentUser().getId();
            boolean isLead = update.getLeadingBidderName().equals(currentUserId);

            lblCurrentPrice.setText("$" + update.getCurrentHighestBid());
            lblHighestBidder.setText("Highest Bidder: " + update.getLeadingBidderName());
            lblStatus.setText(update.getStatus());

            // Visual indicator for outbidding
            if (!isLead) {
                lblCurrentPrice.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red if outbid
            } else {
                lblCurrentPrice.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Green if leading
            }

            // Add new bid to table
            BidTransaction newBid = new BidTransaction(update.getAuctionId(), update.getLeadingBidderName(), update.getCurrentHighestBid());
            bidHistory.add(0, newBid);

            String time = LocalDateTime.now().format(TIME_FORMATTER);
            priceSeries.getData().add(new XYChart.Data<>(time, update.getCurrentHighestBid()));
            
            if (priceSeries.getData().size() > 15) {
                priceSeries.getData().remove(0);
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
                    ((AdminMainController) controller).loadView("AuctionList.fxml");
                }
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
}
