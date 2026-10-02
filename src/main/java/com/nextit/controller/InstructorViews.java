package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.security.Session;
import com.nextit.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class InstructorViews {

    private static final AcademicService academic = new AcademicService();
    private static final ProjectService projects = new ProjectService();
    private static final SupportService support = new SupportService();

    private static int instructorId() {
        com.nextit.repository.JdbcInstructorRepository repo = new com.nextit.repository.JdbcInstructorRepository();
        return repo.findByUserId(Session.currentUser().getUserId())
                .map(Instructor::getInstructorId)
                .orElseThrow(() -> new AppException("Instructor profile not found."));
    }

    public static Node dashboard() {
        VBox box = new VBox(16);
        box.setStyle("-fx-padding: 20;");
        Label title = new Label("Instructor Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        HBox cards = new HBox(12);
        cards.getChildren().addAll(
                card("Grades", String.valueOf(academic.allGrades().size())),
                card("Attendance", String.valueOf(academic.allAttendance().size())),
                card("Evaluations", String.valueOf(projects.allEvaluations().size())),
                card("Support", String.valueOf(support.all().size()))
        );
        box.getChildren().addAll(title, cards);
        return box;
    }

    public static Node grades() {
        TableView<Grade> tv = new TableView<>();
        refreshGrades(tv);
        addCol(tv, "Activity", Grade::getActivityName);
        addCol(tv, "Type", Grade::getActivityType);
        addCol(tv, "Score", Grade::getScore);
        addCol(tv, "Max", Grade::getMaxScore);
        addCol(tv, "Grade", Grade::getGrade);
        HBox actions = new HBox(8);
        Button add = new Button("Add Grade");
        add.setOnAction(e -> {
            TextField activity = new TextField(); activity.setPromptText("Activity name");
            TextField type = new TextField(); type.setPromptText("Activity type");
            TextField score = new TextField(); score.setPromptText("Score");
            TextField max = new TextField(); max.setPromptText("Max score");
            Dialog<Grade> d = new Dialog<>();
            d.setTitle("Add Grade");
            d.getDialogPane().setContent(new VBox(8, activity, type, score, max));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    Grade g = new Grade();
                    g.setInstructorId(instructorId());
                    g.setEnrollmentId(1);
                    g.setActivityName(activity.getText());
                    g.setActivityType(type.getText());
                    try {
                        g.setScore(Double.parseDouble(score.getText()));
                        g.setMaxScore(Double.parseDouble(max.getText()));
                    } catch (NumberFormatException ex) { throw new AppException("Scores must be numbers."); }
                    return g;
                }
                return null;
            });
            d.showAndWait().ifPresent(g -> { academic.recordGrade(g); refreshGrades(tv); });
        });
        actions.getChildren().add(add);
        VBox box = new VBox(10, actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }

    private static void refreshGrades(TableView<Grade> tv) {
        tv.setItems(FXCollections.observableArrayList(academic.allGrades()));
    }

    public static Node attendance() {
        TableView<Attendance> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(academic.allAttendance()));
        addCol(tv, "Date", Attendance::getAttendanceDate);
        addCol(tv, "Status", Attendance::getStatus);
        addCol(tv, "Remarks", Attendance::getRemarks);
        HBox actions = new HBox(8);
        Button add = new Button("Record Attendance");
        add.setOnAction(e -> {
            ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Present", "Absent", "Late", "Excused"));
            status.getSelectionModel().selectFirst();
            TextField remarks = new TextField(); remarks.setPromptText("Remarks");
            Dialog<Attendance> d = new Dialog<>();
            d.setTitle("Record Attendance");
            d.getDialogPane().setContent(new VBox(8, status, remarks));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    Attendance a = new Attendance();
                    a.setEnrollmentId(1);
                    a.setRecordedBy(instructorId());
                    a.setAttendanceDate(LocalDate.now());
                    a.setStatus(status.getValue());
                    a.setRemarks(remarks.getText());
                    return a;
                }
                return null;
            });
            d.showAndWait().ifPresent(a -> { academic.recordAttendance(a); tv.setItems(FXCollections.observableArrayList(academic.allAttendance())); });
        });
        actions.getChildren().add(add);
        VBox box = new VBox(10, actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }

    public static Node evaluations() {
        TableView<ProjectEvaluation> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(projects.allEvaluations()));
        addCol(tv, "Project", ProjectEvaluation::getProjectId);
        addCol(tv, "Student", ProjectEvaluation::getStudentId);
        addCol(tv, "Contribution", ProjectEvaluation::getContributionScore);
        addCol(tv, "Technical", ProjectEvaluation::getTechnicalScore);
        addCol(tv, "Teamwork", ProjectEvaluation::getTeamworkScore);
        addCol(tv, "Overall", ProjectEvaluation::getOverallScore);
        HBox actions = new HBox(8);
        Button add = new Button("Evaluate Project");
        add.setOnAction(e -> {
            TextField projectId = new TextField(); projectId.setPromptText("Project ID");
            TextField studentId = new TextField(); studentId.setPromptText("Student ID");
            TextField contribution = new TextField(); contribution.setPromptText("Contribution score");
            TextField technical = new TextField(); technical.setPromptText("Technical score");
            TextField teamwork = new TextField(); teamwork.setPromptText("Teamwork score");
            TextField feedback = new TextField(); feedback.setPromptText("Feedback");
            Dialog<ProjectEvaluation> d = new Dialog<>();
            d.setTitle("Evaluate Project");
            d.getDialogPane().setContent(new VBox(8, projectId, studentId, contribution, technical, teamwork, feedback));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    ProjectEvaluation ev = new ProjectEvaluation();
                    ev.setInstructorId(instructorId());
                    try {
                        ev.setProjectId(Integer.parseInt(projectId.getText()));
                        ev.setStudentId(Integer.parseInt(studentId.getText()));
                        ev.setContributionScore(parseDouble(contribution.getText()));
                        ev.setTechnicalScore(parseDouble(technical.getText()));
                        ev.setTeamworkScore(parseDouble(teamwork.getText()));
                        ev.setOverallScore(average(ev.getContributionScore(), ev.getTechnicalScore(), ev.getTeamworkScore()));
                    } catch (NumberFormatException ex) { throw new AppException("IDs and scores must be numbers."); }
                    ev.setFeedback(feedback.getText());
                    return ev;
                }
                return null;
            });
            d.showAndWait().ifPresent(ev -> { projects.evaluate(ev); tv.setItems(FXCollections.observableArrayList(projects.allEvaluations())); });
        });
        actions.getChildren().add(add);
        VBox box = new VBox(10, actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        return Double.parseDouble(s);
    }

    private static Double average(Double a, Double b, Double c) {
        double sum = 0; int n = 0;
        if (a != null) { sum += a; n++; }
        if (b != null) { sum += b; n++; }
        if (c != null) { sum += c; n++; }
        return n == 0 ? null : Math.round((sum / n) * 100.0) / 100.0;
    }

    public static Node support() {
        TableView<AcademicSupport> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(support.all()));
        addCol(tv, "Type", AcademicSupport::getSupportType);
        addCol(tv, "Date", AcademicSupport::getSupportDate);
        addCol(tv, "Reason", AcademicSupport::getReason);
        addCol(tv, "Outcome", AcademicSupport::getOutcome);
        HBox actions = new HBox(8);
        Button add = new Button("Record Support");
        add.setOnAction(e -> {
            TextField type = new TextField(); type.setPromptText("Type (Review/Remedial/Consultation/Additional Exercise)");
            TextField reason = new TextField(); reason.setPromptText("Reason");
            TextField outcome = new TextField(); outcome.setPromptText("Outcome");
            Dialog<AcademicSupport> d = new Dialog<>();
            d.setTitle("Record Academic Support");
            d.getDialogPane().setContent(new VBox(8, type, reason, outcome));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    AcademicSupport a = new AcademicSupport();
                    a.setInstructorId(instructorId());
                    a.setStudentId(1);
                    a.setSupportType(type.getText());
                    a.setSupportDate(LocalDate.now());
                    a.setReason(reason.getText());
                    a.setOutcome(outcome.getText());
                    return a;
                }
                return null;
            });
            d.showAndWait().ifPresent(a -> { support.record(a); tv.setItems(FXCollections.observableArrayList(support.all())); });
        });
        actions.getChildren().add(add);
        VBox box = new VBox(10, actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }

    private static VBox card(String label, String value) {
        VBox v = new VBox(4);
        v.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8;");
        Label l = new Label(label); l.setStyle("-fx-text-fill: #6b7280;");
        Label val = new Label(value); val.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        v.getChildren().addAll(val, l);
        return v;
    }

    private static <T> void addCol(TableView<T> tv, String name, java.util.function.Function<T, ?> getter) {
        TableColumn<T, String> col = new TableColumn<>(name);
        col.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(getter.apply(cd.getValue()))));
        tv.getColumns().add(col);
    }
}
