package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ControllerUtils {

    private static final Logger logger = LoggerFactory.getLogger(ControllerUtils.class);

    // Private constructor to prevent instantiation of this utility class.
    private ControllerUtils() {}

    /**
     * Method for changing scene when an event (mouse click) happens.
     */
    public static void changeScene(ActionEvent event, String fxmlFile) {
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        changeScene(stage, fxmlFile);
    }

    /**
     * Method for changing scene using a Stage reference.
     */
    public static void changeScene(Stage stage, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(ControllerUtils.class.getResource("/com/auction/client/view/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e) {
            logger.error("Error changing scene to " + fxmlFile, e);
        }
    }

    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }
}
