package com.nextit.util;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service to export any JavaFX TableView data to CSV or printable styled HTML report
 * (which users can view or Save as PDF directly in their browser).
 */
public final class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private ExportService() {}

    /**
     * Exports a TableView to a CSV file selected by the user.
     */
    public static boolean exportTableViewToCsv(TableView<?> tableView, String defaultFilename, String title, Window window) {
        if (tableView == null || tableView.getItems() == null || tableView.getItems().isEmpty()) {
            Alerts.warn("No data available to export in this table.");
            return false;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Data to CSV - " + title);
        chooser.setInitialFileName(defaultFilename != null && !defaultFilename.isBlank() ? defaultFilename : "export.csv");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Comma-Separated Values (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
        );

        File target = chooser.showSaveDialog(window);
        if (target == null) {
            return false; // User cancelled
        }

        try (PrintWriter writer = new PrintWriter(target, StandardCharsets.UTF_8)) {
            // Write UTF-8 BOM so Excel opens it with proper encoding
            writer.write('\ufeff');

            ObservableList<? extends TableColumn<?, ?>> columns = tableView.getVisibleLeafColumns();
            List<String> headerNames = new ArrayList<>();
            for (TableColumn<?, ?> col : columns) {
                String text = col.getText();
                if (text != null && !text.isBlank() && !text.equalsIgnoreCase("Actions")) {
                    headerNames.add(escapeCsv(text));
                }
            }
            writer.println(String.join(",", headerNames));

            // Write Rows
            for (int r = 0; r < tableView.getItems().size(); r++) {
                List<String> rowData = new ArrayList<>();
                for (TableColumn<?, ?> col : columns) {
                    String text = col.getText();
                    if (text != null && !text.isBlank() && !text.equalsIgnoreCase("Actions")) {
                        Object cellVal = col.getCellData(r);
                        String strVal = cellVal != null ? cellVal.toString() : "";
                        rowData.add(escapeCsv(strVal));
                    }
                }
                writer.println(String.join(",", rowData));
            }

            log.info("Successfully exported {} rows to {}", tableView.getItems().size(), target.getAbsolutePath());
            promptOpenFile(target, "CSV Export Successful", "Exported " + tableView.getItems().size() + " records to:\n" + target.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("Failed to export CSV to " + target.getAbsolutePath(), e);
            Alerts.error("Failed to export CSV: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exports a TableView to an attractive, printable HTML report that can be viewed
     * or saved as PDF via browser Print -> Save as PDF.
     */
    public static boolean exportTableViewToHtmlReport(TableView<?> tableView, String defaultFilename, String title, Window window) {
        if (tableView == null || tableView.getItems() == null || tableView.getItems().isEmpty()) {
            Alerts.warn("No data available to generate report.");
            return false;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Printable Report (PDF / HTML) - " + title);
        chooser.setInitialFileName(defaultFilename.replace(".csv", ".html"));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Printable HTML / PDF Report (*.html)", "*.html"),
                new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
        );

        File target = chooser.showSaveDialog(window);
        if (target == null) return false;

        try (PrintWriter writer = new PrintWriter(target, StandardCharsets.UTF_8)) {
            ObservableList<? extends TableColumn<?, ?>> columns = tableView.getVisibleLeafColumns();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='utf-8'>\n");
            sb.append("<title>").append(escapeHtml(title)).append(" - NextIT Report</title>\n");
            sb.append("<style>\n");
            sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #faf9f6; color: #2d2630; margin: 0; padding: 32px; }\n");
            sb.append(".header { background: linear-gradient(135deg, #FFCBE1 0%, #E8D7FF 100%); padding: 24px 32px; border-radius: 18px; margin-bottom: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }\n");
            sb.append(".header h1 { margin: 0 0 6px 0; font-size: 24px; color: #383139; }\n");
            sb.append(".header p { margin: 0; font-size: 13px; color: #5a505e; font-weight: 500; }\n");
            sb.append(".meta { margin-bottom: 16px; font-size: 12px; color: #7a7080; display: flex; justify-content: space-between; }\n");
            sb.append("table { width: 100%; border-collapse: collapse; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }\n");
            sb.append("th { background: #f4edf8; color: #443a47; font-weight: 700; text-align: left; padding: 12px 16px; font-size: 13px; border-bottom: 2px solid #e5d8ea; }\n");
            sb.append("td { padding: 12px 16px; border-bottom: 1px solid #f0e9f2; font-size: 13px; }\n");
            sb.append("tr:nth-child(even) td { background: #faf7fc; }\n");
            sb.append("tr:hover td { background: #f3ebf7; }\n");
            sb.append(".footer { margin-top: 24px; text-align: center; font-size: 11px; color: #998e9e; }\n");
            sb.append("@media print { body { padding: 0; background: #fff; } .header { box-shadow: none; } table { box-shadow: none; } }\n");
            sb.append("</style>\n</head>\n<body>\n");

            sb.append("<div class='header'>\n");
            sb.append("  <h1>⚡ NextIT Official Academic Report</h1>\n");
            sb.append("  <p>Track Your Progress, Build Your Future • ").append(escapeHtml(title)).append("</p>\n");
            sb.append("</div>\n");

            sb.append("<div class='meta'>\n");
            sb.append("  <span>Total Records: <strong>").append(tableView.getItems().size()).append("</strong></span>\n");
            sb.append("  <span>Generated on: <strong>").append(timestamp).append("</strong></span>\n");
            sb.append("</div>\n");

            sb.append("<table>\n<thead>\n<tr>\n");
            for (TableColumn<?, ?> col : columns) {
                String text = col.getText();
                if (text != null && !text.isBlank() && !text.equalsIgnoreCase("Actions")) {
                    sb.append("  <th>").append(escapeHtml(text)).append("</th>\n");
                }
            }
            sb.append("</tr>\n</thead>\n<tbody>\n");

            for (int r = 0; r < tableView.getItems().size(); r++) {
                sb.append("<tr>\n");
                for (TableColumn<?, ?> col : columns) {
                    String text = col.getText();
                    if (text != null && !text.isBlank() && !text.equalsIgnoreCase("Actions")) {
                        Object cellVal = col.getCellData(r);
                        String strVal = cellVal != null ? cellVal.toString() : "";
                        sb.append("  <td>").append(escapeHtml(strVal)).append("</td>\n");
                    }
                }
                sb.append("</tr>\n");
            }

            sb.append("</tbody>\n</table>\n");
            sb.append("<div class='footer'>\n");
            sb.append("  <p>NextIT Educational Platform • Tip: Press <strong>Ctrl + P</strong> (or Cmd + P) in your browser to print or save this report as a PDF.</p>\n");
            sb.append("</div>\n");
            sb.append("</body>\n</html>\n");

            writer.write(sb.toString());
            log.info("Successfully exported HTML report to {}", target.getAbsolutePath());
            promptOpenFile(target, "Report Generated Successfully", "Printable report generated with " + tableView.getItems().size() + " records.\n\nTip: You can press Ctrl+P in your browser to save it directly as a PDF!");
            return true;
        } catch (IOException e) {
            log.error("Failed to export HTML report to " + target.getAbsolutePath(), e);
            Alerts.error("Failed to export report: " + e.getMessage());
            return false;
        }
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static void promptOpenFile(File file, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        ButtonType openButton = new ButtonType("Open File", ButtonBar.ButtonData.YES);
        ButtonType closeButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(openButton, closeButton);

        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isPresent() && choice.get() == openButton) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    log.warn("Could not open file automatically: {}", ex.getMessage());
                }
            }
        }
    }
}

