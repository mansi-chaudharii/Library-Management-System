package com.library.dao;

import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import com.library.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory data store providing full mock CRUD capabilities.
 * Ensures the application runs smoothly even before MySQL is installed or configured.
 */
public class MockDataStore {
    private static final MockDataStore instance = new MockDataStore();

    private final List<User> users = new ArrayList<>();
    private final List<Book> books = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<IssueRecord> issueRecords = new ArrayList<>();

    private final AtomicInteger userSeq = new AtomicInteger(1);
    private final AtomicInteger bookSeq = new AtomicInteger(1);
    private final AtomicInteger memberSeq = new AtomicInteger(1);
    private final AtomicInteger issueSeq = new AtomicInteger(1);

    private MockDataStore() {
        seedInitialData();
    }

    public static MockDataStore getInstance() {
        return instance;
    }

    private synchronized void seedInitialData() {
        // Users
        users.add(new User(userSeq.getAndIncrement(), "admin", "admin123", "Library Administrator", "Admin"));
        users.add(new User(userSeq.getAndIncrement(), "librarian", "lib123", "John Doe", "Librarian"));

        // Books
        books.add(new Book(bookSeq.getAndIncrement(), "978-0131103627", "The C Programming Language", "Brian W. Kernighan, Dennis M. Ritchie", "Programming", "Prentice Hall", 1988, 5, 4));
        books.add(new Book(bookSeq.getAndIncrement(), "978-0262033848", "Introduction to Algorithms (CLRS)", "Thomas H. Cormen, Charles E. Leiserson", "Computer Science", "MIT Press", 2009, 6, 5));
        books.add(new Book(bookSeq.getAndIncrement(), "978-0132143011", "Computer Networks", "Andrew S. Tanenbaum, David J. Wetherall", "Networking", "Pearson", 2011, 4, 3));
        books.add(new Book(bookSeq.getAndIncrement(), "978-0132350884", "Clean Code: A Handbook of Agile Software", "Robert C. Martin", "Software Engineering", "Prentice Hall", 2008, 5, 5));
        books.add(new Book(bookSeq.getAndIncrement(), "978-0073523323", "Database System Concepts", "Abraham Silberschatz, Henry F. Korth", "Database Systems", "McGraw-Hill", 2010, 7, 6));
        books.add(new Book(bookSeq.getAndIncrement(), "978-0133594140", "Operating System Concepts", "Abraham Silberschatz, Peter B. Galvin", "Operating Systems", "Wiley", 2018, 5, 4));
        books.add(new Book(bookSeq.getAndIncrement(), "978-0134685991", "Effective Java (3rd Edition)", "Joshua Bloch", "Programming", "Addison-Wesley", 2018, 4, 3));
        books.add(new Book(bookSeq.getAndIncrement(), "978-1449355739", "Learning Python (5th Edition)", "Mark Lutz", "Programming", "O'Reilly Media", 2013, 3, 3));

        // Members
        members.add(new Member(memberSeq.getAndIncrement(), "STU-2024-001", "Aarav Sharma", "aarav.sharma@college.edu", "9876543210", "Computer Engineering", LocalDate.of(2024, 8, 1), "Active"));
        members.add(new Member(memberSeq.getAndIncrement(), "STU-2024-002", "Diya Patel", "diya.patel@college.edu", "9876543211", "Information Technology", LocalDate.of(2024, 8, 1), "Active"));
        members.add(new Member(memberSeq.getAndIncrement(), "STU-2024-003", "Rohan Kulkarni", "rohan.kulkarni@college.edu", "9876543212", "Computer Engineering", LocalDate.of(2024, 8, 5), "Active"));
        members.add(new Member(memberSeq.getAndIncrement(), "STU-2024-004", "Ananya Gupta", "ananya.gupta@college.edu", "9876543213", "Electronics & Comm.", LocalDate.of(2024, 8, 10), "Active"));
        members.add(new Member(memberSeq.getAndIncrement(), "STU-2024-005", "Karthik Nair", "karthik.nair@college.edu", "9876543214", "Mechanical Engineering", LocalDate.of(2024, 8, 15), "Active"));

        // Issues
        LocalDate today = LocalDate.now();
        issueRecords.add(new IssueRecord(issueSeq.getAndIncrement(), 1, "The C Programming Language", "978-0131103627", 1, "Aarav Sharma", "STU-2024-001", today.minusDays(10), today.plusDays(4), null, 0.0, "Issued", "Standard issue for semester exam"));
        issueRecords.add(new IssueRecord(issueSeq.getAndIncrement(), 3, "Computer Networks", "978-0132143011", 2, "Diya Patel", "STU-2024-002", today.minusDays(20), today.minusDays(6), null, 30.0, "Overdue", "Reminder email sent"));
        issueRecords.add(new IssueRecord(issueSeq.getAndIncrement(), 5, "Database System Concepts", "978-0073523323", 3, "Rohan Kulkarni", "STU-2024-003", today.minusDays(15), today.minusDays(1), today.minusDays(1), 0.0, "Returned", "Returned in good condition"));
        issueRecords.add(new IssueRecord(issueSeq.getAndIncrement(), 6, "Operating System Concepts", "978-0133594140", 4, "Ananya Gupta", "STU-2024-004", today.minusDays(5), today.plusDays(9), null, 0.0, "Issued", "Reference copy"));
    }

