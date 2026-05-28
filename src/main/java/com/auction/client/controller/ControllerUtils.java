package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public final class ControllerUtils {

    // Private constructor to prevent instantiation of this utility class.
    private ControllerUtils() {}
    /**
     * Method for changing scene when an event (mouse click) happens.
     *
     * @param event The event is mouse click on a button.
     * @param fxmlFile The name of the fxml file that we want to change our scene to.
     */
    public static void changeScene(ActionEvent event, String fxmlFile) {
        Parent root = null;

        try {
            FXMLLoader loader = new FXMLLoader(ControllerUtils.class.getResource("/com/auction/client/view/" + fxmlFile));
            root = loader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }
}
