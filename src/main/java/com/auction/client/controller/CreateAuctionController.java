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
    @FXML private ComboBox<ItemType> cbCategory;
    @FXML private Label lblSpecificAttribute;
    @FXML private TextField txtSpecificAttribute;
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
        // Populate Categories with custom display names
        cbCategory.setItems(FXCollections.observableArrayList(ItemType.values()));
        cbCategory.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ItemType type) {
                if (type == null) return "";
                String name = type.name().toLowerCase();
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }

            @Override
            public ItemType fromString(String string) {
                return ItemType.valueOf(string.toUpperCase());
            }
        });
        cbCategory.getSelectionModel().select(ItemType.ELECTRONICS);
        updateSpecificAttributeField(ItemType.ELECTRONICS);

        // Listener for category change
        cbCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSpecificAttributeField(newVal);
            }
        });

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
                        ControllerUtils.showSuccess("Thành công", response.getMessage());
                        // Cleanup listener and navigate back only on success
                        ClientManager.getInstance().removeMessageListener(responseListener);
                        navigateBack(null); 
                    } else {
                        ControllerUtils.showError("Lỗi", response.getMessage());
                    }
                });
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);
    }

    private void updateSpecificAttributeField(ItemType type) {
        switch (type) {
            case ART -> {
                lblSpecificAttribute.setText("Artist:");
                txtSpecificAttribute.setPromptText("e.g. Leonardo da Vinci");
            }
            case ELECTRONICS -> {
                lblSpecificAttribute.setText("Brand:");
                txtSpecificAttribute.setPromptText("e.g. Apple, Sony");
            }
            case VEHICLE -> {
                lblSpecificAttribute.setText("Mileage:");
                txtSpecificAttribute.setPromptText("e.g. 50,000 km");
            }
            case OTHERS -> {
                lblSpecificAttribute.setText("Specific Info:");
                txtSpecificAttribute.setPromptText("e.g. Material, Origin");
            }
        }
    }

    @FXML
    public void handleSubmitAuction(ActionEvent event) {
        String title = txtTitle.getText().trim();
        ItemType category = cbCategory.getValue();
        String specificAttr = txtSpecificAttribute.getText().trim();
        String description = txtDescription.getText().trim();
        String priceRaw = txtStartingPrice.getText().trim();
        LocalDate startDate = dpStartDate.getValue();
        String startHourRaw = cbStartHour.getValue();
        LocalDate endDate = dpEndDate.getValue();
        String endHourRaw = cbEndHour.getValue();

        if (title.isEmpty() || category == null || priceRaw.isEmpty() || startDate == null || endDate == null) {
            ControllerUtils.showError("Thiếu thông tin", "Vui lòng điền đầy đủ các trường bắt buộc!");
            return;
        }

        if (specificAttr.isEmpty()) {
            ControllerUtils.showError("Thiếu thông tin", "Vui lòng điền thông tin chi tiết cho loại mặt hàng này!");
            return;
        }

        BigDecimal startingPrice;
        try {
            startingPrice = new BigDecimal(priceRaw);
            if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                ControllerUtils.showError("Dữ liệu không hợp lệ", "Giá khởi điểm phải lớn hơn 0!");
                return;
            }
        } catch (NumberFormatException e) {
            ControllerUtils.showError("Dữ liệu không hợp lệ", "Giá khởi điểm phải là một số hợp lệ!");
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.parse(startHourRaw));
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.parse(endHourRaw));

        if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
            ControllerUtils.showError("Thời gian không hợp lệ", "Thời gian kết thúc phải sau thời gian bắt đầu!");
            return;
        }

        // Create the request
        User currentUser = SessionManager.getInstance().getCurrentUser();
        CreateAuctionRequest request = new CreateAuctionRequest(
                currentUser,
                category,
                title,
                description,
                startingPrice,
                specificAttr,
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
}
