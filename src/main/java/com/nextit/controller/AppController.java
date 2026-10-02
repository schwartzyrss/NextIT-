package com.nextit.controller;

import com.nextit.model.User;
import com.nextit.security.Session;
import com.nextit.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AppController {

    @FXML private Label headerLabel;
    @FXML private Label userLabel;
    @FXML private BorderPane contentPane;
    @FXML private VBox sidebar;

    private Stage stage;

    public void initialize(Stage stage, User user) {
        this.stage = stage;
        userLabel.setText(user.getFullName() + " (" + user.getRole() + ")");
        buildSidebar(user);
        showDashboard(user);
    }

    private void buildSidebar(User user) {
        sidebar.getChildren().clear();
        Button dashboard = navButton("Dashboard", () -> showDashboard(user));
        sidebar.getChildren().add(dashboard);
        switch (user.getRole()) {
            case STUDENT -> {
                sidebar.getChildren().add(navButton("Academic", () -> contentPane.setCenter(StudentViews.academic())));
                sidebar.getChildren().add(navButton("Skills", () -> contentPane.setCenter(StudentViews.skills())));
                sidebar.getChildren().add(navButton("Projects", () -> contentPane.setCenter(StudentViews.projects())));
                sidebar.getChildren().add(navButton("Portfolio", () -> contentPane.setCenter(StudentViews.portfolio())));
                sidebar.getChildren().add(navButton("Opportunities", () -> contentPane.setCenter(StudentViews.opportunities())));
                sidebar.getChildren().add(navButton("Applications", () -> contentPane.setCenter(StudentViews.applications())));
            }
            case INSTRUCTOR -> {
                sidebar.getChildren().add(navButton("Grades", () -> contentPane.setCenter(InstructorViews.grades())));
                sidebar.getChildren().add(navButton("Attendance", () -> contentPane.setCenter(InstructorViews.attendance())));
                sidebar.getChildren().add(navButton("Evaluations", () -> contentPane.setCenter(InstructorViews.evaluations())));
                sidebar.getChildren().add(navButton("Academic Support", () -> contentPane.setCenter(InstructorViews.support())));
            }
            case EMPLOYER -> {
                sidebar.getChildren().add(navButton("Opportunities", () -> contentPane.setCenter(EmployerViews.opportunities())));
                sidebar.getChildren().add(navButton("Applications", () -> contentPane.setCenter(EmployerViews.applications())));
                sidebar.getChildren().add(navButton("Portfolios", () -> contentPane.setCenter(EmployerViews.portfolios())));
            }
            case ADMIN -> {
                sidebar.getChildren().add(navButton("Users", () -> contentPane.setCenter(AdminViews.users())));
                sidebar.getChildren().add(navButton("Subjects", () -> contentPane.setCenter(AdminViews.subjects())));
                sidebar.getChildren().add(navButton("Technical Skills", () -> contentPane.setCenter(AdminViews.skills())));
            }
        }
        Button logout = navButton("Logout", this::logout);
        sidebar.getChildren().add(logout);
    }

    private Button navButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> { try { action.run(); } catch (Exception ex) { com.nextit.util.Alerts.error(ex.getMessage()); } });
        return b;
    }

    private void showDashboard(User user) {
        switch (user.getRole()) {
            case STUDENT -> contentPane.setCenter(StudentViews.dashboard());
            case INSTRUCTOR -> contentPane.setCenter(InstructorViews.dashboard());
            case EMPLOYER -> contentPane.setCenter(EmployerViews.dashboard());
            case ADMIN -> contentPane.setCenter(AdminViews.dashboard());
        }
    }

    private void logout() {
        Session.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nextit/view/Login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            scene.getStylesheets().add(getClass().getResource("/com/nextit/css/application.css").toExternalForm());
            LoginController controller = loader.getController();
            controller.setAuthenticationService(new AuthenticationService(new com.nextit.repository.JdbcUserRepository()));
            stage.setScene(scene);
        } catch (Exception e) {
            com.nextit.util.Alerts.error("Could not return to login.");
        }
    }
}
