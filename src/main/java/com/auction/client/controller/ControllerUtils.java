package com.auction.client.controller;

import com.auction.server.database.dao.impl.UserDAOImpl;
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
            FXMLLoader loader = new FXMLLoader(ControllerUtils.class.getResource("/com/auction/view/" + fxmlFile));
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


    /**
     * Method to handle Sign up logic: check if the given username exists in the database, if not, then create a new
     * user in the database with the given username, password, and role.
     *
     * @param event The event is mouse click on the Sign up button.
     * @param username
     * @param password
     * @param role
     */
    public static void signupUser(ActionEvent event, String username, String password, String role) {
        Connection connection = null;

        // Statement used to query database to add new user into table.
        PreparedStatement psInsert = null;

        // Statement used to query the database to find the user with given username.
        PreparedStatement psCheckUserExists = null;

        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");

            psCheckUserExists = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            psCheckUserExists.setString(1, username);
            resultSet = psCheckUserExists.executeQuery();

            // Neu username da ton tai: resultSet has 1 row, and isBeforeFirst() returns true.
            if (resultSet.isBeforeFirst()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("You cannot use this username");
                alert.show();
            }
            else { // Neu username CHUA ton tai: resultSet is empty, and isBeforeFirst() returns false.

                // HAY GOI REGISTERUSER() O DAY!

                psInsert = connection.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
                psInsert.setString(1, username);
                psInsert.setString(2, password);
                psInsert.setString(3, role);

                // Execute query cua statement da gan cho psInsert, khong can return ket qua gi vi chi add
                // thong tin user vao database ma khong lay du lieu hay check xem co du lieu hay khong.
                psInsert.executeUpdate();

                changeScene(event, "AuctionList.fxml");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally { // Close all connection to database (if there is any, which will make the variable not null).
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (psInsert != null) {
                try {
                    psInsert.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (psCheckUserExists != null) {
                try {
                    psCheckUserExists.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
