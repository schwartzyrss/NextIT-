package com.nextit;

import com.nextit.controller.LoginController;
import com.nextit.repository.JdbcUserRepository;
import com.nextit.service.AuthenticationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
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

    public static void main(String[] args) {
        launch(args);
    }
}
