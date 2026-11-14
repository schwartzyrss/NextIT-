package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.security.Session;
import com.nextit.service.*;
import com.nextit.util.Alerts;
import com.nextit.util.Ui;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StudentViews {

    private static final AcademicService academic = new AcademicService();
    private static final ProjectService projects = new ProjectService();
    private static final CareerService career = new CareerService();
    private static final PortfolioService portfolio = new PortfolioService();
    private static final StudentSkillService skillService = new StudentSkillService();
    private static final StudentService studentService = new StudentService();
    private static final SupportService support = new SupportService();
    private static final DirectoryService directory = new DirectoryService();
    private static final ProfileService profiles = new ProfileService();

    private StudentViews() {}

    private static int studentId() {
        return academic.studentIdForUser(Session.currentUser().getUserId());
    }

    public static Node profile() {
        User account = profiles.account(Session.currentUser().getUserId());
        Student student = profiles.studentForUser(account.getUserId())
                .orElseThrow(() -> new AppException("Student profile not found."));

        // 1. Identity Hero Cushion
        HBox hero = new HBox(20);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("profile-hero-card");

        StackPane avatar = Ui.avatar(Ui.initials(account.getFullName()), "avatar-lavender");

        VBox identityDetails = new VBox(6);
        Label nameLabel = new Label(value(account.getFullName()));
        nameLabel.getStyleClass().add("profile-hero-name");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().addAll(
                Ui.badge("@" + account.getUsername(), "badge-pill-cream"),
                Ui.badge(String.valueOf(account.getRole()), "badge-pill-lavender"),
                Ui.badge(value(student.getEnrollmentStatus()), "badge-pill-mint")
        );
        identityDetails.getChildren().addAll(nameLabel, badges);

        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);

        VBox heroRight = new VBox(6);
        heroRight.setAlignment(Pos.CENTER_RIGHT);
        String heroProg = (student.getProgram() != null && !student.getProgram().isBlank())
                ? student.getProgram().trim()
                : "Undeclared Program";
        Label programHighlight = new Label(heroProg);
        programHighlight.getStyleClass().add("profile-hero-highlight-title");
        Label yearSectionHighlight = new Label("Year " + student.getYearLevel() + " • Section " + value(student.getSection()));
        yearSectionHighlight.getStyleClass().add("profile-hero-highlight-subtitle");
        heroRight.getChildren().addAll(programHighlight, yearSectionHighlight);

        hero.getChildren().addAll(avatar, identityDetails, heroSpacer, heroRight);

        // 2. Account Information Tray
        GridPane accountGrid = new GridPane();
        accountGrid.setHgap(16);
        accountGrid.setVgap(14);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        accountGrid.getColumnConstraints().addAll(col1, col2);

        accountGrid.add(Ui.propertyTile("Username", value(account.getUsername())), 0, 0);
        accountGrid.add(Ui.propertyTile("Email Address", value(account.getEmail())), 1, 0);
        accountGrid.add(Ui.propertyTile("Full Name", value(account.getFullName())), 0, 1);
        accountGrid.add(Ui.propertyTile("Account Role", Ui.badge(String.valueOf(account.getRole()), "badge-pill-lavender")), 1, 1);

        VBox accountTray = Ui.tray("Account Details", "Your personal credentials and access role in NextIT", accountGrid);
        HBox.setHgrow(accountTray, Priority.ALWAYS);

        // 3. Academic & Enrollment Information Tray
        GridPane academicGrid = new GridPane();
        academicGrid.setHgap(16);
        academicGrid.setVgap(14);
        academicGrid.getColumnConstraints().addAll(col1, col2);

        academicGrid.add(Ui.propertyTile("Student Number", value(student.getStudentNumber())), 0, 0);
        String degreeProg = (student.getProgram() != null && !student.getProgram().isBlank())
                ? student.getProgram().trim()
                : "Undeclared";
        academicGrid.add(Ui.propertyTile("Degree Program", degreeProg), 1, 0);
        academicGrid.add(Ui.propertyTile("Year Level", "Year " + student.getYearLevel()), 0, 1);
        academicGrid.add(Ui.propertyTile("Class Section", "Section " + value(student.getSection())), 1, 1);
        academicGrid.add(Ui.propertyTile("Enrollment Status", Ui.badge(value(student.getEnrollmentStatus()), "badge-pill-mint")), 0, 2);

        VBox academicTray = Ui.tray("Academic Standing", "Course program placement and enrollment registration", academicGrid);
        HBox.setHgrow(academicTray, Priority.ALWAYS);

        HBox traysRow = new HBox(20, accountTray, academicTray);

        // 4. Notice Cushion
        HBox notice = new HBox(12);
        notice.getStyleClass().add("profile-notice-cushion");
        Label noticeIcon = new Label("ℹ");
        noticeIcon.getStyleClass().add("profile-notice-icon");
        Label noticeText = new Label("Student records and credentials are synchronized with the registrar. Contact an administrator if any details need correction.");
        noticeText.getStyleClass().add("profile-notice-text");
        noticeText.setWrapText(true);
        notice.getChildren().addAll(noticeIcon, noticeText);

        return Ui.page("My Profile", "Your account and student profile information. Contact an administrator if any details need correction.",
                hero, traysRow, notice);
    }

    public static Node dashboard() {
        int sid = studentId();
        User currentUser = Session.currentUser();
        Student student = profiles.studentForUser(currentUser.getUserId()).orElse(null);

        // 1. Hero cushion with status and live badges
        HBox hero = new HBox(16);
        hero.getStyleClass().add("page-header-cushion");
        hero.setAlignment(Pos.CENTER_LEFT);

        VBox heroTitles = new VBox(4);
        heroTitles.getChildren().addAll(
                Ui.title("Student Dashboard"),
                Ui.subtitle("Welcome back, " + currentUser.getFullName() + ". Track your academic standing, technical milestones, and career progress.")
        );

        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);

        HBox statusPillBox = new HBox(8);
        statusPillBox.setAlignment(Pos.CENTER_RIGHT);
        StackPane statusDot = Ui.statusDot(true);
        Label statusText = new Label("Active Student");
        statusText.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d6a4f; -fx-font-size: 12px;");
        HBox liveChip = new HBox(8, statusDot, statusText);
        liveChip.setAlignment(Pos.CENTER);
        liveChip.getStyleClass().addAll("badge-pill", "badge-pill-mint");

        String progTag = student != null && student.getProgram() != null ? student.getProgram() : "BS Information Technology";
        Label progChip = Ui.badge(progTag, "badge-pill-lavender");
        statusPillBox.getChildren().addAll(liveChip, progChip);

        hero.getChildren().addAll(heroTitles, heroSpacer, statusPillBox);

        // Fixed sticky header container
        VBox headerContainer = new VBox(hero);
        headerContainer.setStyle("-fx-background-color: #f1f2fa;");
        headerContainer.setPadding(new Insets(24, 24, 10, 24));

        // 2. Full-Width 4-Column Stat Cards (Row 1 & Row 2)
        HBox cards1 = new HBox(14);
        HBox cards2 = new HBox(14);
        cards1.setFillHeight(true);
        cards2.setFillHeight(true);

        // 3. Middle Analytics & Academic Standing Row
        HBox middleRow = new HBox(16);
        middleRow.setAlignment(Pos.TOP_LEFT);

        HBox chartsRow = new HBox(14);
        chartsRow.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(chartsRow, Priority.ALWAYS);

        VBox standingHolder = new VBox();
        standingHolder.setPrefWidth(320);
        standingHolder.setMinWidth(280);
        standingHolder.setMaxWidth(360);

        middleRow.getChildren().addAll(chartsRow, standingHolder);

        // 4. Lower Evaluations & Student Hub Row
        HBox lowerRow = new HBox(16);
        lowerRow.setAlignment(Pos.TOP_LEFT);

        VBox gradesTray = new VBox(14);
        gradesTray.getStyleClass().add("clay-tray");
        gradesTray.setFillWidth(true);
        HBox.setHgrow(gradesTray, Priority.ALWAYS);

        Map<Integer, String> subjects = directory.subjectNames();
        Map<Integer, Enrollment> enrollmentMap = new java.util.HashMap<>();
        for (Enrollment e : academic.enrollmentsForStudent(sid)) {
            enrollmentMap.put(e.getEnrollmentId(), e);
        }

        TableView<Grade> gradeTable = Ui.table("No grades recorded yet.");
        gradeTable.setPrefHeight(290);
        gradeTable.setMinHeight(220);

        Ui.addColumn(gradeTable, "Subject", g -> {
            Enrollment e = enrollmentMap.get(g.getEnrollmentId());
            if (e != null) {
                return subjects.getOrDefault(e.getSubjectId(), "Subject #" + e.getSubjectId());
            }
            return "Enrollment #" + g.getEnrollmentId();
        });
        Ui.addColumn(gradeTable, "Activity", Grade::getActivityName);
        gradeTable.getColumns().add(Ui.actionColumn("Type", g -> {
            String t = g.getActivityType() == null ? "Activity" : g.getActivityType();
            String pill = switch (t.toLowerCase(Locale.ROOT)) {
                case "examination" -> "badge-pill-pink";
                case "laboratory" -> "badge-pill-blue";
                case "quiz" -> "badge-pill-yellow";
                case "project" -> "badge-pill-mint";
                default -> "badge-pill-lavender";
            };
            return Ui.badge(t, pill);
        }));
        Ui.addColumn(gradeTable, "Score", g -> g.getScore() + " / " + g.getMaxScore());
        gradeTable.getColumns().add(Ui.actionColumn("Grade", g -> {
            String grText = g.getGrade() == null ? "" : String.format("%.1f%%", g.getGrade());
            return Ui.badge(grText, "badge-pill-cream");
        }));
        Ui.addColumn(gradeTable, "Remarks", Grade::getRemarks);

        TextField searchGrades = Ui.field("Filter by subject or activity...");
        searchGrades.setPrefWidth(220);

        HBox trayHeader = new HBox(12);
        trayHeader.setAlignment(Pos.CENTER_LEFT);

        VBox trayTitles = new VBox(3);
        Label trayTitle = new Label("Academic Evaluations & Grades");
        trayTitle.getStyleClass().add("tray-title");
        Label traySub = new Label("Official marks and feedback from course instructors");
        traySub.getStyleClass().add("tray-subtitle");
        trayTitles.getChildren().addAll(trayTitle, traySub);

        Region traySpacer = new Region();
        HBox.setHgrow(traySpacer, Priority.ALWAYS);

        Button exportCsvBtn = Ui.button("Export CSV", "secondary-button");
        exportCsvBtn.setOnAction(e -> com.nextit.util.ExportService.exportTableViewToCsv(gradeTable, "student_grades.csv", "Academic Evaluations & Grades", exportCsvBtn.getScene().getWindow()));

        Button exportReportBtn = Ui.button("Print / PDF", "refresh-pill-button");
        exportReportBtn.setOnAction(e -> com.nextit.util.ExportService.exportTableViewToHtmlReport(gradeTable, "student_grades_report.html", "Academic Evaluations & Grades", exportReportBtn.getScene().getWindow()));

        trayHeader.getChildren().addAll(trayTitles, traySpacer, searchGrades, exportCsvBtn, exportReportBtn);
        gradesTray.getChildren().addAll(trayHeader, gradeTable);

        VBox hubHolder = new VBox();
        hubHolder.setPrefWidth(320);
        hubHolder.setMinWidth(280);
        hubHolder.setMaxWidth(360);

        lowerRow.getChildren().addAll(gradesTray, hubHolder);

        // Self-refreshing dashboard logic
        final Runnable[] refreshHolder = new Runnable[1];
        Runnable refreshDashboard = () -> {
            List<Grade> grades = academic.gradesForStudent(sid);
            List<Attendance> attendance = academic.attendanceForStudent(sid);
            Portfolio p = portfolio.portfolioForStudent(sid).orElse(null);
            List<Enrollment> enrollments = academic.enrollmentsForStudent(sid);

            subjects.clear();
            subjects.putAll(directory.subjectNames());
            enrollmentMap.clear();
            for (Enrollment e : enrollments) {
                enrollmentMap.put(e.getEnrollmentId(), e);
            }

            // 1. Update 8 Full-Width Stat Cards with Pastel Clay Palette
            cards1.getChildren().clear();
            cards2.getChildren().clear();

            VBox c1 = Ui.card("Enrolled Subjects", String.valueOf(enrollments.size()), "card-cream");
            VBox c2 = Ui.card("Average Grade", String.format("%.1f%%", GradeCalculatorFacade.average(grades)), "card-mint");
            VBox c3 = Ui.card("Attendance Rate", String.format("%.1f%%", GradeCalculatorFacade.attendance(attendance)), "card-lavender");
            VBox c4 = Ui.card("Technical Skills", String.valueOf(skillService.forStudent(sid).size()), "card-pink");

            VBox c5 = Ui.card("Active Projects", String.valueOf(projects.projectsForStudent(sid).size()), "card-yellow");
            VBox c6 = Ui.card("Achievements", String.valueOf(studentService.achievementsFor(sid).size()), "card-blue");
            VBox c7 = Ui.card("Certifications", String.valueOf(studentService.certificationsFor(sid).size()), "card-cream");
            VBox c8 = Ui.card("Open Opportunities", String.valueOf(career.openOpportunities().size()), "card-mint");

            for (VBox c : List.of(c1, c2, c3, c4, c5, c6, c7, c8)) {
                HBox.setHgrow(c, Priority.ALWAYS);
                c.setMaxWidth(Double.MAX_VALUE);
            }

            cards1.getChildren().addAll(c1, c2, c3, c4);
            cards2.getChildren().addAll(c5, c6, c7, c8);

            // 2. Update Dual Charts & Academic Standing
            chartsRow.getChildren().clear();
            Node gradeChart = DashboardCharts.gradeTrend(grades);
            Node attendanceChart = DashboardCharts.attendanceSummary(attendance);
            HBox.setHgrow(gradeChart, Priority.ALWAYS);
            HBox.setHgrow(attendanceChart, Priority.ALWAYS);
            chartsRow.getChildren().addAll(gradeChart, attendanceChart);

            standingHolder.getChildren().clear();
            standingHolder.getChildren().add(createStandingCard(student));

            // 3. Update Student Hub Card
            hubHolder.getChildren().clear();
            hubHolder.getChildren().add(createHubCard(sid, () -> refreshHolder[0].run()));

            // 4. Update Grades Table
            String q = searchGrades.getText() == null ? "" : searchGrades.getText().toLowerCase(Locale.ROOT);
            List<Grade> filtered = grades.stream().filter(g -> {
                Enrollment e = enrollmentMap.get(g.getEnrollmentId());
                String subName = e != null ? subjects.getOrDefault(e.getSubjectId(), "").toLowerCase(Locale.ROOT) : "";
                String actName = String.valueOf(g.getActivityName()).toLowerCase(Locale.ROOT);
                return q.isBlank() || subName.contains(q) || actName.contains(q);
            }).toList();
            gradeTable.setItems(FXCollections.observableArrayList(filtered));
        };

        refreshHolder[0] = refreshDashboard;
        searchGrades.textProperty().addListener((obs, old, val) -> refreshDashboard.run());
        refreshDashboard.run();

        VBox scrollContent = new VBox(20);
        scrollContent.setFillWidth(true);
        scrollContent.setStyle("-fx-background-color: #f1f2fa;");
        scrollContent.setPadding(new Insets(10, 24, 32, 24));
        scrollContent.getChildren().addAll(cards1, cards2, middleRow, lowerRow);

        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("content-scroll");

        // Forward mouse scroll events on the sticky header to the scrollPane
        headerContainer.addEventHandler(ScrollEvent.SCROLL, event -> {
            scrollPane.fireEvent(event.copyFor(scrollPane, scrollPane));
            event.consume();
        });

        BorderPane dashboardRoot = new BorderPane();
        dashboardRoot.getProperties().put("self-scrolling", Boolean.TRUE);
        dashboardRoot.setStyle("-fx-background-color: #f1f2fa;");
        dashboardRoot.setTop(headerContainer);
        dashboardRoot.setCenter(scrollPane);

        return dashboardRoot;
    }

    private static VBox createStandingCard(Student student) {
        VBox standingTray = new VBox(12);
        standingTray.getStyleClass().add("clay-tray");
        standingTray.setStyle("-fx-padding: 16;");

        Label standingTitle = new Label("Academic Standing");
        standingTitle.getStyleClass().add("tray-title");
        Label standingSub = new Label("Program placement & progress");
        standingSub.getStyleClass().add("tray-subtitle");

        VBox standingItems = new VBox(8);
        String sNum = student != null && student.getStudentNumber() != null ? student.getStudentNumber() : "2023-0001";
        String sYear = student != null ? "Year " + student.getYearLevel() + " • Section " + (student.getSection() != null ? student.getSection() : "A") : "Year 3 • Section A";
        String sStatus = student != null && student.getEnrollmentStatus() != null ? student.getEnrollmentStatus() : "Enrolled";

        standingItems.getChildren().addAll(
                Ui.propertyTile("Student Number", sNum),
                Ui.propertyTile("Level & Section", sYear),
                Ui.propertyTile("Enrollment Status", Ui.badge(sStatus, "badge-pill-mint"))
        );

        standingTray.getChildren().addAll(standingTitle, standingSub, standingItems);
        return standingTray;
    }

    private static VBox createHubCard(int studentId, Runnable refresh) {
        VBox actionsTray = new VBox(12);
        actionsTray.getStyleClass().add("clay-tray");
        actionsTray.setStyle("-fx-padding: 16;");

        Label actionsTitle = new Label("Student Hub");
        actionsTitle.getStyleClass().add("tray-title");
        Label actionsSub = new Label("Quick actions & career portfolio");
        actionsSub.getStyleClass().add("tray-subtitle");

        Button myProfileBtn = new Button("View Profile");
        myProfileBtn.setMaxWidth(Double.MAX_VALUE);
        myProfileBtn.getStyleClass().add("primary-button");
        myProfileBtn.setOnAction(e -> {
            if (AppController.getInstance() != null) {
                AppController.getInstance().navigateTo(StudentViews.profile(), "Profile");
            }
        });

        Button addProjectBtn = new Button("Showcase Project");
        addProjectBtn.setMaxWidth(Double.MAX_VALUE);
        addProjectBtn.getStyleClass().add("secondary-button");
        addProjectBtn.setOnAction(e -> editProject(studentId, null, refresh));

        Button portfolioBtn = new Button("Career Portfolio");
        portfolioBtn.setMaxWidth(Double.MAX_VALUE);
        portfolioBtn.getStyleClass().add("secondary-button");
        portfolioBtn.setOnAction(e -> {
            if (AppController.getInstance() != null) {
                AppController.getInstance().navigateTo(StudentViews.portfolio(), "Portfolio");
            }
        });

        Button refreshBtn = new Button("Refresh Dashboard");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setOnAction(e -> {
            refresh.run();
            Alerts.info("Student dashboard refreshed.");
        });

        actionsTray.getChildren().addAll(actionsTitle, actionsSub, myProfileBtn, addProjectBtn, portfolioBtn, refreshBtn);
        return actionsTray;
    }

    /** Small facade so the dashboard does not need to import calculation details. */
    private static final class GradeCalculatorFacade {
        static double average(List<Grade> grades) { return com.nextit.util.GradeCalculator.average(grades); }
        static double attendance(List<Attendance> records) { return com.nextit.util.GradeCalculator.attendanceRate(records); }
    }

    public static Node academic() {
        int sid = studentId();
        Map<Integer, String> subjects = directory.subjectNames();
        Map<Integer, String> instructors = directory.instructorNames();
        Map<Integer, String> enrollments = new java.util.HashMap<>();
        for (Enrollment e : academic.enrollmentsForStudent(sid)) {
            enrollments.put(e.getEnrollmentId(), subjects.getOrDefault(e.getSubjectId(), "Subject #" + e.getSubjectId()));
        }

        Map<Integer, Subject> subjectMap = directory.subjectMap();
        TableView<Enrollment> subjectTable = Ui.table("No subjects available.");
        Ui.addColumn(subjectTable, "Subject Code", e -> {
            Subject s = subjectMap.get(e.getSubjectId());
            return s != null ? s.getSubjectCode() : "SUBJ-" + e.getSubjectId();
        });
        Ui.addColumn(subjectTable, "Subject Title", e -> {
            Subject s = subjectMap.get(e.getSubjectId());
            return s != null ? s.getSubjectName() : "Subject #" + e.getSubjectId();
        });
        Ui.addColumn(subjectTable, "Units", e -> {
            Subject s = subjectMap.get(e.getSubjectId());
            return s != null ? String.valueOf(s.getUnits()) : "3.0";
        });
        Ui.addColumn(subjectTable, "Academic Year", Enrollment::getAcademicYear);
        Ui.addColumn(subjectTable, "Semester", Enrollment::getSemester);
        Ui.addColumn(subjectTable, "Status", Enrollment::getStatus);
        subjectTable.setItems(FXCollections.observableArrayList(academic.enrollmentsForStudent(sid)));

        TableView<Grade> gradeTable = Ui.table();
        Ui.addColumn(gradeTable, "Subject", g -> enrollments.getOrDefault(g.getEnrollmentId(), "Enrollment #" + g.getEnrollmentId()));
        Ui.addColumn(gradeTable, "Activity", Grade::getActivityName);
        Ui.addColumn(gradeTable, "Type", Grade::getActivityType);
        Ui.addColumn(gradeTable, "Score", g -> g.getScore() + " / " + g.getMaxScore());
        Ui.addColumn(gradeTable, "Grade", g -> g.getGrade() == null ? "" : String.format("%.2f%%", g.getGrade()));
        Ui.addColumn(gradeTable, "Remarks", Grade::getRemarks);
        Ui.addColumn(gradeTable, "Recorded", g -> g.getRecordedAt() == null ? "" : g.getRecordedAt().toLocalDate().toString());
        gradeTable.setItems(FXCollections.observableArrayList(academic.gradesForStudent(sid)));

        TableView<Attendance> attendanceTable = Ui.table();
        Ui.addColumn(attendanceTable, "Subject", a -> enrollments.getOrDefault(a.getEnrollmentId(), "Enrollment #" + a.getEnrollmentId()));
        Ui.addColumn(attendanceTable, "Date", a -> Ui.formatDate(a.getAttendanceDate()));
        Ui.addColumn(attendanceTable, "Status", Attendance::getStatus);
        Ui.addColumn(attendanceTable, "Remarks", Attendance::getRemarks);
        Ui.addColumn(attendanceTable, "Recorded By", a -> instructors.getOrDefault(a.getRecordedBy(), "Instructor #" + a.getRecordedBy()));
        attendanceTable.setItems(FXCollections.observableArrayList(academic.attendanceForStudent(sid)));

        Map<Integer, String> supportSubjects = subjects;
        TableView<AcademicSupport> supportTable = Ui.table();
        Ui.addColumn(supportTable, "Type", AcademicSupport::getSupportType);
        Ui.addColumn(supportTable, "Subject", s -> s.getSubjectId() == null ? "" : supportSubjects.getOrDefault(s.getSubjectId(), "Subject #" + s.getSubjectId()));
        Ui.addColumn(supportTable, "Date", s -> Ui.formatDate(s.getSupportDate()));
        Ui.addColumn(supportTable, "Reason", AcademicSupport::getReason);
        Ui.addColumn(supportTable, "Action Taken", AcademicSupport::getActionTaken);
        Ui.addColumn(supportTable, "Outcome", AcademicSupport::getOutcome);
        Ui.addColumn(supportTable, "Instructor", s -> instructors.getOrDefault(s.getInstructorId(), "Instructor #" + s.getInstructorId()));
        supportTable.setItems(FXCollections.observableArrayList(support.forStudent(sid)));

        Map<Integer, String> projectTitles = projects.allProjects().stream()
                .collect(java.util.stream.Collectors.toMap(Project::getProjectId, Project::getProjectTitle, (a, b) -> a));
        TableView<ProjectEvaluation> projectTable = Ui.table();
        Ui.addColumn(projectTable, "Project", e -> projectTitles.getOrDefault(e.getProjectId(), "Project #" + e.getProjectId()));
        Ui.addColumn(projectTable, "Contribution", e -> score(e.getContributionScore()));
        Ui.addColumn(projectTable, "Technical", e -> score(e.getTechnicalScore()));
        Ui.addColumn(projectTable, "Teamwork", e -> score(e.getTeamworkScore()));
        Ui.addColumn(projectTable, "Overall", e -> score(e.getOverallScore()));
        Ui.addColumn(projectTable, "Feedback", ProjectEvaluation::getFeedback);
        Ui.addColumn(projectTable, "Evaluated", e -> e.getEvaluatedAt() == null ? "" : e.getEvaluatedAt().toLocalDate().toString());
        projectTable.setItems(FXCollections.observableArrayList(projects.evaluationsForStudent(sid)));

        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                tab("Subjects", subjectTable),
                tab("Grades", gradeTable),
                tab("Attendance", attendanceTable),
                tab("Project Performance", projectTable),
                tab("Academic Support", supportTable));
        return Ui.page("Academic Progress", "Review your subjects, grades, attendance, project performance and instructor remarks. These records are maintained by your instructors.", tabs);
    }

    private static String score(Double value) {
        return value == null ? "" : String.format("%.2f", value);
    }

    private static Tab tab(String title, Node content) {
        if (content instanceof TableView<?> table) {
            Button exportCsv = Ui.button("📥 Export CSV", "clay-btn-secondary");
            exportCsv.setOnAction(e -> com.nextit.util.ExportService.exportTableViewToCsv(table, title.toLowerCase(Locale.ROOT).replace(" ", "_") + ".csv", title, exportCsv.getScene().getWindow()));

            Button exportReport = Ui.button("📄 Print / PDF", "clay-btn-secondary");
            exportReport.setOnAction(e -> com.nextit.util.ExportService.exportTableViewToHtmlReport(table, title.toLowerCase(Locale.ROOT).replace(" ", "_") + ".html", title, exportReport.getScene().getWindow()));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox toolbar = new HBox(8, spacer, exportCsv, exportReport);
            toolbar.setAlignment(Pos.CENTER_RIGHT);
            toolbar.setStyle("-fx-padding: 0 0 10 0;");

            VBox box = new VBox(8, toolbar, table);
            VBox.setVgrow(table, Priority.ALWAYS);
            box.setStyle("-fx-padding: 12 0 0 0;");

            Tab tab = new Tab(title, box);
            tab.setClosable(false);
            return tab;
        }
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    public static Node skills() {
        int sid = studentId();
        Map<Integer, TechnicalSkill> skillMap = directory.skillMap();
        Map<Integer, String> instructors = directory.instructorNames();
        TableView<StudentSkill> table = Ui.table("No technical skills added yet.");
        Ui.addColumn(table, "Technical Skill", s -> {
            TechnicalSkill ts = skillMap.get(s.getSkillId());
            return ts != null ? ts.getSkillName() : "Skill #" + s.getSkillId();
        });
        Ui.addColumn(table, "Category", s -> {
            TechnicalSkill ts = skillMap.get(s.getSkillId());
            return ts != null && ts.getCategory() != null ? ts.getCategory() : "General";
        });
        Ui.addColumn(table, "Proficiency", StudentSkill::getProficiencyLevel);
        Ui.addColumn(table, "Assessment Score", s -> s.getAssessmentScore() == null ? "Pending" : String.format("%.2f%%", s.getAssessmentScore()));
        Ui.addColumn(table, "Assessed By", s -> s.getAssessedBy() == null ? "Self-Reported" : instructors.getOrDefault(s.getAssessedBy(), "Instructor #" + s.getAssessedBy()));
        Ui.addColumn(table, "Assessment Date", s -> s.getAssessedAt() == null ? "--" : s.getAssessedAt().toLocalDate().toString());
        Ui.addColumn(table, "Instructor Remarks", s -> s.getRemarks() == null || s.getRemarks().isBlank() ? "--" : s.getRemarks());

        List<StudentSkill> all = skillService.forStudent(sid);
        table.setItems(FXCollections.observableArrayList(all));
        TextField search = Ui.field("Search by skill, category, proficiency or remarks...");
        Runnable filter = () -> {
            skillMap.clear();
            skillMap.putAll(directory.skillMap());
            instructors.clear();
            instructors.putAll(directory.instructorNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(skillService.forStudent(sid).stream().filter(s -> {
                TechnicalSkill ts = skillMap.get(s.getSkillId());
                String skillName = ts != null ? ts.getSkillName() : "";
                String category = ts != null && ts.getCategory() != null ? ts.getCategory() : "";
                return skillName.toLowerCase(Locale.ROOT).contains(q)
                        || category.toLowerCase(Locale.ROOT).contains(q)
                        || String.valueOf(s.getProficiencyLevel()).toLowerCase(Locale.ROOT).contains(q)
                        || String.valueOf(s.getRemarks()).toLowerCase(Locale.ROOT).contains(q);
            }).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> filter.run());
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());
        return Ui.page("Technical Skills", "Skills assessed by your instructors, including proficiency, competency categories, and assessment scores.",
                Ui.toolbar(new Label("Search:"), search, refresh), table);
    }

    public static Node projects() {
        int sid = studentId();
        Map<Integer, String> subjects = directory.subjectNames();
        TableView<Project> table = Ui.table();
        Ui.addColumn(table, "Title", Project::getProjectTitle);
        Ui.addColumn(table, "Type", Project::getProjectType);
        Ui.addColumn(table, "Subject", p -> p.getSubjectId() == null ? "" : subjects.getOrDefault(p.getSubjectId(), "Subject #" + p.getSubjectId()));
        Ui.addColumn(table, "Status", Project::getStatus);
        Ui.addColumn(table, "Your role", p -> projects.isOwner(p, sid) ? "Owner" : "Group member");
        Ui.addColumn(table, "Technologies", Project::getTechnologiesUsed);
        Ui.addColumn(table, "Repository", Project::getRepositoryUrl);
        Ui.addColumn(table, "Demo", Project::getDemoUrl);
        List<Project> all = projects.projectsForStudent(sid);
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by title, type, technology or status...");
        Runnable applyFilter = () -> {
            subjects.clear();
            subjects.putAll(directory.subjectNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(projects.projectsForStudent(sid).stream().filter(p ->
                    String.valueOf(p.getProjectTitle()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(p.getProjectType()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(p.getTechnologiesUsed()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(p.getStatus()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Add Project");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editProject(sid, null, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to edit."); return; }
            if (!projects.isOwner(selected, sid)) {
                Alerts.error("Only the project owner can edit this project.");
                return;
            }
            editProject(sid, selected, applyFilter);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to delete."); return; }
            if (!projects.isOwner(selected, sid)) {
                Alerts.error("Only the project owner can delete this project.");
                return;
            }
            if (Ui.confirmed("Delete project \"" + selected.getProjectTitle() + "\"?")) {
                guard(() -> {
                    projects.deleteProject(selected.getProjectId(), sid);
                    applyFilter.run();
                });
            }
        });
        Button members = new Button("Group Members");
        members.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to manage its members."); return; }
            if (!projects.isOwner(selected, sid)) {
                Alerts.error("Only the project owner can manage group members.");
                return;
            }
            guard(() -> manageMembers(selected, sid));
        });
        Button details = new Button("View Details");
        details.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to view."); return; }
            guard(() -> showProject(selected));
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());

        return Ui.page("My Projects", "Add projects, link repositories and demos, and record the contributions of group members.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, members, details, refresh), table);
    }

    private static void showProject(Project p) {
        Map<Integer, String> subjects = directory.subjectNames();
        Label description = new Label(value(p.getDescription()));
        description.setWrapText(true);
        StringBuilder memberText = new StringBuilder();
        projects.membersForProject(p.getProjectId()).forEach(m -> memberText.append("• ")
                .append(directory.studentName(m.getStudentId()))
                .append(m.getRole() == null ? "" : " — " + m.getRole())
                .append(m.getContribution() == null ? "" : ": " + m.getContribution())
                .append('\n'));
        Label members = new Label(memberText.isEmpty() ? "No group members recorded." : memberText.toString());
        members.setWrapText(true);
        Node form = Ui.form(
                new String[]{"Title", "Type", "Subject", "Status", "Technologies", "Repository", "Demo", "Date", "Description", "Group members"},
                new Node[]{new Label(value(p.getProjectTitle())), new Label(value(p.getProjectType())),
                        new Label(p.getSubjectId() == null ? "" : subjects.getOrDefault(p.getSubjectId(), "Subject #" + p.getSubjectId())),
                        new Label(value(p.getStatus())), new Label(value(p.getTechnologiesUsed())),
                        new Label(value(p.getRepositoryUrl())), new Label(value(p.getDemoUrl())),
                        new Label(Ui.formatDate(p.getProjectDate())), description, members});
        Ui.viewDialog("Project - " + p.getProjectTitle(), form);
    }

    private static void editProject(int studentId, Project existing, Runnable refresh) {
        Project p = existing == null ? new Project() : existing;
        List<Subject> subjectList = directory.subjects();
        TextField title = Ui.field("Project title");
        title.setText(p.getProjectTitle());
        ComboBox<String> type = Ui.combo(List.of("Laboratory", "Academic Project", "Programming", "Capstone", "Personal", "Research", "Other"), s -> s);
        type.getSelectionModel().select(p.getProjectType() == null ? "Academic Project" : p.getProjectType());
        ComboBox<Subject> subject = Ui.combo(subjectList, s -> s.getSubjectCode() + " - " + s.getSubjectName());
        if (p.getSubjectId() != null) {
            subjectList.stream().filter(s -> s.getSubjectId() == p.getSubjectId()).findFirst()
                    .ifPresent(subject.getSelectionModel()::select);
        }
        TextArea description = Ui.area("Description");
        description.setText(p.getDescription());
        TextField technologies = Ui.field("Java, JavaFX, MySQL");
        technologies.setText(p.getTechnologiesUsed());
        TextField repository = Ui.field("https://github.com/...");
        repository.setText(p.getRepositoryUrl());
        TextField demo = Ui.field("https://...");
        demo.setText(p.getDemoUrl());
        DatePicker projectDate = Ui.date(p.getProjectDate() == null ? LocalDate.now() : p.getProjectDate());
        ComboBox<String> status = Ui.combo(List.of("Draft", "In Progress", "Completed", "Archived"), s -> s);
        status.getSelectionModel().select(p.getStatus() == null ? "Draft" : p.getStatus());

        Node form = Ui.form(
                new String[]{"Title", "Type", "Subject", "Description", "Technologies", "Repository URL", "Demo URL", "Project date", "Status"},
                new Node[]{title, type, subject, description, technologies, repository, demo, projectDate, status});
        Ui.dialog(existing == null ? "Add Project" : "Edit Project", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                Project target = new Project();
                target.setStudentId(existing == null ? Integer.valueOf(studentId) : existing.getStudentId());
                if (existing != null) target.setProjectId(existing.getProjectId());
                target.setProjectTitle(title.getText());
                target.setProjectType(type.getValue());
                target.setSubjectId(subject.getValue() == null ? null : subject.getValue().getSubjectId());
                target.setDescription(description.getText());
                target.setTechnologiesUsed(technologies.getText());
                target.setRepositoryUrl(repository.getText());
                target.setDemoUrl(demo.getText());
                target.setProjectDate(projectDate.getValue());
                target.setStatus(status.getValue());
                if (existing == null) projects.addProject(target);
                else projects.updateProject(target, studentId);
                refresh.run();
                Alerts.info("Project saved.");
            });
        });
    }

    private static void manageMembers(Project project, int studentId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Group Members - " + project.getProjectTitle());
        TableView<ProjectMember> table = Ui.table();
        Map<Integer, String> students = directory.studentNames();
        Ui.addColumn(table, "Student", m -> students.getOrDefault(m.getStudentId(), "Student #" + m.getStudentId()));
        Ui.addColumn(table, "Role", ProjectMember::getRole);
        Ui.addColumn(table, "Contribution", ProjectMember::getContribution);
        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(projects.membersForProject(project.getProjectId())));
        refresh.run();

        Button add = new Button("Add Member");
        add.setOnAction(e -> {
            List<ProjectMember> currentMembers = projects.membersForProject(project.getProjectId());
            List<Student> studentList = directory.students().stream()
                    .filter(s -> s.getStudentId() != project.getStudentId())
                    .filter(s -> currentMembers.stream().noneMatch(m -> m.getStudentId() == s.getStudentId()))
                    .toList();
            if (studentList.isEmpty()) {
                Alerts.info("Every available student is already on this project.");
                return;
            }
            ComboBox<Student> student = Ui.combo(studentList, s -> {
                String name = students.getOrDefault(s.getStudentId(), "Student #" + s.getStudentId());
                return name + " (" + s.getStudentNumber() + ")";
            });
            TextField role = Ui.field("Role (e.g. Developer, Designer)");
            TextField contribution = Ui.field("Contribution summary");
            Node form = Ui.form(new String[]{"Student", "Role", "Contribution"}, new Node[]{student, role, contribution});
            Ui.dialog("Add Group Member", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    if (student.getValue() == null) throw new AppException("Select a student.");
                    ProjectMember member = new ProjectMember();
                    member.setProjectId(project.getProjectId());
                    member.setStudentId(student.getValue().getStudentId());
                    member.setRole(role.getText());
                    member.setContribution(contribution.getText());
                    projects.addMember(member, studentId);
                    refresh.run();
                });
            });
        });
        Button remove = new Button("Remove Selected");
        remove.setOnAction(e -> {
            ProjectMember selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a member to remove."); return; }
            if (Ui.confirmed("Remove this member from the project?")) {
                guard(() -> { projects.removeMember(selected.getProjectMemberId(), studentId); refresh.run(); });
            }
        });
        dialog.getDialogPane().setContent(new VBox(10, Ui.toolbar(add, remove), table));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(700, 480);
        dialog.showAndWait();
    }

    public static Node achievements() {
        int sid = studentId();
        TableView<Achievement> table = Ui.table();
        Ui.addColumn(table, "Title", Achievement::getTitle);
        Ui.addColumn(table, "Organization", Achievement::getOrganization);
        Ui.addColumn(table, "Date", a -> Ui.formatDate(a.getAchievementDate()));
        Ui.addColumn(table, "Description", Achievement::getDescription);
        Ui.addColumn(table, "Verification", Achievement::getVerificationUrl);
        TextField search = Ui.field("Search by title, organization or description...");
        Runnable refresh = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(studentService.achievementsFor(sid).stream().filter(a ->
                    String.valueOf(a.getTitle()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(a.getOrganization()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(a.getDescription()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> refresh.run());
        refresh.run();

        Button add = new Button("Add Achievement");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editAchievement(sid, null, refresh));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Achievement selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an achievement to edit."); return; }
            editAchievement(sid, selected, refresh);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Achievement selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an achievement to delete."); return; }
            if (Ui.confirmed("Delete achievement \"" + selected.getTitle() + "\"?")) {
                guard(() -> { studentService.deleteAchievement(selected.getAchievementId(), sid); refresh.run(); });
            }
        });
        return Ui.page("Achievements", "Record awards, recognitions and other milestones that strengthen your portfolio.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refreshButton(refresh)), table);
    }

    private static void editAchievement(int studentId, Achievement existing, Runnable refresh) {
        Achievement a = existing == null ? new Achievement() : existing;
        TextField title = Ui.field("Title");
        title.setText(a.getTitle());
        TextArea description = Ui.area("Description");
        description.setText(a.getDescription());
        TextField organization = Ui.field("Organization");
        organization.setText(a.getOrganization());
        DatePicker date = Ui.date(a.getAchievementDate());
        TextField url = Ui.field("https://...");
        url.setText(a.getVerificationUrl());
        Node form = Ui.form(new String[]{"Title", "Description", "Organization", "Date", "Verification URL"},
                new Node[]{title, description, organization, date, url});
        Ui.dialog(existing == null ? "Add Achievement" : "Edit Achievement", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                Achievement target = new Achievement();
                if (existing != null) target.setAchievementId(existing.getAchievementId());
                target.setStudentId(studentId);
                target.setTitle(title.getText());
                target.setDescription(description.getText());
                target.setOrganization(organization.getText());
                target.setAchievementDate(date.getValue());
                target.setVerificationUrl(url.getText());
                studentService.saveAchievement(target);
                refresh.run();
                Alerts.info("Achievement saved.");
            });
        });
    }

    public static Node certifications() {
        int sid = studentId();
        TableView<Certification> table = Ui.table();
        Ui.addColumn(table, "Certification", Certification::getCertificationName);
        Ui.addColumn(table, "Issuer", Certification::getIssuingOrganization);
        Ui.addColumn(table, "Issued", c -> Ui.formatDate(c.getIssueDate()));
        Ui.addColumn(table, "Expires", c -> Ui.formatDate(c.getExpirationDate()));
        Ui.addColumn(table, "Credential", Certification::getCredentialUrl);
        TextField search = Ui.field("Search by certification or issuer...");
        Runnable refresh = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(studentService.certificationsFor(sid).stream().filter(c ->
                    String.valueOf(c.getCertificationName()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(c.getIssuingOrganization()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> refresh.run());
        refresh.run();

        Button add = new Button("Add Certification");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editCertification(sid, null, refresh));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Certification selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a certification to edit."); return; }
            editCertification(sid, selected, refresh);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Certification selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a certification to delete."); return; }
            if (Ui.confirmed("Delete certification \"" + selected.getCertificationName() + "\"?")) {
                guard(() -> { studentService.deleteCertification(selected.getCertificationId(), sid); refresh.run(); });
            }
        });
        return Ui.page("Certifications", "Track professional certifications, issue dates and expiry information.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refreshButton(refresh)), table);
    }

    private static void editCertification(int studentId, Certification existing, Runnable refresh) {
        Certification c = existing == null ? new Certification() : existing;
        TextField name = Ui.field("Certification name");
        name.setText(c.getCertificationName());
        TextField issuer = Ui.field("Issuing organization");
        issuer.setText(c.getIssuingOrganization());
        DatePicker issued = Ui.date(c.getIssueDate());
        DatePicker expires = Ui.date(c.getExpirationDate());
        TextField url = Ui.field("https://...");
        url.setText(c.getCredentialUrl());
        Node form = Ui.form(new String[]{"Certification", "Issuing organization", "Issue date", "Expiration date", "Credential URL"},
                new Node[]{name, issuer, issued, expires, url});
        Ui.dialog(existing == null ? "Add Certification" : "Edit Certification", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                Certification target = new Certification();
                if (existing != null) target.setCertificationId(existing.getCertificationId());
                target.setStudentId(studentId);
                target.setCertificationName(name.getText());
                target.setIssuingOrganization(issuer.getText());
                target.setIssueDate(issued.getValue());
                target.setExpirationDate(expires.getValue());
                target.setCredentialUrl(url.getText());
                studentService.saveCertification(target);
                refresh.run();
                Alerts.info("Certification saved.");
            });
        });
    }

    public static Node portfolio() {
        int sid = studentId();
        Portfolio loaded = portfolio.portfolioForStudent(sid).orElse(new Portfolio());
        loaded.setStudentId(sid);

        TextField headline = Ui.field("Professional headline");
        headline.setText(loaded.getHeadline());
        TextArea bio = Ui.area("Biography");
        bio.setText(loaded.getBio());
        TextArea goal = Ui.area("Career goal");
        goal.setText(loaded.getCareerGoal());
        ComboBox<String> sharing = Ui.combo(
                List.of("Private", "Public", "Submit for approval"), s -> s);
        if ("Public".equalsIgnoreCase(loaded.getVisibility())) {
            sharing.setValue("Public");
        } else if ("Pending".equalsIgnoreCase(loaded.getVisibility())
                || "Approved".equalsIgnoreCase(loaded.getVisibility())) {
            sharing.setValue("Submit for approval");
        } else {
            sharing.setValue("Private");
        }

        Label visibility = new Label(loaded.getVisibility() == null ? "Private" : loaded.getVisibility());
        Tab previewTab = tab("Preview", portfolioPreview(sid, loaded));
        Tab accessTab = tab("Access", portfolioAccess(loaded));

        Button save = new Button("Save Portfolio");
        save.getStyleClass().add("primary-button");
        save.setOnAction(e -> guard(() -> {
            loaded.setHeadline(headline.getText());
            loaded.setBio(bio.getText());
            loaded.setCareerGoal(goal.getText());
            String choice = sharing.getValue();
            if ("Public".equals(choice)) {
                loaded.setVisibility("Public");
            } else if ("Submit for approval".equals(choice)) {
                // Keep an existing approval; new or edited content is submitted
                // for review. PortfolioService downgrades changed approvals.
                if (!"Approved".equalsIgnoreCase(loaded.getVisibility())) loaded.setVisibility("Pending");
            } else {
                loaded.setVisibility("Private");
            }
            portfolio.save(loaded);
            visibility.setText(loaded.getVisibility());
            previewTab.setContent(portfolioPreview(sid, loaded));
            accessTab.setContent(portfolioAccess(loaded));
            String message = "Pending".equals(loaded.getVisibility())
                    ? "It is now awaiting administrator approval."
                    : "Public".equals(loaded.getVisibility())
                            ? "It is now visible to every employer."
                            : "Current visibility: " + loaded.getVisibility() + ".";
            Alerts.info("Portfolio saved. " + message);
        }));

        VBox editor = new VBox(12,
                Ui.subtitle("Private keeps the portfolio hidden, Public shares it with every employer, and Submit for approval asks an administrator to approve it for granted employer access."),
                Ui.form(new String[]{"Headline", "Biography", "Career goal", "Visibility"},
                        new Node[]{headline, bio, goal, visibility}),
                new HBox(8, new Label("Sharing:"), sharing), save);

        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(tab("Editor", editor), previewTab, accessTab);
        return Ui.page("Digital Portfolio", "Present your profile, skills, projects, achievements and certifications to industry partners.", tabs);
    }

    private static Node portfolioPreview(int studentId, Portfolio p) {
        VBox content = new VBox(10);
        content.getStyleClass().add("card");
        Label name = new Label(Session.currentUser().getFullName());
        name.getStyleClass().add("page-title");
        Student student = directory.students().stream()
                .filter(s -> s.getStudentId() == studentId).findFirst().orElse(null);
        Label program = new Label(student == null ? "" : student.getProgram() + " - Year " + student.getYearLevel());
        program.getStyleClass().add("page-subtitle");
        content.getChildren().addAll(name, program,
                section("Professional Headline", value(p.getHeadline())),
                section("Biography", value(p.getBio())),
                section("Career Goal", value(p.getCareerGoal())));

        Map<Integer, String> skillNames = directory.skillNames();
        String skills = skillService.forStudent(studentId).stream()
                .map(s -> "• " + skillNames.getOrDefault(s.getSkillId(), "Skill #" + s.getSkillId())
                        + " — " + value(s.getProficiencyLevel())
                        + (s.getAssessmentScore() == null ? "" : " (" + s.getAssessmentScore() + "%)"))
                .reduce("", (a, b) -> a + b + "\n");
        content.getChildren().add(section("Technical Skills", skills));

        String projectText = projects.projectsForStudent(studentId).stream()
                .map(pr -> "• " + value(pr.getProjectTitle()) + " — " + value(pr.getProjectType())
                        + (pr.getTechnologiesUsed() == null ? "" : " [" + pr.getTechnologiesUsed() + "]")
                        + (pr.getStatus() == null ? "" : " · " + pr.getStatus()))
                .reduce("", (a, b) -> a + b + "\n");
        content.getChildren().add(section("Projects", projectText));

        String achievements = studentService.achievementsFor(studentId).stream()
                .map(a -> "• " + value(a.getTitle()) + (a.getOrganization() == null ? "" : " — " + a.getOrganization()))
                .reduce("", (a, b) -> a + b + "\n");
        content.getChildren().add(section("Achievements", achievements));

        String certifications = studentService.certificationsFor(studentId).stream()
                .map(c -> "• " + value(c.getCertificationName())
                        + (c.getIssuingOrganization() == null ? "" : " — " + c.getIssuingOrganization()))
                .reduce("", (a, b) -> a + b + "\n");
        content.getChildren().add(section("Certifications", certifications));

        Label academicHighlights = new Label("Average grade: "
                + String.format("%.2f%%", academic.averageForStudent(studentId))
                + "   |   Attendance: "
                + String.format("%.2f%%", academic.attendanceRateForStudent(studentId)));
        content.getChildren().add(section("Academic Highlights", academicHighlights.getText()));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static Node section(String title, String text) {
        VBox box = new VBox(4);
        Label heading = new Label(title);
        heading.getStyleClass().add("section-title");
        Label body = new Label(text == null || text.isBlank() ? "Not provided" : text);
        body.setWrapText(true);
        box.getChildren().addAll(heading, body);
        return box;
    }

    private static String value(String text) { return text == null ? "" : text; }

    private static Button refreshButton(Runnable action) {
        Button button = new Button("Refresh");
        button.setOnAction(e -> action.run());
        return button;
    }

    private static Node portfolioAccess(Portfolio p) {
        TableView<PortfolioAccess> table = Ui.table();
        Map<Integer, String> employers = directory.employerNames();
        Ui.addColumn(table, "Employer", a -> employers.getOrDefault(a.getEmployerId(), "Employer #" + a.getEmployerId()));
        Ui.addColumn(table, "Status", PortfolioAccess::getAccessStatus);
        Ui.addColumn(table, "Granted", a -> a.getGrantedAt() == null ? "" : a.getGrantedAt().toLocalDate().toString());
        if (p.getPortfolioId() > 0) {
            table.setItems(FXCollections.observableArrayList(portfolio.accessFor(p.getPortfolioId())));
        }
        return Ui.page("Portfolio Access", "These employers have been granted access to your approved portfolio.", table);
    }

    public static Node opportunities() {
        int sid = studentId();
        List<JobOpportunity> all = career.openOpportunities();
        TableView<JobOpportunity> table = Ui.table();
        Ui.addColumn(table, "Title", JobOpportunity::getTitle);
        Ui.addColumn(table, "Type", JobOpportunity::getOpportunityType);
        Ui.addColumn(table, "Location", o -> value(o.getLocation()));
        Ui.addColumn(table, "Required Skills", o -> value(o.getRequiredSkills()));
        Ui.addColumn(table, "Deadline", o -> Ui.formatDate(o.getDeadline()));
        Ui.addColumn(table, "Posted", o -> Ui.formatDate(o.getCreatedAt() == null ? null : o.getCreatedAt().toLocalDate()));
        Ui.addColumn(table, "Status", JobOpportunity::getStatus);
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search opportunities...");
        ComboBox<String> type = Ui.combo(List.of("All types", "Internship", "Employment"), s -> s);
        type.getSelectionModel().selectFirst();
        Runnable filter = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            String t = type.getValue();
            table.setItems(FXCollections.observableArrayList(career.openOpportunities().stream().filter(o ->
                    (q.isBlank()
                            || String.valueOf(o.getTitle()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(o.getRequiredSkills()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(o.getLocation()).toLowerCase(Locale.ROOT).contains(q))
                    && ("All types".equals(t) || String.valueOf(o.getOpportunityType()).equalsIgnoreCase(t))).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> filter.run());
        type.valueProperty().addListener((obs, old, value) -> filter.run());
        filter.run();

        Button apply = new Button("Apply");
        apply.getStyleClass().add("primary-button");
        apply.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to apply to."); return; }
            TextArea remarks = Ui.area("Why are you a good fit for this opportunity?");
            Node form = Ui.form(new String[]{"Opportunity", "Remarks"},
                    new Node[]{new Label(selected.getTitle() + " at " + selected.getLocation()), remarks});
            Ui.dialog("Apply to Opportunity", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    Application application = new Application();
                    application.setStudentId(sid);
                    application.setOpportunityId(selected.getOpportunityId());
                    application.setRemarks(remarks.getText());
                    career.apply(application);
                    Alerts.info("Application submitted.");
                });
            });
        });
        Button details = new Button("View Details");
        details.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to view."); return; }
            Label description = new Label(value(selected.getDescription()));
            description.setWrapText(true);
            Node content = Ui.form(new String[]{"Title", "Type", "Location", "Required Skills", "Deadline", "Posted", "Status", "Description"},
                    new Node[]{new Label(value(selected.getTitle())), new Label(value(selected.getOpportunityType())),
                            new Label(value(selected.getLocation())), new Label(value(selected.getRequiredSkills())),
                            new Label(Ui.formatDate(selected.getDeadline())),
                            new Label(Ui.formatDate(selected.getCreatedAt() == null ? null : selected.getCreatedAt().toLocalDate())),
                            new Label(value(selected.getStatus())),
                            description});
            Ui.viewDialog("Opportunity Details", content);
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());
        return Ui.page("Opportunities", "Browse internships and employment opportunities from industry partners.",
                Ui.toolbar(new Label("Search:"), search, type, apply, details, refresh), table);
    }

    public static Node applications() {
        int sid = studentId();
        Map<Integer, String> opportunities = new java.util.HashMap<>();
        Map<Integer, String> employers = new java.util.HashMap<>();
        Map<Integer, JobOpportunity> byId = new java.util.HashMap<>();

        TableView<Application> table = Ui.table();
        Ui.addColumn(table, "Opportunity", a -> opportunities.getOrDefault(a.getOpportunityId(), "Opportunity #" + a.getOpportunityId()));
        Ui.addColumn(table, "Company", a -> {
            JobOpportunity o = byId.get(a.getOpportunityId());
            return o == null ? "" : employers.getOrDefault(o.getEmployerId(), "Employer #" + o.getEmployerId());
        });
        Ui.addColumn(table, "Applied", a -> a.getApplicationDate() == null ? "" : a.getApplicationDate().toLocalDate().toString());
        Ui.addColumn(table, "Status", Application::getStatus);
        Ui.addColumn(table, "Remarks", Application::getRemarks);

        ComboBox<String> status = Ui.combo(List.of("All statuses", "Pending", "Reviewed", "Shortlisted", "Rejected", "Accepted"), s -> s);
        status.getSelectionModel().selectFirst();
        TextField search = Ui.field("Search by opportunity, company, status or remarks...");
        Runnable filter = () -> {
            byId.clear();
            opportunities.clear();
            career.allOpportunities().forEach(o -> {
                byId.put(o.getOpportunityId(), o);
                opportunities.put(o.getOpportunityId(), o.getTitle());
            });
            employers.clear();
            employers.putAll(directory.employerNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            String selectedStatus = status.getValue();
            table.setItems(FXCollections.observableArrayList(career.applicationsByStudent(sid).stream().filter(a ->
                    ("All statuses".equals(selectedStatus) || selectedStatus.equalsIgnoreCase(a.getStatus()))
                            && (q.isBlank()
                            || String.valueOf(opportunities.getOrDefault(a.getOpportunityId(), "")).toLowerCase(Locale.ROOT).contains(q)
                            || companyFor(a, byId, employers).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(a.getRemarks()).toLowerCase(Locale.ROOT).contains(q))).toList()));
        };
        filter.run();
        search.textProperty().addListener((obs, old, value) -> filter.run());
        status.valueProperty().addListener((obs, old, value) -> filter.run());
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());

        return Ui.page("My Applications", "Follow the status of every internship and employment application you have submitted.",
                Ui.toolbar(new Label("Search:"), search, new Label("Filter:"), status, refresh), table);
    }

    private static String companyFor(Application a, Map<Integer, JobOpportunity> byId, Map<Integer, String> employers) {
        JobOpportunity o = byId.get(a.getOpportunityId());
        return o == null ? "" : employers.getOrDefault(o.getEmployerId(), "");
    }

    private static void guard(Runnable action) {
        try {
            action.run();
        } catch (AppException e) {
            Alerts.error(e.getMessage());
        } catch (Exception e) {
            Alerts.error("Could not complete the operation. Please check the database connection.");
            org.slf4j.LoggerFactory.getLogger(StudentViews.class).error("Student action failed", e);
        }
    }
}