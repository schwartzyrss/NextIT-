package com.nextit.util;

import com.nextit.exception.AppException;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Small factory for the reusable JavaFX controls used across the dashboard views.
 * Keeping these helpers in one place keeps the module controllers focused on
 * behaviour instead of widget wiring.
 */
public final class Ui {

    private Ui() {}

        private static int cardCounter = 0;
    private static final String[] CLAY_VARIANTS = {
        "card-cream", "card-mint", "card-lavender", "card-pink", "card-yellow", "card-blue"
    };

    public static VBox page(String title, String subtitle, Node... content) {
        VBox box = new VBox(20);
        box.getStyleClass().add("page");
        box.setFillWidth(true);
        VBox headerBox = new VBox(6);
        headerBox.getStyleClass().add("page-header-cushion");
        headerBox.getChildren().add(title(title));
        if (subtitle != null && !subtitle.isBlank()) {
            headerBox.getChildren().add(subtitle(subtitle));
        }
        box.getChildren().add(headerBox);
        box.getChildren().addAll(content);
        return box;
    }

    public static Button button(String text, String... styleClasses) {
        Button btn = new Button(text);
        if (styleClasses != null && styleClasses.length > 0) {
            btn.getStyleClass().addAll(styleClasses);
        }
        return btn;
    }

    public static String value(String text) {
        return text == null ? "" : text;
    }

    public static String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        } else if (parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase();
        } else {
            return parts[0].toUpperCase();
        }
    }

    public static StackPane avatar(String initials, String styleClass) {
        StackPane avatar = new StackPane();
        avatar.getStyleClass().addAll("profile-avatar", styleClass);
        Label initialsLabel = new Label(initials == null || initials.isBlank() ? "U" : initials);
        initialsLabel.getStyleClass().add("profile-avatar-text");
        avatar.getChildren().add(initialsLabel);
        return avatar;
    }

    public static Label badge(String text, String colorVariant) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().addAll("badge-pill", colorVariant);
        return label;
    }

    public static VBox propertyTile(String labelText, Node valueNode) {
        VBox tile = new VBox(6);
        tile.getStyleClass().add("profile-prop-tile");
        Label label = new Label(labelText != null ? labelText.toUpperCase() : "");
        label.getStyleClass().add("profile-prop-label");
        tile.getChildren().addAll(label, valueNode);
        return tile;
    }

    public static VBox propertyTile(String labelText, String valueText) {
        Label valueLabel = new Label(valueText != null && !valueText.isBlank() ? valueText : "—");
        valueLabel.getStyleClass().add("profile-prop-value");
        return propertyTile(labelText, valueLabel);
    }

    public static VBox tray(String sectionTitle, String sectionSubtitle, Node... content) {
        VBox tray = new VBox(16);
        tray.getStyleClass().add("clay-tray");
        tray.setFillWidth(true);
        if (sectionTitle != null && !sectionTitle.isBlank()) {
            VBox titleBox = new VBox(3);
            Label title = new Label(sectionTitle);
            title.getStyleClass().add("tray-title");
            titleBox.getChildren().add(title);
            if (sectionSubtitle != null && !sectionSubtitle.isBlank()) {
                Label sub = new Label(sectionSubtitle);
                sub.getStyleClass().add("tray-subtitle");
                titleBox.getChildren().add(sub);
            }
            tray.getChildren().add(titleBox);
        }
        tray.getChildren().addAll(content);
        return tray;
    }

    public static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }

    public static Label subtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-subtitle");
        return label;
    }

    public static Label section(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

        public static VBox card(String label, String value) {
        VBox box = new VBox(6);
        box.getStyleClass().add("card");
        box.getStyleClass().add(CLAY_VARIANTS[Math.abs(cardCounter++) % CLAY_VARIANTS.length]);
        Label val = new Label(value);
        val.getStyleClass().add("card-value");
        Label name = new Label(label);
        name.getStyleClass().add("card-label");
        box.getChildren().addAll(val, name);
        return box;
    }

    public static HBox toolbar(Node... nodes) {
        HBox box = new HBox(8, nodes);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("toolbar");
        return box;
    }

    public static TextField field(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    public static PasswordField password(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    public static TextArea area(String prompt) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(3);
        area.setWrapText(true);
        area.setMaxWidth(Double.MAX_VALUE);
        return area;
    }

    public static DatePicker date(LocalDate value) {
        DatePicker picker = new DatePicker(value);
        picker.setMaxWidth(Double.MAX_VALUE);
        return picker;
    }

    /** Blank-safe display for optional dates so tables never show a literal "null". */
    public static String formatDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    public static <T> ComboBox<T> combo(List<T> items, Function<T, String> labeler) {
        ComboBox<T> combo = new ComboBox<>(FXCollections.observableArrayList(items));
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(T item) { return item == null ? "" : labeler.apply(item); }
            @Override public T fromString(String text) { return null; }
        });
        return combo;
    }

    public static GridPane form(String[] labels, Node[] controls) {
        if (labels.length != controls.length) {
            throw new IllegalArgumentException("Every form field needs a label.");
        }
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.getStyleClass().add("form-grid");
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.getStyleClass().add("form-label");
            grid.add(label, 0, i);
            grid.add(controls[i], 1, i);
            if (controls[i] instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
            }
        }
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(150);
        ColumnConstraints controlColumn = new ColumnConstraints();
        controlColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelColumn, controlColumn);
        return grid;
    }

    public static <T> TableView<T> table() {
        return table("No records to display.");
    }

    public static <T> TableView<T> table(String emptyMessage) {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        Label empty = new Label(emptyMessage != null ? emptyMessage : "No records to display.");
        empty.getStyleClass().add("empty-state");
        table.setPlaceholder(empty);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    public static <T> TableColumn<T, String> column(String name, Function<T, String> getter) {
        TableColumn<T, String> column = new TableColumn<>(name);
        column.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                value(getter.apply(cell.getValue()))));
        return column;
    }

    public static <T> void addColumn(TableView<T> table, String name, Function<T, String> getter) {
        table.getColumns().add(column(name, getter));
    }

    public static <T> TableColumn<T, T> actionColumn(String name, Function<T, Node> graphic) {
        TableColumn<T, T> column = new TableColumn<>(name);
        column.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue()));
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : graphic.apply(item));
            }
        });
        return column;
    }

    public static Optional<ButtonType> dialog(String title, Node content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().add(Ui.class.getResource("/com/nextit/css/application.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResizable(true);
        return dialog.showAndWait();
    }

    /** Read-only dialog with a single Close button. */
    public static void viewDialog(String title, Node content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().add(Ui.class.getResource("/com/nextit/css/application.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    public static boolean confirmed(String message) {
        return Alerts.confirm(message);
    }

    public static double parseDouble(String value, String fieldName) {
        try {
            double parsed = Double.parseDouble(value == null ? "" : value.trim());
            if (!Double.isFinite(parsed)) {
                throw new AppException(fieldName + " must be a finite number.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new AppException(fieldName + " must be a number.", e);
        }
    }

    public static int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new AppException(fieldName + " must be a whole number.", e);
        }
    }

    public static Double optionalDouble(String value, String fieldName) {
        if (value == null || value.isBlank()) return null;
        return parseDouble(value, fieldName);
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}