package com.library.model;

import java.time.LocalDate;

/**
 * Model class representing a Library Member (Student, Faculty, or Staff).
 * Stores personal details, college department, membership status, and join date.
 */
public class Member {
    private int id;
    private String memberCode;
    private String name;
    private String email;
    private String phone;
    private String department;
    private LocalDate joinDate;
    private String status; // "Active", "Suspended", "Inactive"

    public Member() {
        this.joinDate = LocalDate.now();
        this.status = "Active";
    }

    public Member(int id, String memberCode, String name, String email, 
                  String phone, String department, LocalDate joinDate, String status) {
        this.id = id;
        this.memberCode = memberCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.joinDate = joinDate != null ? joinDate : LocalDate.now();
        this.status = status != null ? status : "Active";
    }

    public Member(String memberCode, String name, String email, 
                  String phone, String department, LocalDate joinDate, String status) {
        this(-1, memberCode, name, email, phone, department, joinDate, status);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return name + " (" + memberCode + ") - " + department;
    }
}
