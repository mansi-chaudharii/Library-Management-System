package com.library.dao;

import com.library.config.DatabaseConnection;
import com.library.model.Member;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Member / Student management using JDBC.
 */
public class MemberDao {

    public List<Member> getAllMembers() {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getAllMembers();
        }

        List<Member> list = new ArrayList<>();
        String sql = "SELECT id, member_code, name, email, phone, department, join_date, status " +
                     "FROM members ORDER BY id DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getAllMembers();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching members: " + e.getMessage());
        }
        return list;
    }

    public Member getMemberById(int id) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getMemberById(id);
        }

        String sql = "SELECT id, member_code, name, email, phone, department, join_date, status " +
                     "FROM members WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getMemberById(id);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting member by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Member> searchMembers(String keyword, String department) {
        if (DatabaseConnection.isUsingMock()) {
            List<Member> all = MockDataStore.getInstance().getAllMembers();
            List<Member> filtered = new ArrayList<>();
            String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();
            String dept = (department == null || "All Departments".equalsIgnoreCase(department)) ? "" : department.trim().toLowerCase();

            for (Member m : all) {
                boolean matchDept = dept.isEmpty() || m.getDepartment().toLowerCase().contains(dept);
                boolean matchKw = kw.isEmpty() ||
                        m.getName().toLowerCase().contains(kw) ||
                        m.getMemberCode().toLowerCase().contains(kw) ||
                        m.getEmail().toLowerCase().contains(kw) ||
                        m.getPhone().toLowerCase().contains(kw);
                if (matchDept && matchKw) {
                    filtered.add(m);
                }
            }
            return filtered;
        }

        List<Member> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, member_code, name, email, phone, department, join_date, status FROM members WHERE 1=1 ");

        boolean hasDept = department != null && !department.trim().isEmpty() && !"All Departments".equalsIgnoreCase(department);
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasDept) {
            sql.append(" AND department = ?");
        }
        if (hasKeyword) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(member_code) LIKE ? OR LOWER(email) LIKE ? OR LOWER(phone) LIKE ?)");
        }
        sql.append(" ORDER BY id DESC");

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasDept) {
                stmt.setString(paramIndex++, department.trim());
            }
            if (hasKeyword) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                stmt.setString(paramIndex++, pattern);
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
            System.err.println("Error searching members: " + e.getMessage());
        }
        return list;
    }

    public boolean addMember(Member member) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().addMember(member);
        }

        String sql = "INSERT INTO members (member_code, name, email, phone, department, join_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().addMember(member);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, member.getMemberCode());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getDepartment());
            stmt.setDate(6, Date.valueOf(member.getJoinDate()));
            stmt.setString(7, member.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        member.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
        }
        return false;
    }

    public boolean updateMember(Member member) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().updateMember(member);
        }

        String sql = "UPDATE members SET member_code = ?, name = ?, email = ?, phone = ?, " +
                     "department = ?, join_date = ?, status = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().updateMember(member);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getMemberCode());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getDepartment());
            stmt.setDate(6, Date.valueOf(member.getJoinDate()));
            stmt.setString(7, member.getStatus());
            stmt.setInt(8, member.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating member: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteMember(int id) {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().deleteMember(id);
        }

        String sql = "DELETE FROM members WHERE id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().deleteMember(id);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting member: " + e.getMessage());
        }
        return false;
    }

    public int getTotalMembersCount() {
        if (DatabaseConnection.isUsingMock()) {
            return MockDataStore.getInstance().getAllMembers().size();
        }

        String sql = "SELECT COUNT(*) FROM members";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return MockDataStore.getInstance().getAllMembers().size();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting members: " + e.getMessage());
        }
        return 0;
    }

    public List<String> getDistinctDepartments() {
        List<String> list = new ArrayList<>();
        list.add("All Departments");

        if (DatabaseConnection.isUsingMock()) {
            for (Member m : MockDataStore.getInstance().getAllMembers()) {
                if (!list.contains(m.getDepartment())) {
                    list.add(m.getDepartment());
                }
            }
            return list;
        }

        String sql = "SELECT DISTINCT department FROM members WHERE department IS NOT NULL AND department != '' ORDER BY department ASC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("department"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching departments: " + e.getMessage());
        }
        return list;
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Date d = rs.getDate("join_date");
        LocalDate joinDate = (d != null) ? d.toLocalDate() : LocalDate.now();
        return new Member(
                rs.getInt("id"),
                rs.getString("member_code"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("department"),
                joinDate,
                rs.getString("status")
        );
    }
}
