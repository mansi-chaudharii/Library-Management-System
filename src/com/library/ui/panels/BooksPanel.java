package com.library.ui.panels;

import com.library.dao.BookDao;
import com.library.model.Book;
import com.library.ui.theme.UITheme;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Books Management Panel: Add, view, search, edit, and delete catalog books.
 */
public class BooksPanel extends JPanel {
    private final BookDao bookDao = new BookDao();

    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JTable booksTable;
    private DefaultTableModel tableModel;

    public BooksPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_APP);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadBooks();
    }

    private void initUI() {
        // --- 1. Header Bar ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLbl = new JLabel("Book Catalog Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_MAIN);

        JLabel subtitleLbl = new JLabel("Browse inventory, search by title/author/ISBN, and manage book copies");
        subtitleLbl.setFont(UITheme.FONT_REGULAR);
        subtitleLbl.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLbl);

        // Action Buttons on Header
        JPanel actionBtnBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtnBlock.setOpaque(false);

        JButton addBtn = UIUtils.createPrimaryButton("+ Add New Book");
        addBtn.addActionListener(e -> showBookDialog(null));

        JButton editBtn = UIUtils.createSecondaryButton("✏️ Edit");
        editBtn.addActionListener(e -> editSelectedBook());

        JButton deleteBtn = UIUtils.createDangerButton("🗑️ Delete");
        deleteBtn.addActionListener(e -> deleteSelectedBook());

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
        searchField.setToolTipText("Search by Title, Author, or ISBN...");
        searchField.addActionListener(e -> filterBooks());

        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(UITheme.FONT_BOLD);
        catLbl.setForeground(UITheme.TEXT_MAIN);

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(UITheme.FONT_REGULAR);
        categoryCombo.setPreferredSize(new Dimension(160, 36));
        populateCategoryFilter();
        categoryCombo.addActionListener(e -> filterBooks());

        JButton searchBtn = UIUtils.createPrimaryButton("🔍 Filter");
        searchBtn.addActionListener(e -> filterBooks());

        JButton resetBtn = UIUtils.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            if (categoryCombo.getItemCount() > 0) categoryCombo.setSelectedIndex(0);
            loadBooks();
        });

        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(catLbl);
        filterBar.add(categoryCombo);
        filterBar.add(searchBtn);
        filterBar.add(resetBtn);

        tableCard.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "ISBN", "Title", "Author", "Category", "Publisher", "Year", "Total Qty", "Available", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        booksTable = new JTable(tableModel);
        UIUtils.styleTable(booksTable);

        // Adjust column sizes
        booksTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        booksTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        booksTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        booksTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        booksTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        booksTable.getColumnModel().getColumn(7).setPreferredWidth(60);
        booksTable.getColumnModel().getColumn(8).setPreferredWidth(60);

        // Status badge renderer for last column
        booksTable.getColumnModel().getColumn(9).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void populateCategoryFilter() {
        categoryCombo.removeAllItems();
        List<String> categories = bookDao.getDistinctCategories();
        for (String cat : categories) {
            categoryCombo.addItem(cat);
        }
    }

    public void loadBooks() {
        populateCategoryFilter();
        tableModel.setRowCount(0);
        List<Book> books = bookDao.getAllBooks();
        for (Book b : books) {
            String status = b.getAvailableCopies() > 0 ? "Available" : "Out of Stock";
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getPublisher(),
                    b.getPublicationYear() > 0 ? b.getPublicationYear() : "-",
                    b.getQuantity(),
                    b.getAvailableCopies(),
                    status
            });
        }
    }

    private void filterBooks() {
        String keyword = searchField.getText().trim();
        String selectedCat = (String) categoryCombo.getSelectedItem();
        tableModel.setRowCount(0);

        List<Book> filtered = bookDao.searchBooks(keyword, selectedCat);
        for (Book b : filtered) {
            String status = b.getAvailableCopies() > 0 ? "Available" : "Out of Stock";
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getPublisher(),
                    b.getPublicationYear() > 0 ? b.getPublicationYear() : "-",
                    b.getQuantity(),
                    b.getAvailableCopies(),
                    status
            });
        }
    }

    private void editSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        Book book = bookDao.getBookById(bookId);
        if (book != null) {
            showBookDialog(book);
        }
    }

    private void deleteSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the book:\n\"" + title + "\" (ID: " + bookId + ")?\nThis will also remove associated loan history.",
                "Confirm Book Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = bookDao.deleteBook(bookId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete book. Please check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Dialog to Add or Edit a Book with input validation.
     */
    private void showBookDialog(Book existingBook) {
        boolean isEdit = (existingBook != null);
        String dialogTitle = isEdit ? "Edit Book (ID: " + existingBook.getId() + ")" : "Add New Book to Catalog";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), dialogTitle, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new GridLayout(8, 2, 10, 14));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField isbnTxt = UIUtils.createTextField(15);
        JTextField titleTxt = UIUtils.createTextField(15);
        JTextField authorTxt = UIUtils.createTextField(15);
        JTextField categoryTxt = UIUtils.createTextField(15);
        JTextField publisherTxt = UIUtils.createTextField(15);
        JTextField yearTxt = UIUtils.createTextField(15);
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        quantitySpinner.setFont(UITheme.FONT_REGULAR);
        quantitySpinner.setPreferredSize(new Dimension(100, 36));

        if (isEdit) {
            isbnTxt.setText(existingBook.getIsbn());
            titleTxt.setText(existingBook.getTitle());
            authorTxt.setText(existingBook.getAuthor());
            categoryTxt.setText(existingBook.getCategory());
            publisherTxt.setText(existingBook.getPublisher() != null ? existingBook.getPublisher() : "");
            yearTxt.setText(existingBook.getPublicationYear() > 0 ? String.valueOf(existingBook.getPublicationYear()) : "");
            quantitySpinner.setValue(existingBook.getQuantity());
        }

        content.add(createFormLabel("ISBN (*):"));
        content.add(isbnTxt);
        content.add(createFormLabel("Book Title (*):"));
        content.add(titleTxt);
        content.add(createFormLabel("Author (*):"));
        content.add(authorTxt);
        content.add(createFormLabel("Category (*):"));
        content.add(categoryTxt);
        content.add(createFormLabel("Publisher:"));
        content.add(publisherTxt);
        content.add(createFormLabel("Publication Year:"));
        content.add(yearTxt);
        content.add(createFormLabel("Total Copies:"));
        content.add(quantitySpinner);

        // Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnBar.setBackground(UITheme.BG_APP);
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UIUtils.createPrimaryButton(isEdit ? "Update Book" : "Save Book");
        saveBtn.addActionListener(e -> {
            String isbn = isbnTxt.getText().trim();
            String title = titleTxt.getText().trim();
            String author = authorTxt.getText().trim();
            String category = categoryTxt.getText().trim();
            String publisher = publisherTxt.getText().trim();
            String yearStr = yearTxt.getText().trim();
            int qty = (int) quantitySpinner.getValue();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields (ISBN, Title, Author, Category).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int year = 0;
            if (!yearStr.isEmpty()) {
                try {
                    year = Integer.parseInt(yearStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Publication Year must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (isEdit) {
                int oldQty = existingBook.getQuantity();
                int oldAvail = existingBook.getAvailableCopies();
                int qtyDiff = qty - oldQty;
                int newAvail = Math.max(0, oldAvail + qtyDiff);

                existingBook.setIsbn(isbn);
                existingBook.setTitle(title);
                existingBook.setAuthor(author);
                existingBook.setCategory(category);
                existingBook.setPublisher(publisher);
                existingBook.setPublicationYear(year);
                existingBook.setQuantity(qty);
                existingBook.setAvailableCopies(newAvail);

                if (bookDao.updateBook(existingBook)) {
                    JOptionPane.showMessageDialog(dialog, "Book updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update book.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                Book newBook = new Book(isbn, title, author, category, publisher, year, qty, qty);
                if (bookDao.addBook(newBook)) {
                    JOptionPane.showMessageDialog(dialog, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add book. ISBN may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
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
