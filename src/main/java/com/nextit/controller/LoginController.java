package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.User;
import com.nextit.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private AuthenticationService authenticationService;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @FXML
    private void handleLogin() {
        try {
            User user = authenticationService.login(usernameField.getText(), passwordField.getText());
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/nextit/view/MainLayout.fxml"));
            javafx.scene.layout.BorderPane root = loader.load();
            AppController appController = loader.getController();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/com/nextit/css/application.css").toExternalForm());
            javafx.stage.Stage stage = (javafx.stage.Stage) errorLabel.getScene().getWindow();
            stage.setScene(scene);
            appController.initialize(stage, user);
        } catch (AppException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Could not log in. Please check the database connection.");
            alert.show();
        }
    }
}
