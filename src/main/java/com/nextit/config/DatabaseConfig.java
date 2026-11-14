package com.nextit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final String DEFAULT_URL =
            "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require";
    private static final String DEFAULT_USER = "postgres.qlmrcmaqkvtmciursqvr";
    private static final String DEFAULT_PASSWORD = "NextIT.2026lolxz";

    private static final Properties DOT_ENV = loadDotEnv();

    private DatabaseConfig() {}

    // Resolved on every call so tests can point the application at an embedded
    // database after the configuration class has already been loaded.
    public static String url() {
        return value("SUPABASE_DB_URL", value("NEXTIT_DB_URL", DEFAULT_URL));
    }

    public static String user() {
        return value("SUPABASE_DB_USER", value("NEXTIT_DB_USER", DEFAULT_USER));
    }

    public static String password() {
        return value("SUPABASE_DB_PASSWORD", value("NEXTIT_DB_PASSWORD", DEFAULT_PASSWORD));
    }

    public static Connection getConnection() throws SQLException {
        String dbUrl = url();
        try {
            if (dbUrl.startsWith("jdbc:postgresql:")) {
                try {
                    Class.forName("org.postgresql.Driver");
                } catch (ClassNotFoundException ignored) {}
            }
            return DriverManager.getConnection(dbUrl, user(), password());
        } catch (SQLException ex) {
            log.error("Database connection to {} failed: {}", dbUrl, ex.getMessage());
            if (EmbeddedDatabaseManager.shouldFallback(dbUrl)) {
                log.info("Falling back to local embedded database...");
                DOT_ENV.remove("SUPABASE_DB_URL");
                DOT_ENV.remove("SUPABASE_DB_USER");
                DOT_ENV.remove("SUPABASE_DB_PASSWORD");
                System.clearProperty("SUPABASE_DB_URL");
                System.clearProperty("SUPABASE_DB_USER");
                System.clearProperty("SUPABASE_DB_PASSWORD");
                EmbeddedDatabaseManager.ensureStarted();
                return DriverManager.getConnection(url(), user(), password());
            }
            throw ex;
        }
    }

    /**
     * Environment variables win, followed by JVM system properties, and finally
     * the optional {@code .env} file in the working directory. This lets the app
     * work both from an IDE and from a terminal without hard-coding credentials.
     */
    private static String value(String key, String fallback) {
        String environment = System.getenv(key);
        if (environment != null && !environment.isBlank()) return environment;
        String property = System.getProperty(key);
        if (property != null && !property.isBlank()) return property;
        String fileValue = DOT_ENV.getProperty(key);
        return fileValue == null || fileValue.isBlank() ? fallback : fileValue;
    }

    private static Properties loadDotEnv() {
        Properties properties = new Properties();
        Path path = Path.of(".env");
        if (!Files.isRegularFile(path)) {
            path = Path.of("app", ".env");
        }
        if (!Files.isRegularFile(path)) {
            path = Path.of(System.getProperty("user.dir"), ".env");
        }
        if (!Files.isRegularFile(path)) return properties;
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int separator = trimmed.indexOf('=');
                if (separator <= 0) continue;
                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                properties.setProperty(key, value);
            }
        } catch (IOException ignored) {
            // A malformed or unreadable .env should not prevent the app from
            // starting; the defaults and real environment variables still work.
        }
        return properties;
    }
}