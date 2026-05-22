package com.auction.server.database;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class DBLoginSignupUtils {

    /**
     * Method for changing scene when an event (mouse click) happens.
     *
     * @param event The event is mouse click on a button.
     * @param fxmlFile The name of the fxml file that we want to change our scene to.
     */
    public static void changeScene(ActionEvent event, String fxmlFile) {
        Parent root = null;

        try {
            FXMLLoader loader = new FXMLLoader(DBLoginSignupUtils.class.getResource("/com/auction/view/" + fxmlFile));
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
     * Method to handle the Login logic: check if the given username exists in the database, if yes, then check if
     * the given password matches the password associated with the username in the database.
     *
     * @param event The event is mouse click on the Login button.
     * @param username
     * @param password
     */
    public static void loginUser(ActionEvent event, String username, String password) {
        // The connection to the database.
        Connection connection = null;

        // Statement used to query the database to find the user with given username.
        PreparedStatement preparedStatement = null;

        // The result set of rows that the database returns when we query it (ex: if we find 3 rows with the usernames
        // that match the username given, the result set is a table with only that 3 rows).
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");

            // Declare the query for preparedStatement.
            preparedStatement = connection.prepareStatement("SELECT password FROM users WHERE username = ?");
            preparedStatement.setString(1, username);

            // Execute the query, and retrieve the results to resultSet.
            resultSet = preparedStatement.executeQuery();

            // Neu username chua ton tai: resultSet is empty, and isBeforeFirst() returns false.
            if (!resultSet.isBeforeFirst()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Provided credentials are incorrect");
                alert.show();
            }
            else { // Neu username da ton tai, thi ta phai check xem password nhap vao co khop voi password lay tu database khong.
                while (resultSet.next()) {
                    String retrievedPassword = resultSet.getString("password");

                    if (password.equals(retrievedPassword)) {
                        changeScene(event, "AuctionList.fxml");
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Provided credentials are incorrect");
                        alert.show();
                    }
                }
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
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
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
