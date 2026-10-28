package com.library.ui.panels;

import com.library.dao.BookDao;
import com.library.dao.IssueDao;
import com.library.dao.MemberDao;
import com.library.model.IssueRecord;
import com.library.ui.theme.UITheme;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dashboard Panel: Central hub displaying key library metrics,
 * quick action shortcuts, recent transactions, and overdue alerts.
 */
public class DashboardPanel extends JPanel {
    private final BookDao bookDao = new BookDao();
    private final MemberDao memberDao = new MemberDao();
    private final IssueDao issueDao = new IssueDao();

    private final Consumer<String> navigationHandler;

    private JPanel statsContainer;
    private JTable recentTable;
    private DefaultTableModel recentModel;
    private JTable overdueTable;
    private DefaultTableModel overdueModel;

    public DashboardPanel(Consumer<String> navigationHandler) {
        this.navigationHandler = navigationHandler;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refreshData();
    }

    private void initUI() {
        // --- 1. Top Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Library Dashboard");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLbl = new JLabel("Real-time summary of catalog, active loans, and student activities");
        subtitleLbl.setFont(UITheme.FONT_REGULAR);
        subtitleLbl.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLbl);

        // Date pill on right
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        JLabel dateBadge = new JLabel("📅 " + dateStr);
        dateBadge.setFont(UITheme.FONT_SMALL_BOLD);
        dateBadge.setForeground(UITheme.PRIMARY_DARK);
        dateBadge.setBorder(new EmptyBorder(6, 12, 6, 12));
        dateBadge.setOpaque(true);
        dateBadge.setBackground(UITheme.PRIMARY_LIGHT);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(dateBadge, BorderLayout.EAST);

