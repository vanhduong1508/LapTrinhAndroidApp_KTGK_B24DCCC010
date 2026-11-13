package com.example.ktra_gki;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "LibraryManager.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE books (" +
                "book_id TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "author TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "is_borrowed INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE loans (" +
                "loan_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "book_id TEXT NOT NULL, " +
                "borrower_name TEXT NOT NULL, " +
                "borrower_phone TEXT NOT NULL, " +
                "borrow_date TEXT NOT NULL, " +
                "due_date TEXT NOT NULL, " +
                "is_active INTEGER DEFAULT 1)");

        db.execSQL("INSERT INTO books VALUES ('S001', 'Lập trình Android cơ bản', 'Nguyễn Văn A', 'Công nghệ thông tin', 0)");
        db.execSQL("INSERT INTO books VALUES ('S002', 'Cấu trúc dữ liệu và giải thuật', 'Trần Thị B', 'Khoa học máy tính', 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS loans");
        db.execSQL("DROP TABLE IF EXISTS books");
        onCreate(db);
    }

    public boolean isBookIdExists(String bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM books WHERE book_id = ?", new String[]{bookId});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean addBook(Book book) {
        if (isBookIdExists(book.getBookId())) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("book_id", book.getBookId());
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("category", book.getCategory());
        values.put("is_borrowed", 0);
        return db.insert("books", null, values) != -1;
    }

    public boolean updateBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("category", book.getCategory());
        return db.update("books", values, "book_id = ?", new String[]{book.getBookId()}) > 0;
    }

    public boolean deleteBook(String bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("books", "book_id = ? AND is_borrowed = 0", new String[]{bookId}) > 0;
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY book_id ASC";
        String arg = "%" + keyword.trim() + "%";
        Cursor cursor = db.rawQuery(query, new String[]{arg, arg});

        if (cursor.moveToFirst()) {
            do {
                list.add(new Book(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Book> getAvailableBooks() {
        List<Book> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM books WHERE is_borrowed = 0 ORDER BY book_id ASC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Book(cursor.getString(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), false));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean createLoan(Loan loan) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT is_borrowed FROM books WHERE book_id = ?", new String[]{loan.getBookId()});
            if (!c.moveToFirst() || c.getInt(0) == 1) {
                c.close();
                return false;
            }
            c.close();

            ContentValues loanValues = new ContentValues();
            loanValues.put("book_id", loan.getBookId());
            loanValues.put("borrower_name", loan.getBorrowerName());
            loanValues.put("borrower_phone", loan.getBorrowerPhone());
            loanValues.put("borrow_date", loan.getBorrowDate());
            loanValues.put("due_date", loan.getDueDate());
            loanValues.put("is_active", 1);
            db.insert("loans", null, loanValues);

            ContentValues bookValues = new ContentValues();
            bookValues.put("is_borrowed", 1);
            db.update("books", bookValues, "book_id = ?", new String[]{loan.getBookId()});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<Loan> getActiveLoans() {
        List<Loan> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT l.loan_id, l.book_id, b.title, l.borrower_name, l.borrower_phone, l.borrow_date, l.due_date " +
                "FROM loans l INNER JOIN books b ON l.book_id = b.book_id " +
                "WHERE l.is_active = 1 ORDER BY l.loan_id DESC";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Loan(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean returnBook(int loanId, String bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues loanValues = new ContentValues();
            loanValues.put("is_active", 0);
            db.update("loans", loanValues, "loan_id = ?", new String[]{String.valueOf(loanId)});

            ContentValues bookValues = new ContentValues();
            bookValues.put("is_borrowed", 0);
            db.update("books", bookValues, "book_id = ?", new String[]{bookId});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }
}