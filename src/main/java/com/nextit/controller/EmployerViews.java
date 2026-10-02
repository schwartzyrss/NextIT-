package com.nextit.controller;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.security.Session;
import com.nextit.service.CareerService;
import com.nextit.service.PortfolioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class EmployerViews {

    private static final CareerService career = new CareerService();
    private static final PortfolioService portfolio = new PortfolioService();

    private static int employerId() { return career.employerIdForUser(Session.currentUser().getUserId()); }

    public static Node dashboard() {
        VBox box = new VBox(16);
        box.setStyle("-fx-padding: 20;");
        Label title = new Label("Employer Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        HBox cards = new HBox(12);
        cards.getChildren().addAll(
                card("Opportunities", String.valueOf(career.opportunitiesForEmployer(employerId()).size())),
                card("Approved Portfolios", String.valueOf(portfolio.approvedPortfolios().size()))
        );
        box.getChildren().addAll(title, cards);
        return box;
    }

    public static Node opportunities() {
        int eid = employerId();
        TableView<JobOpportunity> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(career.opportunitiesForEmployer(eid)));
        addCol(tv, "Title", JobOpportunity::getTitle);
        addCol(tv, "Type", JobOpportunity::getOpportunityType);
        addCol(tv, "Location", JobOpportunity::getLocation);
        addCol(tv, "Status", JobOpportunity::getStatus);
        HBox actions = new HBox(8);
        Button add = new Button("Add Opportunity");
        add.setOnAction(e -> {
            TextField title = new TextField(); title.setPromptText("Title");
            TextField type = new TextField(); type.setPromptText("Type (Internship/Employment)");
            TextField desc = new TextField(); desc.setPromptText("Description");
            Dialog<JobOpportunity> d = new Dialog<>();
            d.setTitle("Add Opportunity");
            d.getDialogPane().setContent(new VBox(8, title, type, desc));
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            d.setResultConverter(b -> {
                if (b == ButtonType.OK) {
                    JobOpportunity o = new JobOpportunity();
                    o.setEmployerId(eid);
                    o.setTitle(title.getText());
                    o.setOpportunityType(type.getText());
                    o.setDescription(desc.getText());
                    o.setStatus("Open");
                    return o;
                }
                return null;
            });
            d.showAndWait().ifPresent(o -> { career.addOpportunity(o); tv.setItems(FXCollections.observableArrayList(career.opportunitiesForEmployer(eid))); });
        });
        Button close = new Button("Close");
        close.setOnAction(e -> {
            JobOpportunity o = tv.getSelectionModel().getSelectedItem();
            if (o != null) { o.setStatus("Closed"); career.updateOpportunity(o); tv.setItems(FXCollections.observableArrayList(career.opportunitiesForEmployer(eid))); }
        });
        actions.getChildren().addAll(add, close);
        VBox box = new VBox(10, actions, tv);
        VBox.setVgrow(tv, Priority.ALWAYS);
        return box;
    }

    public static Node applications() {
        int eid = employerId();
        return table(List.of("Student", "Opportunity", "Date", "Status", "Remarks"),
                (Application a) -> List.of(String.valueOf(a.getStudentId()), String.valueOf(a.getOpportunityId()),
                        String.valueOf(a.getApplicationDate()), a.getStatus(), a.getRemarks()),
                careerOpportunitiesApplications(eid));
    }

    private static List<Application> careerOpportunitiesApplications(int employerId) {
        return career.opportunitiesForEmployer(employerId).stream()
                .flatMap(o -> career.applicationsForOpportunity(o.getOpportunityId()).stream())
                .toList();
    }

    public static Node portfolios() {
        TableView<Portfolio> tv = new TableView<>();
        tv.setItems(FXCollections.observableArrayList(portfolio.approvedPortfolios()));
        addCol(tv, "Student", Portfolio::getStudentId);
        addCol(tv, "Headline", Portfolio::getHeadline);
        addCol(tv, "Visibility", Portfolio::getVisibility);
        TextField search = new TextField();
        search.setPromptText("Search headline...");
        search.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.toLowerCase();
            tv.setItems(FXCollections.observableArrayList(
                    portfolio.approvedPortfolios().stream()
                            .filter(p -> p.getHeadline() == null || p.getHeadline().toLowerCase().contains(q))
                            .toList()));
        });
        VBox box = new VBox(8, new HBox(8, new Label("Search:"), search), tv);
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
