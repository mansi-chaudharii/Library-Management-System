package com.library.ui;

import com.library.config.DatabaseConnection;
import com.library.model.User;
import com.library.ui.panels.*;
import com.library.ui.theme.UITheme;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Application Window featuring a clean, responsive layout:
 * - Left sidebar navigation with modern icons and active tabs
 * - Top header with user profile and live DB status indicator
 * - Center card layout dynamically switching between management views
 */
public class MainFrame extends JFrame {
    private final User currentUser;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    private DashboardPanel dashboardPanel;
    private BooksPanel booksPanel;
    private MembersPanel membersPanel;
    private IssueBookPanel issueBookPanel;
    private ReturnBookPanel returnBookPanel;
    private HistoryPanel historyPanel;

    private final List<JButton> navButtons = new ArrayList<>();
    private JLabel dbStatusLabel;

    public MainFrame(User user) {
        this.currentUser = (user != null) ? user : new User("admin", "admin123", "Administrator", "Admin");

        setTitle("Library Management System - LibMaster");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_APP);

        // 1. Sidebar on West
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Main Content Center Panel with CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UITheme.BG_APP);

        dashboardPanel = new DashboardPanel(this::navigateTo);
        booksPanel = new BooksPanel();
        membersPanel = new MembersPanel();
        issueBookPanel = new IssueBookPanel();
        returnBookPanel = new ReturnBookPanel();
        historyPanel = new HistoryPanel();

        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(booksPanel, "BOOKS");
        mainContentPanel.add(membersPanel, "MEMBERS");
        mainContentPanel.add(issueBookPanel, "ISSUE");
        mainContentPanel.add(returnBookPanel, "RETURN");
        mainContentPanel.add(historyPanel, "HISTORY");

        root.add(mainContentPanel, BorderLayout.CENTER);

        setContentPane(root);
        setActiveNav(0); // Start on Dashboard
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        // Top Brand Header
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 18));
        brandPanel.setOpaque(false);

        JLabel logoLbl = new JLabel("📖");
        logoLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);

        JLabel brandTitle = new JLabel("LibMaster");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandTitle.setForeground(UITheme.PRIMARY_DARK);

        JLabel brandSub = new JLabel("Library System v1.0");
        brandSub.setFont(UITheme.FONT_SMALL);
        brandSub.setForeground(UITheme.TEXT_MUTED);

        brandText.add(brandTitle);
        brandText.add(brandSub);

        brandPanel.add(logoLbl);
        brandPanel.add(brandText);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Middle Nav Items
        JPanel navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);
        navContainer.setBorder(new EmptyBorder(12, 10, 12, 10));

        navButtons.add(createNavButton("📊  Dashboard", "DASHBOARD", 0));
        navButtons.add(createNavButton("📚  Books Catalog", "BOOKS", 1));
        navButtons.add(createNavButton("👥  Members / Students", "MEMBERS", 2));
        navButtons.add(createNavButton("📤  Issue Book", "ISSUE", 3));
        navButtons.add(createNavButton("📥  Return Book", "RETURN", 4));
        navButtons.add(createNavButton("📜  Circulation History", "HISTORY", 5));

        for (JButton btn : navButtons) {
            navContainer.add(btn);
            navContainer.add(Box.createVerticalStrut(6));
        }

        navContainer.add(Box.createVerticalStrut(14));
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        navContainer.add(sep);
        navContainer.add(Box.createVerticalStrut(14));

        JButton dbBtn = createNavAction("⚙️  Database Settings", e -> {
            new DatabaseConfigDialog(this, this::onDatabaseReconnected).setVisible(true);
        });
        navContainer.add(dbBtn);

        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom User & Logout Footer
        JPanel userFooter = new JPanel(new BorderLayout(8, 8));
        userFooter.setOpaque(false);
        userFooter.setBorder(new EmptyBorder(12, 14, 16, 14));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        JLabel userLbl = new JLabel("👤 " + currentUser.getFullName());
        userLbl.setFont(UITheme.FONT_BOLD);
        userLbl.setForeground(UITheme.TEXT_MAIN);

        dbStatusLabel = new JLabel();
        updateDbStatusPill();

        userInfo.add(userLbl);
        userInfo.add(Box.createVerticalStrut(2));
        userInfo.add(dbStatusLabel);

        JButton logoutBtn = UIUtils.createSecondaryButton("🚪 Logout");
        logoutBtn.addActionListener(e -> logout());

        userFooter.add(userInfo, BorderLayout.CENTER);
        userFooter.add(logoutBtn, BorderLayout.SOUTH);

        sidebar.add(userFooter, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String text, String cardKey, int index) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isSelected = (getBackground().equals(UITheme.PRIMARY_LIGHT));
                if (isSelected) {
                    g2.setColor(UITheme.PRIMARY_LIGHT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    // Blue left accent bar
                    g2.setColor(UITheme.PRIMARY);
                    g2.fillRoundRect(0, 2, 4, getHeight() - 4, 4, 4);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(UITheme.TEXT_MAIN);
        btn.setBackground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btn.addActionListener(e -> {
            setActiveNav(index);
            navigateTo(cardKey);
        });

        return btn;
    }

    private JButton createNavAction(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(UITheme.TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.addActionListener(al);
        return btn;
    }

    public void navigateTo(String cardKey) {
        cardLayout.show(mainContentPanel, cardKey);

        // Update active nav button selection state
        for (int i = 0; i < navButtons.size(); i++) {
            JButton b = navButtons.get(i);
            if (cardKey.equals("DASHBOARD") && i == 0) setActiveNav(0);
            else if (cardKey.equals("BOOKS") && i == 1) setActiveNav(1);
            else if (cardKey.equals("MEMBERS") && i == 2) setActiveNav(2);
            else if (cardKey.equals("ISSUE") && i == 3) setActiveNav(3);
            else if (cardKey.equals("RETURN") && i == 4) setActiveNav(4);
            else if (cardKey.equals("HISTORY") && i == 5) setActiveNav(5);
        }

        // Auto-refresh respective panel
        switch (cardKey) {
            case "DASHBOARD":
                dashboardPanel.refreshData();
                break;
            case "BOOKS":
                booksPanel.loadBooks();
                break;
            case "MEMBERS":
                membersPanel.loadMembers();
                break;
            case "ISSUE":
                issueBookPanel.refreshAll();
                break;
            case "RETURN":
                returnBookPanel.loadActiveIssues();
                break;
            case "HISTORY":
                historyPanel.loadHistory();
                break;
        }
    }

    private void setActiveNav(int selectedIndex) {
        for (int i = 0; i < navButtons.size(); i++) {
            JButton btn = navButtons.get(i);
            if (i == selectedIndex) {
                btn.setBackground(UITheme.PRIMARY_LIGHT);
                btn.setForeground(UITheme.PRIMARY_DARK);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(UITheme.TEXT_MAIN);
            }
            btn.repaint();
        }
    }

    private void updateDbStatusPill() {
        if (DatabaseConnection.isUsingMock()) {
            dbStatusLabel.setText("🟡 Mode: Demo / Mock");
            dbStatusLabel.setForeground(UITheme.WARNING.darker());
            dbStatusLabel.setToolTipText("Running with demo mock data. Click Database Settings to connect MySQL.");
        } else {
            dbStatusLabel.setText("🟢 Mode: MySQL Live");
            dbStatusLabel.setForeground(UITheme.SUCCESS.darker());
            dbStatusLabel.setToolTipText("Connected to MySQL database.");
        }
        dbStatusLabel.setFont(UITheme.FONT_SMALL_BOLD);
    }

    private void onDatabaseReconnected() {
        updateDbStatusPill();
        dashboardPanel.refreshData();
        booksPanel.loadBooks();
        membersPanel.loadMembers();
        issueBookPanel.refreshAll();
        returnBookPanel.loadActiveIssues();
        historyPanel.loadHistory();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of the Library System?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }
}
