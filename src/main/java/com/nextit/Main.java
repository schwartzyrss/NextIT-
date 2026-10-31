package com.nextit;

import com.nextit.controller.LoginController;
import com.nextit.repository.JdbcUserRepository;
import com.nextit.service.AuthenticationService;
import com.nextit.util.Alerts;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws Exception {
        installUncaughtExceptionHandler();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nextit/view/Login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setAuthenticationService(new AuthenticationService(new JdbcUserRepository()));
        Scene scene = new Scene(root, 900, 560);
        scene.getStylesheets().add(getClass().getResource("/com/nextit/css/application.css").toExternalForm());
        stage.setTitle("NextIT: Track Your Progress, Build Your Future");
        stage.setScene(scene);
        stage.show();
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
