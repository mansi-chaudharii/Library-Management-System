-- ====================================================================
-- Library Management System - Database Schema & Initial Data
-- Subject: Database Management Systems / Object Oriented Programming
-- Author: 2nd Year Computer Engineering Student Project
-- Compatible with: MySQL 5.7+ / MySQL 8.0+ / MariaDB
-- ====================================================================

-- 1. Create Database if it does not already exist
CREATE DATABASE IF NOT EXISTS `library_db`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `library_db`;

-- ====================================================================
-- Table 1: Users (Admin / Librarians login credentials)
-- ====================================================================
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(30) DEFAULT 'Librarian',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ====================================================================
-- Table 2: Books (Book inventory and available copy tracking)
-- ====================================================================
CREATE TABLE IF NOT EXISTS `books` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `isbn` VARCHAR(20) NOT NULL UNIQUE,
    `title` VARCHAR(200) NOT NULL,
    `author` VARCHAR(150) NOT NULL,
    `category` VARCHAR(100) NOT NULL,
    `publisher` VARCHAR(150),
    `publication_year` INT,
    `quantity` INT NOT NULL DEFAULT 1,
    `available_copies` INT NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ====================================================================
-- Table 3: Members (Students and faculty registered with library)
-- ====================================================================
CREATE TABLE IF NOT EXISTS `members` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `member_code` VARCHAR(30) NOT NULL UNIQUE,
    `name` VARCHAR(120) NOT NULL,
    `email` VARCHAR(120) NOT NULL UNIQUE,
    `phone` VARCHAR(25) NOT NULL,
    `department` VARCHAR(100) NOT NULL,
    `join_date` DATE NOT NULL,
    `status` VARCHAR(20) DEFAULT 'Active',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ====================================================================
-- Table 4: Issue Records (Book borrowing and return tracking)
-- ====================================================================
CREATE TABLE IF NOT EXISTS `issue_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `book_id` INT NOT NULL,
    `member_id` INT NOT NULL,
    `issue_date` DATE NOT NULL,
    `due_date` DATE NOT NULL,
    `return_date` DATE DEFAULT NULL,
    `fine_amount` DECIMAL(8, 2) DEFAULT 0.00,
    `status` VARCHAR(20) DEFAULT 'Issued', -- 'Issued', 'Returned', 'Overdue'
    `notes` VARCHAR(255) DEFAULT '',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_issue_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_issue_member` FOREIGN KEY (`member_id`) REFERENCES `members`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ====================================================================
-- Initial Default Admin User
-- Default Credentials: username = admin, password = admin123
-- ====================================================================
INSERT INTO `users` (`username`, `password`, `full_name`, `role`)
VALUES ('admin', 'admin123', 'Library Administrator', 'Admin')
ON DUPLICATE KEY UPDATE `username` = `username`;

-- ====================================================================
-- Sample Books (Computer Engineering & Technology Core Titles)
-- ====================================================================
INSERT INTO `books` (`isbn`, `title`, `author`, `category`, `publisher`, `publication_year`, `quantity`, `available_copies`)
VALUES
('978-0131103627', 'The C Programming Language', 'Brian W. Kernighan, Dennis M. Ritchie', 'Programming', 'Prentice Hall', 1988, 5, 4),
('978-0262033848', 'Introduction to Algorithms (CLRS)', 'Thomas H. Cormen, Charles E. Leiserson', 'Computer Science', 'MIT Press', 2009, 6, 5),
('978-0132143011', 'Computer Networks', 'Andrew S. Tanenbaum, David J. Wetherall', 'Networking', 'Pearson', 2011, 4, 3),
('978-0132350884', 'Clean Code: A Handbook of Agile Software', 'Robert C. Martin', 'Software Engineering', 'Prentice Hall', 2008, 5, 5),
('978-0073523323', 'Database System Concepts', 'Abraham Silberschatz, Henry F. Korth', 'Database Systems', 'McGraw-Hill', 2010, 7, 6),
('978-0133594140', 'Operating System Concepts', 'Abraham Silberschatz, Peter B. Galvin', 'Operating Systems', 'Wiley', 2018, 5, 4),
('978-0134685991', 'Effective Java (3rd Edition)', 'Joshua Bloch', 'Programming', 'Addison-Wesley', 2018, 4, 3),
('978-1449355739', 'Learning Python (5th Edition)', 'Mark Lutz', 'Programming', 'O''Reilly Media', 2013, 3, 3)
ON DUPLICATE KEY UPDATE `isbn` = `isbn`;

-- ====================================================================
-- Sample Registered Members (Students)
-- ====================================================================
INSERT INTO `members` (`member_code`, `name`, `email`, `phone`, `department`, `join_date`, `status`)
VALUES
('STU-2024-001', 'Aarav Sharma', 'aarav.sharma@college.edu', '9876543210', 'Computer Engineering', '2024-08-01', 'Active'),
('STU-2024-002', 'Diya Patel', 'diya.patel@college.edu', '9876543211', 'Information Technology', '2024-08-01', 'Active'),
('STU-2024-003', 'Rohan Kulkarni', 'rohan.kulkarni@college.edu', '9876543212', 'Computer Engineering', '2024-08-05', 'Active'),
('STU-2024-004', 'Ananya Gupta', 'ananya.gupta@college.edu', '9876543213', 'Electronics & Comm.', '2024-08-10', 'Active'),
('STU-2024-005', 'Karthik Nair', 'karthik.nair@college.edu', '9876543214', 'Mechanical Engineering', '2024-08-15', 'Active')
ON DUPLICATE KEY UPDATE `member_code` = `member_code`;

-- ====================================================================
-- Sample Issue Records (Active, Returned, and an Overdue example)
-- ====================================================================
INSERT INTO `issue_records` (`book_id`, `member_id`, `issue_date`, `due_date`, `return_date`, `fine_amount`, `status`, `notes`)
VALUES
(1, 1, DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), NULL, 0.00, 'Issued', 'Standard issue for semester exam'),
(3, 2, DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), NULL, 30.00, 'Overdue', 'Reminder email sent'),
(5, 3, DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 0.00, 'Returned', 'Returned in good condition'),
(6, 4, DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 9 DAY), NULL, 0.00, 'Issued', 'Reference copy')
ON DUPLICATE KEY UPDATE `id` = `id`;
