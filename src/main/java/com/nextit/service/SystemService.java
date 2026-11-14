package com.nextit.service;

import com.nextit.config.DatabaseConfig;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Real-time database health, diagnostics, and table count monitoring engine.
 * Runs non-blocking heartbeats on a background daemon executor, validates socket
 * reachability using {@code connection.isValid(2)}, and notifies UI listeners
 * automatically without freezing the JavaFX application thread.
 */
public class SystemService {

    private static final Logger log = LoggerFactory.getLogger(SystemService.class);

    public static final List<String> TABLES = List.of(
            "users", "students", "instructors", "employers", "subjects",
            "enrollments", "grades", "attendance", "technical_skills",
            "student_skills", "projects", "project_members", "project_evaluations",
            "achievements", "certifications", "portfolios", "portfolio_access",
            "academic_support", "job_opportunities", "applications");

    public record TableCount(String table, int rows) {}

    public record DatabaseStatus(String url, String user, boolean connected,
                                 String statusLabel, String details,
                                 String product, String version, String error,
                                 Instant lastCheckedAt,
                                 Map<String, Integer> rowCounts,
                                 List<TableCount> tables) {
        public DatabaseStatus {
            tables = List.copyOf(tables);
            rowCounts = Map.copyOf(rowCounts);
        }

        public DatabaseStatus(String url, String user, boolean connected,
                              String product, String version, String error,
                              List<TableCount> tables) {
            this(url, user, connected,
                 connected ? "Connected" : "Disconnected",
                 connected ? "NextIT can reach the configured database and read its schema."
                           : (error != null ? error : "Connection refused or timed out"),
                 product, version, error, Instant.now(), toMap(tables), tables);
        }

        private static Map<String, Integer> toMap(List<TableCount> tables) {
            Map<String, Integer> map = new LinkedHashMap<>();
            if (tables != null) {
                for (TableCount tc : tables) {
                    map.put(tc.table(), tc.rows());
                }
            }
            return map;
        }
    }

    private static final SystemService INSTANCE = new SystemService();

