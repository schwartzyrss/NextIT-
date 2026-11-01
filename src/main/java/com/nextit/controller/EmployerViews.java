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
import java.util.stream.Collectors;

public final class EmployerViews {

    private static final CareerService career = new CareerService();
    private static final PortfolioService portfolios = new PortfolioService();
    private static final StudentService students = new StudentService();
    private static final StudentSkillService skills = new StudentSkillService();
    private static final ProjectService projects = new ProjectService();
    private static final DirectoryService directory = new DirectoryService();
    private static final ProfileService profiles = new ProfileService();

    private EmployerViews() {}

    private static int employerId() { return career.employerIdForUser(Session.currentUser().getUserId()); }

    public static Node profile() {
        User account = profiles.account(Session.currentUser().getUserId());
        Employer employer = profiles.employerForUser(account.getUserId())
                .orElseThrow(() -> new AppException("Employer profile not found."));

        TextField company = Ui.field("Company name");
        company.setText(employer.getCompanyName());
        TextField industry = Ui.field("Industry");
        industry.setText(employer.getIndustry());
        TextField contactPerson = Ui.field("Contact person");
        contactPerson.setText(employer.getContactPerson());
        TextField contactNumber = Ui.field("Contact number");
        contactNumber.setText(employer.getContactNumber());
        TextField address = Ui.field("Address");
        address.setText(employer.getAddress());

        Button save = new Button("Save Company Profile");
        save.getStyleClass().add("primary-button");
        save.setOnAction(e -> guard(() -> {
            employer.setCompanyName(company.getText());
            employer.setIndustry(industry.getText());
            employer.setContactPerson(contactPerson.getText());
            employer.setContactNumber(contactNumber.getText());
            employer.setAddress(address.getText());
            profiles.updateEmployer(employer);
            Alerts.info("Company profile updated.");
        }));

        Node accountForm = Ui.form(new String[]{"Username", "Email", "Full name", "Role"},
                new Node[]{new Label(value(account.getUsername())), new Label(value(account.getEmail())),
                        new Label(value(account.getFullName())), new Label(String.valueOf(account.getRole()))});
        Node companyForm = Ui.form(new String[]{"Company", "Industry", "Contact person", "Contact number", "Address"},
                new Node[]{company, industry, contactPerson, contactNumber, address});
        return Ui.page("Company Profile", "Keep your company information current so students know who is reviewing their applications.",
                Ui.section("Account"), accountForm, Ui.section("Company"), companyForm, save);
    }

    public static Node dashboard() {
        int eid = employerId();
        List<JobOpportunity> owned = career.opportunitiesForEmployer(eid);
        List<Application> applications = career.applicationsForEmployer(eid);
        long open = owned.stream().filter(o -> "Open".equalsIgnoreCase(o.getStatus())).count();
        javafx.scene.layout.HBox cards = new javafx.scene.layout.HBox(12,
                Ui.card("Posted Opportunities", String.valueOf(owned.size())),
                Ui.card("Open Opportunities", String.valueOf(open)),
                Ui.card("Applications", String.valueOf(applications.size())),
                Ui.card("Shared Portfolios", String.valueOf(portfolios.portfoliosForEmployer(eid).size())));
        return Ui.page("Employer Dashboard",
                "Welcome, " + Session.currentUser().getFullName() + ". Manage your opportunities and review student applications.",
                cards,
                DashboardCharts.row(DashboardCharts.statusSummary("Application Status",
                        DashboardCharts.countByStatus(
                                applications.stream().map(Application::getStatus).toList(),
                                List.of("Pending", "Reviewed", "Shortlisted", "Rejected", "Accepted")))));
    }

