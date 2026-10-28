package com.library.ui.panels;

import com.library.config.AppConfig;
import com.library.dao.BookDao;
import com.library.dao.IssueDao;
import com.library.dao.MemberDao;
import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import com.library.ui.theme.UITheme;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Issue Book Panel: Issue books to registered members, validating
 * copy availability and member status with transactional safety.
 */
public class IssueBookPanel extends JPanel {
    private final BookDao bookDao = new BookDao();
    private final MemberDao memberDao = new MemberDao();
    private final IssueDao issueDao = new IssueDao();

    private JComboBox<BookItem> bookCombo;
    private JComboBox<MemberItem> memberCombo;
    private JTextField issueDateTxt;
    private JTextField dueDateTxt;
    private JTextField notesTxt;
    private JLabel bookInfoLbl;
    private JLabel memberInfoLbl;

    private JTable activeTable;
    private DefaultTableModel activeTableModel;

    // Helper wrapper classes for ComboBox items
    private static class BookItem {
        final Book book;
        BookItem(Book book) { this.book = book; }
        @Override
        public String toString() {
            return book.getTitle() + " (Available: " + book.getAvailableCopies() + ")";
        }
    }

    private static class MemberItem {
        final Member member;
        MemberItem(Member member) { this.member = member; }
        @Override
        public String toString() {
            return member.getName() + " [" + member.getMemberCode() + "] - " + member.getDepartment();
        }
    }

