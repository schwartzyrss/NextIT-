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
import java.util.function.Function;
import java.util.stream.Collectors;

public final class InstructorViews {

    private static final AcademicService academic = new AcademicService();
    private static final ProjectService projects = new ProjectService();
    private static final PortfolioService portfolios = new PortfolioService();
    private static final SupportService support = new SupportService();
    private static final StudentSkillService skills = new StudentSkillService();
    private static final DirectoryService directory = new DirectoryService();
    private static final ProfileService profiles = new ProfileService();

    private static final List<String> ACTIVITY_TYPES =
            List.of("Quiz", "Examination", "Laboratory", "Project", "Assignment", "Recitation", "Other");
    private static final List<String> PROFICIENCY_LEVELS =
            List.of("Beginner", "Intermediate", "Advanced", "Expert");

    private InstructorViews() {}

    private static int instructorId() {
        return directory.instructors().stream()
                .filter(i -> i.getUserId() == Session.currentUser().getUserId())
                .map(Instructor::getInstructorId)
                .findFirst()
                .orElseGet(() -> {
                    Instructor inst = new Instructor();
                    inst.setUserId(Session.currentUser().getUserId());
                    inst.setEmployeeNumber("EMP-" + String.format("%04d", Session.currentUser().getUserId()));
                    inst.setDepartment("Computer Science");
                    inst.setSpecialization("Software Engineering");
                    new com.nextit.repository.JdbcInstructorRepository().save(inst);
                    return inst.getInstructorId();
                });
    }

    public static Node profile() {
        User account = profiles.account(Session.currentUser().getUserId());
        Instructor instructor = profiles.instructorForUser(account.getUserId())
                .orElseThrow(() -> new AppException("Instructor profile not found."));

        HBox hero = new HBox(20);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("profile-hero-card");

        StackPane avatar = Ui.avatar(Ui.initials(account.getFullName()), "avatar-mint");

        VBox identityDetails = new VBox(6);
        Label nameLabel = new Label(value(account.getFullName()));
        nameLabel.getStyleClass().add("profile-hero-name");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().addAll(
                Ui.badge("@" + account.getUsername(), "badge-pill-cream"),
                Ui.badge(String.valueOf(account.getRole()), "badge-pill-mint"),
                Ui.badge(value(instructor.getDepartment()), "badge-pill-lavender")
        );
        identityDetails.getChildren().addAll(nameLabel, badges);

        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);

        VBox heroRight = new VBox(6);
        heroRight.setAlignment(Pos.CENTER_RIGHT);
        Label deptHighlight = new Label(value(instructor.getDepartment()));
        deptHighlight.getStyleClass().add("profile-hero-highlight-title");
        Label specHighlight = new Label(value(instructor.getSpecialization()));
        specHighlight.getStyleClass().add("profile-hero-highlight-subtitle");
        heroRight.getChildren().addAll(deptHighlight, specHighlight);

        hero.getChildren().addAll(avatar, identityDetails, heroSpacer, heroRight);

        // Account tray
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
        accountGrid.add(Ui.propertyTile("Account Role", Ui.badge(String.valueOf(account.getRole()), "badge-pill-mint")), 1, 1);

        VBox accountTray = Ui.tray("Account Details", "Your personal login and credentials in NextIT", accountGrid);
        HBox.setHgrow(accountTray, Priority.ALWAYS);

        // Department tray
        GridPane deptGrid = new GridPane();
        deptGrid.setHgap(16);
        deptGrid.setVgap(14);
        deptGrid.getColumnConstraints().addAll(col1, col2);

        deptGrid.add(Ui.propertyTile("Employee Number", value(instructor.getEmployeeNumber())), 0, 0);
        deptGrid.add(Ui.propertyTile("Department", value(instructor.getDepartment())), 1, 0);
        deptGrid.add(Ui.propertyTile("Specialization", value(instructor.getSpecialization())), 0, 1);
        deptGrid.add(Ui.propertyTile("Status", Ui.badge("Active Faculty", "badge-pill-mint")), 1, 1);

        VBox deptTray = Ui.tray("Faculty & Department", "Academic department assignment and subject specialization", deptGrid);
        HBox.setHgrow(deptTray, Priority.ALWAYS);

        HBox traysRow = new HBox(20, accountTray, deptTray);

        HBox notice = new HBox(12);
        notice.getStyleClass().add("profile-notice-cushion");
        Label noticeIcon = new Label("ℹ");
        noticeIcon.getStyleClass().add("profile-notice-icon");
        Label noticeText = new Label("Faculty assignments and instructor records are managed by the academic administration.");
        noticeText.getStyleClass().add("profile-notice-text");
        notice.getChildren().addAll(noticeIcon, noticeText);

