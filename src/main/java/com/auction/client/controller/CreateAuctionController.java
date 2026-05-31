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
    @FXML private TextField txtStartHour;
    @FXML private TextField txtStartMinute;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField txtEndHour;
    @FXML private TextField txtEndMinute;
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

        // Set default dates and times
        LocalDateTime now = LocalDateTime.now();
        dpStartDate.setValue(now.toLocalDate());
        txtStartHour.setText(String.format("%02d", now.getHour()));
        txtStartMinute.setText(String.format("%02d", now.getMinute()));

        LocalDateTime defaultEnd = now.plusDays(3);
        dpEndDate.setValue(defaultEnd.toLocalDate());
        txtEndHour.setText(String.format("%02d", defaultEnd.getHour()));
        txtEndMinute.setText(String.format("%02d", defaultEnd.getMinute()));

        // Auto-cap and validate time fields
        setupTimeField(txtStartHour, 23);
        setupTimeField(txtStartMinute, 59);
        setupTimeField(txtEndHour, 23);
        setupTimeField(txtEndMinute, 59);

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

    /**
     * Helper to enforce numeric input and max value limits in real-time.
     */
    private void setupTimeField(TextField field, int maxValue) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            // 1. Force numeric only
            if (!newValue.matches("\\d*")) {
                field.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }

            // 2. Cap at max value
            try {
                int val = Integer.parseInt(newValue);
                if (val > maxValue) {
                    field.setText(String.valueOf(maxValue));
                }
            } catch (NumberFormatException ignored) {
                field.setText(oldValue);
            }
        });
    }

    @FXML
    public void handleSubmitAuction(ActionEvent event) {
        String title = txtTitle.getText().trim();
        ItemType category = cbCategory.getValue();
        String specificAttr = txtSpecificAttribute.getText().trim();
        String description = txtDescription.getText().trim();
        String priceRaw = txtStartingPrice.getText().trim();
        LocalDate startDate = dpStartDate.getValue();
        String startHourStr = txtStartHour.getText().trim();
        String startMinStr = txtStartMinute.getText().trim();
        LocalDate endDate = dpEndDate.getValue();
        String endHourStr = txtEndHour.getText().trim();
        String endMinStr = txtEndMinute.getText().trim();

        if (title.isEmpty() || category == null || priceRaw.isEmpty() || startDate == null || endDate == null
            || startHourStr.isEmpty() || startMinStr.isEmpty() || endHourStr.isEmpty() || endMinStr.isEmpty()) {
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

        int startH, startM, endH, endM;
        try {
            startH = Integer.parseInt(startHourStr);
            startM = Integer.parseInt(startMinStr);
            endH = Integer.parseInt(endHourStr);
            endM = Integer.parseInt(endMinStr);

            if (startH < 0 || startH > 23 || endH < 0 || endH > 23 ||
                startM < 0 || startM > 59 || endM < 0 || endM > 59) {
                ControllerUtils.showError("Dữ liệu không hợp lệ", "Giờ phải từ 0-23 và Phút phải từ 0-59!");
                return;
            }
        } catch (NumberFormatException e) {
            ControllerUtils.showError("Dữ liệu không hợp lệ", "Giờ và phút phải là số!");
            return;
        }

        LocalTime startTime = LocalTime.of(startH, startM);
        LocalTime endTime = LocalTime.of(endH, endM);

        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

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
