package com.auction.client.controller;

import com.auction.client.util.SessionManager;
import com.auction.server.factory.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SellerCreateAuctionController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private DatePicker dpStartDate;
    @FXML private ComboBox<String> cbStartHour;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbEndHour;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

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

        // TODO: Call your Client-Side Service or Network Manager here to send the request to the server
        System.out.println("Submitting Auction: " + title + " | $" + startingPrice);

        showAlert(Alert.AlertType.INFORMATION, "Success", "Auction created successfully!");

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
}