    public IssueBookPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refreshAll();
    }

    private void initUI() {
        // --- 1. Header Bar ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Issue Book to Member");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLbl = new JLabel("Check out catalog books to registered students and set return due dates");
        subtitleLbl.setFont(UITheme.FONT_REGULAR);
        subtitleLbl.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLbl);

        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh Data");
        refreshBtn.addActionListener(e -> refreshAll());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Split Content: Form on Left, Active Issues on Right ---
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);

        // Left: Issue Form Card
        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 14));

        JLabel formHeader = new JLabel("📝 Loan Details");
        formHeader.setFont(UITheme.FONT_SUBTITLE);
        formHeader.setForeground(UITheme.PRIMARY_DARK);
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        // Book Selector
        fieldsPanel.add(createFieldLabel("Select Book:"));
        bookCombo = new JComboBox<>();
        bookCombo.setFont(UITheme.FONT_REGULAR);
        bookCombo.setPreferredSize(new Dimension(300, 36));
        bookCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        bookCombo.addActionListener(e -> updateBookInfo());
        fieldsPanel.add(bookCombo);

        bookInfoLbl = new JLabel("Select a book above");
        bookInfoLbl.setFont(UITheme.FONT_SMALL);
        bookInfoLbl.setForeground(UITheme.PRIMARY);
        bookInfoLbl.setBorder(new EmptyBorder(4, 2, 8, 2));
        fieldsPanel.add(bookInfoLbl);

        // Member Selector
        fieldsPanel.add(createFieldLabel("Select Member / Student:"));
        memberCombo = new JComboBox<>();
        memberCombo.setFont(UITheme.FONT_REGULAR);
        memberCombo.setPreferredSize(new Dimension(300, 36));
        memberCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        memberCombo.addActionListener(e -> updateMemberInfo());
        fieldsPanel.add(memberCombo);

        memberInfoLbl = new JLabel("Select a member above");
        memberInfoLbl.setFont(UITheme.FONT_SMALL);
        memberInfoLbl.setForeground(UITheme.PRIMARY);
        memberInfoLbl.setBorder(new EmptyBorder(4, 2, 8, 2));
        fieldsPanel.add(memberInfoLbl);

        // Dates Panel
        JPanel datesRow = new JPanel(new GridLayout(1, 2, 12, 0));
        datesRow.setOpaque(false);

        JPanel issueDateCol = new JPanel(new BorderLayout(0, 4));
        issueDateCol.setOpaque(false);
        issueDateCol.add(createFieldLabel("Issue Date (YYYY-MM-DD):"), BorderLayout.NORTH);
        issueDateTxt = UIUtils.createTextField(10);
        issueDateTxt.setText(LocalDate.now().toString());
        issueDateCol.add(issueDateTxt, BorderLayout.CENTER);

        JPanel dueDateCol = new JPanel(new BorderLayout(0, 4));
        dueDateCol.setOpaque(false);
        dueDateCol.add(createFieldLabel("Due Date (YYYY-MM-DD):"), BorderLayout.NORTH);
        dueDateTxt = UIUtils.createTextField(10);
        dueDateTxt.setText(LocalDate.now().plusDays(AppConfig.getDefaultIssueDays()).toString());
        dueDateCol.add(dueDateTxt, BorderLayout.CENTER);

        datesRow.add(issueDateCol);
        datesRow.add(dueDateCol);
        fieldsPanel.add(datesRow);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Notes
        fieldsPanel.add(createFieldLabel("Notes / Purpose (Optional):"));
        notesTxt = UIUtils.createTextField(20);
        notesTxt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        fieldsPanel.add(notesTxt);
        fieldsPanel.add(Box.createVerticalGlue());

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Issue Button
        JButton issueConfirmBtn = UIUtils.createPrimaryButton("✓ Confirm & Issue Book");
        issueConfirmBtn.setPreferredSize(new Dimension(200, 42));
        issueConfirmBtn.addActionListener(e -> handleIssueBook());
        formCard.add(issueConfirmBtn, BorderLayout.SOUTH);

        // Right: Active Loans Card
        JPanel activeCard = UIUtils.createCardPanel();
        activeCard.setLayout(new BorderLayout(0, 10));

        JLabel activeHeader = new JLabel("📋 Currently Active Loans");
        activeHeader.setFont(UITheme.FONT_SUBTITLE);
        activeHeader.setForeground(UITheme.TEXT_MAIN);
        activeCard.add(activeHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "Book Title", "Student Name", "Due Date", "Status"};
        activeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        activeTable = new JTable(activeTableModel);
        UIUtils.styleTable(activeTable);
        activeTable.getColumnModel().getColumn(4).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        JScrollPane activeScroll = new JScrollPane(activeTable);
        activeScroll.setBorder(BorderFactory.createEmptyBorder());
        activeCard.add(activeScroll, BorderLayout.CENTER);

        contentPanel.add(formCard);
        contentPanel.add(activeCard);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MAIN);
        lbl.setBorder(new EmptyBorder(6, 0, 4, 0));
        return lbl;
    }

    public void refreshAll() {
        // Reload books
        bookCombo.removeAllItems();
        List<Book> books = bookDao.getAllBooks();
        for (Book b : books) {
            bookCombo.addItem(new BookItem(b));
        }

        // Reload members
        memberCombo.removeAllItems();
        List<Member> members = memberDao.getAllMembers();
        for (Member m : members) {
            memberCombo.addItem(new MemberItem(m));
        }

        // Reset dates
        issueDateTxt.setText(LocalDate.now().toString());
        dueDateTxt.setText(LocalDate.now().plusDays(AppConfig.getDefaultIssueDays()).toString());
        notesTxt.setText("");

        updateBookInfo();
        updateMemberInfo();

        // Reload active issues
        activeTableModel.setRowCount(0);
        List<IssueRecord> activeIssues = issueDao.getActiveIssues();
        for (IssueRecord r : activeIssues) {
            activeTableModel.addRow(new Object[]{
                    r.getId(),
                    r.getBookTitle(),
                    r.getMemberName(),
                    r.getDueDate().toString(),
                    r.getStatus()
            });
        }
    }

    private void updateBookInfo() {
        BookItem item = (BookItem) bookCombo.getSelectedItem();
        if (item != null) {
            Book b = item.book;
            bookInfoLbl.setText("ISBN: " + b.getIsbn() + " | Author: " + b.getAuthor() + " | Available Copies: " + b.getAvailableCopies() + " of " + b.getQuantity());
            if (b.getAvailableCopies() <= 0) {
                bookInfoLbl.setForeground(UITheme.DANGER);
            } else {
                bookInfoLbl.setForeground(UITheme.SUCCESS.darker());
            }
        } else {
            bookInfoLbl.setText("No books available");
        }
    }

    private void updateMemberInfo() {
        MemberItem item = (MemberItem) memberCombo.getSelectedItem();
        if (item != null) {
            Member m = item.member;
            memberInfoLbl.setText("Student Code: " + m.getMemberCode() + " | Email: " + m.getEmail() + " | Status: " + m.getStatus());
            if (!m.isActive()) {
                memberInfoLbl.setForeground(UITheme.DANGER);
            } else {
                memberInfoLbl.setForeground(UITheme.SUCCESS.darker());
            }
        } else {
            memberInfoLbl.setText("No members available");
        }
    }

    private void handleIssueBook() {
        BookItem selectedBookItem = (BookItem) bookCombo.getSelectedItem();
        MemberItem selectedMemberItem = (MemberItem) memberCombo.getSelectedItem();

        if (selectedBookItem == null || selectedMemberItem == null) {
            JOptionPane.showMessageDialog(this, "Please select both a book and a member.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Book book = selectedBookItem.book;
        Member member = selectedMemberItem.member;

        // Availability check
        if (book.getAvailableCopies() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Cannot issue \"" + book.getTitle() + "\". No copies are currently available.",
                    "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Active status check
        if (!member.isActive()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot issue book to member \"" + member.getName() + "\" because membership status is " + member.getStatus() + ".",
                    "Member Inactive", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate issueDate;
        LocalDate dueDate;
        try {
            issueDate = LocalDate.parse(issueDateTxt.getText().trim());
            dueDate = LocalDate.parse(dueDateTxt.getText().trim());
            if (dueDate.isBefore(issueDate)) {
                JOptionPane.showMessageDialog(this, "Due Date cannot be before Issue Date.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter dates in YYYY-MM-DD format.", "Invalid Date Format", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String notes = notesTxt.getText().trim();
        boolean success = issueDao.issueBook(book.getId(), member.getId(), issueDate, dueDate, notes);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Book successfully issued!\n\nBook: " + book.getTitle() +
                    "\nBorrower: " + member.getName() + " (" + member.getMemberCode() + ")" +
                    "\nDue Date: " + dueDate,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to issue book. Please check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
