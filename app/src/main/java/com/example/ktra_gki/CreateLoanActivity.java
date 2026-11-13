package com.example.ktra_gki;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateLoanActivity extends AppCompatActivity {

    private Spinner spnAvailableBooks;
    private EditText edtBorrowerName, edtBorrowerPhone, edtBorrowDate, edtDueDate;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_loan);

        View mainRoot = findViewById(R.id.mainRoot);
        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        spnAvailableBooks = findViewById(R.id.spnAvailableBooks);
        edtBorrowerName = findViewById(R.id.edtBorrowerName);
        edtBorrowerPhone = findViewById(R.id.edtBorrowerPhone);
        edtBorrowDate = findViewById(R.id.edtBorrowDate);
        edtDueDate = findViewById(R.id.edtDueDate);
        Button btnSaveLoan = findViewById(R.id.btnSaveLoan);
        Button btnCancelLoan = findViewById(R.id.btnCancelLoan);
        View btnCancelHeader = findViewById(R.id.btnCancelLoanHeader);

        if (btnCancelHeader != null) {
            btnCancelHeader.setOnClickListener(v -> finish());
        }

        List<Book> availableBooks = dbHelper.getAvailableBooks();
        ArrayAdapter<Book> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, availableBooks);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAvailableBooks.setAdapter(spinnerAdapter);

        if (availableBooks.isEmpty()) {
            Toast.makeText(this, "Không còn sách nào có sẵn để mượn!", Toast.LENGTH_LONG).show();
            btnSaveLoan.setEnabled(false);
        }

        edtBorrowDate.setOnClickListener(v -> showDatePicker(edtBorrowDate));
        edtDueDate.setOnClickListener(v -> showDatePicker(edtDueDate));

        btnSaveLoan.setOnClickListener(v -> saveLoan());
        if (btnCancelLoan != null) btnCancelLoan.setOnClickListener(v -> finish());
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            targetEditText.setText(dateStr);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveLoan() {
        Book selectedBook = (Book) spnAvailableBooks.getSelectedItem();
        if (selectedBook == null) return;

        String name = edtBorrowerName.getText().toString().trim();
        String phone = edtBorrowerPhone.getText().toString().trim();
        String borrowDate = edtBorrowDate.getText().toString().trim();
        String dueDate = edtDueDate.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { edtBorrowerName.setError("Nhập họ tên người mượn"); return; }
        if (!phone.matches("^0\\d{9}$")) { edtBorrowerPhone.setError("SĐT phải gồm 10 chữ số, bắt đầu bằng 0"); return; }
        if (TextUtils.isEmpty(borrowDate)) { Toast.makeText(this, "Vui lòng chọn ngày mượn", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(dueDate)) { Toast.makeText(this, "Vui lòng chọn ngày dự kiến trả", Toast.LENGTH_SHORT).show(); return; }

        Loan loan = new Loan(0, selectedBook.getBookId(), selectedBook.getTitle(),
                name, phone, borrowDate, dueDate);

        if (dbHelper.createLoan(loan)) {
            Toast.makeText(this, "Tạo phiếu mượn thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Sách này vừa được mượn bởi người khác!", Toast.LENGTH_SHORT).show();
        }
    }
}