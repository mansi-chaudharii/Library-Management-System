package com.library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Model class representing a Book Borrowing / Issue transaction.
 * Tracks book details, borrower details, dates, status, and overdue fines.
 */
public class IssueRecord {
    private int id;
    private int bookId;
    private String bookTitle;
    private String bookIsbn;
    private int memberId;
    private String memberName;
    private String memberCode;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fineAmount;
    private String status; // "Issued", "Returned", "Overdue"
    private String notes;

    public IssueRecord() {
        this.status = "Issued";
        this.fineAmount = 0.0;
        this.notes = "";
    }

    public IssueRecord(int id, int bookId, String bookTitle, String bookIsbn,
                       int memberId, String memberName, String memberCode,
                       LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                       double fineAmount, String status, String notes) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookIsbn = bookIsbn;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberCode = memberCode;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = status;
        this.notes = notes != null ? notes : "";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Calculates the number of days overdue as of a given date (or returnDate if already returned).
     */
    public long getOverdueDays(LocalDate asOfDate) {
        LocalDate checkDate = (returnDate != null) ? returnDate : asOfDate;
        if (dueDate != null && checkDate.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, checkDate);
        }
        return 0;
    }

    /**
     * Checks if the record is currently overdue (not returned and past due date).
     */
    public boolean isCurrentlyOverdue() {
        if ("Returned".equalsIgnoreCase(status)) {
            return false;
        }
        return dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return "Issue #" + id + ": " + bookTitle + " -> " + memberName + " (" + status + ")";
    }
}
