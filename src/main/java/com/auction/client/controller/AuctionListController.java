package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class AuctionListController {

    @FXML
    private Button buttonLogout;

    public void handleLogout(ActionEvent event) {
        ControllerUtils.changeScene(event, "Login.fxml");
    }
}
