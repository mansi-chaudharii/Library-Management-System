package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Book catalog CRUD operations using JDBC.
 */
public class BookDao {

    public List<Book> getAllBooks() {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getAllBooks();
        }

        List<Book> list = new ArrayList<>();
        String sql = "SELECT id, isbn, title, author, category, publisher, publication_year, quantity, available_copies " +
                     "FROM books ORDER BY id DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getAllBooks();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books: " + e.getMessage());
        }
        return list;
    }

    public Book getBookById(int id) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getBookById(id);
        }

        String sql = "SELECT id, isbn, title, author, category, publisher, publication_year, quantity, available_copies " +
                     "FROM books WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getBookById(id);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting book by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Book> searchBooks(String keyword, String category) {
        if (DatabaseConnection.isUsingMock()) {
            List<Book> all = MockDataStore.getInstance().getAllBooks();
            List<Book> filtered = new ArrayList<>();
            String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();
            String cat = (category == null || "All Categories".equalsIgnoreCase(category)) ? "" : category.trim().toLowerCase();

            for (Book b : all) {
                boolean matchCat = cat.isEmpty() || b.getCategory().toLowerCase().contains(cat);
                boolean matchKw = kw.isEmpty() ||
                        b.getTitle().toLowerCase().contains(kw) ||
                        b.getAuthor().toLowerCase().contains(kw) ||
                        b.getIsbn().toLowerCase().contains(kw);
                if (matchCat && matchKw) {
                    filtered.add(b);
                }
            }
            return filtered;
        }

        List<Book> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, isbn, title, author, category, publisher, publication_year, quantity, available_copies FROM books WHERE 1=1 ");
        
        boolean hasCategory = category != null && !category.trim().isEmpty() && !"All Categories".equalsIgnoreCase(category);
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasCategory) {
            sql.append(" AND category = ?");
        }
        if (hasKeyword) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ?)");
        }
        sql.append(" ORDER BY id DESC");

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return list;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasCategory) {
                stmt.setString(paramIndex++, category.trim());
            }
            if (hasKeyword) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                stmt.setString(paramIndex++, pattern);
                stmt.setString(paramIndex++, pattern);
                stmt.setString(paramIndex++, pattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching books: " + e.getMessage());
        }
        return list;
    }

    public boolean addBook(Book book) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().addBook(book);
        }

        String sql = "INSERT INTO books (isbn, title, author, category, publisher, publication_year, quantity, available_copies) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().addBook(book);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCategory());
            stmt.setString(5, book.getPublisher());
            stmt.setInt(6, book.getPublicationYear());
            stmt.setInt(7, book.getQuantity());
            stmt.setInt(8, book.getAvailableCopies());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        book.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
        }
        return false;
    }

    public boolean updateBook(Book book) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().updateBook(book);
        }

        String sql = "UPDATE books SET isbn = ?, title = ?, author = ?, category = ?, " +
                     "publisher = ?, publication_year = ?, quantity = ?, available_copies = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().updateBook(book);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getCategory());
            stmt.setString(5, book.getPublisher());
            stmt.setInt(6, book.getPublicationYear());
            stmt.setInt(7, book.getQuantity());
            stmt.setInt(8, book.getAvailableCopies());
            stmt.setInt(9, book.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteBook(int id) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().deleteBook(id);
        }

        String sql = "DELETE FROM books WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().deleteBook(id);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
        }
        return false;
    }

    public int getTotalBookTitles() {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getAllBooks().size();
        }

        String sql = "SELECT COUNT(*) FROM books";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getAllBooks().size();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting books: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalCopiesCount() {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getAllBooks().stream().mapToInt(Book::getQuantity).sum();
        }

        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM books";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getAllBooks().stream().mapToInt(Book::getQuantity).sum();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error summing total copies: " + e.getMessage());
        }
        return 0;
    }

    public int getAvailableCopiesCount() {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getAllBooks().stream().mapToInt(Book::getAvailableCopies).sum();
        }

        String sql = "SELECT COALESCE(SUM(available_copies), 0) FROM books";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getAllBooks().stream().mapToInt(Book::getAvailableCopies).sum();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error summing available copies: " + e.getMessage());
        }
        return 0;
    }

    public List<String> getDistinctCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");

        if (DatabaseConnection.isUsingMock()) {
            for (Book b : MockDataStore.getInstance().getAllBooks()) {
                if (!categories.contains(b.getCategory())) {
                    categories.add(b.getCategory());
                }
            }
            return categories;
        }

        String sql = "SELECT DISTINCT category FROM books WHERE category IS NOT NULL AND category != '' ORDER BY category ASC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return categories;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        }
        return categories;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                rs.getString("publisher"),
                rs.getInt("publication_year"),
                rs.getInt("quantity"),
                rs.getInt("available_copies")
        );
    }
}
