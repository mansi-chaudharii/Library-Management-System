package com.library.model;

/**
 * Model class representing a Book in the library catalog.
 * Encapsulates book details, physical quantity, and available copies.
 */
public class Book {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private String publisher;
    private int publicationYear;
    private int quantity;
    private int availableCopies;

    public Book() {
    }

    public Book(int id, String isbn, String title, String author, String category, 
                String publisher, int publicationYear, int quantity, int availableCopies) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.quantity = quantity;
        this.availableCopies = availableCopies;
    }

    public Book(String isbn, String title, String author, String category, 
                String publisher, int publicationYear, int quantity, int availableCopies) {
        this(-1, isbn, title, author, category, publisher, publicationYear, quantity, availableCopies);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public int getBorrowedCopies() {
        return Math.max(0, quantity - availableCopies);
    }

    @Override
    public String toString() {
        return title + " (" + author + ") - " + availableCopies + " available";
    }
}
