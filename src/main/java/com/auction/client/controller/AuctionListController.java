package com.auction.client.controller;

import com.auction.client.util.SessionManager;
import com.auction.models.Auction;
import com.auction.models.Seller;
import com.auction.models.User;
import com.auction.server.factory.UserFactory;
import com.auction.server.factory.UserRole;
import com.auction.server.observer.AuctionStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AuctionListController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionListController.class);

    // Table UI Elements
    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, String> colTitle;
    @FXML private TableColumn<Auction, BigDecimal> colPrice;
    @FXML private TableColumn<Auction, String> colStatus;
    @FXML private TableColumn<Auction, LocalDateTime> colEndTime;

    // Used to check if
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

        // 3. Handle selection
        auctionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                openAuctionDetail(newSelection);
            }
        });
    }

    private void openAuctionDetail(Auction auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/AuctionDetail.fxml"));
            Parent view = loader.load();
            
            AuctionDetailController controller = loader.getController();
            controller.setAuction(auction);
            
            Stage stage = (Stage) auctionTable.getScene().getWindow();
            stage.getScene().setRoot(view);
        } catch (IOException e) {
            logger.error("Error loading AuctionDetail view", e);
            ControllerUtils.showAlert("Error loading AuctionDetail.fxml");
        }
    }

    public void refreshAuctionList() {
        // TODO: Send a network request to your server asking for all active auctions, then put them in allData.

        // --- TEMPORARY DUMMY DATA ---
        ObservableList<Auction> allData = FXCollections.observableArrayList();

        Auction dummy1 = new Auction();
        dummy1.setTitle("Vintage Rolex Watch");
        dummy1.setCurrentPrice(new BigDecimal("1500.00"));
        dummy1.setStatus(AuctionStatus.RUNNING);
        dummy1.setEndTime(LocalDateTime.now().plusDays(2));
        Seller dummySeller1 = (Seller) UserFactory.createNewUser(UserRole.SELLER, "guy1", "111111");
        dummySeller1.setId("dummy-seller-id-1");
        dummy1.setSeller(dummySeller1);

        Auction dummy2 = new Auction();
        dummy2.setTitle("MacBook Pro 2023");
        dummy2.setCurrentPrice(new BigDecimal("2200.00"));
        dummy2.setStatus(AuctionStatus.OPEN);
        dummy2.setEndTime(LocalDateTime.now().plusDays(5));

        // Use SessionManager to get current user's ID
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getId() : "unknown";

        if (currentUser instanceof Seller) {
            dummy2.setSeller((Seller) currentUser);
        } else {
            Seller dummySeller2 = (Seller) UserFactory.createNewUser(UserRole.SELLER, "guy2", "222222");
            dummySeller2.setId("dummy-seller-id-2");
            dummy2.setSeller(dummySeller2);
        }

        allData.addAll(dummy1, dummy2);

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