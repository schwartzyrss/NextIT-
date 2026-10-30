package com.nextit.service;

import com.nextit.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only diagnostics for the administrator's system configuration screen.
 * Connection failures are returned as a status instead of being thrown so the
 * screen remains usable when MySQL is unavailable.
 */
public class SystemService {

    private static final Logger log = LoggerFactory.getLogger(SystemService.class);

    private static final List<String> TABLES = List.of(
            "users", "students", "instructors", "employers", "subjects",
            "enrollments", "grades", "attendance", "technical_skills",
            "student_skills", "projects", "project_members", "project_evaluations",
            "achievements", "certifications", "portfolios", "portfolio_access",
            "academic_support", "job_opportunities", "applications");

    public record TableCount(String table, int rows) {}

    public record DatabaseStatus(String url, String user, boolean connected,
                                 String product, String version, String error,
                                 List<TableCount> tables) {}

    public DatabaseStatus status() {
        String url = safeUrl(DatabaseConfig.url());
        String user = DatabaseConfig.user();
        try (Connection connection = DatabaseConfig.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            String version = connection.getMetaData().getDatabaseProductVersion();
            List<TableCount> tables = new ArrayList<>();
            for (String table : TABLES) {
                tables.add(new TableCount(table, count(connection, table)));
            }
            return new DatabaseStatus(url, user, true, product, version, null, tables);
        } catch (SQLException e) {
            log.warn("Database diagnostics could not connect", e);
            return new DatabaseStatus(url, user, false, "Unknown", "Unknown",
                    "Could not connect to the configured database. Check that MySQL is running and that the credentials are correct.",
                    List.of());
        }
    }

    private int count(Connection connection, String table) {
        // Table names come from the fixed allowlist above, never from user input.
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.warn("Could not count rows in {}", table, e);
            return -1;
        }
    }

    private String safeUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("(?i)(password|pwd)=[^&;]*", "$1=***");
    }
}
