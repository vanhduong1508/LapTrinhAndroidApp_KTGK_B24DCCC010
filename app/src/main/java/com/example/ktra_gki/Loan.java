package com.example.ktra_gki;

public class Loan {
    private int loanId;
    private String bookId;
    private String bookTitle;
    private String borrowerName;
    private String borrowerPhone;
    private String borrowDate;
    private String dueDate;

    public Loan(int loanId, String bookId, String bookTitle, String borrowerName,
                String borrowerPhone, String borrowDate, String dueDate) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowerName = borrowerName;
        this.borrowerPhone = borrowerPhone;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    public int getLoanId() { return loanId; }
    public String getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getBorrowerName() { return borrowerName; }
    public String getBorrowerPhone() { return borrowerPhone; }
    public String getBorrowDate() { return borrowDate; }
    public String getDueDate() { return dueDate; }
}
