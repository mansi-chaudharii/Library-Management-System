package com.library.config;

import com.library.util.DatabaseInitializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connectivity with MySQL using JDBC.
 * Includes graceful fallback to MockDataStore if MySQL server is offline.
 */
public class DatabaseConnection {
    private static Connection connection = null;
    private static boolean useMock = false;
    private static String lastError = null;

    static {
        try {
            // Load MySQL Connector/J driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver class not found: " + e.getMessage());
        }
        initConnection();
    }

    public static synchronized void initConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}

        connection = null;
        lastError = null;

        // First test if database exists or create it
        boolean initialized = DatabaseInitializer.initializeDatabase();
        if (initialized) {
            try {
                connection = DriverManager.getConnection(
                        AppConfig.getJdbcUrl(),
                        AppConfig.getDbUser(),
                        AppConfig.getDbPassword()
                );
                useMock = false;
                System.out.println("Connected to MySQL database successfully: " + AppConfig.getDbName());
                return;
            } catch (SQLException e) {
                lastError = e.getMessage();
                System.err.println("Failed to connect to MySQL database: " + e.getMessage());
            }
        } else {
            lastError = "Could not reach MySQL server on " + AppConfig.getDbHost() + ":" + AppConfig.getDbPort();
        }

        // Fallback to Demo / Mock mode
        useMock = true;
        System.out.println("Running in Demo / Mock Data mode (MySQL offline or unconfigured).");
    }

    public static synchronized Connection getConnection() {
        if (useMock) {
            return null;
        }
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = DriverManager.getConnection(
                        AppConfig.getJdbcUrl(),
                        AppConfig.getDbUser(),
                        AppConfig.getDbPassword()
                );
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            useMock = true;
            return null;
        }
        return connection;
    }

    public static boolean isUsingMock() {
        return useMock;
    }

    public static void setUseMock(boolean mock) {
        useMock = mock;
    }

    public static String getLastError() {
        return lastError;
    }

    /**
     * Tests a new connection with specified parameters without saving yet.
     */
    public static boolean testConnection(String host, String port, String dbName, String user, String password) throws SQLException {
        String testUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName 
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000";
        try (Connection conn = DriverManager.getConnection(testUrl, user, password)) {
            return conn.isValid(3);
        }
    }
}
