package com.example.ktra_gki;

public class Book {
    private String bookId;
    private String title;
    private String author;
    private String category;
    private boolean isBorrowed;

    public Book(String bookId, String title, String author, String category, boolean isBorrowed) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isBorrowed = isBorrowed;
    }

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public boolean isBorrowed() { return isBorrowed; }

    @Override
    public String toString() {
        return bookId + " - " + title;
    }
}