    public static Node opportunities() {
        int eid = employerId();
        List<JobOpportunity> owned = career.opportunitiesForEmployer(eid);
        TableView<JobOpportunity> table = Ui.table();
        Ui.addColumn(table, "Title", JobOpportunity::getTitle);
        Ui.addColumn(table, "Type", JobOpportunity::getOpportunityType);
        Ui.addColumn(table, "Location", o -> value(o.getLocation()));
        Ui.addColumn(table, "Required Skills", o -> value(o.getRequiredSkills()));
        Ui.addColumn(table, "Deadline", o -> Ui.formatDate(o.getDeadline()));
        Ui.addColumn(table, "Posted", o -> Ui.formatDate(o.getCreatedAt() == null ? null : o.getCreatedAt().toLocalDate()));
        Ui.addColumn(table, "Status", JobOpportunity::getStatus);
        table.setItems(FXCollections.observableArrayList(owned));

        TextField search = Ui.field("Search your opportunities...");
        Runnable applyFilter = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(career.opportunitiesForEmployer(eid).stream().filter(o ->
                    String.valueOf(o.getTitle()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(o.getLocation()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(o.getRequiredSkills()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Add Opportunity");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editOpportunity(eid, null, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to edit."); return; }
            editOpportunity(eid, selected, applyFilter);
        });
        Button close = new Button("Close");
        close.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to close."); return; }
            if (Ui.confirmed("Close \"" + selected.getTitle() + "\"? Students will no longer be able to apply.")) {
                guard(() -> {
                    JobOpportunity stored = career.opportunityById(selected.getOpportunityId());
                    stored.setStatus("Closed");
                    career.updateOpportunity(stored, eid);
                    applyFilter.run();
                });
            }
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            JobOpportunity selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an opportunity to delete."); return; }
            if (Ui.confirmed("Delete \"" + selected.getTitle() + "\" and all of its applications?")) {
                guard(() -> { career.deleteOpportunity(selected.getOpportunityId(), eid); applyFilter.run(); });
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Opportunities", "Create and maintain internship and employment opportunities for NextIT students.",
                Ui.toolbar(new Label("Search:"), search, add, edit, close, delete, refresh), table);
    }

    private static void editOpportunity(int employerId, JobOpportunity existing, Runnable refresh) {
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
        Node form = Ui.form(new String[]{"Title", "Type", "Description", "Required skills", "Location", "Deadline", "Status"},
                new Node[]{title, type, description, requiredSkills, location, deadline, status});
        Ui.dialog(existing == null ? "Add Opportunity" : "Edit Opportunity", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                JobOpportunity target = new JobOpportunity();
                if (existing != null) {
                    target.setOpportunityId(existing.getOpportunityId());
                    target.setCreatedAt(existing.getCreatedAt());
                }
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

    public static Node applications() {
        int eid = employerId();
        Map<Integer, String> opportunityTitles = new java.util.HashMap<>();
        Map<Integer, String> studentsById = new java.util.HashMap<>();

        TableView<Application> table = Ui.table();
        Ui.addColumn(table, "Student", a -> studentsById.getOrDefault(a.getStudentId(), "Student #" + a.getStudentId()));
        Ui.addColumn(table, "Opportunity", a -> opportunityTitles.getOrDefault(a.getOpportunityId(), "Opportunity #" + a.getOpportunityId()));
        Ui.addColumn(table, "Applied", a -> a.getApplicationDate() == null ? "" : a.getApplicationDate().toLocalDate().toString());
        Ui.addColumn(table, "Status", Application::getStatus);
        Ui.addColumn(table, "Remarks", Application::getRemarks);

        ComboBox<String> statusFilter = Ui.combo(List.of("All statuses", "Pending", "Reviewed", "Shortlisted", "Rejected", "Accepted"), s -> s);
        statusFilter.getSelectionModel().selectFirst();
        TextField search = Ui.field("Search by student, opportunity or remarks...");
        Runnable filter = () -> {
            opportunityTitles.clear();
            career.opportunitiesForEmployer(eid)
                    .forEach(o -> opportunityTitles.put(o.getOpportunityId(), o.getTitle()));
            studentsById.clear();
            studentsById.putAll(directory.studentNames());
            String value = statusFilter.getValue();
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(career.applicationsForEmployer(eid).stream()
                    .filter(a -> "All statuses".equals(value) || value.equalsIgnoreCase(a.getStatus()))
                    .filter(a -> q.isBlank()
                            || studentsById.getOrDefault(a.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || opportunityTitles.getOrDefault(a.getOpportunityId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(a.getRemarks()).toLowerCase(Locale.ROOT).contains(q))
                    .toList()));
        };
        statusFilter.valueProperty().addListener((obs, old, value) -> filter.run());
        search.textProperty().addListener((obs, old, value) -> filter.run());
        filter.run();

        Button review = new Button("Update Status");
        review.getStyleClass().add("primary-button");
        review.setOnAction(e -> {
            Application selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an application to review."); return; }
            ComboBox<String> status = Ui.combo(List.of("Pending", "Reviewed", "Shortlisted", "Rejected", "Accepted"), s -> s);
            status.getSelectionModel().select(selected.getStatus() == null ? "Pending" : selected.getStatus());
            TextArea remarks = Ui.area("Remarks");
            remarks.setText(selected.getRemarks());
            Node form = Ui.form(new String[]{"Applicant", "Status", "Remarks"},
                    new Node[]{new Label(studentsById.getOrDefault(selected.getStudentId(), "Student #" + selected.getStudentId())), status, remarks});
            Ui.dialog("Review Application", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    career.updateApplicationStatus(eid, selected, status.getValue(), remarks.getText());
                    filter.run();
                    Alerts.info("Application updated.");
                });
            });
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());
        return Ui.page("Applications", "Review applications submitted to your opportunities and update their status.",
                Ui.toolbar(new Label("Search:"), search, new Label("Filter:"), statusFilter, review, refresh), table);
    }

    public static Node portfolioSearch() {
        int eid = employerId();
        Map<Integer, String> studentNames = directory.studentNames();
        List<Portfolio> authorized = portfolios.portfoliosForEmployer(eid);
        List<TechnicalSkill> allSkills = directory.technicalSkills();

        TableView<Portfolio> table = Ui.table();
        Ui.addColumn(table, "Student", p -> studentNames.getOrDefault(p.getStudentId(), "Student #" + p.getStudentId()));
        Ui.addColumn(table, "Headline", Portfolio::getHeadline);
        Ui.addColumn(table, "Career Goal", Portfolio::getCareerGoal);
        Ui.addColumn(table, "Status", Portfolio::getVisibility);
        table.setItems(FXCollections.observableArrayList(authorized));

        TextField search = Ui.field("Search by student, skill, project, certification or achievement...");
        List<String> skillOptions = new java.util.ArrayList<>();
        skillOptions.add("All technical skills");
        allSkills.stream().map(TechnicalSkill::getSkillName).distinct().sorted().forEach(skillOptions::add);
        ComboBox<String> skillFilter = Ui.combo(skillOptions, s -> s);
        skillFilter.getSelectionModel().selectFirst();

        List<String> categoryOptions = new java.util.ArrayList<>();
        categoryOptions.add("All skill categories");
        allSkills.stream().map(TechnicalSkill::getCategory).filter(c -> c != null && !c.isBlank())
                .distinct().sorted().forEach(categoryOptions::add);
        ComboBox<String> categoryFilter = Ui.combo(categoryOptions, s -> s);
        categoryFilter.getSelectionModel().selectFirst();

        Runnable filter = () -> {
            studentNames.clear();
            studentNames.putAll(directory.studentNames());
            allSkills.clear();
            allSkills.addAll(directory.technicalSkills());
            refreshChoices(skillFilter, "All technical skills",
                    allSkills.stream().map(TechnicalSkill::getSkillName).distinct().sorted().toList());
            refreshChoices(categoryFilter, "All skill categories",
                    allSkills.stream().map(TechnicalSkill::getCategory)
                            .filter(c -> c != null && !c.isBlank()).distinct().sorted().toList());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            String skill = skillFilter.getValue();
            String category = categoryFilter.getValue();
            // Re-read access on every filter so newly granted portfolios appear
            // without leaving and reopening the screen.
            table.setItems(FXCollections.observableArrayList(portfolios.portfoliosForEmployer(eid).stream()
                    .filter(p -> q.isBlank() || searchTextFor(p.getStudentId(), studentNames).contains(q))
                    .filter(p -> "All technical skills".equals(skill) || hasSkill(p.getStudentId(), skill, allSkills))
                    .filter(p -> "All skill categories".equals(category) || hasSkillCategory(p.getStudentId(), category, allSkills))
                    .toList()));
        };
        search.textProperty().addListener((obs, old, value) -> filter.run());
        skillFilter.valueProperty().addListener((obs, old, value) -> filter.run());
        categoryFilter.valueProperty().addListener((obs, old, value) -> filter.run());
        filter.run();

        Button view = new Button("View Portfolio");
        view.getStyleClass().add("primary-button");
        view.setOnAction(e -> {
            Portfolio selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a portfolio to view."); return; }
            if (!portfolios.isVisibleToEmployer(selected.getPortfolioId(), eid)) {
                Alerts.error("You are not authorised to view this portfolio.");
                return;
            }
            guard(() -> PortfolioDetailsView.show(selected, false));
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> filter.run());

        return Ui.page("Student Portfolios", "Public portfolios and portfolios explicitly shared with your company are shown here. Filter by technical skill or category.",
                Ui.toolbar(new Label("Search:"), search, skillFilter, categoryFilter, view, refresh), table);
    }

    private static void refreshChoices(ComboBox<String> combo, String allLabel, List<String> values) {
        List<String> options = new java.util.ArrayList<>();
        options.add(allLabel);
        options.addAll(values);
        if (options.equals(combo.getItems())) return;
        String selected = combo.getValue();
        combo.setItems(FXCollections.observableArrayList(options));
        combo.setValue(options.contains(selected) ? selected : allLabel);
    }

    private static boolean hasSkill(int studentId, String skillName, List<TechnicalSkill> allSkills) {
        Map<Integer, String> names = allSkills.stream().collect(Collectors.toMap(
                TechnicalSkill::getSkillId, TechnicalSkill::getSkillName, (a, b) -> a));
        return skills.forStudent(studentId).stream()
                .anyMatch(s -> skillName.equalsIgnoreCase(names.get(s.getSkillId())));
    }

    private static boolean hasSkillCategory(int studentId, String category, List<TechnicalSkill> allSkills) {
        Map<Integer, String> categories = allSkills.stream().collect(Collectors.toMap(
                TechnicalSkill::getSkillId, TechnicalSkill::getCategory, (a, b) -> a));
        return skills.forStudent(studentId).stream()
                .anyMatch(s -> category.equalsIgnoreCase(categories.get(s.getSkillId())));
    }

    private static String searchTextFor(int studentId, Map<Integer, String> studentNames) {
        StringBuilder text = new StringBuilder();
        text.append(studentNames.getOrDefault(studentId, "")).append(' ');
        portfolios.portfolioForStudent(studentId).ifPresent(p -> text.append(value(p.getHeadline())).append(' ')
                .append(value(p.getBio())).append(' ').append(value(p.getCareerGoal())).append(' '));
        Map<Integer, String> skillNames = directory.skillNames();
        skills.forStudent(studentId).forEach(s -> text.append(skillNames.getOrDefault(s.getSkillId(), "")).append(' ')
                .append(value(s.getProficiencyLevel())).append(' '));
        projects.projectsForStudent(studentId).forEach(p -> text.append(value(p.getProjectTitle())).append(' ')
                .append(value(p.getTechnologiesUsed())).append(' '));
        students.achievementsFor(studentId).forEach(a -> text.append(value(a.getTitle())).append(' ')
                .append(value(a.getOrganization())).append(' '));
        students.certificationsFor(studentId).forEach(c -> text.append(value(c.getCertificationName())).append(' ')
                .append(value(c.getIssuingOrganization())).append(' '));
        return text.toString().toLowerCase(Locale.ROOT);
    }

    private static String value(String text) { return text == null ? "" : text; }

    private static void guard(Runnable action) {
        try {
            action.run();
        } catch (AppException e) {
            Alerts.error(e.getMessage());
        } catch (Exception e) {
            Alerts.error("Could not complete the operation. Please check the database connection.");
            org.slf4j.LoggerFactory.getLogger(EmployerViews.class).error("Employer action failed", e);
        }
    }
}
