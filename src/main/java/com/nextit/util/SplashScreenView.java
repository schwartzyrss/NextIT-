package com.nextit.util;

import com.nextit.config.DatabaseConfig;
import com.nextit.config.EmbeddedDatabaseManager;
import com.nextit.controller.LoginController;
import com.nextit.repository.JdbcUserRepository;
import com.nextit.service.AuthenticationService;
import com.nextit.service.SystemService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * Branded NextIT desktop splash screen with background database health validation
 * and smooth transition to the main login/dashboard experience.
 */
public final class SplashScreenView {

    private static final Logger log = LoggerFactory.getLogger(SplashScreenView.class);

    private SplashScreenView() {}

    public static void showAndInitialize(Stage stage) {
        WindowBoundsManager.configureStage(stage);

        // Build Claymorphic Splash Card
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(460);
        card.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #FFFFFF 0%, #FDFBF7 60%, #F6F1E7 100%);"
                        + "-fx-background-radius: 28;"
                        + "-fx-padding: 44 36;"
                        + "-fx-effect: dropshadow(gaussian, rgba(56, 49, 57, 0.12), 24, 0.15, 0, 8);"
        );

        // Pastel lavender-periwinkle gradient logo orb
        Region logoOrb = new Region();
        logoOrb.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #C4B5FD 0%, #818CF8 100%);"
                        + "-fx-background-radius: 999;"
                        + "-fx-min-width: 64; -fx-min-height: 64; -fx-max-width: 64; -fx-max-height: 64;"
                        + "-fx-effect: dropshadow(gaussian, rgba(129, 140, 248, 0.45), 16, 0.2, 0, 4);"
        );

        Label titleLabel = new Label("NextIT");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #1E293B;");

        Label subtitleLabel = new Label("Track Your Progress, Build Your Future");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

        VBox brandBox = new VBox(6, logoOrb, titleLabel, subtitleLabel);
        brandBox.setAlignment(Pos.CENTER);

        // Status Message & Progress Indicator
        Label statusLabel = new Label("Initializing desktop application environment...");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        ProgressBar progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setPrefWidth(320);
        progressBar.setPrefHeight(6);
        progressBar.setStyle("-fx-accent: #F472B6;");

        // Trust chip
        HBox trustChip = new HBox(8);
        trustChip.setAlignment(Pos.CENTER);
        trustChip.setStyle(
                "-fx-background-color: rgba(16, 185, 129, 0.10);"
                        + "-fx-background-radius: 999;"
                        + "-fx-padding: 4 14;"
        );
        Region trustDot = new Region();
        trustDot.setStyle("-fx-background-color: #10B981; -fx-background-radius: 999; -fx-min-width: 7; -fx-min-height: 7; -fx-max-width: 7; -fx-max-height: 7;");
        Label trustText = new Label("Desktop Client • Standalone Edition");
        trustText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #065F46;");
        trustChip.getChildren().addAll(trustDot, trustText);

        card.getChildren().addAll(brandBox, progressBar, statusLabel, trustChip);

        StackPane root = new StackPane(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F3F5FA;");

        Scene splashScene = new Scene(root, stage.getWidth(), stage.getHeight());
        splashScene.getStylesheets().add(SplashScreenView.class.getResource("/com/nextit/css/application.css").toExternalForm());
        WindowBoundsManager.installDesktopEventFilters(splashScene);

        stage.setScene(splashScene);
        stage.show();

        // Intercept close event for clean background shutdown
        stage.setOnCloseRequest(e -> {
            e.consume();
            shutdownApplication(stage);
        });

        // Run background initialization
        Task<Boolean> initTask = new Task<>() {
            @Override
            protected Boolean call() {
                try {
                    updateMessage("Loading application settings and environment...");
                    Thread.sleep(400);

                    updateMessage("Connecting to database and validating schema...");
                    boolean dbOk = false;
                    try (Connection conn = DatabaseConfig.getConnection()) {
                        dbOk = (conn != null && conn.isValid(2));
                    } catch (Exception e) {
                        log.warn("Direct connection check failed, trying embedded manager: {}", e.getMessage());
                        try {
                            EmbeddedDatabaseManager.ensureStarted();
                            try (Connection conn = DatabaseConfig.getConnection()) {
                                dbOk = (conn != null && conn.isValid(2));
                            }
                        } catch (Exception ex) {
                            log.error("Failed to start fallback database: {}", ex.getMessage());
                        }
                    }

                    updateMessage(dbOk ? "Database connected! Launching NextIT..." : "Offline mode ready. Launching NextIT...");
                    Thread.sleep(350);
                    return dbOk;
                } catch (Exception e) {
                    log.error("Error during splash initialization", e);
                    return false;
                }
            }
        };

        statusLabel.textProperty().bind(initTask.messageProperty());

        initTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                FadeTransition fade = new FadeTransition(Duration.millis(300), card);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(ev -> transitionToLogin(stage));
                fade.play();
            });
        });

        initTask.setOnFailed(e -> {
            Platform.runLater(() -> transitionToLogin(stage));
        });

        Thread thread = new Thread(initTask, "nextit-splash-init");
        thread.setDaemon(true);
        thread.start();
    }

    private static void transitionToLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(SplashScreenView.class.getResource("/com/nextit/view/Login.fxml"));
            Parent loginRoot = loader.load();
            LoginController controller = loader.getController();
            controller.setAuthenticationService(new AuthenticationService(new JdbcUserRepository()));

            Scene loginScene = new Scene(loginRoot, stage.getWidth(), stage.getHeight());
            loginScene.getStylesheets().add(SplashScreenView.class.getResource("/com/nextit/css/application.css").toExternalForm());
            WindowBoundsManager.installDesktopEventFilters(loginScene);

            stage.setScene(loginScene);
            log.info("Transitioned from splash screen to main Login scene.");
        } catch (Exception e) {
            log.error("Failed to load Login scene", e);
        }
    }

    public static void shutdownApplication(Stage stage) {
        log.info("Gracefully terminating NextIT background services and closing resources...");
        try {
            WindowBoundsManager.saveBounds(stage);
        } catch (Exception ignored) {}

        try {
            SystemService.getInstance().stop();
        } catch (Exception ignored) {}

        try {
            EmbeddedDatabaseManager.stop();
        } catch (Exception ignored) {}

        Platform.exit();
        System.exit(0);
    }
}
