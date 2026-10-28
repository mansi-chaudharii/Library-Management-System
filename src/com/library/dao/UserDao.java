package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) for User authentication and management.
 */
public class UserDao {

    /**
     * Authenticates a user by username and password.
     * Returns the User object if credentials are valid, or null otherwise.
     */
    public User authenticate(String username, String password) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().authenticate(username, password);
        }

        String sql = "SELECT id, username, password, full_name, role, created_at " +
                     "FROM users WHERE username = ? AND password = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().authenticate(username, password);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("User authentication error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Creates a new user in the database.
     */
    public boolean createUser(User user) {
        if (DatabaseConnection.isUsingMock()) {
            // In mock mode
            return false;
        }

        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("User creation error: " + e.getMessage());
            return false;
        }
    }
}
