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
                sidebar.getChildren().add(navButton("Profile", () -> contentPane.setCenter(StudentViews.profile())));
                sidebar.getChildren().add(navButton("Academic", () -> contentPane.setCenter(StudentViews.academic())));
                sidebar.getChildren().add(navButton("Skills", () -> contentPane.setCenter(StudentViews.skills())));
                sidebar.getChildren().add(navButton("Projects", () -> contentPane.setCenter(StudentViews.projects())));
                sidebar.getChildren().add(navButton("Achievements", () -> contentPane.setCenter(StudentViews.achievements())));
                sidebar.getChildren().add(navButton("Certifications", () -> contentPane.setCenter(StudentViews.certifications())));
                sidebar.getChildren().add(navButton("Portfolio", () -> contentPane.setCenter(StudentViews.portfolio())));
                sidebar.getChildren().add(navButton("Opportunities", () -> contentPane.setCenter(StudentViews.opportunities())));
                sidebar.getChildren().add(navButton("Applications", () -> contentPane.setCenter(StudentViews.applications())));
            }
            case INSTRUCTOR -> {
                sidebar.getChildren().add(navButton("Profile", () -> contentPane.setCenter(InstructorViews.profile())));
                sidebar.getChildren().add(navButton("Students", () -> contentPane.setCenter(InstructorViews.students())));
                sidebar.getChildren().add(navButton("Subjects", () -> contentPane.setCenter(InstructorViews.subjects())));
                sidebar.getChildren().add(navButton("Technical Skills", () -> contentPane.setCenter(InstructorViews.skills())));
                sidebar.getChildren().add(navButton("Grades", () -> contentPane.setCenter(InstructorViews.grades())));
                sidebar.getChildren().add(navButton("Attendance", () -> contentPane.setCenter(InstructorViews.attendance())));
                sidebar.getChildren().add(navButton("Technical Evaluations", () -> contentPane.setCenter(InstructorViews.technicalEvaluations())));
                sidebar.getChildren().add(navButton("Projects", () -> contentPane.setCenter(InstructorViews.projects())));
                sidebar.getChildren().add(navButton("Project Evaluations", () -> contentPane.setCenter(InstructorViews.evaluations())));
                sidebar.getChildren().add(navButton("Academic Support", () -> contentPane.setCenter(InstructorViews.support())));
            }
            case EMPLOYER -> {
                sidebar.getChildren().add(navButton("Company Profile", () -> contentPane.setCenter(EmployerViews.profile())));
                sidebar.getChildren().add(navButton("Opportunities", () -> contentPane.setCenter(EmployerViews.opportunities())));
                sidebar.getChildren().add(navButton("Applications", () -> contentPane.setCenter(EmployerViews.applications())));
                sidebar.getChildren().add(navButton("Portfolio Search", () -> contentPane.setCenter(EmployerViews.portfolioSearch())));
            }
            case ADMIN -> {
                sidebar.getChildren().add(navButton("Profile", () -> contentPane.setCenter(AdminViews.profile())));
                sidebar.getChildren().add(navButton("Users", () -> contentPane.setCenter(AdminViews.users())));
                sidebar.getChildren().add(navButton("Subjects", () -> contentPane.setCenter(AdminViews.subjects())));
                sidebar.getChildren().add(navButton("Enrollments", () -> contentPane.setCenter(AdminViews.enrollments())));
                sidebar.getChildren().add(navButton("Technical Skills", () -> contentPane.setCenter(AdminViews.skills())));
                sidebar.getChildren().add(navButton("Students", () -> contentPane.setCenter(AdminViews.students())));
                sidebar.getChildren().add(navButton("Instructors", () -> contentPane.setCenter(AdminViews.instructors())));
                sidebar.getChildren().add(navButton("Employers", () -> contentPane.setCenter(AdminViews.employers())));
                sidebar.getChildren().add(navButton("Portfolios", () -> contentPane.setCenter(AdminViews.portfolios())));
                sidebar.getChildren().add(navButton("System", () -> contentPane.setCenter(AdminViews.system())));
            }
        }
        Button logout = navButton("Logout", this::logout);
        sidebar.getChildren().add(logout);
    }

    private Button navButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> runSafely(action));
        return b;
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            String message = ex.getMessage();
            com.nextit.util.Alerts.error(message == null || message.isBlank()
                    ? "The action could not be completed." : message);
        }
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
