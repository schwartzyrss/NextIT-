package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.TechnicalSkill;
import com.nextit.service.SkillService;
import com.nextit.util.Alerts;
import com.nextit.util.Ui;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.List;

/**
 * Shared technical skill catalogue screen. The permission matrix allows both
 * instructors and administrators to manage the catalogue, so both dashboards
 * route here instead of keeping two copies of the same CRUD workflow.
 */
public final class SkillCatalogView {

    private static final SkillService skills = new SkillService();

    private SkillCatalogView() {}

    public static Node view() {
        List<TechnicalSkill> all = skills.skills();
        TableView<TechnicalSkill> table = Ui.table();
        Ui.addColumn(table, "Skill", TechnicalSkill::getSkillName);
        Ui.addColumn(table, "Category", TechnicalSkill::getCategory);
        Ui.addColumn(table, "Description", TechnicalSkill::getDescription);
        table.setItems(FXCollections.observableArrayList(all));

        TextField search = Ui.field("Search by skill, category or description...");
        Runnable applyFilter = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase();
            table.setItems(FXCollections.observableArrayList(skills.skills().stream().filter(s ->
                    String.valueOf(s.getSkillName()).toLowerCase().contains(q)
                            || String.valueOf(s.getCategory()).toLowerCase().contains(q)
                            || String.valueOf(s.getDescription()).toLowerCase().contains(q)).toList()));
        };
        search.textProperty().addListener((obs, old, value) -> applyFilter.run());

        Button add = new Button("Add Skill");
        add.getStyleClass().add("primary-button");
        add.setOnAction(e -> edit(table, null, applyFilter));
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            TechnicalSkill selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a skill to edit."); return; }
            edit(table, selected, applyFilter);
        });
        Button delete = new Button("Delete");
        delete.setOnAction(e -> {
            TechnicalSkill selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { Alerts.error("Select a skill to delete."); return; }
            if (Ui.confirmed("Delete technical skill \"" + selected.getSkillName() + "\"? Skills that are already used by students or projects cannot be deleted.")) {
                guard(() -> {
                    skills.delete(selected.getSkillId());
                    applyFilter.run();
                    Alerts.info("Technical skill deleted.");
                });
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> applyFilter.run());
        return Ui.page("Technical Skills", "Maintain the technical skill catalogue used for student assessments and portfolio filtering.",
                Ui.toolbar(new Label("Search:"), search, add, edit, delete, refresh), table);
    }

    private static void edit(TableView<TechnicalSkill> table, TechnicalSkill existing, Runnable refresh) {
        TextField name = Ui.field("Skill name");
        name.setText(existing == null ? "" : existing.getSkillName());
        TextField category = Ui.field("Category");
        category.setText(existing == null ? "" : existing.getCategory());
        TextArea description = Ui.area("Description");
        description.setText(existing == null ? "" : existing.getDescription());
        Node form = Ui.form(new String[]{"Skill", "Category", "Description"}, new Node[]{name, category, description});
        Ui.dialog(existing == null ? "Add Technical Skill" : "Edit Technical Skill", form).ifPresent(button -> {
            if (button != ButtonType.OK) return;
            guard(() -> {
                TechnicalSkill target = existing == null ? new TechnicalSkill() : existing;
                target.setSkillName(name.getText());
                target.setCategory(category.getText());
                target.setDescription(description.getText());
                skills.save(target);
                refresh.run();
                Alerts.info("Technical skill saved.");
            });
        });
    }

    private static void guard(Runnable action) {
        try {
            action.run();
        } catch (AppException e) {
            Alerts.error(e.getMessage());
        } catch (Exception e) {
            Alerts.error("Could not complete the operation. The skill may be referenced by student assessments or projects.");
            org.slf4j.LoggerFactory.getLogger(SkillCatalogView.class).error("Technical skill action failed", e);
        }
    }
}
