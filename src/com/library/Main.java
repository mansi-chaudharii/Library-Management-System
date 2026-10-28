package com.library;

import com.library.config.DatabaseConnection;
import com.library.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Main application entry point for the Library Management System.
 * Initializes modern Look and Feel, database connection, and launches LoginFrame.
 *
 * Designed for 2nd Year Computer Engineering Student Project.
 */
public class Main {
    public static void main(String[] args) {
        // Set rendering hints for smooth text
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Try setting FlatLaf Light Look & Feel for modern aesthetics
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize Database Connection in background / initial setup
        DatabaseConnection.getConnection();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