        // --- 2. Top Content: Stats Cards + Quick Actions ---
        JPanel topContainer = new JPanel(new BorderLayout(0, 14));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);

        statsContainer = new JPanel(new GridLayout(1, 5, 14, 0));
        statsContainer.setOpaque(false);
        topContainer.add(statsContainer, BorderLayout.CENTER);

        // Quick Actions Bar
        JPanel quickActionsCard = UIUtils.createCardPanel();
        quickActionsCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        JLabel qaTitle = new JLabel("⚡ Quick Actions:");
        qaTitle.setFont(UITheme.FONT_HEADER);
        qaTitle.setForeground(UITheme.TEXT_MAIN);
        quickActionsCard.add(qaTitle);

        JButton issueBtn = UIUtils.createPrimaryButton("+ Issue Book");
        issueBtn.addActionListener(e -> navigationHandler.accept("ISSUE"));
        quickActionsCard.add(issueBtn);

        JButton returnBtn = UIUtils.createSecondaryButton("📥 Return Book");
        returnBtn.addActionListener(e -> navigationHandler.accept("RETURN"));
        quickActionsCard.add(returnBtn);

        JButton addBookBtn = UIUtils.createSecondaryButton("📚 Manage Books");
        addBookBtn.addActionListener(e -> navigationHandler.accept("BOOKS"));
        quickActionsCard.add(addBookBtn);

        JButton addMemberBtn = UIUtils.createSecondaryButton("👥 Manage Members");
        addMemberBtn.addActionListener(e -> navigationHandler.accept("MEMBERS"));
        quickActionsCard.add(addMemberBtn);

        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh Stats");
        refreshBtn.addActionListener(e -> refreshData());
        quickActionsCard.add(refreshBtn);

        topContainer.add(quickActionsCard, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // --- 3. Bottom Split: Overdue Alerts & Recent Activity ---
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesPanel.setOpaque(false);

        // Left: Overdue Alerts
        JPanel overdueCard = UIUtils.createCardPanel();
        overdueCard.setLayout(new BorderLayout(0, 10));

        JLabel overdueHeader = new JLabel("⚠️ Overdue Books Requiring Attention");
        overdueHeader.setFont(UITheme.FONT_SUBTITLE);
        overdueHeader.setForeground(UITheme.DANGER);
        overdueCard.add(overdueHeader, BorderLayout.NORTH);

        String[] overdueCols = {"Book Title", "Student Name", "Due Date", "Days Late"};
        overdueModel = new DefaultTableModel(overdueCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        overdueTable = new JTable(overdueModel);
        UIUtils.styleTable(overdueTable);
        JScrollPane overdueScroll = new JScrollPane(overdueTable);
        overdueScroll.setBorder(BorderFactory.createEmptyBorder());
        overdueCard.add(overdueScroll, BorderLayout.CENTER);

        // Right: Recent Activity
        JPanel recentCard = UIUtils.createCardPanel();
        recentCard.setLayout(new BorderLayout(0, 10));

        JLabel recentHeader = new JLabel("🕒 Recent Circulation Activity");
        recentHeader.setFont(UITheme.FONT_SUBTITLE);
        recentHeader.setForeground(UITheme.TEXT_MAIN);
        recentCard.add(recentHeader, BorderLayout.NORTH);

        String[] recentCols = {"Book", "Borrower", "Due / Return Date", "Status"};
        recentModel = new DefaultTableModel(recentCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        recentTable = new JTable(recentModel);
        UIUtils.styleTable(recentTable);
        recentTable.getColumnModel().getColumn(3).setCellRenderer(new UIUtils.StatusBadgeRenderer());
        JScrollPane recentScroll = new JScrollPane(recentTable);
        recentScroll.setBorder(BorderFactory.createEmptyBorder());
        recentCard.add(recentScroll, BorderLayout.CENTER);

        tablesPanel.add(overdueCard);
        tablesPanel.add(recentCard);

        add(tablesPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        // 1. Update KPI stats
        int totalTitles = bookDao.getTotalBookTitles();
        int totalCopies = bookDao.getTotalCopiesCount();
        int availableCopies = bookDao.getAvailableCopiesCount();
        int activeLoans = issueDao.getActiveIssuedCount();
        int membersCount = memberDao.getTotalMembersCount();
        int overdueCount = issueDao.getOverdueCount();

        statsContainer.removeAll();
        statsContainer.add(UIUtils.createStatCard("TOTAL TITLES", String.valueOf(totalTitles), totalCopies + " total copies", UITheme.PRIMARY, "📚"));
        statsContainer.add(UIUtils.createStatCard("AVAILABLE", String.valueOf(availableCopies), "Ready to borrow", UITheme.SUCCESS, "✅"));
        statsContainer.add(UIUtils.createStatCard("BORROWED", String.valueOf(activeLoans), "Active issues", UITheme.INFO, "📤"));
        statsContainer.add(UIUtils.createStatCard("MEMBERS", String.valueOf(membersCount), "Registered students", new Color(139, 92, 246), "👥"));
        statsContainer.add(UIUtils.createStatCard("OVERDUE", String.valueOf(overdueCount), "Needs return", UITheme.DANGER, "⚠️"));
        statsContainer.revalidate();
        statsContainer.repaint();

        // 2. Refresh Overdue Table
        overdueModel.setRowCount(0);
        List<IssueRecord> overdueList = issueDao.getOverdueIssues();
        if (overdueList.isEmpty()) {
            overdueModel.addRow(new Object[]{"No overdue books! All clean.", "-", "-", "-"});
        } else {
            for (IssueRecord r : overdueList) {
                long days = r.getOverdueDays(LocalDate.now());
                overdueModel.addRow(new Object[]{
                        r.getBookTitle(),
                        r.getMemberName() + " (" + r.getMemberCode() + ")",
                        r.getDueDate().toString(),
                        days + " days"
                });
            }
        }

        // 3. Refresh Recent Activity Table
        recentModel.setRowCount(0);
        List<IssueRecord> allIssues = issueDao.getAllIssues();
        int max = Math.min(10, allIssues.size());
        for (int i = 0; i < max; i++) {
            IssueRecord r = allIssues.get(i);
            String dateDisplay = (r.getReturnDate() != null)
                    ? "Ret: " + r.getReturnDate()
                    : "Due: " + r.getDueDate();
            recentModel.addRow(new Object[]{
                    r.getBookTitle(),
                    r.getMemberName(),
                    dateDisplay,
                    r.getStatus()
            });
        }
    }
}
