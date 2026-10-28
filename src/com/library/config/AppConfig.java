package com.library.config;

import java.io.*;
import java.util.Properties;

/**
 * Manages application configuration and database credentials.
 * Persists settings to a local db.properties file.
 */
public class AppConfig {
    private static final String CONFIG_FILE = "db.properties";
    private static final Properties props = new Properties();

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("Warning: Could not read db.properties, using defaults. " + e.getMessage());
            }
        } else {
            // Apply sensible defaults and save
            setDefaults();
            saveConfig();
        }
    }

    private static void setDefaults() {
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "3306");
        props.setProperty("db.name", "library_db");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", ""); // Default for XAMPP / local development
        props.setProperty("library.fine_per_day", "5.0"); // Fine in currency units per day
        props.setProperty("library.default_issue_days", "14"); // Standard 14-day borrowing period
    }

    public static synchronized void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "Library Management System - Database Configuration");
        } catch (IOException e) {
            System.err.println("Warning: Could not save db.properties: " + e.getMessage());
        }
    }

    public static String getDbHost() {
        return props.getProperty("db.host", "localhost");
    }

    public static void setDbHost(String host) {
        props.setProperty("db.host", host);
    }

    public static String getDbPort() {
        return props.getProperty("db.port", "3306");
    }

    public static void setDbPort(String port) {
        props.setProperty("db.port", port);
    }

    public static String getDbName() {
        return props.getProperty("db.name", "library_db");
    }

    public static void setDbName(String name) {
        props.setProperty("db.name", name);
    }

    public static String getDbUser() {
        return props.getProperty("db.user", "root");
    }

    public static void setDbUser(String user) {
        props.setProperty("db.user", user);
    }

    public static String getDbPassword() {
        return props.getProperty("db.password", "");
    }

    public static void setDbPassword(String password) {
        props.setProperty("db.password", password);
    }

    public static double getFinePerDay() {
        try {
            return Double.parseDouble(props.getProperty("library.fine_per_day", "5.0"));
        } catch (NumberFormatException e) {
            return 5.0;
        }
    }

    public static void setFinePerDay(double fine) {
        props.setProperty("library.fine_per_day", String.valueOf(fine));
    }

    public static int getDefaultIssueDays() {
        try {
            return Integer.parseInt(props.getProperty("library.default_issue_days", "14"));
        } catch (NumberFormatException e) {
            return 14;
        }
    }

    public static void setDefaultIssueDays(int days) {
        props.setProperty("library.default_issue_days", String.valueOf(days));
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + getDbHost() + ":" + getDbPort() + "/" + getDbName() 
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    }

    public static String getServerJdbcUrl() {
        return "jdbc:mysql://" + getDbHost() + ":" + getDbPort() 
                + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}
