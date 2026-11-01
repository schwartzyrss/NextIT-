package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.security.Session;
import com.nextit.service.*;
import com.nextit.util.Alerts;
import com.nextit.util.Ui;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;

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
                .orElseThrow(() -> new AppException("Instructor profile not found."));
    }

    public static Node profile() {
        User account = profiles.account(Session.currentUser().getUserId());
        Instructor instructor = profiles.instructorForUser(account.getUserId())
                .orElseThrow(() -> new AppException("Instructor profile not found."));
        Node form = Ui.form(
                new String[]{"Username", "Email", "Full name", "Role", "Employee number", "Department", "Specialization"},
                new Node[]{new Label(value(account.getUsername())), new Label(value(account.getEmail())),
                        new Label(value(account.getFullName())), new Label(String.valueOf(account.getRole())),
                        new Label(value(instructor.getEmployeeNumber())), new Label(value(instructor.getDepartment())),
                        new Label(value(instructor.getSpecialization()))});
        return Ui.page("My Profile", "Your account and instructor profile information. Contact an administrator if any details need correction.",
                form);
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
        return Ui.page("Students", "Browse the students enrolled in the program and their academic profile details.",
                Ui.toolbar(new Label("Search:"), search, refresh), table);
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
        java.util.List<Grade> allGrades = academic.allGrades();
        java.util.List<Attendance> allAttendance = academic.allAttendance();
        javafx.scene.layout.HBox cards1 = new javafx.scene.layout.HBox(12,
                Ui.card("Students", String.valueOf(directory.students().size())),
                Ui.card("Grade Records", String.valueOf(allGrades.size())),
                Ui.card("Attendance Records", String.valueOf(allAttendance.size())),
                Ui.card("Skill Evaluations", String.valueOf(skills.all().size())));
        javafx.scene.layout.HBox cards2 = new javafx.scene.layout.HBox(12,
                Ui.card("Project Evaluations", String.valueOf(projects.allEvaluations().size())),
                Ui.card("Academic Support", String.valueOf(support.all().size())),
                Ui.card("Projects", String.valueOf(projects.allProjects().size())));
        return Ui.page("Instructor Dashboard",
                "Welcome, " + Session.currentUser().getFullName() + ". Manage grades, attendance, evaluations and student support.",
                cards1, cards2,
                DashboardCharts.row(
                        DashboardCharts.attendanceSummary(allAttendance),
                        DashboardCharts.averageByActivityType(allGrades)));
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
