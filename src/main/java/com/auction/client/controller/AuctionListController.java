package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class AuctionListController {

    @FXML
    private Button buttonLogout;

    public void handleLogout(ActionEvent event) {
        ControllerUtils.changeScene(event, "Login.fxml");
    }
}
