package com.nextit.controller;

import com.nextit.model.User;
import com.nextit.security.Session;
import com.nextit.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class AppController {

    private static final Logger log = LoggerFactory.getLogger(AppController.class);

    @FXML private Label headerLabel;
    @FXML private Label userLabel;
    @FXML private BorderPane contentPane;
    @FXML private VBox sidebar;
    @FXML private ScrollPane sidebarScroll;
    @FXML private Button toggleSidebarButton;

    private Stage stage;
    private Button activeNavButton;

    public void initialize(Stage stage, User user) {
        this.stage = stage;
        userLabel.setText(user.getFullName() + " (" + user.getRole() + ")");
        if (toggleSidebarButton != null) {
            toggleSidebarButton.setTooltip(new Tooltip("Toggle Sidebar (Ctrl+B)"));
        }
        if (stage != null && stage.getScene() != null) {
            stage.getScene().getAccelerators().put(
                    new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN),
                    this::toggleSidebar);
        }
        buildSidebar(user);
        showDashboard(user);
    }

    @FXML
    private void toggleSidebar() {
        if (sidebarScroll == null) return;
        boolean currentlyVisible = sidebarScroll.isVisible();
        sidebarScroll.setVisible(!currentlyVisible);
        sidebarScroll.setManaged(!currentlyVisible);
        if (toggleSidebarButton != null) {
            toggleSidebarButton.setText(currentlyVisible ? "☰" : "◀");
            toggleSidebarButton.setTooltip(new Tooltip(currentlyVisible ? "Show Sidebar (Ctrl+B)" : "Hide Sidebar (Ctrl+B)"));
        }
    }

    private void buildSidebar(User user) {
        sidebar.getChildren().clear();
        Button dashboard = navButton("Dashboard", () -> showDashboard(user));
        sidebar.getChildren().add(dashboard);
        setActiveButton(dashboard);
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
                sidebar.getChildren().add(categoryHeader("Academic Management"));
                sidebar.getChildren().add(navButton("📖 Subjects", () -> contentPane.setCenter(InstructorViews.subjects())));
                sidebar.getChildren().add(navButton("👥 Students", () -> contentPane.setCenter(InstructorViews.students())));
                sidebar.getChildren().add(navButton("📅 Attendance", () -> contentPane.setCenter(InstructorViews.attendance())));
                sidebar.getChildren().add(navButton("📊 Grades", () -> contentPane.setCenter(InstructorViews.grades())));

                sidebar.getChildren().add(categoryHeader("Evaluations & Skills"));
                sidebar.getChildren().add(navButton("💻 Technical Skills", () -> contentPane.setCenter(InstructorViews.skills())));
                sidebar.getChildren().add(navButton("📋 Technical Evaluations", () -> contentPane.setCenter(InstructorViews.technicalEvaluations())));
                sidebar.getChildren().add(navButton("📑 Project Evaluations", () -> contentPane.setCenter(InstructorViews.evaluations())));

                sidebar.getChildren().add(categoryHeader("Student Showcase & Career"));
                sidebar.getChildren().add(navButton("📁 Portfolios", () -> contentPane.setCenter(InstructorViews.portfolios())));
                sidebar.getChildren().add(navButton("🚀 Projects", () -> contentPane.setCenter(InstructorViews.projects())));
                sidebar.getChildren().add(navButton("🏆 Achievements", () -> contentPane.setCenter(InstructorViews.achievements())));
                sidebar.getChildren().add(navButton("🎓 Certifications", () -> contentPane.setCenter(InstructorViews.certifications())));

                sidebar.getChildren().add(categoryHeader("Support & Growth"));
                sidebar.getChildren().add(navButton("💡 Academic Support", () -> contentPane.setCenter(InstructorViews.support())));
                sidebar.getChildren().add(navButton("🌟 Opportunities", () -> contentPane.setCenter(InstructorViews.opportunities())));

                sidebar.getChildren().add(categoryHeader("Account"));
                sidebar.getChildren().add(navButton("👤 My Profile", () -> contentPane.setCenter(InstructorViews.profile())));
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
                sidebar.getChildren().add(navButton("Grades", () -> contentPane.setCenter(AdminViews.grades())));
                sidebar.getChildren().add(navButton("Attendance", () -> contentPane.setCenter(AdminViews.attendance())));
                sidebar.getChildren().add(navButton("Technical Skills", () -> contentPane.setCenter(AdminViews.skills())));
                sidebar.getChildren().add(navButton("Technical Evaluations", () -> contentPane.setCenter(AdminViews.technicalEvaluations())));
                sidebar.getChildren().add(navButton("Projects", () -> contentPane.setCenter(AdminViews.projects())));
                sidebar.getChildren().add(navButton("Project Evaluations", () -> contentPane.setCenter(AdminViews.projectEvaluations())));
                sidebar.getChildren().add(navButton("Achievements", () -> contentPane.setCenter(AdminViews.achievements())));
                sidebar.getChildren().add(navButton("Certifications", () -> contentPane.setCenter(AdminViews.certifications())));
                sidebar.getChildren().add(navButton("Academic Support", () -> contentPane.setCenter(AdminViews.academicSupport())));
                sidebar.getChildren().add(navButton("Students", () -> contentPane.setCenter(AdminViews.students())));
                sidebar.getChildren().add(navButton("Instructors", () -> contentPane.setCenter(AdminViews.instructors())));
                sidebar.getChildren().add(navButton("Employers", () -> contentPane.setCenter(AdminViews.employers())));
                sidebar.getChildren().add(navButton("Opportunities", () -> contentPane.setCenter(AdminViews.opportunities())));
                sidebar.getChildren().add(navButton("Applications", () -> contentPane.setCenter(AdminViews.applications())));
                sidebar.getChildren().add(navButton("Portfolios", () -> contentPane.setCenter(AdminViews.portfolios())));
                sidebar.getChildren().add(navButton("System", () -> contentPane.setCenter(AdminViews.system())));
            }
        }

        Region spacer = new Region();
        spacer.setMinHeight(10);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Button logout = new Button("🚪 Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.getStyleClass().addAll("sidebar-button", "sidebar-logout-button");
        logout.setOnAction(e -> runSafely(this::confirmAndLogout));
        sidebar.getChildren().add(logout);
    }

    private Label categoryHeader(String text) {
        Label l = new Label(text.toUpperCase(Locale.ROOT));
        l.getStyleClass().add("sidebar-category-header");
        return l;
    }

    private Button navButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add("sidebar-button");
        b.setOnAction(e -> {
            setActiveButton(b);
            runSafely(action);
        });
        return b;
    }

    private void setActiveButton(Button b) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("sidebar-button-active");
        }
        activeNavButton = b;
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().add("sidebar-button-active");
        }
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            log.error("Sidebar action failed", ex);
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

    private void confirmAndLogout() {
        if (!com.nextit.util.Alerts.confirmLogout()) {
            return;
        }
        logout();
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
        } catch (Exception ex) {
            log.error("Could not return to the login screen", ex);
            com.nextit.util.Alerts.error("Could not return to login.");
        }
    }
}