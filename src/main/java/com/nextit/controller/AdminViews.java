package com.nextit.controller;

import com.nextit.model.*;
import com.nextit.repository.JdbcSubjectRepository;
import com.nextit.repository.JdbcTechnicalSkillRepository;
import com.nextit.repository.JdbcUserRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminViews {

    private static final JdbcUserRepository userRepo = new JdbcUserRepository();
    private static final JdbcSubjectRepository subjectRepo = new JdbcSubjectRepository();
    private static final JdbcTechnicalSkillRepository skillRepo = new JdbcTechnicalSkillRepository();

    public static Node dashboard() {
        VBox box = new VBox(16);
        box.setStyle("-fx-padding: 20;");
        Label title = new Label("Admin Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        HBox cards = new HBox(12);
        cards.getChildren().addAll(
                card("Users", String.valueOf(userRepo.findAll().size())),
                card("Subjects", String.valueOf(subjectRepo.findAll().size())),
                card("Skills", String.valueOf(skillRepo.findAll().size()))
        );
        box.getChildren().addAll(title, cards);
        return box;
    }

    public static Node users() {
        TableView<User> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(userRepo.findAll()));
        addCol(tv, "Username", User::getUsername);
        addCol(tv, "Email", User::getEmail);
        addCol(tv, "Role", User::getRole);
        addCol(tv, "Name", User::getFullName);
        HBox actions = new HBox(8);
        Button add = new Button("Add User");
        add.setOnAction(e -> {
            TextField username = new TextField(); username.setPromptText("Username");
            TextField email = new TextField(); email.setPromptText("Email");
            TextField first = new TextField(); first.setPromptText("First name");
            TextField last = new TextField(); last.setPromptText("Last name");
            TextField password = new TextField(); password.setPromptText("Password");
            ComboBox<Role> role = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
            role.getSelectionModel().selectFirst();
            Dialog<User> d = new Dialog<>();
            d.setTitle("Add User");
            d.getDialogPane().setContent(new VBox(8, username, email, first, last, password, role));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    User u = new User();
                    u.setUsername(username.getText());
                    u.setEmail(email.getText());
                    u.setFirstName(first.getText());
                    u.setLastName(last.getText());
                    u.setRole(role.getValue());
                    u.setPasswordHash(com.nextit.service.AuthenticationService.hashPassword(password.getText()));
                    return u;
                }
                return null;
            });
            d.showAndWait().ifPresent(u -> { userRepo.save(u); tv.setItems(FXCollections.observableArrayList(userRepo.findAll())); });
        });
        Button del = new Button("Delete");
        del.setOnAction(e -> {
            User u = tv.getSelectionModel().getSelectedItem();
            if (u != null && com.nextit.util.Alerts.confirm("Delete user " + u.getUsername() + "?")) {
                userRepo.delete(u.getUserId());
                tv.setItems(FXCollections.observableArrayList(userRepo.findAll()));
            }
        });
        actions.getChildren().addAll(add, del);
        VBox box = new VBox(10, actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }

    public static Node subjects() {
        TableView<Subject> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(subjectRepo.findAll()));
        addCol(tv, "Code", Subject::getSubjectCode);
        addCol(tv, "Name", Subject::getSubjectName);
        addCol(tv, "Units", Subject::getUnits);
        HBox actions = new HBox(8);
        Button add = new Button("Add Subject");
        add.setOnAction(e -> {
            TextField code = new TextField(); code.setPromptText("Code");
            TextField name = new TextField(); name.setPromptText("Name");
            TextField units = new TextField(); units.setPromptText("Units");
            Dialog<Subject> d = new Dialog<>();
            d.getDialogPane().setContent(new VBox(8, code, name, units));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    Subject s = new Subject();
                    s.setSubjectCode(code.getText());
                    s.setSubjectName(name.getText());
                    try { s.setUnits(Double.parseDouble(units.getText())); } catch (NumberFormatException ex) { throw new com.nextit.exception.AppException("Units must be a number."); }
                    return s;
                }
                return null;
            });
            d.showAndWait().ifPresent(s -> { subjectRepo.save(s); tv.setItems(FXCollections.observableArrayList(subjectRepo.findAll())); });
        });
        actions.getChildren().add(add);
        VBox box = new VBox(10, actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }

    public static Node skills() {
        TableView<TechnicalSkill> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(skillRepo.findAll()));
        addCol(tv, "Name", TechnicalSkill::getSkillName);
        addCol(tv, "Category", TechnicalSkill::getCategory);
        HBox actions = new HBox(8);
        Button add = new Button("Add Skill");
        add.setOnAction(e -> {
            TextField name = new TextField(); name.setPromptText("Name");
            TextField category = new TextField(); category.setPromptText("Category");
            Dialog<TechnicalSkill> d = new Dialog<>();
            d.getDialogPane().setContent(new VBox(8, name, category));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    TechnicalSkill s = new TechnicalSkill();
                    s.setSkillName(name.getText());
                    s.setCategory(category.getText());
                    return s;
                }
                return null;
            });
            d.showAndWait().ifPresent(s -> { skillRepo.save(s); tv.setItems(FXCollections.observableArrayList(skillRepo.findAll())); });
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

    private static <T> Node table(List<String> cols, java.util.function.Function<T, List<String>> mapper, List<T> items) {
        TableView<List<String>> tv = new TableView<>();
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
