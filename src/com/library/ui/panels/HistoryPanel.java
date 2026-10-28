package com.library.ui.panels;

import com.library.dao.IssueDao;
import com.library.model.IssueRecord;
import com.library.ui.theme.UITheme;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * History & Reports Panel: Full circulation records, audit trail,
 * status filtering, fine collection totals, and CSV export.
 */
public class HistoryPanel extends JPanel {
    private final IssueDao issueDao = new IssueDao();

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    // Stat labels
    private JLabel totalLoansLbl;
    private JLabel returnedLbl;
    private JLabel overdueLbl;
    private JLabel fineCollectedLbl;

    public HistoryPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadHistory();
    }

    private void initUI() {
        // --- 1. Header Bar ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Circulation History & Reports");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLbl = new JLabel("Audit trail of all book issues, returns, fines, and circulation analytics");
        subtitleLbl.setFont(UITheme.FONT_REGULAR);
        subtitleLbl.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLbl);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        JButton exportBtn = UIUtils.createSuccessButton("📥 Export CSV Report");
        exportBtn.addActionListener(e -> exportToCsv());

        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadHistory());

        headerActions.add(exportBtn);
        headerActions.add(refreshBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(headerActions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Center Card: KPI Mini-bar + Filters + Table ---
        JPanel centerCard = UIUtils.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 14));

        // Mini KPI Stats Bar
        JPanel summaryBar = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryBar.setOpaque(false);
        summaryBar.setBorder(new EmptyBorder(0, 0, 10, 0));

        totalLoansLbl = createSummaryBadge("TOTAL TRANSACTIONS", "0", UITheme.PRIMARY);
        returnedLbl = createSummaryBadge("RETURNED BOOKS", "0", UITheme.SUCCESS);
        overdueLbl = createSummaryBadge("ACTIVE / OVERDUE", "0", UITheme.DANGER);
        fineCollectedLbl = createSummaryBadge("TOTAL FINES COLLECTED", "$0.00", new Color(139, 92, 246));

        summaryBar.add(totalLoansLbl.getParent());
        summaryBar.add(returnedLbl.getParent());
        summaryBar.add(overdueLbl.getParent());
        summaryBar.add(fineCollectedLbl.getParent());
        centerCard.add(summaryBar, BorderLayout.NORTH);

        // Filter / Search Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.FONT_BOLD);
        searchLbl.setForeground(UITheme.TEXT_MAIN);

        searchField = UIUtils.createTextField(18);
        searchField.setToolTipText("Filter by Book Title, Member Name, or Code...");
        searchField.addActionListener(e -> filterHistory());

        JLabel statusLbl = new JLabel("Filter Status:");
        statusLbl.setFont(UITheme.FONT_BOLD);
        statusLbl.setForeground(UITheme.TEXT_MAIN);

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "Issued", "Returned", "Overdue"});
        statusFilterCombo.setFont(UITheme.FONT_REGULAR);
        statusFilterCombo.setPreferredSize(new Dimension(140, 36));
        statusFilterCombo.addActionListener(e -> filterHistory());

        JButton filterBtn = UIUtils.createPrimaryButton("🔍 Filter");
        filterBtn.addActionListener(e -> filterHistory());

        JButton resetBtn = UIUtils.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            loadHistory();
        });

        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(statusLbl);
        filterBar.add(statusFilterCombo);
        filterBar.add(filterBtn);
        filterBar.add(resetBtn);

        // Center Wrapper
        JPanel tableWrapper = new JPanel(new BorderLayout(0, 10));
        tableWrapper.setOpaque(false);
        tableWrapper.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Book Title", "Student Name", "Student ID", "Issue Date", "Due Date", "Return Date", "Fine ($)", "Status", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new JTable(tableModel);
        UIUtils.styleTable(historyTable);

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(85);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(65);
        historyTable.getColumnModel().getColumn(8).setPreferredWidth(90);

        historyTable.getColumnModel().getColumn(8).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        centerCard.add(tableWrapper, BorderLayout.CENTER);
        add(centerCard, BorderLayout.CENTER);
    }

    private JLabel createSummaryBadge(String title, String val, Color color) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_SMALL_BOLD);
        t.setForeground(UITheme.TEXT_MUTED);

        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));
        v.setForeground(color);

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return v;
    }

    public void loadHistory() {
        tableModel.setRowCount(0);
        List<IssueRecord> list = issueDao.getAllIssues();
        populateTable(list);
    }

    private void filterHistory() {
        String kw = searchField.getText().trim().toLowerCase();
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        boolean matchAll = (selectedStatus == null || "All Statuses".equalsIgnoreCase(selectedStatus));

        tableModel.setRowCount(0);
        List<IssueRecord> list = issueDao.getAllIssues();

        List<IssueRecord> filtered = new java.util.ArrayList<>();
        for (IssueRecord r : list) {
            boolean statusMatch = matchAll || selectedStatus.equalsIgnoreCase(r.getStatus());
            boolean kwMatch = kw.isEmpty() ||
                    r.getBookTitle().toLowerCase().contains(kw) ||
                    r.getMemberName().toLowerCase().contains(kw) ||
                    r.getMemberCode().toLowerCase().contains(kw) ||
                    r.getBookIsbn().toLowerCase().contains(kw);

            if (statusMatch && kwMatch) {
                filtered.add(r);
            }
        }
        populateTable(filtered);
    }

    private void populateTable(List<IssueRecord> list) {
        tableModel.setRowCount(0);
        int total = list.size();
        int returned = 0;
        int overdue = 0;
        double fineSum = 0.0;

        for (IssueRecord r : list) {
            if ("Returned".equalsIgnoreCase(r.getStatus())) {
                returned++;
                fineSum += r.getFineAmount();
            } else if (r.isCurrentlyOverdue()) {
                overdue++;
            }

            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getBookTitle(),
                    r.getMemberName(),
                    r.getMemberCode(),
                    r.getIssueDate() != null ? r.getIssueDate().toString() : "-",
                    r.getDueDate() != null ? r.getDueDate().toString() : "-",
                    r.getReturnDate() != null ? r.getReturnDate().toString() : "-",
                    String.format("%.2f", r.getFineAmount()),
                    r.getStatus(),
                    r.getNotes()
            });
        }

        totalLoansLbl.setText(String.valueOf(total));
        returnedLbl.setText(String.valueOf(returned));
        overdueLbl.setText(String.valueOf(overdue));
        fineCollectedLbl.setText("$" + String.format("%.2f", fineSum));
    }

    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Circulation Report as CSV");
        fileChooser.setSelectedFile(new File("library_circulation_report.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                // Header
                writer.println("Transaction ID,Book Title,Student Name,Student ID,Issue Date,Due Date,Return Date,Fine ($),Status,Notes");

                // Rows
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    StringBuilder line = new StringBuilder();
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object val = tableModel.getValueAt(i, j);
                        String strVal = (val != null) ? val.toString().replace("\"", "\"\"") : "";
                        line.append("\"").append(strVal).append("\"");
                        if (j < tableModel.getColumnCount() - 1) {
                            line.append(",");
                        }
                    }
                    writer.println(line.toString());
                }

                JOptionPane.showMessageDialog(this,
                        "Report successfully exported to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving CSV file: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
