package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class AuctionListController implements Initializable {

    @FXML
    private Button buttonLogout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        buttonLogout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ControllerUtils.changeScene(event, "Login.fxml");
            }
        });
    }
}
