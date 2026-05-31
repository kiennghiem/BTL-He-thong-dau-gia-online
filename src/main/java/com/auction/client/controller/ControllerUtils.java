package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Utility class for common Controller operations like scene switching and alerts.
 */
public final class ControllerUtils {

    private static final Logger logger = LoggerFactory.getLogger(ControllerUtils.class);

    private ControllerUtils() {}

    public static void changeScene(ActionEvent event, String fxmlFile) {
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        changeScene(stage, fxmlFile);
    }

    public static void changeScene(Stage stage, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(ControllerUtils.class.getResource("/com/auction/client/view/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        }
        catch (IOException e) {
            logger.error("Error changing scene to " + fxmlFile, e);
        }
    }

    /**
     * Shows a professional Success alert.
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title != null ? title : "Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Add custom styling if needed via CSS, for now standard INFORMATION is better than ERROR
        alert.showAndWait();
    }

    /**
     * Shows a professional Error alert.
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title != null ? title : "Lỗi");
        alert.setHeaderText("Đã xảy ra lỗi");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a professional Warning alert.
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title != null ? title : "Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Generic alert method (legacy support, defaults to error).
     */
    public static void showAlert(String message) {
        showError("Thông báo", message);
    }
    
    /**
     * Shows a confirmation dialog.
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
