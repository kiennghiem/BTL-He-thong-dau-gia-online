package com.auction.client.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class LoginControllerTest {

    @Start
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Login.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void testLoginButtonExists(FxRobot robot) {
        verifyThat("#buttonLogin", hasText("Login"));
    }

    @Test
    void testSignupButtonExists(FxRobot robot) {
        verifyThat("#buttonSignUp", hasText("Sign up"));
    }

    @Test
    void testInputFields(FxRobot robot) {
        robot.clickOn("#tfUsername").write("testUser");
        robot.clickOn("#tfPassword").write("password123");
        
        // This just verifies the robot can type. 
        // Real testing would involve mocking ClientManager to check if request is sent.
    }
}
