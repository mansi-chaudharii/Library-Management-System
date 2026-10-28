package com.library.ui;

import com.library.config.AppConfig;
import com.library.config.DatabaseConnection;
import com.library.ui.theme.UITheme;
import com.library.ui.theme.UIUtils;
import com.library.util.DatabaseInitializer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog to configure MySQL connection parameters, test connectivity,
 * and initialize/seed the database schema.
 */
public class DatabaseConfigDialog extends JDialog {
    private final JTextField hostTxt;
    private final JTextField portTxt;
    private final JTextField dbNameTxt;
    private final JTextField userTxt;
    private final JPasswordField passTxt;
    private final JTextField fineTxt;
    private final JTextField daysTxt;
    private final JLabel statusMsgLbl;
    private final Runnable onConnectedCallback;

    public DatabaseConfigDialog(Frame parent, Runnable onConnectedCallback) {
        super(parent, "Database & System Configuration", true);
        this.onConnectedCallback = onConnectedCallback;

        setSize(520, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_LIGHT);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("⚙️ MySQL Database Settings");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        titleLbl.setForeground(UITheme.PRIMARY_DARK);

        JLabel subLbl = new JLabel("Configure your local MySQL server (XAMPP / MySQL Workbench / Docker)");
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(subLbl, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridLayout(8, 2, 10, 12));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 24, 18, 24));

        hostTxt = UIUtils.createTextField(15);
        hostTxt.setText(AppConfig.getDbHost());

        portTxt = UIUtils.createTextField(15);
        portTxt.setText(AppConfig.getDbPort());

        dbNameTxt = UIUtils.createTextField(15);
        dbNameTxt.setText(AppConfig.getDbName());

        userTxt = UIUtils.createTextField(15);
        userTxt.setText(AppConfig.getDbUser());

        passTxt = UIUtils.createPasswordField(15);
        passTxt.setText(AppConfig.getDbPassword());

        fineTxt = UIUtils.createTextField(15);
        fineTxt.setText(String.valueOf(AppConfig.getFinePerDay()));

        daysTxt = UIUtils.createTextField(15);
        daysTxt.setText(String.valueOf(AppConfig.getDefaultIssueDays()));

        form.add(createLabel("MySQL Host:"));
        form.add(hostTxt);
        form.add(createLabel("Port:"));
        form.add(portTxt);
        form.add(createLabel("Database Name:"));
        form.add(dbNameTxt);
        form.add(createLabel("Username:"));
        form.add(userTxt);
        form.add(createLabel("Password:"));
        form.add(passTxt);
        form.add(createLabel("Fine per Day ($):"));
        form.add(fineTxt);
        form.add(createLabel("Borrow Period (Days):"));
        form.add(daysTxt);

        statusMsgLbl = new JLabel(DatabaseConnection.isUsingMock()
                ? "Status: Currently running in Demo / Mock mode"
                : "Status: Live MySQL Connected (" + AppConfig.getDbName() + ")");
        statusMsgLbl.setFont(UITheme.FONT_SMALL_BOLD);
        statusMsgLbl.setForeground(DatabaseConnection.isUsingMock() ? UITheme.WARNING.darker() : UITheme.SUCCESS.darker());
        form.add(statusMsgLbl);
        form.add(new JLabel("")); // Spacer

        add(form, BorderLayout.CENTER);

        // Actions Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.BG_APP);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JButton testBtn = UIUtils.createSecondaryButton("🔌 Test Connection");
        testBtn.addActionListener(e -> testConnection());

        JButton initDbBtn = UIUtils.createSecondaryButton("🛠️ Init Schema");
        initDbBtn.addActionListener(e -> initDatabase());

        leftActions.add(testBtn);
        leftActions.add(initDbBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton cancelBtn = UIUtils.createSecondaryButton("Close");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = UIUtils.createPrimaryButton("💾 Save & Reconnect");
        saveBtn.addActionListener(e -> saveAndReconnect());

        rightActions.add(cancelBtn);
        rightActions.add(saveBtn);

        footer.add(leftActions, BorderLayout.WEST);
        footer.add(rightActions, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    private void testConnection() {
        String host = hostTxt.getText().trim();
        String port = portTxt.getText().trim();
        String db = dbNameTxt.getText().trim();
        String user = userTxt.getText().trim();
        String pass = new String(passTxt.getPassword());

        try {
            boolean ok = DatabaseConnection.testConnection(host, port, db, user, pass);
            if (ok) {
                statusMsgLbl.setText("✓ Connection successful!");
                statusMsgLbl.setForeground(UITheme.SUCCESS.darker());
                JOptionPane.showMessageDialog(this, "Successfully connected to MySQL database: " + db, "Connection Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                statusMsgLbl.setText("✗ Could not reach database.");
                statusMsgLbl.setForeground(UITheme.DANGER);
                JOptionPane.showMessageDialog(this, "Could not validate connection.", "Connection Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            statusMsgLbl.setText("✗ Error: " + ex.getMessage());
            statusMsgLbl.setForeground(UITheme.DANGER);
            JOptionPane.showMessageDialog(this,
                    "Connection failed: " + ex.getMessage() +
                    "\n\nTips:\n- Make sure MySQL/XAMPP service is running.\n- Verify host, port (default 3306), username, and password.\n- If database does not exist, click 'Init Schema' to create it automatically.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initDatabase() {
        saveSettingsToConfig();
        boolean ok = DatabaseInitializer.initializeDatabase();
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Database schema and sample records initialized successfully in MySQL!",
                    "Initialization Complete", JOptionPane.INFORMATION_MESSAGE);
            DatabaseConnection.initConnection();
            if (onConnectedCallback != null) onConnectedCallback.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Database initialization failed.\nPlease ensure MySQL server is running on " + AppConfig.getDbHost() + ":" + AppConfig.getDbPort(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSettingsToConfig() {
        AppConfig.setDbHost(hostTxt.getText().trim());
        AppConfig.setDbPort(portTxt.getText().trim());
        AppConfig.setDbName(dbNameTxt.getText().trim());
        AppConfig.setDbUser(userTxt.getText().trim());
        AppConfig.setDbPassword(new String(passTxt.getPassword()));

        try {
            AppConfig.setFinePerDay(Double.parseDouble(fineTxt.getText().trim()));
        } catch (NumberFormatException ignored) {}

        try {
            AppConfig.setDefaultIssueDays(Integer.parseInt(daysTxt.getText().trim()));
        } catch (NumberFormatException ignored) {}

        AppConfig.saveConfig();
    }

    private void saveAndReconnect() {
        saveSettingsToConfig();
        DatabaseConnection.initConnection();

        if (!DatabaseConnection.isUsingMock()) {
            JOptionPane.showMessageDialog(this,
                    "Connected to MySQL database: " + AppConfig.getDbName() + " successfully!",
                    "Connected", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to live MySQL server.\nContinuing in Demo/Mock Mode.\n\nError: " + DatabaseConnection.getLastError(),
                    "Demo Mode Active", JOptionPane.WARNING_MESSAGE);
        }

        if (onConnectedCallback != null) {
            onConnectedCallback.run();
        }
        dispose();
    }
}
