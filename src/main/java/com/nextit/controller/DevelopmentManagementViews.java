package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.service.CareerService;
import com.nextit.service.DirectoryService;
import com.nextit.service.ProjectService;
import com.nextit.service.StudentService;
import com.nextit.util.Alerts;
import com.nextit.util.Ui;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared development-record screens for instructors and administrators.
 *
 * <p>These screens operate on records owned by students or employers, so the
 * operator explicitly selects the owning student or employer. The underlying
 * services still enforce their normal ownership and validation rules.</p>
 */
final class DevelopmentManagementViews {

    private static final StudentService students = new StudentService();
    private static final ProjectService projects = new ProjectService();
    private static final CareerService career = new CareerService();
    private static final DirectoryService directory = new DirectoryService();

    private static final List<String> PROJECT_TYPES =
            List.of("Laboratory", "Academic Project", "Programming", "Capstone", "Personal", "Research", "Other");
    private static final List<String> PROJECT_STATUSES =
            List.of("Draft", "In Progress", "Completed", "Archived");
    private static final List<String> APPLICATION_STATUSES =
            List.of("Pending", "Reviewed", "Shortlisted", "Rejected", "Accepted");

    private DevelopmentManagementViews() {}

    static Node achievements() {
        Map<Integer, String> studentNames = directory.studentNames();
        TableView<Achievement> table = Ui.table();
        Ui.addColumn(table, "Student", a -> studentNames.getOrDefault(a.getStudentId(), "Student #" + a.getStudentId()));
        Ui.addColumn(table, "Title", Achievement::getTitle);
        Ui.addColumn(table, "Organization", Achievement::getOrganization);
        Ui.addColumn(table, "Date", a -> Ui.formatDate(a.getAchievementDate()));
        Ui.addColumn(table, "Description", Achievement::getDescription);
        Ui.addColumn(table, "Verification", Achievement::getVerificationUrl);

        TextField search = Ui.field("Search by student, title, organization or description...");
        Runnable refresh = () -> {
            studentNames.clear();
            studentNames.putAll(directory.studentNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(students.allAchievements().stream().filter(a ->
                    studentNames.getOrDefault(a.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || value(a.getTitle()).toLowerCase(Locale.ROOT).contains(q)
                            || value(a.getOrganization()).toLowerCase(Locale.ROOT).contains(q)
                            || value(a.getDescription()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> refresh.run());
        refresh.run();

        Button add = new Button("Add Achievement");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editAchievement(null, refresh));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Achievement selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an achievement to edit."); return; }
            editAchievement(selected, refresh);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Achievement selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an achievement to delete."); return; }
            if (Ui.confirmed("Delete achievement \"" + selected.getTitle() + "\"?")) {
                guard(() -> {
                    students.deleteAchievement(selected.getAchievementId(), selected.getStudentId());
                    refresh.run();
                });
            }
        });
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh.run());
        return Ui.page("Student Achievements",
                "Record and maintain awards, recognitions and milestones for any student. Select the owning student when adding a record.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refreshButton), table);
    }

    private static void editAchievement(Achievement existing, Runnable refresh) {
        List<Student> studentList = directory.students();
        ComboBox<Student> owner = studentCombo(studentList);
        if (existing != null) {
            studentList.stream().filter(s -> s.getStudentId() == existing.getStudentId())
                    .findFirst().ifPresent(owner.getSelectionModel()::select);
            owner.setDisable(true);
        }
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
        Node form = Ui.form(new String[]{"Student", "Title", "Description", "Organization", "Date", "Verification URL"},
                new Node[]{owner, title, description, organization, date, url});
        Ui.dialog(existing == null ? "Add Achievement" : "Edit Achievement", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                if (existing == null && owner.getValue() == null) throw new AppException("Select the student who owns this achievement.");
                Achievement target = new Achievement();
                if (existing != null) target.setAchievementId(existing.getAchievementId());
                target.setStudentId(existing == null ? owner.getValue().getStudentId() : existing.getStudentId());
                target.setTitle(title.getText());
                target.setDescription(description.getText());
                target.setOrganization(organization.getText());
                target.setAchievementDate(date.getValue());
                target.setVerificationUrl(url.getText());
                students.saveAchievement(target);
                refresh.run();
                Alerts.info("Achievement saved.");
            });
        });
    }

    static Node certifications() {
        Map<Integer, String> studentNames = directory.studentNames();
        TableView<Certification> table = Ui.table();
        Ui.addColumn(table, "Student", c -> studentNames.getOrDefault(c.getStudentId(), "Student #" + c.getStudentId()));
        Ui.addColumn(table, "Certification", Certification::getCertificationName);
        Ui.addColumn(table, "Issuer", Certification::getIssuingOrganization);
        Ui.addColumn(table, "Issued", c -> Ui.formatDate(c.getIssueDate()));
        Ui.addColumn(table, "Expires", c -> Ui.formatDate(c.getExpirationDate()));
        Ui.addColumn(table, "Credential", Certification::getCredentialUrl);

        TextField search = Ui.field("Search by student, certification or issuer...");
        Runnable refresh = () -> {
            studentNames.clear();
            studentNames.putAll(directory.studentNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(students.allCertifications().stream().filter(c ->
                    studentNames.getOrDefault(c.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || value(c.getCertificationName()).toLowerCase(Locale.ROOT).contains(q)
                            || value(c.getIssuingOrganization()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> refresh.run());
        refresh.run();

        Button add = new Button("Add Certification");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editCertification(null, refresh));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Certification selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a certification to edit."); return; }
            editCertification(selected, refresh);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Certification selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a certification to delete."); return; }
            if (Ui.confirmed("Delete certification \"" + selected.getCertificationName() + "\"?")) {
                guard(() -> {
                    students.deleteCertification(selected.getCertificationId(), selected.getStudentId());
                    refresh.run();
                });
            }
        });
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh.run());
        return Ui.page("Student Certifications",
                "Record professional certifications, issue dates and expiry information for any student.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refreshButton), table);
    }

    private static void editCertification(Certification existing, Runnable refresh) {
        List<Student> studentList = directory.students();
        ComboBox<Student> owner = studentCombo(studentList);
        if (existing != null) {
            studentList.stream().filter(s -> s.getStudentId() == existing.getStudentId())
                    .findFirst().ifPresent(owner.getSelectionModel()::select);
            owner.setDisable(true);
        }
        Certification c = existing == null ? new Certification() : existing;
        TextField name = Ui.field("Certification name");
        name.setText(c.getCertificationName());
        TextField issuer = Ui.field("Issuing organization");
        issuer.setText(c.getIssuingOrganization());
        DatePicker issued = Ui.date(c.getIssueDate());
        DatePicker expires = Ui.date(c.getExpirationDate());
        TextField url = Ui.field("https://...");
        url.setText(c.getCredentialUrl());
        Node form = Ui.form(new String[]{"Student", "Certification", "Issuing organization", "Issue date", "Expiration date", "Credential URL"},
                new Node[]{owner, name, issuer, issued, expires, url});
        Ui.dialog(existing == null ? "Add Certification" : "Edit Certification", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                if (existing == null && owner.getValue() == null) throw new AppException("Select the student who owns this certification.");
                Certification target = new Certification();
                if (existing != null) target.setCertificationId(existing.getCertificationId());
                target.setStudentId(existing == null ? owner.getValue().getStudentId() : existing.getStudentId());
                target.setCertificationName(name.getText());
                target.setIssuingOrganization(issuer.getText());
                target.setIssueDate(issued.getValue());
                target.setExpirationDate(expires.getValue());
                target.setCredentialUrl(url.getText());
                students.saveCertification(target);
                refresh.run();
                Alerts.info("Certification saved.");
            });
        });
    }

    static Node projects() {
        Map<Integer, String> studentNames = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        TableView<Project> table = Ui.table();
        Ui.addColumn(table, "Owner", p -> p.getStudentId() == null ? ""
                : studentNames.getOrDefault(p.getStudentId(), "Student #" + p.getStudentId()));
        Ui.addColumn(table, "Title", Project::getProjectTitle);
        Ui.addColumn(table, "Type", Project::getProjectType);
        Ui.addColumn(table, "Subject", p -> p.getSubjectId() == null ? ""
                : subjects.getOrDefault(p.getSubjectId(), "Subject #" + p.getSubjectId()));
        Ui.addColumn(table, "Status", Project::getStatus);
        Ui.addColumn(table, "Technologies", Project::getTechnologiesUsed);
        Ui.addColumn(table, "Repository", Project::getRepositoryUrl);

        TextField search = Ui.field("Search by owner, title, type, technology or status...");
        ComboBox<String> statusFilter = Ui.combo(
                List.of("All statuses", "Draft", "In Progress", "Completed", "Archived"), s -> s);
        statusFilter.getSelectionModel().selectFirst();
        Runnable refresh = () -> {
            studentNames.clear();
            studentNames.putAll(directory.studentNames());
            subjects.clear();
            subjects.putAll(directory.subjectNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            String status = statusFilter.getValue();
            table.setItems(FXCollections.observableArrayList(projects.allProjects().stream().filter(p ->
                    ("All statuses".equals(status) || status.equalsIgnoreCase(p.getStatus()))
                            && (q.isBlank()
                            || value(p.getProjectTitle()).toLowerCase(Locale.ROOT).contains(q)
                            || value(p.getProjectType()).toLowerCase(Locale.ROOT).contains(q)
                            || value(p.getTechnologiesUsed()).toLowerCase(Locale.ROOT).contains(q)
                            || value(p.getStatus()).toLowerCase(Locale.ROOT).contains(q)
                            || (p.getStudentId() != null
                                && studentNames.getOrDefault(p.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)))).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> refresh.run());
        statusFilter.valueProperty().addListener((obs, old, value) -> refresh.run());

        Button add = new Button("Add Project");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editProject(null, refresh));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to edit."); return; }
            editProject(selected, refresh);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to delete."); return; }
            if (selected.getStudentId() == null) { Alerts.error("This project has no recorded owner."); return; }
            if (Ui.confirmed("Delete project \"" + selected.getProjectTitle() + "\"?")) {
                guard(() -> {
                    projects.deleteProject(selected.getProjectId(), selected.getStudentId());
                    refresh.run();
                });
            }
        });
        Button members = new Button("Group Members");
        members.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to manage its members."); return; }
            if (selected.getStudentId() == null) { Alerts.error("This project has no recorded owner."); return; }
            guard(() -> manageMembers(selected, refresh));
        });
        Button details = new Button("View Details");
        details.setOnAction(e -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a project to view."); return; }
            guard(() -> showProject(selected));
        });
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh.run());
        refresh.run();
        return Ui.page("Project Management",
                "Maintain student projects, group membership and contribution records. Project changes are recorded against the selected owner.",
                Ui.toolbar(new Label("Search:"), search, statusFilter, add, edit, delete, members, details, refreshButton), table);
    }

    private static void editProject(Project existing, Runnable refresh) {
        List<Student> studentList = directory.students();
        ComboBox<Student> owner = studentCombo(studentList);
        if (existing != null && existing.getStudentId() != null) {
            studentList.stream().filter(s -> s.getStudentId() == existing.getStudentId())
                    .findFirst().ifPresent(owner.getSelectionModel()::select);
            owner.setDisable(true);
        }
        Project p = existing == null ? new Project() : existing;
        List<Subject> subjectList = directory.subjects();
        TextField title = Ui.field("Project title");
        title.setText(p.getProjectTitle());
        ComboBox<String> type = Ui.combo(PROJECT_TYPES, s -> s);
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
        ComboBox<String> status = Ui.combo(PROJECT_STATUSES, s -> s);
        status.getSelectionModel().select(p.getStatus() == null ? "Draft" : p.getStatus());

        Node form = Ui.form(
                new String[]{"Owner", "Title", "Type", "Subject", "Description", "Technologies", "Repository URL", "Demo URL", "Project date", "Status"},
                new Node[]{owner, title, type, subject, description, technologies, repository, demo, projectDate, status});
        Ui.dialog(existing == null ? "Add Project" : "Edit Project", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                if (existing == null && owner.getValue() == null) throw new AppException("Select the student who owns this project.");
                Project target = new Project();
                target.setProjectId(existing == null ? 0 : existing.getProjectId());
                target.setStudentId(existing == null ? owner.getValue().getStudentId() : existing.getStudentId());
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
                else projects.updateProject(target, existing.getStudentId());
                refresh.run();
                Alerts.info("Project saved.");
            });
        });
    }

    private static void manageMembers(Project project, Runnable refresh) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Group Members - " + project.getProjectTitle());
        TableView<ProjectMember> table = Ui.table();
        Map<Integer, String> studentNames = directory.studentNames();
        Ui.addColumn(table, "Student", m -> studentNames.getOrDefault(m.getStudentId(), "Student #" + m.getStudentId()));
        Ui.addColumn(table, "Role", ProjectMember::getRole);
        Ui.addColumn(table, "Contribution", ProjectMember::getContribution);
        Runnable refreshMembers = () -> table.setItems(FXCollections.observableArrayList(projects.membersForProject(project.getProjectId())));
        refreshMembers.run();

        Button add = new Button("Add Member");
        add.setOnAction(e -> {
            List<ProjectMember> currentMembers = projects.membersForProject(project.getProjectId());
            List<Student> studentList = directory.students().stream()
                    .filter(s -> project.getStudentId() == null || s.getStudentId() != project.getStudentId())
                    .filter(s -> currentMembers.stream().noneMatch(m -> m.getStudentId() == s.getStudentId()))
                    .toList();
            if (studentList.isEmpty()) {
                Alerts.info("Every available student is already on this project.");
                return;
            }
            ComboBox<Student> student = studentCombo(studentList);
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
                    projects.addMember(member, project.getStudentId());
                    refreshMembers.run();
                    refresh.run();
                });
            });
        });
        Button remove = new Button("Remove Selected");
        remove.setOnAction(e -> {
            ProjectMember selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a member to remove."); return; }
            if (Ui.confirmed("Remove this member from the project?")) {
                guard(() -> {
                    projects.removeMember(selected.getProjectMemberId(), project.getStudentId());
                    refreshMembers.run();
                    refresh.run();
                });
            }
        });
        dialog.getDialogPane().setContent(new VBox(10, Ui.toolbar(add, remove), table));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(700, 480);
        dialog.showAndWait();
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
        StringBuilder evaluationText = new StringBuilder();
        projects.allEvaluations().stream().filter(e -> e.getProjectId() == p.getProjectId())
                .forEach(e -> evaluationText.append("• ")
                        .append(directory.studentName(e.getStudentId()))
                        .append(" — overall ").append(e.getOverallScore() == null ? "n/a" : e.getOverallScore())
                        .append(", by ").append(directory.instructorName(e.getInstructorId()))
                        .append(e.getFeedback() == null ? "" : ": " + e.getFeedback())
                        .append('\n'));
        Label evaluations = new Label(evaluationText.isEmpty() ? "No evaluations recorded." : evaluationText.toString());
        evaluations.setWrapText(true);
        Node form = Ui.form(
                new String[]{"Owner", "Title", "Type", "Subject", "Status", "Technologies", "Repository", "Demo", "Date", "Description", "Group members", "Evaluations"},
                new Node[]{new Label(p.getStudentId() == null ? "" : directory.studentName(p.getStudentId())),
                        new Label(value(p.getProjectTitle())), new Label(value(p.getProjectType())),
                        new Label(p.getSubjectId() == null ? "" : subjects.getOrDefault(p.getSubjectId(), "Subject #" + p.getSubjectId())),
                        new Label(value(p.getStatus())), new Label(value(p.getTechnologiesUsed())),
                        new Label(value(p.getRepositoryUrl())), new Label(value(p.getDemoUrl())),
                        new Label(Ui.formatDate(p.getProjectDate())), description, members, evaluations});
        Ui.viewDialog("Project - " + p.getProjectTitle(), form);
    }

    static Node opportunities(boolean manage) {
        Map<Integer, String> employers = directory.employerNames();
        TableView<JobOpportunity> table = Ui.table();
        Ui.addColumn(table, "Title", JobOpportunity::getTitle);
        Ui.addColumn(table, "Employer", o -> employers.getOrDefault(o.getEmployerId(), "Employer #" + o.getEmployerId()));
        Ui.addColumn(table, "Type", JobOpportunity::getOpportunityType);
        Ui.addColumn(table, "Location", o -> value(o.getLocation()));
        Ui.addColumn(table, "Required Skills", o -> value(o.getRequiredSkills()));
        Ui.addColumn(table, "Deadline", o -> Ui.formatDate(o.getDeadline()));
        Ui.addColumn(table, "Posted", o -> Ui.formatDate(o.getCreatedAt() == null ? null : o.getCreatedAt().toLocalDate()));
        Ui.addColumn(table, "Status", JobOpportunity::getStatus);

        TextField search = Ui.field("Search by title, employer, skills or location...");
        ComboBox<String> statusFilter = Ui.combo(List.of("All statuses", "Open", "Closed", "Archived"), s -> s);
        statusFilter.getSelectionModel().selectFirst();
        Runnable refresh = () -> {
            employers.clear();
            employers.putAll(directory.employerNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            String status = statusFilter.getValue();
            table.setItems(FXCollections.observableArrayList(career.allOpportunities().stream()
                    .filter(o -> "All statuses".equals(status) || status.equalsIgnoreCase(o.getStatus()))
                    .filter(o -> q.isBlank()
                            || value(o.getTitle()).toLowerCase(Locale.ROOT).contains(q)
                            || value(o.getLocation()).toLowerCase(Locale.ROOT).contains(q)
                            || value(o.getRequiredSkills()).toLowerCase(Locale.ROOT).contains(q)
                            || employers.getOrDefault(o.getEmployerId(), "").toLowerCase(Locale.ROOT).contains(q))
                    .toList()));
        };
        search.textProperty().addListener((obs, old, value) -> refresh.run());
        statusFilter.valueProperty().addListener((obs, old, value) -> refresh.run());

        Button details = new Button("View Details");
        details.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to view."); return; }
            showOpportunity(selected, employers);
        });

        if (!manage) {
            refresh.run();
            Button refreshButton = new Button("Refresh");
            refreshButton.setOnAction(e -> refresh.run());
            return Ui.page("Career Opportunities",
                    "Browse every internship and employment opportunity posted by industry partners.",
                    Ui.toolbar(new Label("Search:"), search, statusFilter, details, refreshButton), table);
        }

        Button add = new Button("Add Opportunity");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editOpportunity(null, refresh));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to edit."); return; }
            editOpportunity(selected, refresh);
        });
        Button close = new Button("Close");
        close.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to close."); return; }
            if (Ui.confirmed("Close \"" + selected.getTitle() + "\"? Students will no longer be able to apply.")) {
                guard(() -> {
                    JobOpportunity stored = career.opportunityById(selected.getOpportunityId());
                    stored.setStatus("Closed");
                    career.updateOpportunity(stored, stored.getEmployerId());
                    refresh.run();
                });
            }
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to delete."); return; }
            if (Ui.confirmed("Delete \"" + selected.getTitle() + "\" and all of its applications?")) {
                guard(() -> {
                    career.deleteOpportunity(selected.getOpportunityId(), selected.getEmployerId());
                    refresh.run();
                });
            }
        });
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh.run());
        refresh.run();
        return Ui.page("Career Opportunities",
                "Create and maintain internship and employment opportunities on behalf of any industry partner.",
                Ui.toolbar(new Label("Search:"), search, statusFilter, add, edit, details, close, delete, refreshButton), table);
    }

    private static void editOpportunity(JobOpportunity existing, Runnable refresh) {
        List<Employer> employerList = directory.employers();
        Map<Integer, String> employerNames = directory.employerNames();
        ComboBox<Employer> employer = Ui.combo(employerList,
                e -> employerNames.getOrDefault(e.getEmployerId(), e.getCompanyName()));
        if (existing != null) {
            employerList.stream().filter(e -> e.getEmployerId() == existing.getEmployerId())
                    .findFirst().ifPresent(employer.getSelectionModel()::select);
            employer.setDisable(true);
        }
        JobOpportunity o = existing == null ? new JobOpportunity() : existing;
        TextField title = Ui.field("Opportunity title");
        title.setText(o.getTitle());
        ComboBox<String> type = Ui.combo(List.of("Internship", "Employment"), s -> s);
        type.getSelectionModel().select(o.getOpportunityType() == null ? "Internship" : o.getOpportunityType());
        TextArea description = Ui.area("Description");
        description.setText(o.getDescription());
        TextField requiredSkills = Ui.field("Java, SQL, Git");
        requiredSkills.setText(o.getRequiredSkills());
        TextField location = Ui.field("Location");
        location.setText(o.getLocation());
        DatePicker deadline = Ui.date(o.getDeadline() == null ? LocalDate.now().plusMonths(1) : o.getDeadline());
        ComboBox<String> status = Ui.combo(List.of("Open", "Closed", "Archived"), s -> s);
        status.getSelectionModel().select(o.getStatus() == null ? "Open" : o.getStatus());
        Node form = Ui.form(new String[]{"Employer", "Title", "Type", "Description", "Required skills", "Location", "Deadline", "Status"},
                new Node[]{employer, title, type, description, requiredSkills, location, deadline, status});
        Ui.dialog(existing == null ? "Add Opportunity" : "Edit Opportunity", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                if (existing == null && employer.getValue() == null) throw new AppException("Select the employer posting this opportunity.");
                JobOpportunity target = new JobOpportunity();
                if (existing != null) {
                    target.setOpportunityId(existing.getOpportunityId());
                    target.setCreatedAt(existing.getCreatedAt());
                }
                int employerId = existing == null ? employer.getValue().getEmployerId() : existing.getEmployerId();
                target.setEmployerId(employerId);
                target.setTitle(title.getText());
                target.setOpportunityType(type.getValue());
                target.setDescription(description.getText());
                target.setRequiredSkills(requiredSkills.getText());
                target.setLocation(location.getText());
                target.setDeadline(deadline.getValue());
                target.setStatus(status.getValue());
                if (existing == null) career.addOpportunity(target, employerId);
                else career.updateOpportunity(target, employerId);
                refresh.run();
                Alerts.info("Opportunity saved.");
            });
        });
    }

    private static void showOpportunity(JobOpportunity o, Map<Integer, String> employers) {
        Label description = new Label(value(o.getDescription()));
        description.setWrapText(true);
        Label skills = new Label(value(o.getRequiredSkills()));
        skills.setWrapText(true);
        Node form = Ui.form(new String[]{"Employer", "Title", "Type", "Location", "Deadline", "Posted", "Status", "Required skills", "Description"},
                new Node[]{new Label(employers.getOrDefault(o.getEmployerId(), "Employer #" + o.getEmployerId())),
                        new Label(value(o.getTitle())), new Label(value(o.getOpportunityType())),
                        new Label(value(o.getLocation())), new Label(Ui.formatDate(o.getDeadline())),
                        new Label(Ui.formatDate(o.getCreatedAt() == null ? null : o.getCreatedAt().toLocalDate())),
                        new Label(value(o.getStatus())), skills, description});
        Ui.viewDialog("Opportunity - " + o.getTitle(), form);
    }

    static Node applications(boolean manage) {
        Map<Integer, String> studentNames = directory.studentNames();
        Map<Integer, String> opportunityTitles = career.allOpportunities().stream()
                .collect(Collectors.toMap(JobOpportunity::getOpportunityId, JobOpportunity::getTitle, (a, b) -> a));
        Map<Integer, String> employers = directory.employerNames();
        Map<Integer, JobOpportunity> opportunityIndex = career.allOpportunities().stream()
                .collect(Collectors.toMap(JobOpportunity::getOpportunityId, o -> o, (a, b) -> a));

        TableView<Application> table = Ui.table();
        Ui.addColumn(table, "Student", a -> studentNames.getOrDefault(a.getStudentId(), "Student #" + a.getStudentId()));
        Ui.addColumn(table, "Opportunity", a -> opportunityTitles.getOrDefault(a.getOpportunityId(), "Opportunity #" + a.getOpportunityId()));
        Ui.addColumn(table, "Employer", a -> {
            JobOpportunity opportunity = opportunityIndex.get(a.getOpportunityId());
            return opportunity == null ? "" : employers.getOrDefault(opportunity.getEmployerId(), "Employer #" + opportunity.getEmployerId());
        });
        Ui.addColumn(table, "Applied", a -> a.getApplicationDate() == null ? "" : a.getApplicationDate().toLocalDate().toString());
        Ui.addColumn(table, "Status", Application::getStatus);
        Ui.addColumn(table, "Remarks", Application::getRemarks);

        ComboBox<String> statusFilter = Ui.combo(List.of("All statuses", "Pending", "Reviewed", "Shortlisted", "Rejected", "Accepted"), s -> s);
        statusFilter.getSelectionModel().selectFirst();
        TextField search = Ui.field("Search by student, opportunity, employer or remarks...");
        Runnable refresh = () -> {
            studentNames.clear();
            studentNames.putAll(directory.studentNames());
            employers.clear();
            employers.putAll(directory.employerNames());
            opportunityTitles.clear();
            career.allOpportunities().forEach(o -> opportunityTitles.put(o.getOpportunityId(), o.getTitle()));
            opportunityIndex.clear();
            career.allOpportunities().forEach(o -> opportunityIndex.put(o.getOpportunityId(), o));
            String status = statusFilter.getValue();
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(career.allApplications().stream()
                    .filter(a -> "All statuses".equals(status) || status.equalsIgnoreCase(a.getStatus()))
                    .filter(a -> q.isBlank()
                            || studentNames.getOrDefault(a.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || opportunityTitles.getOrDefault(a.getOpportunityId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || value(a.getRemarks()).toLowerCase(Locale.ROOT).contains(q))
                    .toList()));
        };
        statusFilter.valueProperty().addListener((obs, old, value) -> refresh.run());
        search.textProperty().addListener((obs, old, value) -> refresh.run());
        refresh.run();

        if (!manage) {
            Button refreshButton = new Button("Refresh");
            refreshButton.setOnAction(e -> refresh.run());
            return Ui.page("Applications", "Review the applications students have submitted to partner opportunities.",
                    Ui.toolbar(new Label("Search:"), search, new Label("Filter:"), statusFilter, refreshButton), table);
        }

        Button record = new Button("Record Application");
        record.getStyleClass().add("primary-button");
        record.setOnAction(e -> {
            ComboBox<Student> student = studentCombo(directory.students());
            ComboBox<JobOpportunity> opportunity = Ui.combo(career.openOpportunities(),
                    o -> o.getTitle() + " · " + employers.getOrDefault(o.getEmployerId(), "Employer #" + o.getEmployerId()));
            TextArea remarks = Ui.area("Remarks");
            Node form = Ui.form(new String[]{"Student", "Open opportunity", "Remarks"},
                    new Node[]{student, opportunity, remarks});
            Ui.dialog("Record Application", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    if (student.getValue() == null) throw new AppException("Select a student.");
                    if (opportunity.getValue() == null) throw new AppException("Select an open opportunity.");
                    Application application = new Application();
                    application.setStudentId(student.getValue().getStudentId());
                    application.setOpportunityId(opportunity.getValue().getOpportunityId());
                    application.setRemarks(remarks.getText());
                    career.apply(application);
                    refresh.run();
                    Alerts.info("Application recorded.");
                });
            });
        });
        Button review = new Button("Update Status");
        review.setOnAction(e -> {
            Application selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an application to review."); return; }
            JobOpportunity opportunity = opportunityIndex.get(selected.getOpportunityId());
            if (opportunity == null) { Alerts.error("The related opportunity could not be found."); return; }
            ComboBox<String> status = Ui.combo(APPLICATION_STATUSES, s -> s);
            status.getSelectionModel().select(selected.getStatus() == null ? "Pending" : selected.getStatus());
            TextArea remarks = Ui.area("Remarks");
            remarks.setText(selected.getRemarks());
            Node form = Ui.form(new String[]{"Applicant", "Opportunity", "Status", "Remarks"},
                    new Node[]{new Label(studentNames.getOrDefault(selected.getStudentId(), "Student #" + selected.getStudentId())),
                            new Label(opportunity.getTitle()), status, remarks});
            Ui.dialog("Review Application", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    career.updateApplicationStatus(opportunity.getEmployerId(), selected, status.getValue(), remarks.getText());
                    refresh.run();
                    Alerts.info("Application updated.");
                });
            });
        });
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh.run());
        return Ui.page("Applications",
                "Record applications on behalf of students and update their review status for the posting employer.",
                Ui.toolbar(new Label("Search:"), search, new Label("Filter:"), statusFilter, record, review, refreshButton), table);
    }

    private static ComboBox<Student> studentCombo(List<Student> studentList) {
        Map<Integer, String> names = directory.studentNames();
        return Ui.combo(studentList, s -> names.getOrDefault(s.getStudentId(), "Student #" + s.getStudentId())
                + " (" + value(s.getStudentNumber()) + ")");
    }

    private static String value(String text) { return text == null ? "" : text; }

    private static void guard(Runnable action) {
        try {
            action.run();
        } catch (AppException e) {
            Alerts.error(e.getMessage());
        } catch (Exception e) {
            Alerts.error("Could not complete the operation. Please check the database connection.");
            org.slf4j.LoggerFactory.getLogger(DevelopmentManagementViews.class).error("Development management action failed", e);
        }
    }
}
