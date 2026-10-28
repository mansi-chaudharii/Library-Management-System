package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.IssueRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Book Issue & Return transactions.
 * Uses JDBC transactions to guarantee book inventory consistency.
 */
public class IssueDao {

    /**
     * Issues a book to a member using a transactional commit.
     * Decrements book available copies and records the issue transaction.
     */
    public boolean issueBook(int bookId, int memberId, LocalDate issueDate, LocalDate dueDate, String notes) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().issueBook(bookId, memberId, issueDate, dueDate, notes);
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().issueBook(bookId, memberId, issueDate, dueDate, notes);
        }

        String checkAvailabilitySql = "SELECT available_copies FROM books WHERE id = ? FOR UPDATE";
        String decrementBookSql = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ?";
        String insertIssueSql = "INSERT INTO issue_records (book_id, member_id, issue_date, due_date, status, notes) " +
                                "VALUES (?, ?, ?, ?, 'Issued', ?)";

        try {
            conn.setAutoCommit(false); // Start transaction

            // 1. Check availability
            try (PreparedStatement checkStmt = conn.prepareStatement(checkAvailabilitySql)) {
                checkStmt.setInt(1, bookId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next() || rs.getInt("available_copies") <= 0) {
                        conn.rollback();
                        return false; // Book not available
                    }
                }
            }

            // 2. Decrement available copies
            try (PreparedStatement decStmt = conn.prepareStatement(decrementBookSql)) {
                decStmt.setInt(1, bookId);
                decStmt.executeUpdate();
            }

            // 3. Insert issue record
            try (PreparedStatement issueStmt = conn.prepareStatement(insertIssueSql)) {
                issueStmt.setInt(1, bookId);
                issueStmt.setInt(2, memberId);
                issueStmt.setDate(3, Date.valueOf(issueDate));
                issueStmt.setDate(4, Date.valueOf(dueDate));
                issueStmt.setString(5, notes != null ? notes : "");
                issueStmt.executeUpdate();
            }

            conn.commit(); // Transaction success
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error issuing book: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Returns an issued book using a transaction.
     * Updates return date, fine amount, status, and restores available copies.
     */
    public boolean returnBook(int issueId, LocalDate returnDate, double fineAmount) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().returnBook(issueId, returnDate, fineAmount);
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().returnBook(issueId, returnDate, fineAmount);
        }

        String findIssueSql = "SELECT book_id, status FROM issue_records WHERE id = ? FOR UPDATE";
        String updateIssueSql = "UPDATE issue_records SET return_date = ?, fine_amount = ?, status = 'Returned' WHERE id = ?";
        String incrementBookSql = "UPDATE books SET available_copies = LEAST(quantity, available_copies + 1) WHERE id = ?";

        try {
            conn.setAutoCommit(false); // Start transaction

            int bookId = -1;
            try (PreparedStatement findStmt = conn.prepareStatement(findIssueSql)) {
                findStmt.setInt(1, issueId);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        if ("Returned".equalsIgnoreCase(rs.getString("status"))) {
                            conn.rollback();
                            return false; // Already returned
                        }
                        bookId = rs.getInt("book_id");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Update Issue Record
            try (PreparedStatement updateStmt = conn.prepareStatement(updateIssueSql)) {
                updateStmt.setDate(1, Date.valueOf(returnDate));
                updateStmt.setBigDecimal(2, java.math.BigDecimal.valueOf(fineAmount));
                updateStmt.setInt(3, issueId);
                updateStmt.executeUpdate();
            }

            // Increment Book Available Copies
            try (PreparedStatement incStmt = conn.prepareStatement(incrementBookSql)) {
                incStmt.setInt(1, bookId);
                incStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            System.err.println("Error returning book: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Retrieves all issue records with joined book and member metadata.
     */
    public List<IssueRecord> getAllIssues() {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getAllIssues();
        }

        List<IssueRecord> list = new ArrayList<>();
        String sql = "SELECT i.id, i.book_id, b.title AS book_title, b.isbn AS book_isbn, " +
                     "i.member_id, m.name AS member_name, m.member_code, " +
                     "i.issue_date, i.due_date, i.return_date, i.fine_amount, i.status, i.notes " +
                     "FROM issue_records i " +
                     "JOIN books b ON i.book_id = b.id " +
                     "JOIN members m ON i.member_id = m.id " +
                     "ORDER BY i.id DESC";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getAllIssues();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching issues: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves active loans (books not yet returned).
     */
    public List<IssueRecord> getActiveIssues() {
        List<IssueRecord> all = getAllIssues();
        List<IssueRecord> active = new ArrayList<>();
        for (IssueRecord r : all) {
            if (!"Returned".equalsIgnoreCase(r.getStatus())) {
                active.add(r);
            }
        }
        return active;
    }

    /**
     * Retrieves overdue loans.
     */
    public List<IssueRecord> getOverdueIssues() {
        List<IssueRecord> all = getAllIssues();
        List<IssueRecord> overdue = new ArrayList<>();
        for (IssueRecord r : all) {
            if (r.isCurrentlyOverdue()) {
                overdue.add(r);
            }
        }
        return overdue;
    }

    /**
     * Counts currently issued (unreturned) books.
     */
    public int getActiveIssuedCount() {
        if (DatabaseConnection.isUsingMock()) {
            return (int) MockDataStore.getInstance().getAllIssues().stream()
                    .filter(i -> !"Returned".equalsIgnoreCase(i.getStatus()))
                    .count();
        }

        String sql = "SELECT COUNT(*) FROM issue_records WHERE status != 'Returned' AND return_date IS NULL";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return getActiveIssues().size();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting active issues: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts currently overdue books.
     */
    public int getOverdueCount() {
        if (DatabaseConnection.isUsingMock()) {
            return (int) MockDataStore.getInstance().getAllIssues().stream()
                    .filter(IssueRecord::isCurrentlyOverdue)
                    .count();
        }

        String sql = "SELECT COUNT(*) FROM issue_records WHERE status != 'Returned' AND due_date < CURRENT_DATE";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return getOverdueIssues().size();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting overdue: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns total fines collected in the system.
     */
    public double getTotalFinesCollected() {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getAllIssues().stream()
                    .filter(i -> "Returned".equalsIgnoreCase(i.getStatus()))
                    .mapToDouble(IssueRecord::getFineAmount)
                    .sum();
        }

        String sql = "SELECT COALESCE(SUM(fine_amount), 0) FROM issue_records WHERE status = 'Returned'";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0.0;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error calculating fines: " + e.getMessage());
        }
        return 0.0;
    }

    private IssueRecord mapRow(ResultSet rs) throws SQLException {
        Date issueD = rs.getDate("issue_date");
        Date dueD = rs.getDate("due_date");
        Date retD = rs.getDate("return_date");

        LocalDate issueDate = (issueD != null) ? issueD.toLocalDate() : null;
        LocalDate dueDate = (dueD != null) ? dueD.toLocalDate() : null;
        LocalDate returnDate = (retD != null) ? retD.toLocalDate() : null;

        String status = rs.getString("status");
        if (!"Returned".equalsIgnoreCase(status) && dueDate != null && LocalDate.now().isAfter(dueDate)) {
            status = "Overdue";
        }

        return new IssueRecord(
                rs.getInt("id"),
                rs.getInt("book_id"),
                rs.getString("book_title"),
                rs.getString("book_isbn"),
                rs.getInt("member_id"),
                rs.getString("member_name"),
                rs.getString("member_code"),
                issueDate,
                dueDate,
                returnDate,
                rs.getDouble("fine_amount"),
                status,
                rs.getString("notes")
        );
    }
}
