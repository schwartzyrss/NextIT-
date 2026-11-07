package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.Employer;
import com.nextit.model.Instructor;
import com.nextit.model.Role;
import com.nextit.model.Student;
import com.nextit.model.User;
import com.nextit.repository.JdbcEmployerRepository;
import com.nextit.repository.JdbcInstructorRepository;
import com.nextit.repository.JdbcStudentRepository;
import com.nextit.repository.JdbcUserRepository;
import com.nextit.security.Session;
import com.nextit.service.AuthenticationService;
import com.nextit.service.EmailService;
import com.nextit.service.OtpService;
import com.nextit.util.Alerts;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class OnboardingView {

    private static final Logger log = LoggerFactory.getLogger(OnboardingView.class);

    public static void open(Stage stage) {
        try {
            WebView webView = new WebView();
            WebEngine engine = webView.getEngine();
            engine.setJavaScriptEnabled(true);

            JavaBridge bridge = new JavaBridge(stage, engine);

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    try {
                        JSObject window = (JSObject) engine.executeScript("window");
                        window.setMember("javaBridge", bridge);
                    } catch (Exception e) {
                        log.error("Failed to inject JavaBridge into WebView", e);
                    }
                }
            });

            URL htmlUrl = OnboardingView.class.getResource("/com/nextit/view/onboarding.html");
            if (htmlUrl == null) {
                throw new AppException("Could not find onboarding.html resource");
            }
            engine.load(htmlUrl.toExternalForm());

            StackPane root = new StackPane(webView);
            webView.prefWidthProperty().bind(root.widthProperty());
            webView.prefHeightProperty().bind(root.heightProperty());

            Scene scene = new Scene(root, 1150, 750);
            stage.setTitle("NextIT: Account Onboarding & Role Selection");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            log.error("Failed to open Onboarding View", e);
            Alerts.error("Could not load onboarding screen: " + e.getMessage());
        }
    }

    public static class JavaBridge {
        private final Stage stage;
        private final WebEngine engine;

        public JavaBridge(Stage stage, WebEngine engine) {
            this.stage = stage;
            this.engine = engine;
        }

        public String requestOtp(String email) {
            OtpService.OtpResponse resp = OtpService.requestOtp(email);
            String safeMsg = resp.message() != null ? resp.message().replace("\"", "\\\"").replace("\n", " ") : "";
            return String.format("{\"sent\":%b,\"configured\":%b,\"code\":\"%s\",\"rateLimited\":%b,\"message\":\"%s\"}",
                    resp.sent(), resp.configured(), resp.code(), resp.rateLimited(), safeMsg);
        }

        public boolean verifyOtp(String email, String enteredCode) {
            return OtpService.verifyOtp(email, enteredCode);
        }

        public String saveSmtpSettings(String host, String port, String user, String pass, String from, String emailToTest) {
            try {
                EmailService.updateAndPersistConfig(host, port, user, pass, from);
                String targetEmail = (emailToTest != null && !emailToTest.isBlank()) ? emailToTest.trim() : user.trim();
                OtpService.OtpResponse resp = OtpService.requestOtp(targetEmail);
                if (resp.sent()) {
                    return String.format("{\"success\":true,\"message\":\"SMTP configured successfully! Code sent to %s\",\"code\":\"%s\"}",
                            targetEmail, resp.code());
                } else {
                    String safeErr = (resp.message() != null ? resp.message() : "Failed to deliver email").replace("\"", "\\\"").replace("\n", " ");
                    return String.format("{\"success\":false,\"message\":\"%s\"}", safeErr);
                }
            } catch (Exception e) {
                String safeErr = (e.getMessage() != null ? e.getMessage() : e.toString()).replace("\"", "\\\"").replace("\n", " ");
                return String.format("{\"success\":false,\"message\":\"%s\"}", safeErr);
            }
        }

        public void registerAndLaunch(String roleStr, String username, String firstName, String lastName,
                                      String email, String password, String detail1, String detail2, String detail3) {
            // Run database queries on a background thread to prevent UI freezing
            CompletableFuture.runAsync(() -> {
                try {
                    Role role = Role.valueOf(roleStr.trim().toUpperCase());
                    String uName = (username != null && !username.isBlank())
                            ? username.trim()
                            : (email != null && email.contains("@") ? email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "") : "user" + System.currentTimeMillis() % 10000);

                    JdbcUserRepository userRepo = new JdbcUserRepository();

                    Optional<User> existing = userRepo.findByUsername(uName);
                    if (existing.isEmpty() && email != null && !email.isBlank()) {
                        existing = userRepo.findByEmail(email.trim());
                    }

                    User user;
                    if (existing.isPresent()) {
                        user = existing.get();
                        // Validate credentials if user already exists
                        if (password != null && !password.isBlank() && !AuthenticationService.verifyPassword(password, user.getPasswordHash())) {
                            throw new AppException("An account with this username or email already exists. Please verify your password or sign in directly.");
                        }
                        log.info("User already exists with valid credentials, logging into existing account: {}", user.getUsername());
                    } else {
                        user = new User();
                        user.setUsername(uName);
                        user.setPasswordHash(AuthenticationService.hashPassword(password != null && !password.isBlank() ? password : "Password123!"));
                        user.setEmail(email != null ? email.trim() : uName + "@nextit.edu");
                        user.setRole(role);
                        user.setFirstName(firstName != null && !firstName.isBlank() ? firstName.trim() : "New");
                        user.setLastName(lastName != null && !lastName.isBlank() ? lastName.trim() : "Member");

                        userRepo.save(user);

                        // Save role-specific details
                        if (role == Role.STUDENT) {
                            Student s = new Student();
                            s.setUserId(user.getUserId());
                            s.setStudentNumber(detail3 != null && !detail3.isBlank() ? detail3.trim() : "2026-" + String.format("%04d", user.getUserId()));
                            s.setProgram("BS Information Technology");
                            int yr = 1;
                            try {
                                if (detail2 != null && !detail2.isBlank()) {
                                    yr = Integer.parseInt(detail2.replaceAll("\\D", ""));
                                }
                            } catch (Exception ignored) {}
                            s.setYearLevel(yr > 0 ? yr : 1);
                            s.setSection("A");
                            s.setEnrollmentStatus("Enrolled");
                            new JdbcStudentRepository().save(s);
                        } else if (role == Role.INSTRUCTOR) {
                            Instructor inst = new Instructor();
                            inst.setUserId(user.getUserId());
                            inst.setEmployeeNumber(detail3 != null && !detail3.isBlank() ? detail3.trim() : "EMP-" + String.format("%04d", user.getUserId()));
                            inst.setDepartment(detail2 != null && !detail2.isBlank() ? detail2.trim() : "Computer Science");
                            inst.setSpecialization("Software Engineering");
                            new JdbcInstructorRepository().save(inst);
                        } else if (role == Role.EMPLOYER) {
                            Employer emp = new Employer();
                            emp.setUserId(user.getUserId());
                            emp.setCompanyName(detail1 != null && !detail1.isBlank() ? detail1.trim() : "NextIT Industry Partner");
                            emp.setIndustry("Information Technology");
                            emp.setContactPerson(user.getFullName());
                            emp.setContactNumber("+1-555-0199");
                            emp.setAddress("Technology Campus");
                            new JdbcEmployerRepository().save(emp);
                        }
                    }

                    // Authenticate session
                    Session.login(user);

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nextit/view/MainLayout.fxml"));
                            BorderPane root = loader.load();
                            AppController appController = loader.getController();
                            Scene scene = new Scene(root, 1100, 700);
                            scene.getStylesheets().add(getClass().getResource("/com/nextit/css/application.css").toExternalForm());
                            stage.setTitle("NextIT: Track Your Progress, Build Your Future");
                            stage.setScene(scene);
                            stage.centerOnScreen();
                            appController.initialize(stage, user);
                        } catch (Exception e) {
                            log.error("Failed to load main layout view", e);
                            Alerts.error("Could not load dashboard: " + e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    log.error("Failed to complete registration and launch dashboard", e);
                    Platform.runLater(() -> Alerts.error("Registration failed: " + e.getMessage()));
                }
            });
        }

        public void backToLogin() {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nextit/view/Login.fxml"));
                    Parent root = loader.load();
                    LoginController controller = loader.getController();
                    controller.setAuthenticationService(new AuthenticationService(new JdbcUserRepository()));
                    Scene scene = new Scene(root, 900, 560);
                    scene.getStylesheets().add(getClass().getResource("/com/nextit/css/application.css").toExternalForm());
                    stage.setTitle("NextIT: Track Your Progress, Build Your Future");
                    stage.setScene(scene);
                    stage.centerOnScreen();
                } catch (Exception e) {
                    log.error("Failed to return to login", e);
                }
            });
        }
    }
}
