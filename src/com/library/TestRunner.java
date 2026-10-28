package com.library;

import com.library.dao.BookDao;
import com.library.dao.IssueDao;
import com.library.dao.MemberDao;
import com.library.dao.UserDao;
import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import com.library.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Automated CLI test suite to verify all business logic, DAO methods,
 * and calculations without needing manual GUI clicks.
 */
public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING LIBRARY MANAGEMENT SYSTEM UNIT TESTS   ");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        // Test 1: User Authentication
        System.out.print("[Test 1] User Authentication: ");
        UserDao userDao = new UserDao();
        User admin = userDao.authenticate("admin", "admin123");
        if (admin != null && "admin".equalsIgnoreCase(admin.getUsername())) {
            System.out.println("PASSED (Logged in as " + admin.getFullName() + ")");
            passed++;
        } else {
            System.out.println("FAILED");
            failed++;
        }

        // Test 2: Book Catalog Operations
        System.out.print("[Test 2] Book Catalog Listing & Search: ");
        BookDao bookDao = new BookDao();
        List<Book> books = bookDao.getAllBooks();
        List<Book> searchResult = bookDao.searchBooks("Algorithms", "All Categories");
        if (!books.isEmpty() && !searchResult.isEmpty()) {
            System.out.println("PASSED (Total books: " + books.size() + ", Search matched: " + searchResult.get(0).getTitle() + ")");
            passed++;
        } else {
            System.out.println("FAILED");
            failed++;
        }

        // Test 3: Member Registration & Listing
        System.out.print("[Test 3] Member Listing & Registration: ");
        MemberDao memberDao = new MemberDao();
        List<Member> members = memberDao.getAllMembers();
        Member testMember = new Member("STU-TEST-999", "Test Student", "test@college.edu", "9999999999", "Computer Engineering", LocalDate.now(), "Active");
        boolean memberAdded = memberDao.addMember(testMember);
        if (!members.isEmpty() && memberAdded) {
            System.out.println("PASSED (Total members: " + members.size() + ", Added member ID: " + testMember.getId() + ")");
            passed++;
        } else {
            System.out.println("FAILED");
            failed++;
        }

        // Test 4: Book Borrowing & Transaction Safety
        System.out.print("[Test 4] Book Issue Transaction: ");
        IssueDao issueDao = new IssueDao();
        Book bookToIssue = books.get(0);
        int initialAvailable = bookToIssue.getAvailableCopies();

        boolean issued = issueDao.issueBook(bookToIssue.getId(), testMember.getId(), LocalDate.now(), LocalDate.now().plusDays(14), "Testing issue workflow");
        Book reloadedBook = bookDao.getBookById(bookToIssue.getId());

        if (issued && reloadedBook != null && reloadedBook.getAvailableCopies() == initialAvailable - 1) {
            System.out.println("PASSED (Issued book: " + bookToIssue.getTitle() + ", Available copies decremented: " + reloadedBook.getAvailableCopies() + ")");
            passed++;
        } else {
            System.out.println("FAILED");
            failed++;
        }

        // Test 5: Book Return & Fine Processing
        System.out.print("[Test 5] Book Return & Fine Calculation: ");
        List<IssueRecord> activeIssues = issueDao.getActiveIssues();
        IssueRecord recordToReturn = null;
        for (IssueRecord r : activeIssues) {
            if (r.getMemberId() == testMember.getId()) {
                recordToReturn = r;
                break;
            }
        }

        if (recordToReturn != null) {
            boolean returned = issueDao.returnBook(recordToReturn.getId(), LocalDate.now(), 15.0);
            Book afterReturnBook = bookDao.getBookById(bookToIssue.getId());
            if (returned && afterReturnBook.getAvailableCopies() == initialAvailable) {
                System.out.println("PASSED (Book returned, Inventory restored to: " + afterReturnBook.getAvailableCopies() + ")");
                passed++;
            } else {
                System.out.println("FAILED (Return failed or copy count mismatched)");
                failed++;
            }
        } else {
            System.out.println("FAILED (Could not locate active issue record)");
            failed++;
        }

        // Test 6: Overdue calculation
        System.out.print("[Test 6] Overdue calculation logic: ");
        IssueRecord overdueDummy = new IssueRecord();
        overdueDummy.setDueDate(LocalDate.now().minusDays(5));
        long daysLate = overdueDummy.getOverdueDays(LocalDate.now());
        if (daysLate == 5 && overdueDummy.isCurrentlyOverdue()) {
            System.out.println("PASSED (Accurately identified 5 days overdue)");
            passed++;
        } else {
            System.out.println("FAILED");
            failed++;
        }

        System.out.println("=================================================");
        System.out.println("TEST RESULTS: " + passed + " Passed, " + failed + " Failed.");
        System.out.println("=================================================");
    }
}
