package com.nextit;

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
import java.util.Properties;

/**
 * Supplies a MySQL-compatible database for the schema integration tests.
 *
 * <p>When {@code NEXTIT_TEST_DB_URL} is set, that database is used as-is (the schema is expected to
 * be loaded already). Otherwise an embedded MariaDB instance is unpacked and started on a free
 * port, and {@code database/schema.sql} is applied before the tests run.</p>
 */
public final class DatabaseTestServer {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseTestServer.class);

    private static final String ENV_URL = "NEXTIT_TEST_DB_URL";
    private static final String ENV_USER = "NEXTIT_TEST_DB_USER";
    private static final String ENV_PASSWORD = "NEXTIT_TEST_DB_PASSWORD";
    private static final String DATABASE_NAME = "nextit";
    private static final String CONNECTION_PARAMETERS =
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

    private static DatabaseTestServer instance;
    private static boolean shutdownHookRegistered;

    private final String url;
    private final String user;
    private final String password;
    private final DB embeddedDatabase;

    private DatabaseTestServer(String url, String user, String password, DB embeddedDatabase) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.embeddedDatabase = embeddedDatabase;
    }

    public static synchronized DatabaseTestServer getInstance() throws SQLException {
        if (instance == null) {
            instance = create();
            registerShutdownHook();
        }
        return instance;
    }

    /**
     * Points the production {@code DatabaseConfig} at this test database. The
     * configuration class resolves system properties on every call, so this is
     * safe even after application classes have been loaded.
     */
    public DatabaseTestServer useForApplication() {
        System.setProperty("NEXTIT_DB_URL", url);
        System.setProperty("NEXTIT_DB_USER", user == null ? "" : user);
        System.setProperty("NEXTIT_DB_PASSWORD", password == null ? "" : password);
        return this;
    }

    public String url() { return url; }
    public String user() { return user; }
    public String password() { return password; }

    public static synchronized void shutdown() {
        if (instance == null || instance.embeddedDatabase == null) {
            instance = null;
            return;
        }
        try {
            instance.embeddedDatabase.stop();
        } catch (Exception e) {
            LOG.warn("Unable to stop the embedded test database", e);
        } finally {
            instance = null;
        }
    }

    private static void registerShutdownHook() {
        if (shutdownHookRegistered) return;
        shutdownHookRegistered = true;
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseTestServer::shutdown, "nextit-test-db-shutdown"));
    }

    public Connection openConnection() throws SQLException {
        Properties properties = new Properties();
        if (user != null && !user.isBlank()) properties.setProperty("user", user);
        if (password != null && !password.isBlank()) properties.setProperty("password", password);
        return DriverManager.getConnection(url, properties);
    }

    private static DatabaseTestServer create() throws SQLException {
        String configuredUrl = System.getenv(ENV_URL);
        if (configuredUrl != null && configuredUrl.startsWith("jdbc:mysql:")) {
            LOG.info("Using externally configured test database at {}", configuredUrl);
            return new DatabaseTestServer(configuredUrl, System.getenv(ENV_USER),
                    System.getenv(ENV_PASSWORD), null);
        }
        return startEmbeddedDatabase();
    }

    private static DatabaseTestServer startEmbeddedDatabase() throws SQLException {
        LOG.info("Starting an embedded MariaDB instance for schema integration tests");
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(0);
        DBConfiguration configuration = builder.build();

        DB database;
        try {
            database = DB.newEmbeddedDB(configuration);
            database.start();
        } catch (Exception e) {
            throw new SQLException("Unable to start the embedded test database", e);
        }

        String serverUrl = "jdbc:mysql://127.0.0.1:" + configuration.getPort() + "/";
        try {
            initializeSchema(serverUrl);
        } catch (SQLException e) {
            try {
                database.stop();
            } catch (Exception stopFailure) {
                e.addSuppressed(stopFailure);
            }
            throw e;
        }
        return new DatabaseTestServer(serverUrl + DATABASE_NAME + CONNECTION_PARAMETERS,
                "root", "", database);
    }

    private static void initializeSchema(String serverUrl) throws SQLException {
        Path schemaFile = Path.of(System.getProperty("user.dir"), "database", "schema.sql");
        if (!Files.isRegularFile(schemaFile)) {
            throw new SQLException("Schema file not found at " + schemaFile.toAbsolutePath());
        }

        String script;
        try {
            script = Files.readString(schemaFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Unable to read " + schemaFile.toAbsolutePath(), e);
        }

        try (Connection connection = DriverManager.getConnection(serverUrl + CONNECTION_PARAMETERS,
                "root", "");
             Statement statement = connection.createStatement()) {
            for (String sql : splitStatements(script)) {
                statement.execute(sql);
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
