package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.service.AcademicService;
import com.nextit.service.DirectoryService;
import com.nextit.service.ProjectService;
import com.nextit.service.StudentSkillService;
import com.nextit.service.SupportService;
import com.nextit.util.Alerts;
import com.nextit.util.Ui;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

/**
 * Shared instructor/administrator academic record screens.
 *
 * <p>Instructors act as themselves. Administrators choose the instructor who
 * should own a new grade, attendance record, assessment or evaluation, which
 * keeps the audit trail accurate while still allowing administrative
 * correction.</p>
 */
final class AcademicManagementViews {

    private static final AcademicService academic = new AcademicService();
    private static final ProjectService projects = new ProjectService();
    private static final SupportService support = new SupportService();
    private static final StudentSkillService skills = new StudentSkillService();
    private static final DirectoryService directory = new DirectoryService();

    private static final List<String> ACTIVITY_TYPES =
            List.of("Quiz", "Examination", "Laboratory", "Project", "Assignment", "Recitation", "Other");
    private static final List<String> PROFICIENCY_LEVELS =
            List.of("Beginner", "Intermediate", "Advanced", "Expert");

    private AcademicManagementViews() {}

    /** Instructor picker used by administrator screens. */
    static ComboBox<Instructor> instructorSelector() {
        List<Instructor> instructors = directory.instructors();
        Map<Integer, String> names = directory.instructorNames();
        ComboBox<Instructor> combo = Ui.combo(instructors, i ->
                names.getOrDefault(i.getInstructorId(), "Instructor #" + i.getInstructorId())
                        + " (" + value(i.getEmployeeNumber()) + ")");
        combo.setPrefWidth(250);
        if (!instructors.isEmpty()) combo.getSelectionModel().selectFirst();
        return combo;
    }

    static Node instructorControl(ComboBox<Instructor> combo) {
        HBox box = new HBox(6, new Label("Acting instructor:"), combo);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    static int selectedInstructor(ComboBox<Instructor> combo) {
        if (combo.getValue() == null) {
            throw new AppException("Select the instructor who should own this academic record.");
        }
        return combo.getValue().getInstructorId();
    }

    static Node grades(IntSupplier actingInstructor, Node actorSelector) {
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
        add.setOnAction(e -> editGrade(null, actingInstructor, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Grade selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a grade to edit."); return; }
            editGrade(selected, actingInstructor, applyFilter);
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
        return Ui.page("Grade Management",
                "Record activity scores for enrolled students. Grades are calculated automatically from score and maximum score.",
                toolbar(actorSelector, new Label("Search:"), search, add, edit, delete, refresh), table);
    }

    private static void editGrade(Grade existing, IntSupplier actingInstructor, Runnable refresh) {
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
                    grade.setInstructorId(actingInstructor.getAsInt());
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

    static Node attendance(IntSupplier actingInstructor, Node actorSelector) {
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
        add.setOnAction(e -> editAttendance(null, actingInstructor, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Attendance selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an attendance record to edit."); return; }
            editAttendance(selected, actingInstructor, applyFilter);
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
                toolbar(actorSelector, new Label("Search:"), search, add, edit, delete, refresh), table);
    }

    private static void editAttendance(Attendance existing, IntSupplier actingInstructor, Runnable refresh) {
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
                    attendance.setRecordedBy(actingInstructor.getAsInt());
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

    static Node technicalEvaluations(IntSupplier actingInstructor, Node actorSelector) {
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
                    skills.recordEvaluation(evaluation, actingInstructor.getAsInt());
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
        return Ui.page("Technical Skill Evaluation",
                "Assess student proficiency against the technical skill catalogue maintained by instructors and administrators.",
                toolbar(actorSelector, new Label("Search:"), search, evaluate, delete, refresh), table);
    }

    static Node evaluations(IntSupplier actingInstructor, Node actorSelector) {
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
                    evaluation.setInstructorId(actingInstructor.getAsInt());
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
                toolbar(actorSelector, new Label("Search:"), search, evaluate, refresh), table);
    }

    static Node support(IntSupplier actingInstructor, Node actorSelector) {
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
                    record.setInstructorId(actingInstructor.getAsInt());
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
        return Ui.page("Academic Support",
                "Document reviews, remedial sessions, consultations and additional exercises for students who need help.",
                toolbar(actorSelector, new Label("Search:"), search, add, delete, refresh), table);
    }

    private static Node toolbar(Node actorSelector, Node... base) {
        List<Node> nodes = new ArrayList<>(List.of(base));
        if (actorSelector != null) nodes.add(actorSelector);
        return Ui.toolbar(nodes.toArray(Node[]::new));
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
            org.slf4j.LoggerFactory.getLogger(AcademicManagementViews.class).error("Academic management action failed", e);
        }
    }
}
