package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.BidTransaction;
import com.auction.models.Notification;
import com.auction.models.User;
import com.auction.models.dto.*;
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

/**
 * Controller for the Auction Bidding View.
 * Handles real-time updates, price charts, and bid placement.
 */
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

    @FXML private VBox paySection;
    @FXML private Label lblPayTitle;
    @FXML private Button btnPay;

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

        // SUBSCRIBE: Sync request for initial state and register for updates
        ClientManager.getInstance().sendRequest(new SubscribeRequest(auction.getId()));
    }

    @FXML
    public void initialize() {
        // Setup table columns
        colBidder.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("bidAmount"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        bidTable.setItems(bidHistory);

        // Optimize Chart Scaling
        yAxis.setForceZeroInRange(false);
        yAxis.setAutoRanging(true);
        
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Price Trend ($)");
        priceChart.getData().add(priceSeries);
        priceChart.setCreateSymbols(true);
        priceChart.setAnimated(false);

        messageListener = msg -> {
            if (msg instanceof Notification notification) {
                if (currentAuction != null && notification.getAuctionId().equals(currentAuction.getId())) {
                    Platform.runLater(() -> handleNotification(notification));
                }
            } else if (msg instanceof GenericResponse resp) {
                if (resp.getMessage().toLowerCase().contains("thanh toán") || resp.getMessage().toLowerCase().contains("pay")) {
                    Platform.runLater(() -> {
                        if (resp.isSuccess()) {
                            ControllerUtils.showSuccess("Thanh toán thành công", resp.getMessage());
                        } else {
                            ControllerUtils.showError("Thanh toán thất bại", resp.getMessage());
                        }
                    });
                } else if (resp.getMessage().toLowerCase().contains("đặt giá") || resp.getMessage().toLowerCase().contains("bid")) {
                    if (!resp.isSuccess()) {
                        Platform.runLater(() -> ControllerUtils.showError("Lỗi đặt giá", resp.getMessage()));
                    }
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
            case PAID -> lblStatus.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-background-radius: 5;");
            default -> lblStatus.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-background-radius: 5;");
        }

        lblCurrentPrice.setText("$" + currentAuction.getCurrentPrice().toString());
        
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getId() : "";
        BigDecimal currentBalance = (currentUser != null) ? currentUser.getBalance() : BigDecimal.ZERO;

        String bidderId = currentAuction.getHighestBidderId();
        boolean isWinner = bidderId != null && bidderId.equals(currentUserId);
        boolean isFinished = currentAuction.getStatus() == AuctionStatus.FINISHED;
        boolean hasPaid = currentAuction.getStatus() == AuctionStatus.PAID;

        // Manage Visibility Sections
        boolean isSeller = (currentUser != null && currentUser.getRole() == com.auction.server.factory.UserRole.SELLER);

        if (hasPaid) {
            bidSection.setVisible(false);
            bidSection.setManaged(false);
            paySection.setVisible(true);
            paySection.setManaged(true);
            paySection.setStyle("-fx-border-color: #3498db; -fx-background-color: #ebf5fb; -fx-padding: 15; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5;");
            lblPayTitle.setText("This Item has been PAID.");
            btnPay.setVisible(false);
            btnPay.setManaged(false);
        } else if (isFinished) {
            bidSection.setVisible(false);
            bidSection.setManaged(false);
            if (isWinner) {
                paySection.setVisible(true);
                paySection.setManaged(true);
                if (currentBalance.compareTo(currentAuction.getCurrentPrice()) >= 0) {
                    btnPay.setDisable(false);
                    btnPay.setText("Pay for Item ($" + currentAuction.getCurrentPrice() + ")");
                } else {
                    btnPay.setDisable(true);
                    btnPay.setText("Insufficient Balance ($" + currentBalance + ")");
                }
            } else {
                paySection.setVisible(false);
                paySection.setManaged(false);
            }
        } else {
            // ONLY Bidders can see the bid section in a running/open auction
            boolean canBid = currentAuction.getStatus() == AuctionStatus.RUNNING && !isSeller;
            bidSection.setVisible(canBid);
            bidSection.setManaged(canBid);
            paySection.setVisible(false);
            paySection.setManaged(false);
        }

        // Leader Color Logic
        if (bidderId == null || bidderId.equalsIgnoreCase("None")) {
            lblHighestBidder.setText("Highest Bidder: None");
            lblCurrentPrice.setStyle("-fx-text-fill: black;");
        } else {
            String name = (currentAuction.getHighestBid() != null) ? currentAuction.getHighestBid().getBidderName() : bidderId;
            lblHighestBidder.setText("Highest Bidder: " + name);
            if (bidderId.equals(currentUserId)) {
                lblCurrentPrice.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                lblCurrentPrice.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
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
        
        if (currentAuction.getStartTime() != null) {
            priceSeries.getData().add(new XYChart.Data<>(currentAuction.getStartTime().format(TIME_FORMATTER), currentAuction.getStartingPrice()));
        }
        
        java.util.List<BidTransaction> sorted = new java.util.ArrayList<>(bidHistory);
        sorted.sort(java.util.Comparator.comparing(BidTransaction::getTimestamp));
        for (BidTransaction bid : sorted) {
            priceSeries.getData().add(new XYChart.Data<>(bid.getTimestamp().format(TIME_FORMATTER), bid.getBidAmount()));
        }
    }

    @FXML
    private void handlePlaceBid(ActionEvent event) {
        if (currentAuction == null) return;
        if (currentAuction.getStatus() != AuctionStatus.RUNNING) {
            ControllerUtils.showAlert("Auction not active!");
            return;
        }
        try {
            String input = tfBidAmount.getText().trim();
            if (input.isEmpty()) return;
            
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(currentAuction.getCurrentPrice()) <= 0) {
                ControllerUtils.showAlert("Bid must be higher than current price!");
                return;
            }
            
            User user = SessionManager.getInstance().getCurrentUser();
            if (user == null) {
                ControllerUtils.showAlert("You must be logged in to place a bid!");
                return;
            }

            // Proactive local update for responsiveness
            BidTransaction localBid = new BidTransaction(currentAuction.getId(), user.getId(), user.getUsername(), amount);
            if (bidHistory.stream().noneMatch(b -> b.getBidAmount().equals(amount) && b.getBidderId().equals(user.getId()))) {
                bidHistory.add(0, localBid);
                currentAuction.setCurrentPrice(amount);
                currentAuction.setHighestBid(localBid);
                currentAuction.setHighestBidderId(user.getId());
                updateUI();
                priceSeries.getData().add(new XYChart.Data<>(LocalDateTime.now().format(TIME_FORMATTER), amount));
            }

            ClientManager.getInstance().sendRequest(new BidRequest(currentAuction.getId(), user.getId(), amount));
            tfBidAmount.clear();
        } catch (NumberFormatException e) {
            ControllerUtils.showAlert("Invalid input! Please enter a number.");
        } catch (Exception e) {
            ControllerUtils.showAlert("Error: " + e.getMessage());
        }
    }

    private void handleNotification(Notification notification) {
        if (notification.getData() instanceof AuctionUpdateDTO update) {
            if (currentAuction == null) return;
            
            boolean isNewPrice = update.getCurrentHighestBid().compareTo(currentAuction.getCurrentPrice()) > 0;
            
            // Sync local model
            currentAuction.setCurrentPrice(update.getCurrentHighestBid());
            currentAuction.setHighestBidderId(update.getLeadingBidderId());
            currentAuction.setEndTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(update.getEndTimeMillis()), ZoneId.systemDefault()));
            
            BidTransaction latest = new BidTransaction(
                update.getAuctionId(), 
                update.getLeadingBidderId(), 
                update.getLeadingBidderName(), 
                update.getCurrentHighestBid()
            );
            currentAuction.setHighestBid(latest);
            
            try { currentAuction.updateStatus(AuctionStatus.valueOf(update.getStatus())); } catch (Exception ignored) {}
            
            updateUI();

            // History Update
            if (notification.getType() == Notification.Type.BID_PLACED || (isNewPrice && !"None".equals(update.getLeadingBidderId()))) {
                if (bidHistory.stream().noneMatch(b -> b.getBidAmount().compareTo(latest.getBidAmount()) == 0 && b.getBidderId().equals(latest.getBidderId()))) {
                    bidHistory.add(0, latest);
                    priceSeries.getData().add(new XYChart.Data<>(LocalDateTime.now().format(TIME_FORMATTER), update.getCurrentHighestBid()));
                    if (priceSeries.getData().size() > 15) priceSeries.getData().remove(0);
                }
            }
        }
    }

    @FXML
    private void handlePay(ActionEvent event) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (currentAuction != null && user != null) {
            ClientManager.getInstance().sendRequest(new PayRequest(currentAuction.getId(), user.getId(), currentAuction.getCurrentPrice()));
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (currentAuction != null) ClientManager.getInstance().sendRequest(new UnsubscribeRequest(currentAuction.getId()));
        ClientManager.getInstance().removeMessageListener(messageListener);
        try {
            BorderPane mainPane = (BorderPane) lblItemName.getScene().lookup("#mainBorderPane");
            if (mainPane != null) {
                Object controller = mainPane.getProperties().get("controller");
                if (controller instanceof BidderMainController) ((BidderMainController) controller).loadView("AuctionList.fxml", false);
                else if (controller instanceof SellerMainController) ((SellerMainController) controller).loadView("AuctionList.fxml", false);
                else if (controller instanceof AdminMainController) ((AdminMainController) controller).loadView("AuctionList.fxml", false);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