        return Ui.page("My Profile", "Your account and instructor profile information. Contact an administrator if any details need correction.",
                hero, traysRow, notice);
    }

    public static Node students() {
        Map<Integer, String> names = directory.studentNames();
        TableView<Student> table = Ui.table();
        Ui.addColumn(table, "Student No.", Student::getStudentNumber);
        Ui.addColumn(table, "Name", s -> names.getOrDefault(s.getStudentId(), "Student #" + s.getStudentId()));
        Ui.addColumn(table, "Program", Student::getProgram);
        Ui.addColumn(table, "Year", s -> String.valueOf(s.getYearLevel()));
        Ui.addColumn(table, "Section", Student::getSection);
        Ui.addColumn(table, "Status", Student::getEnrollmentStatus);

        TextField search = Ui.field("Search by name, student number, program or section...");
        Runnable filter = () -> {
            names.clear();
            names.putAll(directory.studentNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(directory.students().stream().filter(s ->
                    names.getOrDefault(s.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getStudentNumber()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getProgram()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getSection()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        filter.run();
        search.textProperty().addListener((obs, old, value) -> filter.run());
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());
        Button exportCsv = Ui.button("📥 Export CSV", "clay-btn-secondary");
        exportCsv.setOnAction(e -> com.nextit.util.ExportService.exportTableViewToCsv(table, "students_roster.csv", "Enrolled Students Roster", exportCsv.getScene().getWindow()));
        Button exportReport = Ui.button("📄 Print / PDF", "clay-btn-secondary");
        exportReport.setOnAction(e -> com.nextit.util.ExportService.exportTableViewToHtmlReport(table, "students_roster_report.html", "Enrolled Students Roster", exportReport.getScene().getWindow()));
        return Ui.page("Students", "Browse the students enrolled in the program and their academic profile details.",
                Ui.toolbar(new Label("Search:"), search, refresh, exportCsv, exportReport), table);
    }

    public static Node portfolios() {
        Map<Integer, String> studentNames = directory.studentNames();
        TableView<Portfolio> table = Ui.table();
        Ui.addColumn(table, "Student", p -> studentNames.getOrDefault(p.getStudentId(), "Student #" + p.getStudentId()));
        Ui.addColumn(table, "Headline", Portfolio::getHeadline);
        Ui.addColumn(table, "Career Goal", Portfolio::getCareerGoal);
        Ui.addColumn(table, "Visibility", Portfolio::getVisibility);
        Ui.addColumn(table, "Updated", p -> p.getUpdatedAt() == null ? "" : p.getUpdatedAt().toLocalDate().toString());

        TextField search = Ui.field("Search by student, headline or career goal...");
        Runnable filter = () -> {
            studentNames.clear();
            studentNames.putAll(directory.studentNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(portfolios.approvedPortfolios().stream()
                    .filter(p -> q.isBlank()
                            || studentNames.getOrDefault(p.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || value(p.getHeadline()).toLowerCase(Locale.ROOT).contains(q)
                            || value(p.getCareerGoal()).toLowerCase(Locale.ROOT).contains(q))
                    .toList()));
        };
        filter.run();
        search.textProperty().addListener((obs, old, value) -> filter.run());

        Button view = new Button("View Portfolio");
        view.getStyleClass().add("primary-button");
        view.setOnAction(e -> {
            Portfolio selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a portfolio to view."); return; }
            guard(() -> PortfolioDetailsView.show(selected, true));
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());
        return Ui.page("Student Portfolios",
                "Browse the portfolios that have been approved or made public by students.",
                Ui.toolbar(new Label("Search:"), search, view, refresh), table);
    }

    public static Node subjects() {
        TableView<Subject> table = Ui.table();
        Ui.addColumn(table, "Code", Subject::getSubjectCode);
        Ui.addColumn(table, "Subject", Subject::getSubjectName);
        Ui.addColumn(table, "Units", s -> String.valueOf(s.getUnits()));
        Ui.addColumn(table, "Description", Subject::getDescription);

        TextField search = Ui.field("Search by code or subject name...");
        Runnable filter = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(directory.subjects().stream().filter(s ->
                    String.valueOf(s.getSubjectCode()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getSubjectName()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        filter.run();
        search.textProperty().addListener((obs, old, value) -> filter.run());
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());
        return Ui.page("Subjects", "Reference information for the subjects used by grades, attendance and projects.",
                Ui.toolbar(new Label("Search:"), search, refresh), table);
    }

    public static Node skills() {
        return SkillCatalogView.view();
    }

    public static Node achievements() {
        return DevelopmentManagementViews.achievements();
    }

    public static Node certifications() {
        return DevelopmentManagementViews.certifications();
    }

    public static Node opportunities() {
        return DevelopmentManagementViews.opportunities(false);
    }

    public static Node projects() {
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        TableView<Project> table = Ui.table();
        Ui.addColumn(table, "Title", Project::getProjectTitle);
        Ui.addColumn(table, "Student", p -> p.getStudentId() == null ? ""
                : students.getOrDefault(p.getStudentId(), "Student #" + p.getStudentId()));
        Ui.addColumn(table, "Type", Project::getProjectType);
        Ui.addColumn(table, "Subject", p -> p.getSubjectId() == null ? ""
                : subjects.getOrDefault(p.getSubjectId(), "Subject #" + p.getSubjectId()));
        Ui.addColumn(table, "Status", Project::getStatus);
        Ui.addColumn(table, "Technologies", Project::getTechnologiesUsed);
        Ui.addColumn(table, "Repository", Project::getRepositoryUrl);
        List<Project> all = projects.allProjects();
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by title, student, subject, technology or status...");
        ComboBox<String> statusFilter = Ui.combo(
                List.of("All statuses", "Draft", "In Progress", "Completed", "Archived"), s -> s);
        statusFilter.getSelectionModel().selectFirst();
        Runnable filter = () -> {
            students.clear();
            students.putAll(directory.studentNames());
            subjects.clear();
            subjects.putAll(directory.subjectNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            String status = statusFilter.getValue();
            table.setItems(FXCollections.observableArrayList(projects.allProjects().stream().filter(p ->
                    ("All statuses".equals(status) || status.equalsIgnoreCase(p.getStatus()))
                            && (q.isBlank()
                            || String.valueOf(p.getProjectTitle()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(p.getProjectType()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(p.getTechnologiesUsed()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(p.getStatus()).toLowerCase(Locale.ROOT).contains(q)
                            || (p.getStudentId() != null
                                && students.getOrDefault(p.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q))
                            || (p.getSubjectId() != null
                                && subjects.getOrDefault(p.getSubjectId(), "").toLowerCase(Locale.ROOT).contains(q)))).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> filter.run());
        statusFilter.valueProperty().addListener((obs, old, value) -> filter.run());

        Button view = new Button("View Details");
        view.getStyleClass().add("primary-button");
        view.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to review."); return; }
            guard(() -> showProjectReview(selected));
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());
        return Ui.page("Student Projects", "Review the projects students have recorded, including group members and instructor evaluations.",
                Ui.toolbar(new Label("Search:"), search, statusFilter, view, refresh), table);
    }

    private static void showProjectReview(Project p) {
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        Map<Integer, String> instructors = directory.instructorNames();
        Label description = new Label(value(p.getDescription()));
        description.setWrapText(true);

        StringBuilder memberText = new StringBuilder();
        projects.membersForProject(p.getProjectId()).forEach(m -> memberText.append("• ")
                .append(students.getOrDefault(m.getStudentId(), "Student #" + m.getStudentId()))
                .append(m.getRole() == null ? "" : " — " + m.getRole())
                .append(m.getContribution() == null ? "" : ": " + m.getContribution())
                .append('\n'));

        StringBuilder evaluationText = new StringBuilder(64);
        projects.allEvaluations().stream()
                .filter(e -> e.getProjectId() == p.getProjectId())
                .forEach(e -> evaluationText.append("• ")
                        .append(students.getOrDefault(e.getStudentId(), "Student #" + e.getStudentId()))
                        .append(" — overall ").append(e.getOverallScore() == null ? "n/a" : e.getOverallScore())
                        .append(", by ").append(instructors.getOrDefault(e.getInstructorId(),
                                "Instructor #" + e.getInstructorId()))
                        .append(e.getFeedback() == null ? "" : ": " + e.getFeedback())
                        .append('\n'));

        Node form = Ui.form(
                new String[]{"Title", "Owner", "Subject", "Type", "Status", "Technologies", "Repository",
                        "Demo", "Project date", "Description", "Group members", "Evaluations"},
                new Node[]{new Label(value(p.getProjectTitle())),
                        new Label(p.getStudentId() == null ? ""
                                : students.getOrDefault(p.getStudentId(), "Student #" + p.getStudentId())),
                        new Label(p.getSubjectId() == null ? ""
                                : subjects.getOrDefault(p.getSubjectId(), "Subject #" + p.getSubjectId())),
                        new Label(value(p.getProjectType())), new Label(value(p.getStatus())),
                        new Label(value(p.getTechnologiesUsed())), new Label(value(p.getRepositoryUrl())),
                        new Label(value(p.getDemoUrl())), new Label(Ui.formatDate(p.getProjectDate())),
                        description,
                        new Label(memberText.isEmpty() ? "No group members recorded." : memberText.toString()),
                        new Label(evaluationText.isEmpty() ? "No evaluations recorded." : evaluationText.toString())});
        Ui.viewDialog("Project - " + p.getProjectTitle(), form);
    }

    public static Node dashboard() {
        // 1. Hero cushion with status and live badges
        HBox hero = new HBox(16);
        hero.getStyleClass().add("page-header-cushion");
        hero.setAlignment(Pos.CENTER_LEFT);

        VBox heroTitles = new VBox(4);
        heroTitles.getChildren().addAll(
                Ui.title("Instructor Dashboard"),
                Ui.subtitle("Welcome, " + Session.currentUser().getFullName() + ". Manage grades, attendance, evaluations, and student support.")
        );

        Region heroSpacer = new Region();
        HBox.setHgrow(heroSpacer, Priority.ALWAYS);

        HBox statusPillBox = new HBox(8);
        statusPillBox.setAlignment(Pos.CENTER_RIGHT);
        StackPane statusDot = Ui.statusDot(true);
        Label statusText = new Label("Faculty Portal Active");
        statusText.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d6a4f; -fx-font-size: 12px;");
        HBox liveChip = new HBox(8, statusDot, statusText);
        liveChip.setAlignment(Pos.CENTER);
        liveChip.getStyleClass().addAll("badge-pill", "badge-pill-mint");

        Label termChip = Ui.badge("AY 2026-2027 • 1st Sem", "badge-pill-lavender");
        statusPillBox.getChildren().addAll(liveChip, termChip);

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

        // 3. Main Dashboard Body: Left Work Area + Dedicated Right Side Panel
        HBox dashboardBody = new HBox(20);
        dashboardBody.setAlignment(Pos.TOP_LEFT);

        VBox leftColumn = new VBox(20);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        VBox rightColumnHolder = new VBox();

        // Left Component A: Dual Charts Row
        HBox chartsRow = new HBox(14);
        chartsRow.setAlignment(Pos.TOP_LEFT);

        // Left Component B: Recent Student Grades Table Tray
        VBox gradesTray = new VBox(14);
        gradesTray.getStyleClass().add("clay-tray");
        gradesTray.setFillWidth(true);

        Map<Integer, Enrollment> enrollments = enrollmentIndex();
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();

        TableView<Grade> gradeTable = Ui.table("No grade records found matching filter.");
        gradeTable.setPrefHeight(290);
        gradeTable.setMinHeight(220);

        Ui.addColumn(gradeTable, "Student", g -> studentLabel(g.getEnrollmentId(), enrollments, students));
        Ui.addColumn(gradeTable, "Subject", g -> subjectLabel(g.getEnrollmentId(), enrollments, subjects));
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
        Ui.addColumn(gradeTable, "Recorded", g -> g.getRecordedAt() == null ? "" : g.getRecordedAt().toLocalDate().toString());

        TextField searchGrades = Ui.field("Filter by student, subject, or activity...");
        searchGrades.setPrefWidth(240);

        ComboBox<String> typeFilter = new ComboBox<>(FXCollections.observableArrayList(
                "All Types", "Quiz", "Examination", "Laboratory", "Project", "Assignment"));
        typeFilter.setValue("All Types");

        HBox trayHeader = new HBox(12);
        trayHeader.setAlignment(Pos.CENTER_LEFT);

        VBox trayTitles = new VBox(3);
        Label trayTitle = new Label("Recent Grade Entries");
        trayTitle.getStyleClass().add("tray-title");
        Label traySub = new Label("Student performance records and activity evaluations");
        traySub.getStyleClass().add("tray-subtitle");
        trayTitles.getChildren().addAll(trayTitle, traySub);

        Region traySpacer = new Region();
        HBox.setHgrow(traySpacer, Priority.ALWAYS);

        Button exportCsvBtn = Ui.button("📥 Export CSV", "clay-btn-secondary");
        exportCsvBtn.setOnAction(e -> com.nextit.util.ExportService.exportTableViewToCsv(gradeTable, "instructor_grades.csv", "Student Grade Records", exportCsvBtn.getScene().getWindow()));

        Button exportReportBtn = Ui.button("📄 Print / PDF", "clay-btn-secondary");
        exportReportBtn.setOnAction(e -> com.nextit.util.ExportService.exportTableViewToHtmlReport(gradeTable, "instructor_grades_report.html", "Student Grade Records", exportReportBtn.getScene().getWindow()));

        trayHeader.getChildren().addAll(trayTitles, traySpacer, searchGrades, typeFilter, exportCsvBtn, exportReportBtn);
        gradesTray.getChildren().addAll(trayHeader, gradeTable);

        leftColumn.getChildren().addAll(chartsRow, gradesTray);
        dashboardBody.getChildren().addAll(leftColumn, rightColumnHolder);

        // Self-refreshing dashboard logic
        final Runnable[] refreshHolder = new Runnable[1];
        Runnable refreshDashboard = () -> {
            List<Grade> allGrades = academic.allGrades();
            List<Attendance> allAttendance = academic.allAttendance();

            // Refresh Map Lookups
            enrollments.clear();
            enrollments.putAll(enrollmentIndex());
            students.clear();
            students.putAll(directory.studentNames());
            subjects.clear();
            subjects.putAll(directory.subjectNames());

            // 1. Update 8 Full-Width Stat Cards
            cards1.getChildren().clear();
            cards2.getChildren().clear();

            VBox c1 = Ui.card("Students", String.valueOf(directory.students().size()));
            VBox c2 = Ui.card("Grade Records", String.valueOf(allGrades.size()));
            VBox c3 = Ui.card("Attendance Records", String.valueOf(allAttendance.size()));
            VBox c4 = Ui.card("Skill Evaluations", String.valueOf(skills.all().size()));

            VBox c5 = Ui.card("Project Evaluations", String.valueOf(projects.allEvaluations().size()));
            VBox c6 = Ui.card("Academic Support", String.valueOf(support.all().size()));
            VBox c7 = Ui.card("Projects", String.valueOf(projects.allProjects().size()));
            VBox c8 = Ui.card("Catalog Subjects", String.valueOf(directory.subjects().size()));

            for (VBox c : List.of(c1, c2, c3, c4, c5, c6, c7, c8)) {
                HBox.setHgrow(c, Priority.ALWAYS);
                c.setMaxWidth(Double.MAX_VALUE);
            }

            cards1.getChildren().addAll(c1, c2, c3, c4);
            cards2.getChildren().addAll(c5, c6, c7, c8);

            // 2. Update Dual Charts
            chartsRow.getChildren().clear();
            Node attendanceChart = DashboardCharts.attendanceSummary(allAttendance);
            Node gradesChart = DashboardCharts.averageByActivityType(allGrades);
            HBox.setHgrow(attendanceChart, Priority.ALWAYS);
            HBox.setHgrow(gradesChart, Priority.ALWAYS);
            chartsRow.getChildren().addAll(attendanceChart, gradesChart);

            // 3. Update Right Side Panel
            rightColumnHolder.getChildren().clear();
            VBox rightPanel = createInstructorRightSidePanel(allAttendance, allGrades, enrollments, students, () -> refreshHolder[0].run());
            rightColumnHolder.getChildren().add(rightPanel);

            // 4. Update Grades Table
            String q = searchGrades.getText() == null ? "" : searchGrades.getText().toLowerCase(Locale.ROOT);
            String selectedType = typeFilter.getValue();
            List<Grade> filtered = allGrades.stream().filter(g -> {
                String sName = studentLabel(g.getEnrollmentId(), enrollments, students).toLowerCase(Locale.ROOT);
                String subName = subjectLabel(g.getEnrollmentId(), enrollments, subjects).toLowerCase(Locale.ROOT);
                String actName = String.valueOf(g.getActivityName()).toLowerCase(Locale.ROOT);
                boolean matchesQ = q.isBlank() || sName.contains(q) || subName.contains(q) || actName.contains(q);
                boolean matchesType = selectedType == null || "All Types".equalsIgnoreCase(selectedType)
                        || String.valueOf(g.getActivityType()).equalsIgnoreCase(selectedType);
                return matchesQ && matchesType;
            }).toList();
            gradeTable.setItems(FXCollections.observableArrayList(filtered));
        };
        refreshHolder[0] = refreshDashboard;

        // Action Column: Edit Grade
        gradeTable.getColumns().add(Ui.actionColumn("Action", g -> {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 10; -fx-background-radius: 12;");
            editBtn.setOnAction(ev -> editGrade(g, refreshDashboard));
            return editBtn;
        }));

        searchGrades.textProperty().addListener((obs, old, val) -> refreshDashboard.run());
        typeFilter.valueProperty().addListener((obs, old, val) -> refreshDashboard.run());

        refreshDashboard.run();

        VBox scrollContent = new VBox(20);
        scrollContent.setFillWidth(true);
        scrollContent.setStyle("-fx-background-color: #f1f2fa;");
        scrollContent.setPadding(new Insets(10, 24, 24, 24));
        scrollContent.getChildren().addAll(cards1, cards2, dashboardBody);

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

    private static VBox createInstructorRightSidePanel(List<Attendance> allAttendance, List<Grade> allGrades,
                                                       Map<Integer, Enrollment> enrollments,
                                                       Map<Integer, String> students, Runnable refresh) {
        VBox rightSide = new VBox(16);
        rightSide.setPrefWidth(360);
        rightSide.setMinWidth(320);

        // 1. Quick Actions Launcher
        VBox quickActions = new VBox(12);
        quickActions.getStyleClass().add("clay-tray");

        Label actTitle = new Label("Quick Actions");
        actTitle.getStyleClass().add("tray-title");
        Label actSub = new Label("Faculty academic shortcuts");
        actSub.getStyleClass().add("tray-subtitle");

        Button recordGradeBtn = new Button("📝  Record Student Grade");
        recordGradeBtn.setMaxWidth(Double.MAX_VALUE);
        recordGradeBtn.getStyleClass().add("primary-button");
        recordGradeBtn.setOnAction(e -> editGrade(null, refresh));

        Button recordAttendanceBtn = new Button("📅  Log Class Attendance");
        recordAttendanceBtn.setMaxWidth(Double.MAX_VALUE);
        recordAttendanceBtn.getStyleClass().add("secondary-button");
        recordAttendanceBtn.setOnAction(e -> editAttendance(null, refresh));

        Button refreshBtn = new Button("🔄  Refresh Academic Stats");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setOnAction(e -> {
            refresh.run();
            Alerts.info("Academic stats refreshed.");
        });

        quickActions.getChildren().addAll(new VBox(3, actTitle, actSub), recordGradeBtn, recordAttendanceBtn, refreshBtn);

        // 2. Class Engagement & Health Card
        VBox engagementCard = new VBox(14);
        engagementCard.getStyleClass().add("clay-tray");

        Label engTitle = new Label("Academic Engagement");
        engTitle.getStyleClass().add("tray-title");
        Label engSub = new Label("Classroom participation & grading");
        engSub.getStyleClass().add("tray-subtitle");

        VBox queueBox = new VBox(8);
        Label queueHeader = new Label("CLASSROOM METRICS");
        queueHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #8e8394; -fx-letter-spacing: 0.5px;");

        long presentCount = allAttendance.stream().filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();

        HBox attRow = new HBox(8);
        attRow.setAlignment(Pos.CENTER_LEFT);
        Label aLabel = new Label("Attendance Logged:");
        aLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #383139;");
        Region aSp = new Region();
        HBox.setHgrow(aSp, Priority.ALWAYS);
        Node aBadge = Ui.badge(allAttendance.size() + " (" + presentCount + " Present)", "badge-pill-mint");
        attRow.getChildren().addAll(aLabel, aSp, aBadge);

        HBox gradeRow = new HBox(8);
        gradeRow.setAlignment(Pos.CENTER_LEFT);
        Label gLabel = new Label("Graded Activities:");
        gLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #383139;");
        Region gSp = new Region();
        HBox.setHgrow(gSp, Priority.ALWAYS);
        Node gBadge = Ui.badge(allGrades.size() + " Submissions", "badge-pill-blue");
        gradeRow.getChildren().addAll(gLabel, gSp, gBadge);

        HBox supRow = new HBox(8);
        supRow.setAlignment(Pos.CENTER_LEFT);
        Label sLabel = new Label("Support Requests:");
        sLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #383139;");
        Region sSp = new Region();
        HBox.setHgrow(sSp, Priority.ALWAYS);
        Node sBadge = Ui.badge(support.all().size() + " Active", "badge-pill-lavender");
        supRow.getChildren().addAll(sLabel, sSp, sBadge);

        queueBox.getChildren().addAll(queueHeader, attRow, gradeRow, supRow);

        Separator sep = new Separator();

        // Faculty clearance
        VBox facultyBox = new VBox(6);
        Label fHeader = new Label("FACULTY CLEARANCE");
        fHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #8e8394; -fx-letter-spacing: 0.5px;");
        Label fName = new Label(Session.currentUser().getFullName());
        fName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #383139;");
        Label fRole = new Label("Instructor / Faculty Evaluator");
        fRole.setStyle("-fx-font-size: 11px; -fx-text-fill: #8e8394;");
        facultyBox.getChildren().addAll(fHeader, fName, fRole);

        engagementCard.getChildren().addAll(new VBox(3, engTitle, engSub), queueBox, sep, facultyBox);

        // 3. Recent Submissions Stream
        VBox recentGradesCard = new VBox(12);
        recentGradesCard.getStyleClass().add("clay-tray");
        Label rTitle = new Label("Latest Grade Submissions");
        rTitle.getStyleClass().add("tray-title");
        Label rSub = new Label("Recent student evaluations");
        rSub.getStyleClass().add("tray-subtitle");
        recentGradesCard.getChildren().add(new VBox(3, rTitle, rSub));

        List<Grade> latestGrades = allGrades.stream()
                .sorted((a, b) -> Integer.compare(b.getGradeId(), a.getGradeId()))
                .limit(3)
                .toList();

        if (latestGrades.isEmpty()) {
            Label empty = new Label("No graded submissions yet.");
            empty.setStyle("-fx-font-size: 11px; -fx-text-fill: #8e8394;");
            recentGradesCard.getChildren().add(empty);
        } else {
            for (Grade g : latestGrades) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                item.getStyleClass().add("activity-item");

                String stName = studentLabel(g.getEnrollmentId(), enrollments, students);
                StackPane avatar = Ui.avatar(Ui.initials(stName), "avatar-pink");
                avatar.setPrefSize(32, 32);
                avatar.setMinSize(32, 32);

                VBox info = new VBox(2);
                Label name = new Label(stName);
                name.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #383139;");
                Label act = new Label(Ui.value(g.getActivityName()));
                act.setStyle("-fx-font-size: 10px; -fx-text-fill: #8e8394;");
                info.getChildren().addAll(name, act);
                HBox.setHgrow(info, Priority.ALWAYS);

                String grText = g.getGrade() == null ? "" : String.format("%.1f%%", g.getGrade());
                Label scoreBadge = Ui.badge(grText, "badge-pill-yellow");
                item.getChildren().addAll(avatar, info, scoreBadge);
                recentGradesCard.getChildren().add(item);
            }
        }

        rightSide.getChildren().addAll(quickActions, engagementCard, recentGradesCard);
        return rightSide;
    }

    public static Node grades() {
        Map<Integer, Enrollment> enrollments = enrollmentIndex();
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        Map<Integer, String> instructors = directory.instructorNames();
        List<Grade> all = academic.allGrades();

        TableView<Grade> table = Ui.table();
        Ui.addColumn(table, "Student", g -> studentLabel(g.getEnrollmentId(), enrollments, students));
        Ui.addColumn(table, "Subject", g -> subjectLabel(g.getEnrollmentId(), enrollments, subjects));
        Ui.addColumn(table, "Activity", Grade::getActivityName);
        Ui.addColumn(table, "Type", Grade::getActivityType);
        Ui.addColumn(table, "Score", g -> g.getScore() + " / " + g.getMaxScore());
        Ui.addColumn(table, "Grade", g -> g.getGrade() == null ? "" : String.format("%.2f%%", g.getGrade()));
        Ui.addColumn(table, "Remarks", Grade::getRemarks);
        Ui.addColumn(table, "Recorded By", g -> instructors.getOrDefault(g.getInstructorId(), "Instructor #" + g.getInstructorId()));
        Ui.addColumn(table, "Recorded", g -> g.getRecordedAt() == null ? "" : g.getRecordedAt().toLocalDate().toString());
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by student, subject or activity...");
        Runnable applyFilter = () -> {
            enrollments.clear();
            enrollments.putAll(enrollmentIndex());
            students.clear();
            students.putAll(directory.studentNames());
            subjects.clear();
            subjects.putAll(directory.subjectNames());
            instructors.clear();
            instructors.putAll(directory.instructorNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(academic.allGrades().stream().filter(g ->
                    studentLabel(g.getEnrollmentId(), enrollments, students).toLowerCase(Locale.ROOT).contains(q)
                            || subjectLabel(g.getEnrollmentId(), enrollments, subjects).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(g.getActivityName()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Record Grade");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editGrade(null, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Grade selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a grade to edit."); return; }
            editGrade(selected, applyFilter);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Grade selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a grade to delete."); return; }
            if (Ui.confirmed("Delete the grade for \"" + selected.getActivityName() + "\"?")) {
                guard(() -> {
                    academic.deleteGrade(selected.getGradeId());
                    applyFilter.run();
                });
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Grade Management", "Record activity scores for students enrolled in your subjects. Grades are calculated automatically from score and maximum score.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refresh), table);
    }

    private static void editGrade(Grade existing, Runnable refresh) {
        List<Enrollment> enrollmentList = academic.allEnrollments();
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        ComboBox<Enrollment> enrollment = enrollmentCombo(enrollmentList, students, subjects);
        Enrollment current = existing == null ? null : enrollmentList.stream()
                .filter(e -> e.getEnrollmentId() == existing.getEnrollmentId())
                .findFirst().orElse(null);
        if (current != null) enrollment.getSelectionModel().select(current);
        TextField activity = Ui.field("Activity name");
        activity.setText(existing == null ? "" : existing.getActivityName());
        ComboBox<String> type = Ui.combo(ACTIVITY_TYPES, s -> s);
        if (existing == null) type.getSelectionModel().selectFirst();
        else type.getSelectionModel().select(existing.getActivityType());
        TextField score = Ui.field("Score");
        if (existing != null) score.setText(String.valueOf(existing.getScore()));
        TextField max = Ui.field("Maximum score");
        if (existing != null) max.setText(String.valueOf(existing.getMaxScore()));
        TextField remarks = Ui.field("Remarks");
        remarks.setText(existing == null ? "" : existing.getRemarks());
        Node form = Ui.form(new String[]{"Enrollment", "Activity", "Type", "Score", "Maximum score", "Remarks"},
                new Node[]{enrollment, activity, type, score, max, remarks});
        Ui.dialog(existing == null ? "Record Grade" : "Edit Grade", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                if (enrollment.getValue() == null) throw new AppException("Select a student enrollment.");
                Grade grade = new Grade();
                if (existing != null) {
                    grade.setGradeId(existing.getGradeId());
                    grade.setInstructorId(existing.getInstructorId());
                    grade.setRecordedAt(existing.getRecordedAt());
                } else {
                    grade.setInstructorId(instructorId());
                }
                grade.setEnrollmentId(enrollment.getValue().getEnrollmentId());
                grade.setActivityName(activity.getText());
                grade.setActivityType(type.getValue());
                grade.setScore(Ui.parseDouble(score.getText(), "Score"));
                grade.setMaxScore(Ui.parseDouble(max.getText(), "Maximum score"));
                grade.setRemarks(remarks.getText());
                if (existing == null) academic.recordGrade(grade);
                else academic.updateGrade(grade);
                refresh.run();
                Alerts.info("Grade saved.");
            });
        });
    }

    public static Node attendance() {
        Map<Integer, Enrollment> enrollments = enrollmentIndex();
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        Map<Integer, String> instructors = directory.instructorNames();
        List<Attendance> all = academic.allAttendance();

        TableView<Attendance> table = Ui.table();
        Ui.addColumn(table, "Student", a -> studentLabel(a.getEnrollmentId(), enrollments, students));
        Ui.addColumn(table, "Subject", a -> subjectLabel(a.getEnrollmentId(), enrollments, subjects));
        Ui.addColumn(table, "Date", a -> Ui.formatDate(a.getAttendanceDate()));
        Ui.addColumn(table, "Status", Attendance::getStatus);
        Ui.addColumn(table, "Remarks", Attendance::getRemarks);
        Ui.addColumn(table, "Recorded By", a -> instructors.getOrDefault(a.getRecordedBy(), "Instructor #" + a.getRecordedBy()));
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by student, subject, date, status or remarks...");
        Runnable applyFilter = () -> {
            enrollments.clear();
            enrollments.putAll(enrollmentIndex());
            students.clear();
            students.putAll(directory.studentNames());
            subjects.clear();
            subjects.putAll(directory.subjectNames());
            instructors.clear();
            instructors.putAll(directory.instructorNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(academic.allAttendance().stream().filter(a ->
                    studentLabel(a.getEnrollmentId(), enrollments, students).toLowerCase(Locale.ROOT).contains(q)
                            || subjectLabel(a.getEnrollmentId(), enrollments, subjects).toLowerCase(Locale.ROOT).contains(q)
                            || Ui.formatDate(a.getAttendanceDate()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(a.getStatus()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(a.getRemarks()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Record Attendance");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editAttendance(null, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Attendance selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an attendance record to edit."); return; }
            editAttendance(selected, applyFilter);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Attendance selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an attendance record to delete."); return; }
            if (Ui.confirmed("Delete the attendance record for " + selected.getAttendanceDate() + "?")) {
                guard(() -> {
                    academic.deleteAttendance(selected.getAttendanceId());
                    applyFilter.run();
                });
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Attendance", "Record attendance for enrolled students and keep an auditable history of each class.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refresh), table);
    }

    private static void editAttendance(Attendance existing, Runnable refresh) {
        List<Enrollment> enrollmentList = academic.allEnrollments();
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        ComboBox<Enrollment> enrollment = enrollmentCombo(enrollmentList, students, subjects);
        Enrollment current = existing == null ? null : enrollmentList.stream()
                .filter(e -> e.getEnrollmentId() == existing.getEnrollmentId())
                .findFirst().orElse(null);
        if (current != null) enrollment.getSelectionModel().select(current);
        DatePicker date = Ui.date(existing == null || existing.getAttendanceDate() == null
                ? LocalDate.now() : existing.getAttendanceDate());
        ComboBox<String> status = Ui.combo(List.of("Present", "Absent", "Late", "Excused"), s -> s);
        if (existing == null) status.getSelectionModel().selectFirst();
        else status.getSelectionModel().select(existing.getStatus());
        TextField remarks = Ui.field("Remarks");
        remarks.setText(existing == null ? "" : existing.getRemarks());
        Node form = Ui.form(new String[]{"Enrollment", "Date", "Status", "Remarks"},
                new Node[]{enrollment, date, status, remarks});
        Ui.dialog(existing == null ? "Record Attendance" : "Edit Attendance", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                if (enrollment.getValue() == null) throw new AppException("Select a student enrollment.");
                Attendance attendance = new Attendance();
                if (existing != null) {
                    attendance.setAttendanceId(existing.getAttendanceId());
                    attendance.setRecordedBy(existing.getRecordedBy());
                } else {
                    attendance.setRecordedBy(instructorId());
                }
                attendance.setEnrollmentId(enrollment.getValue().getEnrollmentId());
                attendance.setAttendanceDate(date.getValue());
                attendance.setStatus(status.getValue());
                attendance.setRemarks(remarks.getText());
                if (existing == null) academic.recordAttendance(attendance);
                else academic.updateAttendance(attendance);
                refresh.run();
                Alerts.info("Attendance saved.");
            });
        });
    }

    public static Node technicalEvaluations() {
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> skillNames = directory.skillNames();
        Map<Integer, String> instructors = directory.instructorNames();
        List<StudentSkill> all = skills.all();

        TableView<StudentSkill> table = Ui.table();
        Ui.addColumn(table, "Student", s -> students.getOrDefault(s.getStudentId(), "Student #" + s.getStudentId()));
        Ui.addColumn(table, "Skill", s -> skillNames.getOrDefault(s.getSkillId(), "Skill #" + s.getSkillId()));
        Ui.addColumn(table, "Proficiency", StudentSkill::getProficiencyLevel);
        Ui.addColumn(table, "Score", s -> s.getAssessmentScore() == null ? "" : String.format("%.2f%%", s.getAssessmentScore()));
        Ui.addColumn(table, "Assessed By", s -> s.getAssessedBy() == null ? "" : instructors.getOrDefault(s.getAssessedBy(), "Instructor #" + s.getAssessedBy()));
        Ui.addColumn(table, "Assessed At", s -> s.getAssessedAt() == null ? "" : s.getAssessedAt().toLocalDate().toString());
        Ui.addColumn(table, "Remarks", StudentSkill::getRemarks);
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by student, skill, proficiency, score or remarks...");
        Runnable applyFilter = () -> {
            students.clear();
            students.putAll(directory.studentNames());
            skillNames.clear();
            skillNames.putAll(directory.skillNames());
            instructors.clear();
            instructors.putAll(directory.instructorNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(skills.all().stream().filter(s ->
                    students.getOrDefault(s.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || skillNames.getOrDefault(s.getSkillId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getProficiencyLevel()).toLowerCase(Locale.ROOT).contains(q)
                            || (s.getAssessmentScore() != null
                                && String.format("%.2f%%", s.getAssessmentScore()).toLowerCase(Locale.ROOT).contains(q))
                            || String.valueOf(s.getRemarks()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button evaluate = new Button("Assess Skill");
        evaluate.getStyleClass().add("primary-button");
        evaluate.setOnAction(e -> {
            ComboBox<Student> student = studentCombo(directory.students());
            ComboBox<TechnicalSkill> skill = Ui.combo(directory.technicalSkills(), s -> s.getSkillName() + " (" + s.getCategory() + ")");
            ComboBox<String> level = Ui.combo(PROFICIENCY_LEVELS, s -> s);
            level.getSelectionModel().selectFirst();
            TextField score = Ui.field("Assessment score (0-100)");
            TextField remarks = Ui.field("Remarks");
            Runnable prefill = () -> {
                if (student.getValue() == null || skill.getValue() == null) return;
                java.util.Optional<StudentSkill> existing =
                        skills.find(student.getValue().getStudentId(), skill.getValue().getSkillId());
                if (existing.isPresent()) {
                    level.getSelectionModel().select(existing.get().getProficiencyLevel());
                    score.setText(scoreText(existing.get().getAssessmentScore()));
                    remarks.setText(value(existing.get().getRemarks()));
                } else {
                    level.getSelectionModel().selectFirst();
                    score.clear();
                    remarks.clear();
                }
            };
            student.valueProperty().addListener((obs, old, selected) -> guard(prefill));
            skill.valueProperty().addListener((obs, old, selected) -> guard(prefill));
            Node form = Ui.form(new String[]{"Student", "Technical skill", "Proficiency", "Score", "Remarks"},
                    new Node[]{student, skill, level, score, remarks});
            Ui.dialog("Assess Technical Skill", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    if (student.getValue() == null) throw new AppException("Select a student.");
                    if (skill.getValue() == null) throw new AppException("Select a technical skill.");
                    StudentSkill evaluation = new StudentSkill();
                    evaluation.setStudentId(student.getValue().getStudentId());
                    evaluation.setSkillId(skill.getValue().getSkillId());
                    evaluation.setProficiencyLevel(level.getValue());
                    evaluation.setAssessmentScore(Ui.optionalDouble(score.getText(), "Assessment score"));
                    evaluation.setRemarks(remarks.getText());
                    skills.recordEvaluation(evaluation, instructorId());
                    applyFilter.run();
                    Alerts.info("Skill assessment saved.");
                });
            });
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            StudentSkill selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an evaluation to delete."); return; }
            if (Ui.confirmed("Delete this skill assessment?")) {
                guard(() -> { skills.delete(selected.getStudentSkillId()); applyFilter.run(); });
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Technical Skill Evaluation", "Assess student proficiency against the technical skill catalogue maintained by instructors and administrators.",
                Ui.toolbar(new Label("Search:"), search, evaluate, delete, refresh), table);
    }

    public static Node evaluations() {
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> projectsById = projects.allProjects().stream()
                .collect(Collectors.toMap(Project::getProjectId, Project::getProjectTitle, (a, b) -> a));
        Map<Integer, String> instructors = directory.instructorNames();
        List<ProjectEvaluation> all = projects.allEvaluations();

        TableView<ProjectEvaluation> table = Ui.table();
        Ui.addColumn(table, "Project", e -> projectsById.getOrDefault(e.getProjectId(), "Project #" + e.getProjectId()));
        Ui.addColumn(table, "Student", e -> students.getOrDefault(e.getStudentId(), "Student #" + e.getStudentId()));
        Ui.addColumn(table, "Contribution", e -> e.getContributionScore() == null ? "" : String.valueOf(e.getContributionScore()));
        Ui.addColumn(table, "Technical", e -> e.getTechnicalScore() == null ? "" : String.valueOf(e.getTechnicalScore()));
        Ui.addColumn(table, "Teamwork", e -> e.getTeamworkScore() == null ? "" : String.valueOf(e.getTeamworkScore()));
        Ui.addColumn(table, "Overall", e -> e.getOverallScore() == null ? "" : String.valueOf(e.getOverallScore()));
        Ui.addColumn(table, "Feedback", ProjectEvaluation::getFeedback);
        Ui.addColumn(table, "Instructor", e -> instructors.getOrDefault(e.getInstructorId(), "Instructor #" + e.getInstructorId()));
        Ui.addColumn(table, "Evaluated", e -> e.getEvaluatedAt() == null ? "" : e.getEvaluatedAt().toLocalDate().toString());
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by project, student, feedback or instructor...");
        Runnable applyFilter = () -> {
            projectsById.clear();
            projects.allProjects().forEach(p -> projectsById.put(p.getProjectId(), p.getProjectTitle()));
            students.clear();
            students.putAll(directory.studentNames());
            instructors.clear();
            instructors.putAll(directory.instructorNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(projects.allEvaluations().stream().filter(e ->
                    projectsById.getOrDefault(e.getProjectId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || students.getOrDefault(e.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(e.getFeedback()).toLowerCase(Locale.ROOT).contains(q)
                            || instructors.getOrDefault(e.getInstructorId(), "").toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button evaluate = new Button("Evaluate Project");
        evaluate.getStyleClass().add("primary-button");
        evaluate.setOnAction(e -> {
            ComboBox<Project> project = Ui.combo(projects.allProjects(), Project::getProjectTitle);
            List<Student> allStudents = directory.students();
            ComboBox<Student> student = studentCombo(allStudents);
            project.valueProperty().addListener((obs, old, selectedProject) -> {
                if (selectedProject == null) return;
                java.util.Set<Integer> eligible = new java.util.HashSet<>();
                if (selectedProject.getStudentId() != null) eligible.add(selectedProject.getStudentId());
                projects.membersForProject(selectedProject.getProjectId())
                        .forEach(m -> eligible.add(m.getStudentId()));
                student.setItems(FXCollections.observableArrayList(allStudents.stream()
                        .filter(s -> eligible.contains(s.getStudentId())).toList()));
                student.getSelectionModel().clearSelection();
            });
            TextField contribution = Ui.field("Contribution score (0-100)");
            TextField technical = Ui.field("Technical score (0-100)");
            TextField teamwork = Ui.field("Teamwork score (0-100)");
            TextArea feedback = Ui.area("Feedback");
            student.valueProperty().addListener((obs, old, selectedStudent) -> {
                contribution.clear();
                technical.clear();
                teamwork.clear();
                feedback.clear();
                if (selectedStudent == null || project.getValue() == null) return;
                projects.evaluationFor(project.getValue().getProjectId(), selectedStudent.getStudentId())
                        .ifPresent(existing -> {
                            contribution.setText(scoreText(existing.getContributionScore()));
                            technical.setText(scoreText(existing.getTechnicalScore()));
                            teamwork.setText(scoreText(existing.getTeamworkScore()));
                            feedback.setText(existing.getFeedback());
                        });
            });
            Node form = Ui.form(new String[]{"Project", "Student", "Contribution", "Technical", "Teamwork", "Feedback"},
                    new Node[]{project, student, contribution, technical, teamwork, feedback});
            Ui.dialog("Evaluate Project", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    if (project.getValue() == null) throw new AppException("Select a project.");
                    if (student.getValue() == null) throw new AppException("Select a student.");
                    ProjectEvaluation evaluation = new ProjectEvaluation();
                    evaluation.setProjectId(project.getValue().getProjectId());
                    evaluation.setStudentId(student.getValue().getStudentId());
                    evaluation.setInstructorId(instructorId());
                    evaluation.setContributionScore(Ui.optionalDouble(contribution.getText(), "Contribution score"));
                    evaluation.setTechnicalScore(Ui.optionalDouble(technical.getText(), "Technical score"));
                    evaluation.setTeamworkScore(Ui.optionalDouble(teamwork.getText(), "Teamwork score"));
                    evaluation.setFeedback(feedback.getText());
                    projects.evaluate(evaluation);
                    applyFilter.run();
                    Alerts.info("Project evaluation saved.");
                });
            });
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Project Evaluations", "Evaluate individual group members on contribution, technical work and teamwork.",
                Ui.toolbar(new Label("Search:"), search, evaluate, refresh), table);
    }

    public static Node support() {
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        Map<Integer, String> instructors = directory.instructorNames();
        List<AcademicSupport> all = support.all();

        TableView<AcademicSupport> table = Ui.table();
        Ui.addColumn(table, "Student", s -> students.getOrDefault(s.getStudentId(), "Student #" + s.getStudentId()));
        Ui.addColumn(table, "Subject", s -> s.getSubjectId() == null ? "" : subjects.getOrDefault(s.getSubjectId(), "Subject #" + s.getSubjectId()));
        Ui.addColumn(table, "Type", AcademicSupport::getSupportType);
        Ui.addColumn(table, "Date", s -> Ui.formatDate(s.getSupportDate()));
        Ui.addColumn(table, "Reason", AcademicSupport::getReason);
        Ui.addColumn(table, "Action Taken", AcademicSupport::getActionTaken);
        Ui.addColumn(table, "Outcome", AcademicSupport::getOutcome);
        Ui.addColumn(table, "Remarks", AcademicSupport::getRemarks);
        Ui.addColumn(table, "Instructor", s -> instructors.getOrDefault(s.getInstructorId(), "Instructor #" + s.getInstructorId()));
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by student, subject, type, date, reason or outcome...");
        Runnable applyFilter = () -> {
            students.clear();
            students.putAll(directory.studentNames());
            subjects.clear();
            subjects.putAll(directory.subjectNames());
            instructors.clear();
            instructors.putAll(directory.instructorNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(support.all().stream().filter(s ->
                    students.getOrDefault(s.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || (s.getSubjectId() != null
                                && subjects.getOrDefault(s.getSubjectId(), "").toLowerCase(Locale.ROOT).contains(q))
                            || String.valueOf(s.getSupportType()).toLowerCase(Locale.ROOT).contains(q)
                            || Ui.formatDate(s.getSupportDate()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getReason()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getActionTaken()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getOutcome()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getRemarks()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Record Support");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> {
            ComboBox<Student> student = studentCombo(directory.students());
            ComboBox<Subject> subject = subjectCombo(directory.subjects());
            ComboBox<String> type = Ui.combo(List.of("Review", "Remedial", "Consultation", "Additional Exercise"), s -> s);
            type.getSelectionModel().selectFirst();
            DatePicker date = Ui.date(LocalDate.now());
            TextField reason = Ui.field("Reason");
            TextField action = Ui.field("Action taken");
            TextField outcome = Ui.field("Outcome");
            TextField remarks = Ui.field("Remarks");
            Node form = Ui.form(new String[]{"Student", "Subject", "Support type", "Date", "Reason", "Action taken", "Outcome", "Remarks"},
                    new Node[]{student, subject, type, date, reason, action, outcome, remarks});
            Ui.dialog("Record Academic Support", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    if (student.getValue() == null) throw new AppException("Select a student.");
                    AcademicSupport record = new AcademicSupport();
                    record.setStudentId(student.getValue().getStudentId());
                    record.setInstructorId(instructorId());
                    record.setSubjectId(subject.getValue() == null ? null : subject.getValue().getSubjectId());
                    record.setSupportType(type.getValue());
                    record.setSupportDate(date.getValue());
                    record.setReason(reason.getText());
                    record.setActionTaken(action.getText());
                    record.setOutcome(outcome.getText());
                    record.setRemarks(remarks.getText());
                    support.record(record);
                    applyFilter.run();
                    Alerts.info("Academic support record saved.");
                });
            });
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            AcademicSupport selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a support record to delete."); return; }
            if (Ui.confirmed("Delete this academic support record?")) {
                guard(() -> { support.delete(selected.getSupportId()); applyFilter.run(); });
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Academic Support", "Document reviews, remedial sessions, consultations and additional exercises for students who need help.",
                Ui.toolbar(new Label("Search:"), search, add, delete, refresh), table);
    }

    /** Enrollment selector that keeps the academic term visible, preventing ambiguous grades or attendance when a student retakes a subject. */
    private static ComboBox<Enrollment> enrollmentCombo(List<Enrollment> enrollments,
                                                        Map<Integer, String> students,
                                                        Map<Integer, String> subjects) {
        return Ui.combo(enrollments, e -> students.getOrDefault(e.getStudentId(), "Student #" + e.getStudentId())
                + " · " + subjects.getOrDefault(e.getSubjectId(), "Subject #" + e.getSubjectId())
                + " · " + value(e.getAcademicYear()) + " " + value(e.getSemester()));
    }

    private static Map<Integer, Enrollment> enrollmentIndex() {
        return academic.allEnrollments().stream()
                .collect(Collectors.toMap(Enrollment::getEnrollmentId, Function.identity(), (a, b) -> a));
    }

    private static String studentLabel(int enrollmentId, Map<Integer, Enrollment> enrollments, Map<Integer, String> students) {
        Enrollment e = enrollments.get(enrollmentId);
        return e == null ? "" : students.getOrDefault(e.getStudentId(), "Student #" + e.getStudentId());
    }

    private static String subjectLabel(int enrollmentId, Map<Integer, Enrollment> enrollments, Map<Integer, String> subjects) {
        Enrollment e = enrollments.get(enrollmentId);
        return e == null ? "" : subjects.getOrDefault(e.getSubjectId(), "Subject #" + e.getSubjectId());
    }

    private static ComboBox<Student> studentCombo(List<Student> students) {
        Map<Integer, String> names = directory.studentNames();
        return Ui.combo(students, s -> names.getOrDefault(s.getStudentId(), "Student #" + s.getStudentId())
                + " (" + s.getStudentNumber() + ")");
    }

    private static ComboBox<Subject> subjectCombo(List<Subject> subjects) {
        return Ui.combo(subjects, s -> s.getSubjectCode() + " - " + s.getSubjectName());
    }

    private static String value(String text) { return text == null ? "" : text; }
    private static String scoreText(Double score) { return score == null ? "" : String.valueOf(score); }

    private static void guard(Runnable action) {
        try {
            action.run();
        } catch (AppException e) {
            Alerts.error(e.getMessage());
        } catch (Exception e) {
            Alerts.error("Could not complete the operation. Please check the database connection.");
            org.slf4j.LoggerFactory.getLogger(InstructorViews.class).error("Instructor action failed", e);
        }
    }
}