package com.library.ui.panels;

import com.library.dao.MemberDao;
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
 * Members Panel: Register, view, edit, search, and manage students and faculty.
 */
public class MembersPanel extends JPanel {
    private final MemberDao memberDao = new MemberDao();

    private JTextField searchField;
    private JComboBox<String> departmentCombo;
    private JTable membersTable;
    private DefaultTableModel tableModel;

    public MembersPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadMembers();
    }

    private void initUI() {
        // --- 1. Header Bar ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Member / Student Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLbl = new JLabel("Manage student memberships, contact information, and enrollment status");
        subtitleLbl.setFont(UITheme.FONT_REGULAR);
        subtitleLbl.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLbl);

        // Action Buttons on Header
        JPanel actionBtnBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnBlock.setOpaque(false);

        JButton addBtn = UIUtils.createPrimaryButton("+ Register Member");
        addBtn.addActionListener(e -> showMemberDialog(null));

        JButton editBtn = UIUtils.createSecondaryButton("✏️ Edit");
        editBtn.addActionListener(e -> editSelectedMember());

        JButton deleteBtn = UIUtils.createDangerButton("🗑️ Delete");
        deleteBtn.addActionListener(e -> deleteSelectedMember());

        actionBtnBlock.add(addBtn);
        actionBtnBlock.add(editBtn);
        actionBtnBlock.add(deleteBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(actionBtnBlock, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Center Card: Filter Toolbar + Table ---
        JPanel tableCard = UIUtils.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 14));

        // Filter / Search Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(UITheme.FONT_BOLD);
        searchLbl.setForeground(UITheme.TEXT_MAIN);

        searchField = UIUtils.createTextField(18);
        searchField.setToolTipText("Search by Name, Student ID, Email, Phone...");
        searchField.addActionListener(e -> filterMembers());

        JLabel deptLbl = new JLabel("Department:");
        deptLbl.setFont(UITheme.FONT_BOLD);
        deptLbl.setForeground(UITheme.TEXT_MAIN);

        departmentCombo = new JComboBox<>();
        departmentCombo.setFont(UITheme.FONT_REGULAR);
        departmentCombo.setPreferredSize(new Dimension(180, 36));
        populateDeptFilter();
        departmentCombo.addActionListener(e -> filterMembers());

        JButton searchBtn = UIUtils.createPrimaryButton("🔍 Filter");
        searchBtn.addActionListener(e -> filterMembers());

        JButton resetBtn = UIUtils.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            if (departmentCombo.getItemCount() > 0) departmentCombo.setSelectedIndex(0);
            loadMembers();
        });

        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(deptLbl);
        filterBar.add(departmentCombo);
        filterBar.add(searchBtn);
        filterBar.add(resetBtn);

        tableCard.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Student / Member Code", "Full Name", "Email Address", "Phone", "Department", "Join Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        membersTable = new JTable(tableModel);
        UIUtils.styleTable(membersTable);

        // Columns preferred widths
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(190);
        membersTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        membersTable.getColumnModel().getColumn(5).setPreferredWidth(160);
        membersTable.getColumnModel().getColumn(6).setPreferredWidth(95);

        // Status badge renderer for last column
        membersTable.getColumnModel().getColumn(7).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void populateDeptFilter() {
        departmentCombo.removeAllItems();
        List<String> depts = memberDao.getDistinctDepartments();
        for (String d : depts) {
            departmentCombo.addItem(d);
        }
    }

    public void loadMembers() {
        populateDeptFilter();
        tableModel.setRowCount(0);
        List<Member> members = memberDao.getAllMembers();
        for (Member m : members) {
            tableModel.addRow(new Object[]{
                    m.getId(),
                    m.getMemberCode(),
                    m.getName(),
                    m.getEmail(),
                    m.getPhone(),
                    m.getDepartment(),
                    m.getJoinDate().toString(),
                    m.getStatus()
            });
        }
    }

    private void filterMembers() {
        String keyword = searchField.getText().trim();
        String selectedDept = (String) departmentCombo.getSelectedItem();
        tableModel.setRowCount(0);

        List<Member> filtered = memberDao.searchMembers(keyword, selectedDept);
        for (Member m : filtered) {
            tableModel.addRow(new Object[]{
                    m.getId(),
                    m.getMemberCode(),
                    m.getName(),
                    m.getEmail(),
                    m.getPhone(),
                    m.getDepartment(),
                    m.getJoinDate().toString(),
                    m.getStatus()
            });
        }
    }

    private void editSelectedMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int memberId = (int) tableModel.getValueAt(selectedRow, 0);
        Member member = memberDao.getMemberById(memberId);
        if (member != null) {
            showMemberDialog(member);
        }
    }

    private void deleteSelectedMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a member from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int memberId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete member:\n\"" + name + "\" (ID: " + memberId + ")?\nThis will remove their membership record.",
                "Confirm Member Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = memberDao.deleteMember(memberId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Member record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMembers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete member. Make sure all issued books are returned first.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Dialog to Add or Edit a Member with validation.
     */
    private void showMemberDialog(Member existingMember) {
        boolean isEdit = (existingMember != null);
        String dialogTitle = isEdit ? "Edit Member (ID: " + existingMember.getId() + ")" : "Register New Member";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), dialogTitle, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new GridLayout(6, 2, 10, 14));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField codeTxt = UIUtils.createTextField(15);
        JTextField nameTxt = UIUtils.createTextField(15);
        JTextField emailTxt = UIUtils.createTextField(15);
        JTextField phoneTxt = UIUtils.createTextField(15);
        JComboBox<String> deptBox = new JComboBox<>(new String[]{
                "Computer Engineering",
                "Information Technology",
                "Electronics & Comm.",
                "Mechanical Engineering",
                "Civil Engineering",
                "Electrical Engineering",
                "Faculty / Staff"
        });
        deptBox.setFont(UITheme.FONT_REGULAR);
        deptBox.setPreferredSize(new Dimension(150, 36));

        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active", "Suspended", "Inactive"});
        statusBox.setFont(UITheme.FONT_REGULAR);
        statusBox.setPreferredSize(new Dimension(150, 36));

        if (isEdit) {
            codeTxt.setText(existingMember.getMemberCode());
            nameTxt.setText(existingMember.getName());
            emailTxt.setText(existingMember.getEmail());
            phoneTxt.setText(existingMember.getPhone());
            deptBox.setSelectedItem(existingMember.getDepartment());
            statusBox.setSelectedItem(existingMember.getStatus());
        } else {
            // Suggest a student code
            codeTxt.setText("STU-" + (int)(Math.random() * 9000 + 1000));
        }

        content.add(createFormLabel("Student / Member ID (*):"));
        content.add(codeTxt);
        content.add(createFormLabel("Full Name (*):"));
        content.add(nameTxt);
        content.add(createFormLabel("Email Address (*):"));
        content.add(emailTxt);
        content.add(createFormLabel("Phone Number (*):"));
        content.add(phoneTxt);
        content.add(createFormLabel("Department:"));
        content.add(deptBox);
        content.add(createFormLabel("Status:"));
        content.add(statusBox);

        // Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnBar.setBackground(UITheme.BG_APP);
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIUtils.createPrimaryButton(isEdit ? "Update Member" : "Register");
        saveBtn.addActionListener(e -> {
            String code = codeTxt.getText().trim();
            String name = nameTxt.getText().trim();
            String email = emailTxt.getText().trim();
            String phone = phoneTxt.getText().trim();
            String dept = (String) deptBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();

            if (code.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields (ID, Name, Email, Phone).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                existingMember.setMemberCode(code);
                existingMember.setName(name);
                existingMember.setEmail(email);
                existingMember.setPhone(phone);
                existingMember.setDepartment(dept);
                existingMember.setStatus(status);

                if (memberDao.updateMember(existingMember)) {
                    JOptionPane.showMessageDialog(dialog, "Member updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadMembers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update member.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                Member newMember = new Member(code, name, email, phone, dept, LocalDate.now(), status);
                if (memberDao.addMember(newMember)) {
                    JOptionPane.showMessageDialog(dialog, "Member registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadMembers();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to register member. Student Code or Email may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(saveBtn);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MAIN);
        return lbl;
    }
}
