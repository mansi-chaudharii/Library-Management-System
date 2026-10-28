package com.library.ui.panels;

import com.library.config.AppConfig;
import com.library.dao.IssueDao;
import com.library.model.IssueRecord;
import com.library.ui.theme.UITheme;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Return Book Panel: Search active loans, calculate late return fines,
 * process book check-in, and restore catalog inventory.
 */
public class ReturnBookPanel extends JPanel {
    private final IssueDao issueDao = new IssueDao();

    private JTextField searchField;
    private JTable activeTable;
    private DefaultTableModel tableModel;

    // Return Details Summary Card fields
    private JLabel selectedBookLbl;
    private JLabel selectedMemberLbl;
    private JLabel issueDateLbl;
    private JLabel dueDateLbl;
    private JLabel overdueDaysLbl;
    private JTextField fineTxt;
    private JButton returnBtn;

    private IssueRecord currentSelectedRecord = null;

    public ReturnBookPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadActiveIssues();
    }

    private void initUI() {
        // --- 1. Header Bar ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Return Book & Fine Processing");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLbl = new JLabel("Check in borrowed books, assess overdue fines, and return copies to shelves");
        subtitleLbl.setFont(UITheme.FONT_REGULAR);
        subtitleLbl.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLbl);

        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh Active Loans");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadActiveIssues();
        });

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Split Content: Table on Left, Return Summary Card on Right ---
        JPanel splitPanel = new JPanel(new BorderLayout(16, 0));
        splitPanel.setOpaque(false);

        // Left: Active Loans Table Card
        JPanel tableCard = UIUtils.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        // Filter / Search Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search Active Loans:");
        searchLbl.setFont(UITheme.FONT_BOLD);
        searchLbl.setForeground(UITheme.TEXT_MAIN);

        searchField = UIUtils.createTextField(18);
        searchField.setToolTipText("Filter by Student Name, Code, or Book Title...");
        searchField.addActionListener(e -> filterActiveIssues());

        JButton searchBtn = UIUtils.createPrimaryButton("🔍 Filter");
        searchBtn.addActionListener(e -> filterActiveIssues());

        JButton clearBtn = UIUtils.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadActiveIssues();
        });

        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(searchBtn);
        filterBar.add(clearBtn);
        tableCard.add(filterBar, BorderLayout.NORTH);

        // Active Loans Table
        String[] columns = {"ID", "Book Title", "Student Name", "Student Code", "Issue Date", "Due Date", "Days Late", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        activeTable = new JTable(tableModel);
        UIUtils.styleTable(activeTable);
        activeTable.getColumnModel().getColumn(7).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        activeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleRowSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(activeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        splitPanel.add(tableCard, BorderLayout.CENTER);

        // Right: Return Check-in Card
        JPanel returnCard = UIUtils.createCardPanel();
        returnCard.setPreferredSize(new Dimension(320, returnCard.getPreferredSize().height));
        returnCard.setLayout(new BorderLayout(0, 14));

        JLabel returnCardHeader = new JLabel("📥 Return Confirmation");
        returnCardHeader.setFont(UITheme.FONT_SUBTITLE);
        returnCardHeader.setForeground(UITheme.PRIMARY_DARK);
        returnCard.add(returnCardHeader, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        selectedBookLbl = createDetailRow(detailsPanel, "Book Title:", "Select a loan from the table");
        selectedMemberLbl = createDetailRow(detailsPanel, "Student:", "-");
        issueDateLbl = createDetailRow(detailsPanel, "Issue Date:", "-");
        dueDateLbl = createDetailRow(detailsPanel, "Due Date:", "-");
        overdueDaysLbl = createDetailRow(detailsPanel, "Days Overdue:", "0 days");

        // Fine input field
        detailsPanel.add(Box.createVerticalStrut(8));
        JLabel fineTitle = new JLabel("Late Fine Calculated:");
        fineTitle.setFont(UITheme.FONT_BOLD);
        fineTitle.setForeground(UITheme.TEXT_MAIN);
        detailsPanel.add(fineTitle);
        detailsPanel.add(Box.createVerticalStrut(4));

        fineTxt = UIUtils.createTextField(10);
        fineTxt.setText("0.00");
        fineTxt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        detailsPanel.add(fineTxt);

        JLabel fineNote = new JLabel("(Rate: " + AppConfig.getFinePerDay() + " per day. You may adjust or waive)");
        fineNote.setFont(UITheme.FONT_SMALL);
        fineNote.setForeground(UITheme.TEXT_MUTED);
        fineNote.setBorder(new EmptyBorder(4, 0, 10, 0));
        detailsPanel.add(fineNote);

        detailsPanel.add(Box.createVerticalGlue());
        returnCard.add(detailsPanel, BorderLayout.CENTER);

        // Process Return Button
        returnBtn = UIUtils.createSuccessButton("✓ Process Return");
        returnBtn.setPreferredSize(new Dimension(220, 42));
        returnBtn.setEnabled(false);
        returnBtn.addActionListener(e -> processReturn());
        returnCard.add(returnBtn, BorderLayout.SOUTH);

        splitPanel.add(returnCard, BorderLayout.EAST);
        add(splitPanel, BorderLayout.CENTER);
    }

    private JLabel createDetailRow(JPanel container, String labelText, String defaultValue) {
        JLabel titleLbl = new JLabel(labelText);
        titleLbl.setFont(UITheme.FONT_BOLD);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel valLbl = new JLabel(defaultValue);
        valLbl.setFont(UITheme.FONT_REGULAR);
        valLbl.setForeground(UITheme.TEXT_MAIN);
        valLbl.setBorder(new EmptyBorder(2, 0, 8, 0));

        container.add(titleLbl);
        container.add(valLbl);
        return valLbl;
    }

    public void loadActiveIssues() {
        tableModel.setRowCount(0);
        List<IssueRecord> activeList = issueDao.getActiveIssues();
        LocalDate today = LocalDate.now();

        for (IssueRecord r : activeList) {
            long daysLate = r.getOverdueDays(today);
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getBookTitle(),
                    r.getMemberName(),
                    r.getMemberCode(),
                    r.getIssueDate().toString(),
                    r.getDueDate().toString(),
                    daysLate > 0 ? (daysLate + " days") : "On Time",
                    r.getStatus()
            });
        }
        resetSummaryCard();
    }

    private void filterActiveIssues() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            loadActiveIssues();
            return;
        }

        tableModel.setRowCount(0);
        List<IssueRecord> activeList = issueDao.getActiveIssues();
        LocalDate today = LocalDate.now();

        for (IssueRecord r : activeList) {
            boolean match = r.getBookTitle().toLowerCase().contains(kw) ||
                            r.getMemberName().toLowerCase().contains(kw) ||
                            r.getMemberCode().toLowerCase().contains(kw) ||
                            r.getBookIsbn().toLowerCase().contains(kw);
            if (match) {
                long daysLate = r.getOverdueDays(today);
                tableModel.addRow(new Object[]{
                        r.getId(),
                        r.getBookTitle(),
                        r.getMemberName(),
                        r.getMemberCode(),
                        r.getIssueDate().toString(),
                        r.getDueDate().toString(),
                        daysLate > 0 ? (daysLate + " days") : "On Time",
                        r.getStatus()
                });
            }
        }
        resetSummaryCard();
    }

    private void handleRowSelected() {
        int selectedRow = activeTable.getSelectedRow();
        if (selectedRow == -1) {
            resetSummaryCard();
            return;
        }

        int issueId = (int) tableModel.getValueAt(selectedRow, 0);
        List<IssueRecord> activeList = issueDao.getActiveIssues();
        currentSelectedRecord = null;
        for (IssueRecord r : activeList) {
            if (r.getId() == issueId) {
                currentSelectedRecord = r;
                break;
            }
        }

        if (currentSelectedRecord != null) {
            selectedBookLbl.setText("<html><b>" + currentSelectedRecord.getBookTitle() + "</b></html>");
            selectedMemberLbl.setText(currentSelectedRecord.getMemberName() + " (" + currentSelectedRecord.getMemberCode() + ")");
            issueDateLbl.setText(currentSelectedRecord.getIssueDate().toString());
            dueDateLbl.setText(currentSelectedRecord.getDueDate().toString());

            long overdueDays = currentSelectedRecord.getOverdueDays(LocalDate.now());
            if (overdueDays > 0) {
                overdueDaysLbl.setText("<html><font color='#EF4444'><b>" + overdueDays + " days overdue</b></font></html>");
                double calculatedFine = overdueDays * AppConfig.getFinePerDay();
                fineTxt.setText(String.format("%.2f", calculatedFine));
            } else {
                overdueDaysLbl.setText("<html><font color='#10B981'><b>0 days (On Time)</b></font></html>");
                fineTxt.setText("0.00");
            }
            returnBtn.setEnabled(true);
        }
    }

    private void resetSummaryCard() {
        currentSelectedRecord = null;
        selectedBookLbl.setText("Select a loan from the table");
        selectedMemberLbl.setText("-");
        issueDateLbl.setText("-");
        dueDateLbl.setText("-");
        overdueDaysLbl.setText("0 days");
        fineTxt.setText("0.00");
        returnBtn.setEnabled(false);
    }

    private void processReturn() {
        if (currentSelectedRecord == null) return;

        double fineAmount = 0.0;
        try {
            fineAmount = Double.parseDouble(fineTxt.getText().trim());
            if (fineAmount < 0) fineAmount = 0.0;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Fine must be a valid number.", "Invalid Fine", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm return for:\n\nBook: " + currentSelectedRecord.getBookTitle() +
                "\nStudent: " + currentSelectedRecord.getMemberName() +
                "\nFine Collected: $" + String.format("%.2f", fineAmount) +
                "\n\nThis will return the copy to available inventory.",
                "Confirm Return Check-in",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = issueDao.returnBook(currentSelectedRecord.getId(), LocalDate.now(), fineAmount);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Book returned successfully!\nInventory updated.",
                        "Return Completed", JOptionPane.INFORMATION_MESSAGE);
                loadActiveIssues();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to return book. Please check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
