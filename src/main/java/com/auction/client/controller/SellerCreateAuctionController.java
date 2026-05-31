package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.User;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.CreateAuctionRequest;
import com.auction.models.dto.GenericResponse;
import com.auction.server.factory.ItemType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class SellerCreateAuctionController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<ItemType> cbItemType;
    @FXML private Label lblSpecificAttribute;
    @FXML private TextField txtSpecificAttribute;
    @FXML private TextField txtStartingPrice;
    @FXML private DatePicker dpStartDate;
    @FXML private ComboBox<String> cbStartHour;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbEndHour;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private User currentSeller = SessionManager.getInstance().getCurrentUser();
    private Consumer<Object> responseListener;

    @FXML
    public void initialize() {
        responseListener = msg -> {
            if (msg instanceof GenericResponse response) {
                Platform.runLater(() -> handleCreateAuctionResponse(response));
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);

        // Populate Hours into the combo boxes
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d:00", i));
        }
        cbStartHour.setItems(hours);
        cbEndHour.setItems(hours);

        // Populate Item Types
        cbItemType.setItems(FXCollections.observableArrayList(ItemType.values()));
        cbItemType.getSelectionModel().select(ItemType.ELECTRONICS);

        // Default selections
        dpStartDate.setValue(LocalDate.now());
        cbStartHour.getSelectionModel().select("12:00");
        dpEndDate.setValue(LocalDate.now().plusDays(3));
        cbEndHour.getSelectionModel().select("12:00");
    }

    @FXML
    public void handleSubmitAuction(ActionEvent event) {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        ItemType itemType = cbItemType.getValue();
        String specificAttribute = txtSpecificAttribute.getText().trim();
        String priceRaw = txtStartingPrice.getText().trim();
        LocalDate startDate = dpStartDate.getValue();
        String startHourRaw = cbStartHour.getValue();
        LocalDate endDate = dpEndDate.getValue();
        String endHourRaw = cbEndHour.getValue();

        if (title.isEmpty() || description.isEmpty() || itemType == null || specificAttribute.isEmpty() ||
            priceRaw.isEmpty() || startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Please fill out all required fields!");
            return;
        }

        BigDecimal startingPrice;
        try {
            startingPrice = new BigDecimal(priceRaw);
            if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Data", "Starting price must be greater than 0!");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Data", "Starting price must be a valid number!");
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.parse(startHourRaw));
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.parse(endHourRaw));

        if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Time", "End time must be after the start time!");
            return;
        }

        // Send request to server via ClientManager
        CreateAuctionRequest request = new CreateAuctionRequest(
                currentSeller,
                itemType,
                title,
                description,
                startingPrice,
                specificAttribute,
                startDateTime,
                endDateTime
        );

        ClientManager.getInstance().sendRequest(request);
        System.out.println("Submitting Auction: " + title + " | $" + startingPrice + " | Type: " + itemType);

        showAlert(Alert.AlertType.INFORMATION, "Success", "Auction creation request submitted!");

        // Go back to the correct view
        navigateBack(event);
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        navigateBack(event);
    }

    private void navigateBack(ActionEvent event) {
        ControllerUtils.changeScene(event, "SellerMainView.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleCreateAuctionResponse(GenericResponse response) {
        if (response.isSuccess()) {
            System.out.println("[CREATEAUCTION] Success: " + response.getMessage());
            showAlert(Alert.AlertType.CONFIRMATION, "Success", "New auction has been created");

            ClientManager.getInstance().removeMessageListener(responseListener);
        } else {
            ControllerUtils.showAlert(response.getMessage());
        }
    }
}