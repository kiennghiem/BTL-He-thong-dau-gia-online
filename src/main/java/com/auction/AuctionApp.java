package com.auction;

import com.auction.server.database.DatabaseConnection2;
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
        Parent root = FXMLLoader.load(getClass().getResource("view/Login.fxml"));
        Scene scene = new Scene(root);

        stage.setTitle("Online Auction system!");
        stage.setResizable(false);

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection2.shutdown();
    }
}
