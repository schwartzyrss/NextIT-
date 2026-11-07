package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.User;
import com.nextit.repository.JdbcUserRepository;
import com.nextit.service.AuthenticationService;
import com.nextit.service.OtpService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private VBox passwordBox;
    @FXML private VBox otpBox;
    @FXML private Button passwordTabBtn;
    @FXML private Button otpTabBtn;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Label errorLabel;

    @FXML private TextField otpEmailField;
    @FXML private TextField otpCodeField;
    @FXML private Button sendOtpBtn;
    @FXML private Button verifyOtpBtn;
    @FXML private Label otpStatusLabel;

    private AuthenticationService authenticationService;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @FXML
    public void showPasswordTab() {
        passwordBox.setVisible(true);
        passwordBox.setManaged(true);
        otpBox.setVisible(false);
        otpBox.setManaged(false);
        passwordTabBtn.getStyleClass().removeAll("clay-tab-inactive");
        if (!passwordTabBtn.getStyleClass().contains("clay-tab-active")) {
            passwordTabBtn.getStyleClass().add("clay-tab-active");
        }
        otpTabBtn.getStyleClass().removeAll("clay-tab-active");
        if (!otpTabBtn.getStyleClass().contains("clay-tab-inactive")) {
            otpTabBtn.getStyleClass().add("clay-tab-inactive");
        }
        errorLabel.setText("");
    }

    @FXML
    public void showOtpTab() {
        passwordBox.setVisible(false);
        passwordBox.setManaged(false);
        otpBox.setVisible(true);
        otpBox.setManaged(true);
        otpTabBtn.getStyleClass().removeAll("clay-tab-inactive");
        if (!otpTabBtn.getStyleClass().contains("clay-tab-active")) {
            otpTabBtn.getStyleClass().add("clay-tab-active");
        }
        passwordTabBtn.getStyleClass().removeAll("clay-tab-active");
        if (!passwordTabBtn.getStyleClass().contains("clay-tab-inactive")) {
            passwordTabBtn.getStyleClass().add("clay-tab-inactive");
        }
        errorLabel.setText("");
        if (otpStatusLabel != null) {
            otpStatusLabel.setText("");
        }
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void togglePassword() {
        boolean show = showPasswordCheck.isSelected();
        if (show) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setManaged(true);
            visiblePasswordField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            visiblePasswordField.requestFocus();
            visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    private String enteredPassword() {
        return showPasswordCheck.isSelected() ? visiblePasswordField.getText() : passwordField.getText();
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void handleLogin() {
        try {
            User user = authenticationService.login(usernameField.getText(), enteredPassword());
            transitionToMain(user);
        } catch (AppException e) {
            com.nextit.security.Session.logout();
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            log.error("Login failed due to an unexpected error", e);
            com.nextit.security.Session.logout();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Could not log in. Please check the database connection.");
            alert.show();
        }
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void handleSendOtp() {
        String email = otpEmailField.getText();
        if (email == null || email.isBlank() || !email.contains("@")) {
            otpStatusLabel.setStyle("-fx-text-fill: #991b1b;");
            otpStatusLabel.setText("Please enter a valid email address.");
            return;
        }

        sendOtpBtn.setDisable(true);
        otpStatusLabel.setStyle("-fx-text-fill: #4b5563;");
        otpStatusLabel.setText("Dispatching 6-digit verification code...");

        CompletableFuture.supplyAsync(() -> OtpService.requestOtp(email))
                .thenAccept(resp -> Platform.runLater(() -> {
                    sendOtpBtn.setDisable(false);
                    if (resp.sent()) {
                        otpStatusLabel.setStyle("-fx-text-fill: #065f46;");
                        otpStatusLabel.setText("Confirmation code sent to " + email.trim() + "!");
                        otpCodeField.requestFocus();
                    } else if (!resp.configured()) {
                        otpStatusLabel.setStyle("-fx-text-fill: #b45309;");
                        otpStatusLabel.setText("Dev Mode: Code is " + resp.code() + " (or 123456)");
                        otpCodeField.setText(resp.code());
                    } else {
                        otpStatusLabel.setStyle("-fx-text-fill: #991b1b;");
                        otpStatusLabel.setText(resp.message() != null ? resp.message() : "Failed to deliver code.");
                    }
                }));
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void handleOtpLogin() {
        String email = otpEmailField.getText();
        String code = otpCodeField.getText();
        if (email == null || email.isBlank()) {
            otpStatusLabel.setStyle("-fx-text-fill: #991b1b;");
            otpStatusLabel.setText("Please enter your registered email.");
            return;
        }
        if (code == null || code.trim().length() < 6) {
            otpStatusLabel.setStyle("-fx-text-fill: #991b1b;");
            otpStatusLabel.setText("Please enter the 6-digit code.");
            return;
        }

        boolean valid = OtpService.verifyOtp(email, code);
        if (!valid) {
            otpStatusLabel.setStyle("-fx-text-fill: #991b1b;");
            otpStatusLabel.setText("Invalid or expired code. Please request a new one.");
            return;
        }

        try {
            JdbcUserRepository userRepo = new JdbcUserRepository();
            Optional<User> userOpt = userRepo.findByEmail(email.trim());
            if (userOpt.isEmpty()) {
                otpStatusLabel.setStyle("-fx-text-fill: #991b1b;");
                otpStatusLabel.setText("No account found for " + email.trim() + ". Please Create an Account first.");
                return;
            }

            User user = userOpt.get();
            com.nextit.security.Session.login(user);
            transitionToMain(user);
        } catch (Exception e) {
            log.error("Failed to login with OTP", e);
            otpStatusLabel.setStyle("-fx-text-fill: #991b1b;");
            otpStatusLabel.setText("Error during login: " + e.getMessage());
        }
    }

    private void transitionToMain(User user) throws Exception {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/nextit/view/MainLayout.fxml"));
        javafx.scene.layout.BorderPane root = loader.load();
        AppController appController = loader.getController();
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/com/nextit/css/application.css").toExternalForm());
        Stage stage = (Stage) errorLabel.getScene().getWindow();
        stage.setScene(scene);
        appController.initialize(stage, user);
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void handleOpenOnboarding() {
        try {
            Stage stage = (Stage) errorLabel.getScene().getWindow();
            OnboardingView.open(stage);
        } catch (Exception e) {
            log.error("Failed to open onboarding flow", e);
        }
    }
}
