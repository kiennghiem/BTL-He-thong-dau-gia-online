package com.auction.client.controller;

import com.auction.client.network.ClientManager;
import com.auction.client.util.SessionManager;
import com.auction.models.User;
import com.auction.models.dto.AuthResponse;
import com.auction.models.dto.DepositRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class DepositController {
    private static final Logger logger = LoggerFactory.getLogger(DepositController.class);

    @FXML private TextField tfUsername;
    @FXML private TextField tfAmount;
    @FXML private Button btnConfirm;

    private Consumer<Object> responseListener;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            tfUsername.setText(currentUser.getUsername());
        }

        responseListener = msg -> {
            if (msg instanceof AuthResponse resp) {
                if (resp.getMessage() != null && (resp.getMessage().startsWith("DEPOSIT_SUCCESS") || resp.getMessage().contains("Lỗi nạp tiền"))) {
                    Platform.runLater(() -> {
                        if (resp.isSuccess()) {
                            ControllerUtils.showSuccess("Nạp tiền thành công", "Số dư mới của bạn là: $" + resp.getUser().getBalance());
                            SessionManager.getInstance().setCurrentUser(resp.getUser());
                            handleCancel(null); 
                        } else {
                            ControllerUtils.showError("Lỗi nạp tiền", resp.getMessage());
                        }
                    });
                }
            }
        };
        ClientManager.getInstance().addMessageListener(responseListener);
    }

    @FXML
    private void handleConfirmDeposit(ActionEvent event) {
        try {
            BigDecimal amount = new BigDecimal(tfAmount.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                ControllerUtils.showAlert("Amount must be positive.");
                return;
            }

            DepositRequest request = new DepositRequest(tfUsername.getText(), amount);
            ClientManager.getInstance().sendRequest(request);
            logger.info("Sending virtual deposit request for {}: ${}", tfUsername.getText(), amount);
        } catch (NumberFormatException e) {
            ControllerUtils.showAlert("Invalid amount. Please enter a valid number.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        ClientManager.getInstance().removeMessageListener(responseListener);
        try {
            BorderPane mainPane = (BorderPane) tfUsername.getScene().lookup("#mainBorderPane");
            if (mainPane != null) {
                Object controller = mainPane.getProperties().get("controller");
                if (controller instanceof BidderMainController) {
                    ((BidderMainController) controller).loadView("AuctionList.fxml", false);
                } else if (controller instanceof SellerMainController) {
                    ((SellerMainController) controller).loadView("AuctionList.fxml", false);
                }
            }
        } catch (Exception e) {
            logger.error("Navigation back failed", e);
        }
    }
}