    // --- Users ---
    public synchronized User authenticate(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    // --- Books ---
    public synchronized List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public synchronized Book getBookById(int id) {
        for (Book b : books) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    public synchronized boolean addBook(Book b) {
        b.setId(bookSeq.getAndIncrement());
        return books.add(b);
    }

    public synchronized boolean updateBook(Book updated) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == updated.getId()) {
                books.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteBook(int id) {
        return books.removeIf(b -> b.getId() == id);
    }

    // --- Members ---
    public synchronized List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    public synchronized Member getMemberById(int id) {
        for (Member m : members) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    public synchronized boolean addMember(Member m) {
        m.setId(memberSeq.getAndIncrement());
        return members.add(m);
    }

    public synchronized boolean updateMember(Member updated) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId() == updated.getId()) {
                members.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteMember(int id) {
        return members.removeIf(m -> m.getId() == id);
    }

    // --- Issues ---
    public synchronized List<IssueRecord> getAllIssues() {
        return new ArrayList<>(issueRecords);
    }

    public synchronized boolean issueBook(int bookId, int memberId, LocalDate issueDate, LocalDate dueDate, String notes) {
        Book book = getBookById(bookId);
        Member member = getMemberById(memberId);
        if (book == null || member == null || book.getAvailableCopies() <= 0) {
            return false;
        }

        // Decrement book available count
        book.setAvailableCopies(book.getAvailableCopies() - 1);

        IssueRecord rec = new IssueRecord(
                issueSeq.getAndIncrement(),
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                member.getId(),
                member.getName(),
                member.getMemberCode(),
                issueDate,
                dueDate,
                null,
                0.0,
                "Issued",
                notes
        );
        issueRecords.add(0, rec);
        return true;
    }

    public synchronized boolean returnBook(int issueId, LocalDate returnDate, double fineAmount) {
        for (IssueRecord rec : issueRecords) {
            if (rec.getId() == issueId && !"Returned".equalsIgnoreCase(rec.getStatus())) {
                rec.setReturnDate(returnDate);
                rec.setFineAmount(fineAmount);
                rec.setStatus("Returned");

                // Increment available book count
                Book b = getBookById(rec.getBookId());
                if (b != null) {
                    b.setAvailableCopies(Math.min(b.getQuantity(), b.getAvailableCopies() + 1));
                }
                return true;
            }
        }
        return false;
    }
}
