package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.User;
import com.auction.models.dto.CreateAuctionRequest;
import com.auction.models.dto.GenericResponse;
import com.auction.server.factory.ItemType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class CreateAuctionController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private DatePicker dpStartDate;
    @FXML private ComboBox<String> cbStartHour;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbEndHour;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private Consumer<Object> responseListener;

    @FXML
    public void initialize() {
        // Populate Hours into the combo boxes
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d:00", i));
        }
        cbStartHour.setItems(hours);
        cbEndHour.setItems(hours);

        // Default selections
        dpStartDate.setValue(LocalDate.now());
        cbStartHour.getSelectionModel().select("12:00");
        dpEndDate.setValue(LocalDate.now().plusDays(3));
        cbEndHour.getSelectionModel().select("12:00");

        // Register listener for server responses
        responseListener = msg -> {
            if (msg instanceof GenericResponse response) {
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", response.getMessage());
                        // Cleanup listener and navigate back only on success
                        ClientManager.getInstance().removeMessageListener(responseListener);
                        navigateBack(null); 
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", response.getMessage());
                    }
                });
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);
    }

    @FXML
    public void handleSubmitAuction(ActionEvent event) {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String priceRaw = txtStartingPrice.getText().trim();
        LocalDate startDate = dpStartDate.getValue();
        String startHourRaw = cbStartHour.getValue();
        LocalDate endDate = dpEndDate.getValue();
        String endHourRaw = cbEndHour.getValue();

        if (title.isEmpty() || priceRaw.isEmpty() || startDate == null || endDate == null) {
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

        // Create the request
        User currentUser = SessionManager.getInstance().getCurrentUser();
        // Fix: Call constructor with 8 arguments (removed the extra BigDecimal)
        CreateAuctionRequest request = new CreateAuctionRequest(
                currentUser,
                ItemType.ELECTRONICS, // Defaulting for now
                title,
                description,
                startingPrice,
                "General",      // specificAttribute
                startDateTime,
                endDateTime
        );

        // Send to server
        ClientManager.getInstance().sendRequest(request);
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        ClientManager.getInstance().removeMessageListener(responseListener);
        navigateBack(event);
    }

    private void navigateBack(ActionEvent event) {
        if (event != null) {
            ControllerUtils.changeScene(event, "SellerMainView.fxml");
        } else {
            Stage stage = (Stage) btnSubmit.getScene().getWindow();
            ControllerUtils.changeScene(stage, "SellerMainView.fxml");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
