package com.nextit.config;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages an embedded MariaDB instance for NextIT when no local MySQL server is running.
 */
public final class EmbeddedDatabaseManager {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedDatabaseManager.class);

    private static final String DATABASE_NAME = "nextit";
    private static final String CONNECTION_PARAMETERS =
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

    private static DB embeddedDatabase;
    private static boolean started;

    private EmbeddedDatabaseManager() {}

    public static synchronized boolean isStarted() {
        return started;
    }

    public static void main(String[] args) throws Exception {
        ensureStarted();
        System.out.println("URL: " + DatabaseConfig.url());
        try (Connection c = DatabaseConfig.getConnection()) {
            System.out.println("Connected to: " + c.getMetaData().getDatabaseProductName() + " " + c.getMetaData().getDatabaseProductVersion());
        }
        stop();
    }

    public static boolean shouldFallback(String url) {
        if (started) return false;
        return true;
    }

    public static synchronized void ensureStarted() throws SQLException {
        if (started) return;

        LOG.info("No external MySQL server detected on port 3306. Starting embedded database...");
        try {
            DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
            builder.setPort(0);
            DBConfiguration configuration = builder.build();

            embeddedDatabase = DB.newEmbeddedDB(configuration);
            embeddedDatabase.start();

            String serverUrl = "jdbc:mysql://127.0.0.1:" + configuration.getPort() + "/";
            initializeSqlScript(serverUrl, "schema.sql");
            initializeSqlScript(serverUrl, "seed.sql");

            String fullUrl = serverUrl + DATABASE_NAME + CONNECTION_PARAMETERS;
            System.setProperty("NEXTIT_DB_URL", fullUrl);
            System.setProperty("NEXTIT_DB_USER", "root");
            System.setProperty("NEXTIT_DB_PASSWORD", "");

            Runtime.getRuntime().addShutdownHook(new Thread(EmbeddedDatabaseManager::stop, "nextit-embedded-db-shutdown"));
            started = true;
            LOG.info("Embedded database initialized successfully on port {} with demo seed data.", configuration.getPort());
        } catch (Exception e) {
            stop();
            throw new SQLException("Failed to start embedded database: " + e.getMessage(), e);
        }
    }

    public static synchronized void stop() {
        if (embeddedDatabase != null) {
            try {
                embeddedDatabase.stop();
            } catch (Exception e) {
                LOG.warn("Error stopping embedded database: {}", e.getMessage());
            } finally {
                embeddedDatabase = null;
                started = false;
            }
        }
    }

    private static void initializeSqlScript(String serverUrl, String fileName) throws SQLException {
        Path file = Path.of(System.getProperty("user.dir"), "database", fileName);
        if (!Files.isRegularFile(file)) {
            LOG.warn("Database script not found at {}", file.toAbsolutePath());
            return;
        }

        String script;
        try {
            script = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Unable to read " + file.toAbsolutePath(), e);
        }

        try (Connection connection = DriverManager.getConnection(serverUrl + CONNECTION_PARAMETERS, "root", "");
             Statement statement = connection.createStatement()) {
            for (String sql : splitStatements(script)) {
                try {
                    statement.execute(sql);
                } catch (SQLException ex) {
                    LOG.debug("Notice while executing statement in {}: {}", fileName, ex.getMessage());
                }
            }
        }
    }

    private static List<String> splitStatements(String script) {
        StringBuilder withoutComments = new StringBuilder(script.length());
        for (String line : script.split("\\R")) {
            if (line.stripLeading().startsWith("--")) continue;
            withoutComments.append(line).append('\n');
        }

        List<String> statements = new ArrayList<>();
        for (String candidate : withoutComments.toString().split(";")) {
            String sql = candidate.strip();
            if (!sql.isEmpty()) statements.add(sql);
        }
        return statements;
    }
}
