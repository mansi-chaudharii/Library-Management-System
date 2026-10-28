package com.library.ui;

import com.library.config.DatabaseConnection;
import com.library.dao.UserDao;
import com.library.model.User;
import com.library.ui.theme.UITheme;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern login window featuring the light-blue and white theme,
 * database connectivity status badge, and quick credentials helper.
 */
public class LoginFrame extends JFrame {
    private final UserDao userDao = new UserDao();

    private JTextField usernameTxt;
    private JPasswordField passwordTxt;
    private JLabel statusBadgeLbl;

    public LoginFrame() {
        setTitle("Library Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Color.WHITE);

        // --- Left Banner Panel (Light-Blue Branding) ---
        JPanel leftBanner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Subtle light blue gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(224, 242, 254),
                        0, getHeight(), new Color(186, 230, 253)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBorder(new EmptyBorder(45, 40, 45, 40));

        JLabel iconLbl = new JLabel("📖", SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandTitle = new JLabel("LibMaster");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brandTitle.setForeground(UITheme.PRIMARY_DARK);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSubtitle = new JLabel("Library Management System");
        brandSubtitle.setFont(UITheme.FONT_SUBTITLE);
        brandSubtitle.setForeground(UITheme.PRIMARY);
        brandSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descText = new JLabel("<html><center style='color:#334155; line-height:1.5; font-size:12px;'>" +
                "Engineered for academic libraries, book cataloging, circulation tracking, and member management." +
                "<br><br><b>College Project 2026</b><br>Computer Engineering Department" +
                "</center></html>");
        descText.setFont(UITheme.FONT_REGULAR);
        descText.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftBanner.add(Box.createVerticalGlue());
        leftBanner.add(iconLbl);
        leftBanner.add(Box.createVerticalStrut(10));
        leftBanner.add(brandTitle);
        leftBanner.add(Box.createVerticalStrut(4));
        leftBanner.add(brandSubtitle);
        leftBanner.add(Box.createVerticalStrut(20));
        leftBanner.add(descText);
        leftBanner.add(Box.createVerticalGlue());

        root.add(leftBanner);

        // --- Right Login Form Panel ---
        JPanel rightForm = new JPanel();
        rightForm.setBackground(Color.WHITE);
        rightForm.setLayout(new BoxLayout(rightForm, BoxLayout.Y_AXIS));
        rightForm.setBorder(new EmptyBorder(40, 45, 30, 45));

        JLabel welcomeLbl = new JLabel("Welcome Back!");
        welcomeLbl.setFont(UITheme.FONT_TITLE);
        welcomeLbl.setForeground(UITheme.TEXT_MAIN);

        JLabel promptLbl = new JLabel("Sign in with your administrative credentials");
        promptLbl.setFont(UITheme.FONT_REGULAR);
        promptLbl.setForeground(UITheme.TEXT_MUTED);

        rightForm.add(welcomeLbl);
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(promptLbl);
        rightForm.add(Box.createVerticalStrut(24));

        // Username
        JLabel uLbl = new JLabel("Username:");
        uLbl.setFont(UITheme.FONT_BOLD);
        uLbl.setForeground(UITheme.TEXT_MAIN);
        usernameTxt = UIUtils.createTextField(15);
        usernameTxt.setText("admin");
        usernameTxt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        rightForm.add(uLbl);
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(usernameTxt);
        rightForm.add(Box.createVerticalStrut(12));

        // Password
        JLabel pLbl = new JLabel("Password:");
        pLbl.setFont(UITheme.FONT_BOLD);
        pLbl.setForeground(UITheme.TEXT_MAIN);
        passwordTxt = UIUtils.createPasswordField(15);
        passwordTxt.setText("admin123");
        passwordTxt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Trigger login on ENTER key inside password field
        passwordTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });

        rightForm.add(pLbl);
        rightForm.add(Box.createVerticalStrut(4));
        rightForm.add(passwordTxt);

        // Show password checkbox
        JCheckBox showPassCheck = new JCheckBox("Show Password");
        showPassCheck.setFont(UITheme.FONT_SMALL);
        showPassCheck.setForeground(UITheme.TEXT_MUTED);
        showPassCheck.setOpaque(false);
        showPassCheck.addActionListener(e -> {
            passwordTxt.setEchoChar(showPassCheck.isSelected() ? (char) 0 : '•');
        });
        rightForm.add(showPassCheck);
        rightForm.add(Box.createVerticalStrut(18));

        // Buttons: Sign In + Quick Demo Login
        JButton signInBtn = UIUtils.createPrimaryButton("Sign In");
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        signInBtn.addActionListener(e -> doLogin());
        rightForm.add(signInBtn);
        rightForm.add(Box.createVerticalStrut(8));

        JButton quickLoginBtn = UIUtils.createSecondaryButton("Quick Admin Demo Access");
        quickLoginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        quickLoginBtn.addActionListener(e -> {
            usernameTxt.setText("admin");
            passwordTxt.setText("admin123");
            doLogin();
        });
        rightForm.add(quickLoginBtn);
        rightForm.add(Box.createVerticalGlue());

        // Status & Settings Footer Bar
        JPanel bottomStatus = new JPanel(new BorderLayout());
        bottomStatus.setOpaque(false);

        statusBadgeLbl = new JLabel();
        updateStatusBadge();

        JButton configBtn = new JButton("⚙️ DB Settings");
        configBtn.setFont(UITheme.FONT_SMALL_BOLD);
        configBtn.setForeground(UITheme.PRIMARY);
        configBtn.setContentAreaFilled(false);
        configBtn.setBorderPainted(false);
        configBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        configBtn.addActionListener(e -> {
            new DatabaseConfigDialog(this, this::updateStatusBadge).setVisible(true);
        });

        bottomStatus.add(statusBadgeLbl, BorderLayout.WEST);
        bottomStatus.add(configBtn, BorderLayout.EAST);
        rightForm.add(bottomStatus);

        root.add(rightForm);
        setContentPane(root);
    }

    private void updateStatusBadge() {
        if (DatabaseConnection.isUsingMock()) {
            statusBadgeLbl.setText("🟡 Mode: Demo / Mock Data");
            statusBadgeLbl.setToolTipText("MySQL not detected. Application is fully functional in mock mode.");
            statusBadgeLbl.setForeground(UITheme.WARNING.darker());
        } else {
            statusBadgeLbl.setText("🟢 Mode: Live MySQL DB");
            statusBadgeLbl.setToolTipText("Connected to MySQL server.");
            statusBadgeLbl.setForeground(UITheme.SUCCESS.darker());
        }
        statusBadgeLbl.setFont(UITheme.FONT_SMALL_BOLD);
    }

    private void doLogin() {
        String username = usernameTxt.getText().trim();
        String password = new String(passwordTxt.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDao.authenticate(username, password);
        if (user != null) {
            // Login successful
            dispose();
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password!\n\nDefault Login:\nUsername: admin\nPassword: admin123",
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
