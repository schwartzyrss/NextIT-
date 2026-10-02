package com.nextit.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {

    private static final String URL =
            System.getenv().getOrDefault("NEXTIT_DB_URL", "jdbc:mysql://localhost:3306/nextit?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = System.getenv().getOrDefault("NEXTIT_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("NEXTIT_DB_PASSWORD", "");

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
