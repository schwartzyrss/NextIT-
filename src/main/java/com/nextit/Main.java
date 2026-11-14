package com.nextit;

import com.nextit.util.Alerts;
import com.nextit.util.SplashScreenView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage stage) {
        installUncaughtExceptionHandler();
        SplashScreenView.showAndInitialize(stage);
    }

    private static void installUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Unhandled exception on thread {}", thread.getName(), throwable);
            Runnable alert = () -> Alerts.error(
                    "An unexpected error occurred. The application will keep running, but the last action may not have completed. "
                            + "Details were written to the application log.");
            if (Platform.isFxApplicationThread()) alert.run();
            else Platform.runLater(alert);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
