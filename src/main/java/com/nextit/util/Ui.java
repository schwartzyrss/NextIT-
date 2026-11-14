package com.nextit.util;

import com.nextit.exception.AppException;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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

    /**
     * Builds a vector circular status dot with a subtle pulsing halo.
     * Prevents missing-glyph boxes across all platforms and display DPIs.
     */
    public static StackPane statusDot(boolean active) {
        StackPane dotBox = new StackPane();
        dotBox.setAlignment(Pos.CENTER);
        dotBox.setPrefSize(16, 16);
        dotBox.setMinSize(16, 16);
        dotBox.setMaxSize(16, 16);

        Circle halo = new Circle(6);
        Circle core = new Circle(4);

        dotBox.getChildren().addAll(halo, core);
        dotBox.getProperties().put("halo", halo);
        dotBox.getProperties().put("core", core);

        applyStatusDotState(dotBox, active);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(1400), halo);
        pulse.setFromX(0.85);
        pulse.setFromY(0.85);
        pulse.setToX(1.35);
        pulse.setToY(1.35);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
        dotBox.getProperties().put("pulse", pulse);

        return dotBox;
    }

    public static void setStatusDot(Node node, boolean active) {
        if (node instanceof StackPane dotBox) {
            applyStatusDotState(dotBox, active);
        }
    }

    private static void applyStatusDotState(StackPane dotBox, boolean active) {
        Circle halo = (Circle) dotBox.getProperties().get("halo");
        Circle core = (Circle) dotBox.getProperties().get("core");
        if (halo != null && core != null) {
            Color primaryColor = active ? Color.web("#10B981") : Color.web("#EF4444");
            Color haloColor = active ? Color.web("#10B981", 0.28) : Color.web("#EF4444", 0.28);
            core.setFill(primaryColor);
            halo.setFill(haloColor);
        }
    }

    public static final String SYNC_ICON_PATH =
            "M 3 12 a 9 9 0 0 1 9 -9 a 9.75 9.75 0 0 1 6.74 2.74 L 21 8 M 21 3 v 5 h -5 M 21 12 a 9 9 0 0 1 -9 9 a 9.75 9.75 0 0 1 -6.74 -2.74 L 3 16 M 3 21 v -5 h 5";

    public static StackPane refreshIcon() {
        SVGPath svg = new SVGPath();
        svg.setContent(SYNC_ICON_PATH);
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web("#1E293B"));
        svg.setStrokeWidth(1.8);
        svg.setStrokeLineCap(StrokeLineCap.ROUND);
        svg.setStrokeLineJoin(StrokeLineJoin.ROUND);

        svg.setScaleX(16.0 / 24.0);
        svg.setScaleY(16.0 / 24.0);

        StackPane box = new StackPane(svg);
        box.setPrefSize(16, 16);
        box.setMinSize(16, 16);
        box.setMaxSize(16, 16);
        box.setAlignment(Pos.CENTER);
        box.getProperties().put("svg", svg);
        return box;
    }

    /**
     * Controls the state, hover micro-interactions, continuous spin, and completion
     * of the vector refresh icon on a pill button.
     */
    public static class RefreshIconController {
        private final StackPane iconBox;
        private final SVGPath svg;
        private final Button button;
        private final AtomicBoolean fetching = new AtomicBoolean(false);
        private RotateTransition continuousSpin;

        public RefreshIconController(Button button, StackPane iconBox) {
            this.button = button;
            this.iconBox = iconBox;
            this.svg = (SVGPath) iconBox.getProperties().get("svg");
            setupInteractions();
        }

        private void setupInteractions() {
            button.hoverProperty().addListener((obs, old, hovered) -> {
                if (svg != null) {
                    svg.setStroke(hovered ? Color.web("#4F46E5") : Color.web("#1E293B"));
                }
                if (!fetching.get()) {
                    RotateTransition hoverTrans = new RotateTransition(Duration.millis(hovered ? 280 : 200), iconBox);
                    hoverTrans.setToAngle(hovered ? 45 : 0);
                    hoverTrans.setInterpolator(hovered ? Interpolator.EASE_OUT : Interpolator.EASE_BOTH);
                    hoverTrans.play();
                }
            });
        }

        public void startSpin() {
            fetching.set(true);
            if (continuousSpin != null) {
                continuousSpin.stop();
            }
            continuousSpin = new RotateTransition(Duration.millis(900), iconBox);
            continuousSpin.setByAngle(360);
            continuousSpin.setInterpolator(Interpolator.LINEAR);
            continuousSpin.setCycleCount(Animation.INDEFINITE);
            continuousSpin.play();
        }

        public void stopSpin() {
            fetching.set(false);
            if (continuousSpin != null) {
                continuousSpin.stop();
                continuousSpin = null;
            }
            // Complete current rotation smoothly back to 0deg
            double cur = (iconBox.getRotate() % 360 + 360) % 360;
            double remaining = 360 - cur;
            if (remaining < 5) remaining += 360;
            RotateTransition finish = new RotateTransition(Duration.millis(Math.max(150, remaining * 1.5)), iconBox);
            finish.setToAngle(iconBox.getRotate() + remaining);
            finish.setInterpolator(Interpolator.EASE_OUT);
            finish.setOnFinished(e -> {
                iconBox.setRotate(button.isHover() ? 45 : 0);
            });
            finish.play();
        }

        public void pulseOnce() {
            if (!fetching.get()) {
                RotateTransition pulse = new RotateTransition(Duration.millis(700), iconBox);
                pulse.setFromAngle(iconBox.getRotate());
                pulse.setToAngle(iconBox.getRotate() + 360);
                pulse.setInterpolator(Interpolator.EASE_BOTH);
                pulse.setOnFinished(e -> {
                    iconBox.setRotate(button.isHover() ? 45 : 0);
                });
                pulse.play();
            }
        }
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
        return card(label, value, CLAY_VARIANTS[Math.abs(cardCounter++) % CLAY_VARIANTS.length]);
    }

    public static VBox card(String label, String value, String variant) {
        VBox box = new VBox(6);
        box.getStyleClass().add("card");
        if (variant != null && !variant.isBlank()) {
            box.getStyleClass().add(variant);
        } else {
            box.getStyleClass().add(CLAY_VARIANTS[Math.abs(cardCounter++) % CLAY_VARIANTS.length]);
        }
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