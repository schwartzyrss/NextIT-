package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.service.AcademicService;
import com.nextit.service.AdminService;
import com.nextit.service.DirectoryService;
import com.nextit.service.PortfolioService;
import com.nextit.service.ProfileService;
import com.nextit.service.SkillService;
import com.nextit.service.SystemService;
import com.nextit.util.Alerts;
import com.nextit.util.Ui;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AdminViews {

    private static final AdminService admin = new AdminService();
    private static final AcademicService academic = new AcademicService();
    private static final DirectoryService directory = new DirectoryService();
    private static final PortfolioService portfolios = new PortfolioService();
    private static final ProfileService profiles = new ProfileService();
    private static final SkillService skills = new SkillService();
    private static final List<String> SEMESTERS = List.of("1st Semester", "2nd Semester", "Summer");
    private static final List<String> ENROLLMENT_STATUSES = List.of("Enrolled", "Dropped", "Completed");

    private AdminViews() {}

    public static Node profile() {
        User account = profiles.account(com.nextit.security.Session.currentUser().getUserId());
        Node form = Ui.form(new String[]{"Username", "Email", "Full name", "Role"},
                new Node[]{new Label(value(account.getUsername())), new Label(value(account.getEmail())),
                        new Label(value(account.getFullName())), new Label(String.valueOf(account.getRole()))});
        return Ui.page("My Profile", "Your administrator account information.", form);
    }

    public static Node dashboard() {
        javafx.scene.layout.HBox row1 = new javafx.scene.layout.HBox(12,
                Ui.card("Users", String.valueOf(admin.users().size())),
                Ui.card("Students", String.valueOf(admin.students().size())),
                Ui.card("Instructors", String.valueOf(admin.instructors().size())),
                Ui.card("Employers", String.valueOf(admin.employers().size())));
        javafx.scene.layout.HBox row2 = new javafx.scene.layout.HBox(12,
                Ui.card("Subjects", String.valueOf(admin.subjects().size())),
                Ui.card("Technical Skills", String.valueOf(skills.skills().size())),
                Ui.card("Portfolios", String.valueOf(portfolios.allPortfolios().size())),
                Ui.card("Pending Portfolios", String.valueOf(portfolios.allPortfolios().stream()
                        .filter(p -> "Pending".equalsIgnoreCase(p.getVisibility())).count())));
        Map<String, Long> enrollmentStatuses = DashboardCharts.countByStatus(
                academic.allEnrollments().stream().map(Enrollment::getStatus).toList(),
                List.of("Enrolled", "Dropped", "Completed"));
        return Ui.page("Administrator Dashboard", "Manage accounts, academic reference data and student portfolios.",
                row1, row2,
                DashboardCharts.row(
                        DashboardCharts.statusSummary("Enrollment Status", enrollmentStatuses),
                        DashboardCharts.statusSummary("Portfolio Visibility",
                                DashboardCharts.countByStatus(
                                        portfolios.allPortfolios().stream().map(Portfolio::getVisibility).toList(),
                                        List.of("Private", "Pending", "Approved", "Public")))));
    }

    public static Node users() {
        List<User> all = admin.users();
        TableView<User> table = Ui.table();
        Ui.addColumn(table, "Username", User::getUsername);
        Ui.addColumn(table, "Full Name", User::getFullName);
        Ui.addColumn(table, "Email", User::getEmail);
        Ui.addColumn(table, "Role", u -> String.valueOf(u.getRole()));
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search users...");
        Runnable applyFilter = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(admin.users().stream().filter(u ->
                    String.valueOf(u.getUsername()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(u.getFullName()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(u.getEmail()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(u.getRole()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Add User");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editUser(null, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a user to edit."); return; }
            editUser(selected, applyFilter);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a user to delete."); return; }
            if (selected.getUserId() == com.nextit.security.Session.currentUser().getUserId()) {
                Alerts.error("You cannot delete the account you are signed in with.");
                return;
            }
            if (Ui.confirmed("Delete user \"" + selected.getUsername() + "\"? This also removes their profile and related records.")) {
                guard(() -> { admin.deleteUser(selected.getUserId()); applyFilter.run(); });
            }
        });
        Button resetPassword = new Button("Reset Password");
        resetPassword.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a user whose password should be reset."); return; }
            PasswordField password = Ui.password("At least 6 characters");
            Node form = Ui.form(new String[]{"User", "New password"},
                    new Node[]{new Label(selected.getFullName() + " (" + selected.getUsername() + ")"), password});
            Ui.dialog("Reset Password", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    admin.resetPassword(selected.getUserId(), password.getText());
                    Alerts.info("Password reset. Share the new password with the user securely.");
                });
            });
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("User Accounts", "Create and maintain accounts for students, instructors, employers and administrators.",
                Ui.toolbar(new Label("Search:"), search, add, edit, resetPassword, delete, refresh), table);
    }

    private static void editUser(User existing, Runnable refresh) {
        User u = existing == null ? new User() : existing;
        TextField username = Ui.field("Username");
        username.setText(u.getUsername());
        TextField email = Ui.field("name@example.com");
        email.setText(u.getEmail());
        TextField first = Ui.field("First name");
        first.setText(u.getFirstName());
        TextField last = Ui.field("Last name");
        last.setText(u.getLastName());
        ComboBox<Role> role = Ui.combo(List.of(Role.values()), Enum::name);
        role.getSelectionModel().select(u.getRole() == null ? Role.STUDENT : u.getRole());
        role.setDisable(existing != null);
        PasswordField password = Ui.password("At least 6 characters");
        Node form = existing == null
                ? Ui.form(new String[]{"Username", "Email", "First name", "Last name", "Role", "Password"},
                    new Node[]{username, email, first, last, role, password})
                : Ui.form(new String[]{"Username", "Email", "First name", "Last name", "Role"},
                    new Node[]{username, email, first, last, role});
        Ui.dialog(existing == null ? "Add User" : "Edit User", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                User target = new User();
                if (existing != null) {
                    target.setUserId(existing.getUserId());
                    target.setPasswordHash(existing.getPasswordHash());
                    target.setCreatedAt(existing.getCreatedAt());
                    target.setUpdatedAt(existing.getUpdatedAt());
                }
                target.setUsername(username.getText());
                target.setEmail(email.getText());
                target.setFirstName(first.getText());
                target.setLastName(last.getText());
                target.setRole(existing == null ? role.getValue() : existing.getRole());
                if (existing == null) admin.createUser(target, password.getText());
                else admin.updateUser(target);
                refresh.run();
                Alerts.info("User saved.");
            });
        });
    }

    public static Node subjects() {
        List<Subject> all = admin.subjects();
        TableView<Subject> table = Ui.table();
        Ui.addColumn(table, "Code", Subject::getSubjectCode);
        Ui.addColumn(table, "Name", Subject::getSubjectName);
        Ui.addColumn(table, "Units", s -> String.valueOf(s.getUnits()));
        Ui.addColumn(table, "Description", Subject::getDescription);
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by code, name or description...");
        Runnable applyFilter = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(admin.subjects().stream().filter(s ->
                    String.valueOf(s.getSubjectCode()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getSubjectName()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getDescription()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Add Subject");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editSubject(null, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Subject selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a subject to edit."); return; }
            editSubject(selected, applyFilter);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Subject selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a subject to delete."); return; }
            if (Ui.confirmed("Delete subject \"" + selected.getSubjectCode() + "\"?")) {
                guard(() -> { admin.deleteSubject(selected.getSubjectId()); applyFilter.run(); });
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Subjects", "Maintain the academic subject catalogue used by enrollments, grades and projects.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refresh), table);
    }

    private static void editSubject(Subject existing, Runnable refresh) {
        Subject s = existing == null ? new Subject() : existing;
        TextField code = Ui.field("e.g. IT301");
        code.setText(s.getSubjectCode());
        TextField name = Ui.field("Subject name");
        name.setText(s.getSubjectName());
        TextField units = Ui.field("Units");
        units.setText(existing == null ? "3.0" : String.valueOf(s.getUnits()));
        TextArea description = Ui.area("Description");
        description.setText(s.getDescription());
        Node form = Ui.form(new String[]{"Code", "Name", "Units", "Description"},
                new Node[]{code, name, units, description});
        Ui.dialog(existing == null ? "Add Subject" : "Edit Subject", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                Subject target = new Subject();
                if (existing != null) target.setSubjectId(existing.getSubjectId());
                target.setSubjectCode(code.getText());
                target.setSubjectName(name.getText());
                target.setUnits(Ui.parseDouble(units.getText(), "Units"));
                target.setDescription(description.getText());
                admin.saveSubject(target);
                refresh.run();
                Alerts.info("Subject saved.");
            });
        });
    }

    public static Node enrollments() {
        Map<Integer, String> students = directory.studentNames();
        Map<Integer, String> subjects = directory.subjectNames();
        TableView<Enrollment> table = Ui.table();
        Ui.addColumn(table, "Student", e -> students.getOrDefault(e.getStudentId(), "Student #" + e.getStudentId()));
        Ui.addColumn(table, "Subject", e -> subjects.getOrDefault(e.getSubjectId(), "Subject #" + e.getSubjectId()));
        Ui.addColumn(table, "Academic Year", Enrollment::getAcademicYear);
        Ui.addColumn(table, "Semester", Enrollment::getSemester);
        Ui.addColumn(table, "Status", Enrollment::getStatus);
        List<Enrollment> all = academic.allEnrollments();
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by student, subject, term or status...");
        Runnable applyFilter = () -> {
            students.clear();
            students.putAll(directory.studentNames());
            subjects.clear();
            subjects.putAll(directory.subjectNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(academic.allEnrollments().stream().filter(e ->
                    students.getOrDefault(e.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || subjects.getOrDefault(e.getSubjectId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(e.getAcademicYear()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(e.getSemester()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(e.getStatus()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Enroll Student");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> editEnrollment(null, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            Enrollment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an enrollment to edit."); return; }
            editEnrollment(selected, applyFilter);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            Enrollment selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an enrollment to delete."); return; }
            if (Ui.confirmed("Delete this enrollment? Its grades and attendance records will also be removed.")) {
                guard(() -> {
                    academic.deleteEnrollment(selected.getEnrollmentId());
                    applyFilter.run();
                });
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Enrollments", "Associate students with the subjects they are taking so grades and attendance can be recorded.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refresh), table);
    }

    private static void editEnrollment(Enrollment existing, Runnable refresh) {
        List<Student> studentList = directory.students();
        List<Subject> subjectList = directory.subjects();
        Map<Integer, String> names = directory.studentNames();
        ComboBox<Student> student = Ui.combo(studentList,
                s -> names.getOrDefault(s.getStudentId(), "Student #" + s.getStudentId()) + " (" + value(s.getStudentNumber()) + ")");
        ComboBox<Subject> subject = Ui.combo(subjectList, s -> s.getSubjectCode() + " - " + s.getSubjectName());
        TextField year = Ui.field("Academic year");
        ComboBox<String> semester = Ui.combo(SEMESTERS, s -> s);
        ComboBox<String> status = Ui.combo(ENROLLMENT_STATUSES, s -> s);

        if (existing == null) {
            int current = LocalDate.now().getYear();
            year.setText(LocalDate.now().getMonthValue() >= 6
                    ? current + "-" + (current + 1) : (current - 1) + "-" + current);
            semester.getSelectionModel().selectFirst();
            status.getSelectionModel().selectFirst();
        } else {
            studentList.stream().filter(s -> s.getStudentId() == existing.getStudentId())
                    .findFirst().ifPresent(student.getSelectionModel()::select);
            subjectList.stream().filter(s -> s.getSubjectId() == existing.getSubjectId())
                    .findFirst().ifPresent(subject.getSelectionModel()::select);
            year.setText(existing.getAcademicYear());
            semester.getSelectionModel().select(existing.getSemester());
            status.getSelectionModel().select(existing.getStatus());
        }

        if (existing != null) {
            student.setDisable(true);
            subject.setDisable(true);
        }
        Node form = Ui.form(new String[]{"Student", "Subject", "Academic year", "Semester", "Status"},
                new Node[]{student, subject, year, semester, status});
        Ui.dialog(existing == null ? "Enroll Student" : "Edit Enrollment", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                if (student.getValue() == null) throw new AppException("Select a student.");
                if (subject.getValue() == null) throw new AppException("Select a subject.");
                Enrollment target = new Enrollment();
                if (existing != null) target.setEnrollmentId(existing.getEnrollmentId());
                target.setStudentId(student.getValue().getStudentId());
                target.setSubjectId(subject.getValue().getSubjectId());
                target.setAcademicYear(year.getText());
                target.setSemester(semester.getValue());
                target.setStatus(status.getValue());
                if (existing == null) academic.saveEnrollment(target);
                else academic.updateEnrollment(target);
                refresh.run();
                Alerts.info("Enrollment saved.");
            });
        });
    }

    public static Node skills() {
        return SkillCatalogView.view();
    }

    public static Node grades() {
        ComboBox<Instructor> acting = AcademicManagementViews.instructorSelector();
        return AcademicManagementViews.grades(
                () -> AcademicManagementViews.selectedInstructor(acting),
                AcademicManagementViews.instructorControl(acting));
    }

    public static Node attendance() {
        ComboBox<Instructor> acting = AcademicManagementViews.instructorSelector();
        return AcademicManagementViews.attendance(
                () -> AcademicManagementViews.selectedInstructor(acting),
                AcademicManagementViews.instructorControl(acting));
    }

    public static Node technicalEvaluations() {
        ComboBox<Instructor> acting = AcademicManagementViews.instructorSelector();
        return AcademicManagementViews.technicalEvaluations(
                () -> AcademicManagementViews.selectedInstructor(acting),
                AcademicManagementViews.instructorControl(acting));
    }

    public static Node projectEvaluations() {
        ComboBox<Instructor> acting = AcademicManagementViews.instructorSelector();
        return AcademicManagementViews.evaluations(
                () -> AcademicManagementViews.selectedInstructor(acting),
                AcademicManagementViews.instructorControl(acting));
    }

    public static Node academicSupport() {
        ComboBox<Instructor> acting = AcademicManagementViews.instructorSelector();
        return AcademicManagementViews.support(
                () -> AcademicManagementViews.selectedInstructor(acting),
                AcademicManagementViews.instructorControl(acting));
    }

    public static Node achievements() {
        return DevelopmentManagementViews.achievements();
    }

    public static Node certifications() {
        return DevelopmentManagementViews.certifications();
    }

    public static Node projects() {
        return DevelopmentManagementViews.projects();
    }

    public static Node opportunities() {
        return DevelopmentManagementViews.opportunities(true);
    }

    public static Node applications() {
        return DevelopmentManagementViews.applications(true);
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
        List<Student> all = admin.students();
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by name, student number, program or section...");
        Runnable applyFilter = () -> {
            names.clear();
            names.putAll(directory.studentNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(admin.students().stream().filter(s ->
                    names.getOrDefault(s.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getStudentNumber()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getProgram()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(s.getSection()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button edit = new Button("Edit Profile");
        edit.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a student to edit."); return; }
            TextField number = Ui.field("Student number");
            number.setText(selected.getStudentNumber());
            TextField program = Ui.field("Program");
            program.setText(selected.getProgram());
            TextField year = Ui.field("Year level");
            year.setText(String.valueOf(selected.getYearLevel()));
            TextField section = Ui.field("Section");
            section.setText(selected.getSection());
            ComboBox<String> status = Ui.combo(List.of("Enrolled", "On Leave", "Graduated", "Dropped"), s -> s);
            status.getSelectionModel().select(selected.getEnrollmentStatus());
            Node form = Ui.form(new String[]{"Student number", "Program", "Year level", "Section", "Enrollment status"},
                    new Node[]{number, program, year, section, status});
            Ui.dialog("Edit Student Profile", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    Student target = new Student();
                    target.setStudentId(selected.getStudentId());
                    target.setUserId(selected.getUserId());
                    target.setStudentNumber(number.getText());
                    target.setProgram(program.getText());
                    target.setYearLevel(Ui.parseInt(year.getText(), "Year level"));
                    target.setSection(section.getText());
                    target.setEnrollmentStatus(status.getValue());
                    admin.updateStudent(target);
                    applyFilter.run();
                    Alerts.info("Student profile updated.");
                });
            });
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Student Profiles", "Review and update student academic profile information.",
                Ui.toolbar(new Label("Search:"), search, edit, refresh), table);
    }

    public static Node instructors() {
        Map<Integer, String> names = directory.instructorNames();
        TableView<Instructor> table = Ui.table();
        Ui.addColumn(table, "Employee No.", Instructor::getEmployeeNumber);
        Ui.addColumn(table, "Name", i -> names.getOrDefault(i.getInstructorId(), "Instructor #" + i.getInstructorId()));
        Ui.addColumn(table, "Department", Instructor::getDepartment);
        Ui.addColumn(table, "Specialization", Instructor::getSpecialization);
        List<Instructor> all = admin.instructors();
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by name, employee number, department or specialization...");
        Runnable applyFilter = () -> {
            names.clear();
            names.putAll(directory.instructorNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(admin.instructors().stream().filter(i ->
                    names.getOrDefault(i.getInstructorId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(i.getEmployeeNumber()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(i.getDepartment()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(i.getSpecialization()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button edit = new Button("Edit Profile");
        edit.setOnAction(e -> {
            Instructor selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an instructor to edit."); return; }
            TextField number = Ui.field("Employee number");
            number.setText(selected.getEmployeeNumber());
            TextField department = Ui.field("Department");
            department.setText(selected.getDepartment());
            TextField specialization = Ui.field("Specialization");
            specialization.setText(selected.getSpecialization());
            Node form = Ui.form(new String[]{"Employee number", "Department", "Specialization"},
                    new Node[]{number, department, specialization});
            Ui.dialog("Edit Instructor Profile", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    Instructor target = new Instructor();
                    target.setInstructorId(selected.getInstructorId());
                    target.setUserId(selected.getUserId());
                    target.setEmployeeNumber(number.getText());
                    target.setDepartment(department.getText());
                    target.setSpecialization(specialization.getText());
                    admin.updateInstructor(target);
                    applyFilter.run();
                    Alerts.info("Instructor profile updated.");
                });
            });
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Instructor Profiles", "Review instructor departments and specializations.",
                Ui.toolbar(new Label("Search:"), search, edit, refresh), table);
    }

    public static Node employers() {
        TableView<Employer> table = Ui.table();
        Ui.addColumn(table, "Company", Employer::getCompanyName);
        Ui.addColumn(table, "Industry", Employer::getIndustry);
        Ui.addColumn(table, "Contact Person", Employer::getContactPerson);
        Ui.addColumn(table, "Contact Number", Employer::getContactNumber);
        Ui.addColumn(table, "Address", Employer::getAddress);
        List<Employer> all = admin.employers();
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by company, industry, contact or address...");
        Runnable applyFilter = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            table.setItems(FXCollections.observableArrayList(admin.employers().stream().filter(e ->
                    String.valueOf(e.getCompanyName()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(e.getIndustry()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(e.getContactPerson()).toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(e.getAddress()).toLowerCase(Locale.ROOT).contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button edit = new Button("Edit Profile");
        edit.setOnAction(e -> {
            Employer selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select an employer to edit."); return; }
            TextField company = Ui.field("Company name");
            company.setText(selected.getCompanyName());
            TextField industry = Ui.field("Industry");
            industry.setText(selected.getIndustry());
            TextField contact = Ui.field("Contact person");
            contact.setText(selected.getContactPerson());
            TextField number = Ui.field("Contact number");
            number.setText(selected.getContactNumber());
            TextField address = Ui.field("Address");
            address.setText(selected.getAddress());
            Node form = Ui.form(new String[]{"Company", "Industry", "Contact person", "Contact number", "Address"},
                    new Node[]{company, industry, contact, number, address});
            Ui.dialog("Edit Employer Profile", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    Employer target = new Employer();
                    target.setEmployerId(selected.getEmployerId());
                    target.setUserId(selected.getUserId());
                    target.setCompanyName(company.getText());
                    target.setIndustry(industry.getText());
                    target.setContactPerson(contact.getText());
                    target.setContactNumber(number.getText());
                    target.setAddress(address.getText());
                    admin.updateEmployer(target);
                    applyFilter.run();
                    Alerts.info("Employer profile updated.");
                });
            });
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Employer Profiles", "Review the companies and industry partners connected to NextIT.",
                Ui.toolbar(new Label("Search:"), search, edit, refresh), table);
    }

    public static Node portfolios() {
        Map<Integer, String> students = directory.studentNames();
        TableView<Portfolio> table = Ui.table();
        Ui.addColumn(table, "Student", p -> students.getOrDefault(p.getStudentId(), "Student #" + p.getStudentId()));
        Ui.addColumn(table, "Headline", Portfolio::getHeadline);
        Ui.addColumn(table, "Visibility", Portfolio::getVisibility);
        Ui.addColumn(table, "Updated", p -> p.getUpdatedAt() == null ? "" : p.getUpdatedAt().toLocalDate().toString());

        ComboBox<String> filter = Ui.combo(List.of("All portfolios", "Pending", "Approved", "Public", "Private"), s -> s);
        filter.getSelectionModel().selectFirst();
        TextField search = Ui.field("Search by student or headline...");
        Runnable applyFilter = () -> {
            students.clear();
            students.putAll(directory.studentNames());
            String q = search.getText() == null ? "" : search.getText().toLowerCase(Locale.ROOT);
            String visibility = filter.getValue();
            table.setItems(FXCollections.observableArrayList(portfolios.allPortfolios().stream()
                    .filter(p -> "All portfolios".equals(visibility) || visibility.equalsIgnoreCase(p.getVisibility()))
                    .filter(p -> q.isBlank()
                            || students.getOrDefault(p.getStudentId(), "").toLowerCase(Locale.ROOT).contains(q)
                            || String.valueOf(p.getHeadline()).toLowerCase(Locale.ROOT).contains(q))
                    .toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());
        filter.valueProperty().addListener((obs, old, value) -> applyFilter.run());
        applyFilter.run();

        Button view = new Button("View Portfolio");
        view.setOnAction(e -> {
            Portfolio selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a portfolio to view."); return; }
            guard(() -> PortfolioDetailsView.show(selected));
        });
        Button approve = new Button("Approve Selected");
        approve.getStyleClass().add("primary-button");
        approve.setOnAction(e -> {
            Portfolio selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a portfolio to approve."); return; }
            if ("Approved".equalsIgnoreCase(selected.getVisibility())) {
                Alerts.info("This portfolio is already approved.");
                return;
            }
            if (!"Pending".equalsIgnoreCase(selected.getVisibility())) {
                Alerts.error("This portfolio has not been submitted for approval.");
                return;
            }
            guard(() -> {
                portfolios.approve(selected.getPortfolioId());
                applyFilter.run();
                Alerts.info("Portfolio approved.");
            });
        });
        Button grant = new Button("Grant Employer Access");
        grant.setOnAction(e -> {
            Portfolio selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a portfolio first."); return; }
            ComboBox<Employer> employer = Ui.combo(directory.employers(), Employer::getCompanyName);
            Node form = Ui.form(new String[]{"Portfolio", "Employer"},
                    new Node[]{new Label(selected.getHeadline()), employer});
            Ui.dialog("Grant Portfolio Access", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    if (employer.getValue() == null) throw new AppException("Select an employer.");
                    portfolios.grantAccess(selected.getPortfolioId(), employer.getValue().getEmployerId());
                    Alerts.info("Portfolio access granted.");
                });
            });
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        Button revoke = new Button("Revoke Access");
        revoke.setOnAction(e -> {
            Portfolio selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a portfolio first."); return; }
            Map<Integer, String> employers = directory.employerNames();
            List<PortfolioAccess> active = portfolios.accessFor(selected.getPortfolioId()).stream()
                    .filter(a -> "Approved".equalsIgnoreCase(a.getAccessStatus()))
                    .toList();
            if (active.isEmpty()) {
                Alerts.info("This portfolio has no active employer access to revoke.");
                return;
            }
            ComboBox<PortfolioAccess> access = Ui.combo(active, a ->
                    employers.getOrDefault(a.getEmployerId(), "Employer #" + a.getEmployerId())
                            + (a.getGrantedAt() == null ? "" : " (granted " + a.getGrantedAt().toLocalDate() + ")"));
            Node form = Ui.form(new String[]{"Portfolio", "Employer access"},
                    new Node[]{new Label(selected.getHeadline()), access});
            Ui.dialog("Revoke Portfolio Access", form).ifPresent(button -> {
                if (button != ButtonType.OK) return;
                guard(() -> {
                    if (access.getValue() == null) throw new AppException("Select an employer access record.");
                    portfolios.revokeAccess(access.getValue().getAccessId());
                    Alerts.info("Portfolio access revoked.");
                });
            });
        });
        return Ui.page("Portfolio Approval", "Approve student portfolios, grant access to authorised employers, and revoke access when it is no longer needed.",
                Ui.toolbar(new Label("Search:"), search, new Label("Filter:"), filter, view, approve, grant, revoke, refresh), table);
    }

    public static Node system() {
        SystemService system = new SystemService();
        Label status = new Label();
        status.setStyle("-fx-font-weight: bold;");
        Label detail = new Label();
        detail.setWrapText(true);
        Label url = new Label();
        url.setWrapText(true);
        Label user = new Label();
        detail.getStyleClass().add("page-subtitle");
        url.getStyleClass().add("page-subtitle");
        user.getStyleClass().add("page-subtitle");

        TableView<SystemService.TableCount> table = Ui.table();
        Ui.addColumn(table, "Table", SystemService.TableCount::table);
        Ui.addColumn(table, "Rows", t -> t.rows() < 0 ? "Unavailable" : String.valueOf(t.rows()));

        Runnable refresh = () -> {
            SystemService.DatabaseStatus current = system.status();
            status.setText(current.connected() ? "Connected" : "Unavailable");
            status.setStyle(current.connected()
                    ? "-fx-font-weight: bold; -fx-text-fill: #1b7f3a;"
                    : "-fx-font-weight: bold; -fx-text-fill: #b3261e;");
            detail.setText(current.connected()
                    ? "NextIT can reach the configured database and read its schema."
                    : current.error());
            url.setText("JDBC URL: " + current.url());
            user.setText("Database user: " + current.user());
            table.setItems(FXCollections.observableArrayList(current.tables()));
        };
        refresh.run();

        Button test = new Button("Test Connection");
        test.getStyleClass().add("primary-button");
        test.setOnAction(e -> guard(refresh));
        Button refreshButton = new Button("Refresh Counts");
        refreshButton.setOnAction(e -> guard(refresh));

        Node summary = Ui.form(new String[]{"Status", "Details", "Configuration", "Account"},
                new Node[]{status, detail, url, user});
        return Ui.page("System Configuration", "Read-only diagnostics for the local database configuration. Credentials are loaded from the environment, JVM properties, or the optional .env file.",
                Ui.toolbar(test, refreshButton), summary, Ui.section("Database Row Counts"), table);
    }

    private static String value(String text) { return text == null ? "" : text; }

    private static void guard(Runnable action) {
        try {
            action.run();
        } catch (AppException e) {
            Alerts.error(e.getMessage());
            org.slf4j.LoggerFactory.getLogger(AdminViews.class).error("Admin action failed", e);
        } catch (Exception e) {
            Alerts.error("Could not complete the operation. The record may be referenced elsewhere, or the database is unavailable.");
            org.slf4j.LoggerFactory.getLogger(AdminViews.class).error("Admin action failed", e);
        }
    }
}