    public static SystemService getInstance() {
        return INSTANCE;
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "nextit-db-monitor");
        t.setDaemon(true);
        return t;
    });

    private final Set<Consumer<DatabaseStatus>> listeners = ConcurrentHashMap.newKeySet();
    private volatile DatabaseStatus latestStatus = null;
    private final AtomicBoolean isChecking = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private ScheduledFuture<?> nextTask = null;
    private static final long POLL_INTERVAL_MS = 3000;

    public SystemService() {}

    /**
     * Start the real-time non-blocking heartbeat loop.
     * Uses non-overlapping scheduling so ticks never stack if a timeout occurs.
     */
    public synchronized void start() {
        if (isRunning.compareAndSet(false, true)) {
            scheduleNextTick(0);
        }
    }

    private synchronized void scheduleNextTick(long delayMs) {
        if (!isRunning.get()) return;
        nextTask = scheduler.schedule(this::runHeartbeat, delayMs, TimeUnit.MILLISECONDS);
    }

    private void runHeartbeat() {
        if (!isRunning.get()) return;
        if (!isChecking.compareAndSet(false, true)) {
            return;
        }
        try {
            DatabaseStatus status = checkDatabase();
            notifyListeners(status);
        } catch (Throwable t) {
            log.error("Unexpected error in heartbeat", t);
        } finally {
            isChecking.set(false);
            if (isRunning.get()) {
                // Non-overlapping loop: wait for current check to fully finish before scheduling next 3s tick
                scheduleNextTick(POLL_INTERVAL_MS);
            }
        }
    }

    /**
     * Trigger an on-demand check immediately. Resets the 3-second auto-poll timer.
     */
    public CompletableFuture<DatabaseStatus> checkNow() {
        return CompletableFuture.supplyAsync(() -> {
            if (isChecking.compareAndSet(false, true)) {
                try {
                    DatabaseStatus status = checkDatabase();
                    notifyListeners(status);
                    return status;
                } finally {
                    isChecking.set(false);
                    synchronized (this) {
                        if (nextTask != null && !nextTask.isDone()) {
                            nextTask.cancel(false);
                        }
                        scheduleNextTick(POLL_INTERVAL_MS);
                    }
                }
            } else {
                return latestStatus != null ? latestStatus : checkDatabase();
            }
        }, scheduler);
    }

    public synchronized void stop() {
        isRunning.set(false);
        if (nextTask != null) {
            nextTask.cancel(false);
        }
    }

    public void addListener(Consumer<DatabaseStatus> listener) {
        listeners.add(listener);
        if (latestStatus != null) {
            try {
                Platform.runLater(() -> listener.accept(latestStatus));
            } catch (Exception ignored) {}
        }
    }

    public void removeListener(Consumer<DatabaseStatus> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(DatabaseStatus status) {
        for (Consumer<DatabaseStatus> listener : listeners) {
            try {
                Platform.runLater(() -> {
                    try {
                        listener.accept(status);
                    } catch (Exception ex) {
                        log.error("Error in diagnostics listener", ex);
                    }
                });
            } catch (Exception ignored) {}
        }
    }

    public DatabaseStatus status() {
        if (latestStatus != null) {
            return latestStatus;
        }
        return checkDatabase();
    }

    public DatabaseStatus getLatestStatus() {
        return latestStatus;
    }

    public DatabaseStatus checkDatabase() {
        String url = safeUrl(DatabaseConfig.url());
        String user = DatabaseConfig.user();
        Instant now = Instant.now();

        try (Connection connection = DatabaseConfig.getConnection()) {
            // Validate socket reachability using connection.isValid(2) (2-second timeout)
            if (!connection.isValid(2)) {
                DatabaseStatus offline = new DatabaseStatus(url, user, false,
                        "Disconnected",
                        "Connection reachability check timed out after 2 seconds. MySQL socket unresponsive.",
                        "Unknown", "Unknown", "Socket timeout or dropped connection.",
                        now, preserveLastCounts(), preserveLastTables());
                latestStatus = offline;
                return offline;
            }

            String product = connection.getMetaData().getDatabaseProductName();
            String version = connection.getMetaData().getDatabaseProductVersion();
            List<TableCount> tables = new ArrayList<>();
            Map<String, Integer> rowCounts = new LinkedHashMap<>();

            for (String table : TABLES) {
                int c = count(connection, table);
                tables.add(new TableCount(table, c));
                rowCounts.put(table, c);
            }

            DatabaseStatus online = new DatabaseStatus(url, user, true,
                    "Connected",
                    "NextIT can reach the configured database and read its schema.",
                    product, version, null,
                    now, rowCounts, tables);
            latestStatus = online;
            return online;
        } catch (SQLException | RuntimeException e) {
            log.warn("Database diagnostics could not connect: {}", e.getMessage());
            String errorMsg = e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage()
                    : "Connection refused or timed out";
            if (errorMsg.contains("Communications link failure")
                    || errorMsg.contains("Connection refused")
                    || errorMsg.contains("timed out")) {
                errorMsg = "Connection refused or timed out. Check that the database server is running.";
            }

            DatabaseStatus offline = new DatabaseStatus(url, user, false,
                    "Disconnected",
                    errorMsg,
                    "Unknown", "Unknown", errorMsg,
                    now, preserveLastCounts(), preserveLastTables());
            latestStatus = offline;
            return offline;
        }
    }

    private Map<String, Integer> preserveLastCounts() {
        if (latestStatus != null && latestStatus.rowCounts() != null && !latestStatus.rowCounts().isEmpty()) {
            return latestStatus.rowCounts();
        }
        Map<String, Integer> empty = new LinkedHashMap<>();
        for (String t : TABLES) {
            empty.put(t, 0);
        }
        return empty;
    }

    private List<TableCount> preserveLastTables() {
        if (latestStatus != null && latestStatus.tables() != null && !latestStatus.tables().isEmpty()) {
            return latestStatus.tables();
        }
        List<TableCount> list = new ArrayList<>();
        for (String t : TABLES) {
            list.add(new TableCount(t, 0));
        }
        return list;
    }

    private int count(Connection connection, String table) {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(2);
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            log.warn("Could not count rows in {}", table, e);
            if (latestStatus != null && latestStatus.rowCounts() != null && latestStatus.rowCounts().containsKey(table)) {
                return latestStatus.rowCounts().get(table);
            }
            return -1;
        }
    }

    private String safeUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("(?i)(password|pwd)=[^&;]*", "$1=***");
    }
}
