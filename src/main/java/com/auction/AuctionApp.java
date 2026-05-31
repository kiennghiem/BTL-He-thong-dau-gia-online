package com.auction;

import com.auction.client.network.ClientManager;
import com.auction.server.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Attempt to connect to the server on startup
        try {
            ClientManager.getInstance().connect();
        } catch (IOException e) {
            System.err.println("[CLIENT] Could not connect to server on startup: " + e.getMessage());
            // We can continue, ClientManager will try to reconnect when sending first request
        }

        Parent root = FXMLLoader.load(getClass().getResource("/com/auction/client/view/Login.fxml"));
        Scene scene = new Scene(root);

        stage.setTitle("Online Auction system!");
        stage.setResizable(false);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        ClientManager.getInstance().close();
        DatabaseManager.getInstance().shutdown();
    }
}