package com.nextit.controller;

import com.nextit.model.Attendance;
import com.nextit.model.Grade;
import com.nextit.util.Ui;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Builds the small set of dashboard charts that add real value: attendance
 * composition, grade trends over time, and average performance by activity
 * type. Every chart degrades to a friendly message when there is no data.
 */
final class DashboardCharts {

    private static final List<String> ATTENDANCE_STATUSES =
            List.of("Present", "Late", "Excused", "Absent");

    private DashboardCharts() {}

    static Node attendanceSummary(List<Attendance> records) {
        if (records.isEmpty()) {
            return placeholder("Attendance Summary", "No attendance records yet.");
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String status : ATTENDANCE_STATUSES) {
            long count = records.stream()
                    .filter(a -> status.equalsIgnoreCase(a.getStatus()))
                    .count();
            if (count > 0) counts.put(status, count);
        }
        if (counts.isEmpty()) {
            return placeholder("Attendance Summary", "No attendance records yet.");
        }

        PieChart chart = new PieChart();
        chart.setLegendSide(javafx.geometry.Side.BOTTOM);
        chart.setLabelsVisible(true);
        counts.forEach((status, count) ->
                chart.getData().add(new PieChart.Data(status + " (" + count + ")", count)));
        return chartCard("Attendance Summary", chart);
    }

    static Node gradeTrend(List<Grade> grades) {
        Map<LocalDate, double[]> perDay = new TreeMap<>();
        for (Grade grade : grades) {
            LocalDateTime recordedAt = grade.getRecordedAt();
            Double value = grade.getGrade();
            if (recordedAt == null || value == null) continue;
            LocalDate day = recordedAt.toLocalDate();
            double[] totals = perDay.computeIfAbsent(day, ignored -> new double[2]);
            totals[0] += value;
            totals[1] += 1;
        }
        if (perDay.isEmpty()) {
            return placeholder("Grade Trend", "No graded activities yet.");
        }

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date recorded");
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Grade (%)");
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        var series = new LineChart.Series<String, Number>();
        series.setName("Average grade");
        perDay.forEach((day, totals) -> series.getData().add(
                new LineChart.Data<>(day.toString(), Math.round(totals[0] / totals[1] * 100.0) / 100.0)));
        chart.getData().add(series);
        return chartCard("Grade Trend", chart);
    }

    static Node averageByActivityType(List<Grade> grades) {
        Map<String, double[]> perType = new TreeMap<>();
        for (Grade grade : grades) {
            Double value = grade.getGrade();
            if (value == null) continue;
            String type = grade.getActivityType() == null || grade.getActivityType().isBlank()
                    ? "Unspecified" : grade.getActivityType();
            double[] totals = perType.computeIfAbsent(type, ignored -> new double[2]);
            totals[0] += value;
            totals[1] += 1;
        }
        if (perType.isEmpty()) {
            return placeholder("Average Grade by Activity Type", "No graded activities yet.");
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Average grade (%)");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        var series = new BarChart.Series<String, Number>();
        series.setName("Average grade");
        perType.forEach((type, totals) -> series.getData().add(
                new BarChart.Data<>(type, Math.round(totals[0] / totals[1] * 100.0) / 100.0)));
        chart.getData().add(series);
        return chartCard("Average Grade by Activity Type", chart);
    }

    static Node statusSummary(String title, Map<String, Long> statuses) {
        if (statuses.isEmpty()) {
            return placeholder(title, "No records yet.");
        }
        PieChart chart = new PieChart();
        chart.setLegendSide(javafx.geometry.Side.BOTTOM);
        statuses.forEach((status, count) ->
                chart.getData().add(new PieChart.Data(status + " (" + count + ")", count)));
        return chartCard(title, chart);
    }

    /** Case-insensitive grouping helper shared by the dashboards. */
    static Map<String, Long> countByStatus(List<String> statuses, List<String> known) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String status : known) {
            long count = statuses.stream().filter(status::equalsIgnoreCase).count();
            if (count > 0) counts.put(status, count);
        }
        statuses.stream()
                .filter(s -> s != null && known.stream().noneMatch(k -> k.equalsIgnoreCase(s)))
                .collect(Collectors.groupingBy(s -> s.toLowerCase(Locale.ROOT), Collectors.counting()))
                .forEach((key, count) -> counts.put(key, count));
        return counts;
    }

    static HBox row(Node... charts) {
        HBox row = new HBox(12, charts);
        row.setAlignment(Pos.TOP_LEFT);
        for (Node chart : charts) {
            HBox.setHgrow(chart, Priority.ALWAYS);
        }
        return row;
    }

    private static Node chartCard(String title, Node chart) {
        VBox box = new VBox(8, Ui.section(title), chart);
        box.getStyleClass().add("chart-card");
        box.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(chart, Priority.ALWAYS);
        if (chart instanceof javafx.scene.chart.Chart fxChart) {
            fxChart.setPrefHeight(240);
            fxChart.setMinHeight(220);
            fxChart.setAnimated(false);
        }
        return box;
    }

    private static Node placeholder(String title, String message) {
        Label label = new Label(message);
        label.getStyleClass().add("empty-state");
        VBox box = new VBox(8, Ui.section(title), label);
        box.getStyleClass().add("chart-card");
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinHeight(120);
        return box;
    }
}
