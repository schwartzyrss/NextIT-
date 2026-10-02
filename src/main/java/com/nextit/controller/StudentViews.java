package com.nextit.controller;

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

import java.util.List;
import java.util.Optional;

public class StudentViews {

    private static final AcademicService academic = new AcademicService();
    private static final ProjectService projects = new ProjectService();
    private static final CareerService career = new CareerService();
    private static final PortfolioService portfolio = new PortfolioService();
    private static final StudentSkillService skillService = new StudentSkillService();

    private static int studentId() {
        return academic.studentIdForUser(Session.currentUser().getUserId());
    }

    public static Node dashboard() {
        int sid = studentId();
        VBox box = new VBox(16);
        box.setStyle("-fx-padding: 20;");
        Label title = new Label("Student Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        HBox cards = new HBox(12);
        long subjects = academic.enrollmentsForStudent(sid).size();
        long skills = skillService.forStudent(sid).size();
        long projectsCount = projects.projectsForStudent(sid).size();
        cards.getChildren().addAll(
                card("Subjects", String.valueOf(subjects)),
                card("Skills", String.valueOf(skills)),
                card("Projects", String.valueOf(projectsCount)),
                card("Grades", String.valueOf(academic.gradesForStudent(sid).size())),
                card("Attendance", String.valueOf(academic.attendanceForStudent(sid).size()))
        );
        box.getChildren().addAll(title, cards);
        return box;
    }

    public static Node academic() {
        int sid = studentId();
        TabPane tabs = new TabPane();
        Tab grades = new Tab("Grades",
                table(List.of("Activity", "Type", "Score", "Max", "Grade", "Remarks"),
                        (Grade g) -> List.of(g.getActivityName(), g.getActivityType(), String.valueOf(g.getScore()),
                                String.valueOf(g.getMaxScore()), String.valueOf(g.getGrade()), g.getRemarks()),
                        academic.gradesForStudent(sid)));
        Tab attendance = new Tab("Attendance",
                table(List.of("Date", "Status", "Remarks"),
                        (Attendance a) -> List.of(String.valueOf(a.getAttendanceDate()), a.getStatus(), a.getRemarks()),
                        academic.attendanceForStudent(sid)));
        grades.setClosable(false);
        attendance.setClosable(false);
        tabs.getTabs().addAll(grades, attendance);
        return tabs;
    }

    public static Node skills() {
        int sid = studentId();
        return table(List.of("Skill", "Level", "Score", "Remarks"),
                (StudentSkill s) -> List.of(String.valueOf(s.getSkillId()), s.getProficiencyLevel(),
                        String.valueOf(s.getAssessmentScore()), s.getRemarks()),
                skillService.forStudent(sid));
    }

    public static Node projects() {
        int sid = studentId();
        TableView<Project> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(projects.projectsForStudent(sid)));
        addCol(tv, "Title", Project::getProjectTitle);
        addCol(tv, "Type", Project::getProjectType);
        addCol(tv, "Status", Project::getStatus);
        addCol(tv, "Technologies", Project::getTechnologiesUsed);
        VBox box = new VBox(10, new HBox(8), tv);
        HBox actions = (HBox) box.getChildren().get(0);
        Button add = new Button("Add Project");
        add.setOnAction(e -> {
            TextField title = new TextField(); title.setPromptText("Title");
            TextField type = new TextField(); type.setPromptText("Type");
            TextField tech = new TextField(); tech.setPromptText("Technologies");
            Dialog<Project> d = new Dialog<>();
            d.setTitle("Add Project");
            d.getDialogPane().setContent(new VBox(8, title, type, tech));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    Project p = new Project();
                    p.setStudentId(sid);
                    p.setProjectTitle(title.getText());
                    p.setProjectType(type.getText());
                    p.setTechnologiesUsed(tech.getText());
                    p.setStatus("Draft");
                    return p;
                }
                return null;
            });
            d.showAndWait().ifPresent(p -> { projects.addProject(p); tv.setItems(FXCollections.observableArrayList(projects.projectsForStudent(sid))); });
        });
        Button del = new Button("Delete");
        del.setOnAction(e -> {
            Project p = tv.getSelectionModel().getSelectedItem();
            if (p != null) { projects.deleteProject(p.getProjectId()); tv.setItems(FXCollections.observableArrayList(projects.projectsForStudent(sid))); }
        });
        Button members = new Button("Members");
        members.setOnAction(e -> {
            Project p = tv.getSelectionModel().getSelectedItem();
            if (p == null) return;
            com.nextit.repository.JdbcProjectMemberRepository repo = new com.nextit.repository.JdbcProjectMemberRepository();
            TextField role = new TextField(); role.setPromptText("Role");
            TextField contribution = new TextField(); contribution.setPromptText("Contribution");
            Dialog<ProjectMember> d = new Dialog<>();
            d.setTitle("Add Member to " + p.getProjectTitle());
            d.getDialogPane().setContent(new VBox(8, role, contribution));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    ProjectMember m = new ProjectMember();
                    m.setProjectId(p.getProjectId());
                    m.setStudentId(sid);
                    m.setRole(role.getText());
                    m.setContribution(contribution.getText());
                    return m;
                }
                return null;
            });
            d.showAndWait().ifPresent(m -> { repo.save(m); com.nextit.util.Alerts.info("Member added."); });
        });
        actions.getChildren().addAll(add, del, members);
        return box;
    }

    public static Node portfolio() {
        int sid = studentId();
        Portfolio p = portfolio.portfolioForStudent(sid).orElse(new Portfolio());
        TextField headline = new TextField(p.getHeadline()); headline.setPromptText("Headline");
        TextArea bio = new TextArea(p.getBio()); bio.setPromptText("Biography");
        TextArea goal = new TextArea(p.getCareerGoal()); goal.setPromptText("Career goal");
        ComboBox<String> visibility = new ComboBox<>(FXCollections.observableArrayList("Private", "Pending", "Approved"));
        visibility.getSelectionModel().select(p.getVisibility() == null ? "Private" : p.getVisibility());
        Button save = new Button("Save");
        save.setOnAction(e -> {
            p.setStudentId(sid);
            p.setHeadline(headline.getText());
            p.setBio(bio.getText());
            p.setCareerGoal(goal.getText());
            p.setVisibility(visibility.getValue());
            portfolio.save(p);
            com.nextit.util.Alerts.info("Portfolio saved.");
        });
        return new VBox(10, new Label("Headline"), headline, new Label("Bio"), bio, new Label("Career goal"), goal, new Label("Visibility"), visibility, save);
    }

    public static Node opportunities() {
        return table(List.of("Title", "Type", "Location", "Deadline", "Status"),
                (JobOpportunity o) -> List.of(o.getTitle(), o.getOpportunityType(), o.getLocation(), String.valueOf(o.getDeadline()), o.getStatus()),
                career.openOpportunities());
    }

    public static Node applications() {
        int sid = studentId();
        return table(List.of("Opportunity", "Date", "Status", "Remarks"),
                (Application a) -> List.of(String.valueOf(a.getOpportunityId()), String.valueOf(a.getApplicationDate()), a.getStatus(), a.getRemarks()),
                career.applicationsByStudent(sid));
    }

    private static VBox card(String label, String value) {
        VBox v = new VBox(4);
        v.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 8;");
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #6b7280;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        v.getChildren().addAll(val, l);
        return v;
    }

    private static <T> Node table(List<String> cols, java.util.function.Function<T, List<String>> mapper, List<T> items) {
        TableView<List<String>> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList());
        for (int i = 0; i < cols.size(); i++) {
            final int idx = i;
            TableColumn<List<String>, String> col = new TableColumn<>(cols.get(i));
            col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get(idx)));
            tv.getColumns().add(col);
        }
        tv.setItems(FXCollections.observableArrayList(items.stream().map(mapper).toList()));
        VBox.setVgrow(tv, Priority.ALWAYS);
        return new VBox(tv);
    }

    private static <T> void addCol(TableView<T> tv, String name, java.util.function.Function<T, ?> getter) {
        TableColumn<T, String> col = new TableColumn<>(name);
        col.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(getter.apply(cd.getValue()))));
        tv.getColumns().add(col);
    }
}
