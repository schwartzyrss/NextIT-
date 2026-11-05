package com.nextit.util;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public final class Alerts {

    private Alerts() {}

    private static void style(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(Alerts.class.getResource("/com/nextit/css/application.css").toExternalForm());
        pane.getStyleClass().add("clay-dialog-pane");
    }

    public static void error(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(message);
        style(a);
        a.showAndWait();
    }

    public static void info(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("NextIT");
        a.setHeaderText(null);
        a.setContentText(message);
        style(a);
        a.showAndWait();
    }

    public static boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm");
        a.setHeaderText(null);
        a.setContentText(message);
        style(a);
        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && (result.get() == ButtonType.OK || result.get() == ButtonType.YES);
    }

    public static boolean confirmLogout() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Logout");
        dialog.setHeaderText(null);

        VBox content = new VBox(16);
        content.getStyleClass().add("logout-dialog-content");
        content.setPrefWidth(380);

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("logout-header-cushion");

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("logout-icon-pane");
        Label icon = new Label("🚪");
        icon.getStyleClass().add("logout-icon");
        iconPane.getChildren().add(icon);

        VBox textBox = new VBox(3);
        Label title = new Label("Log Out of NextIT?");
        title.getStyleClass().add("logout-title");
        Label subtitle = new Label("End your current session");
        subtitle.getStyleClass().add("logout-subtitle");
        textBox.getChildren().addAll(title, subtitle);

        header.getChildren().addAll(iconPane, textBox);

        Label message = new Label("Are you sure you want to log out? You will be returned to the login screen and any unsaved changes will be lost.");
        message.setWrapText(true);
        message.getStyleClass().add("logout-message");

        content.getChildren().addAll(header, message);

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(content);
        style(dialog);

        ButtonType logoutButtonType = new ButtonType("Logout", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(logoutButtonType, cancelButtonType);

        Button logoutBtn = (Button) pane.lookupButton(logoutButtonType);
        if (logoutBtn != null) {
            logoutBtn.setText("Logout");
            logoutBtn.setMinWidth(110);
            ButtonBar.setButtonUniformSize(logoutBtn, false);
            logoutBtn.getStyleClass().addAll("dialog-btn", "logout-confirm-btn");
        }
        Button cancelBtn = (Button) pane.lookupButton(cancelButtonType);
        if (cancelBtn != null) {
            cancelBtn.setText("Cancel");
            cancelBtn.setMinWidth(100);
            ButtonBar.setButtonUniformSize(cancelBtn, false);
            cancelBtn.getStyleClass().addAll("dialog-btn", "logout-cancel-btn");
        }

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == logoutButtonType;
    }
}
