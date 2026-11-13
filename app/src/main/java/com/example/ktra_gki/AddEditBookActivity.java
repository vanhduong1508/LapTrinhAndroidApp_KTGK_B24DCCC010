package com.example.ktra_gki;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AddEditBookActivity extends AppCompatActivity {

    private EditText edtBookId, edtTitle, edtAuthor, edtCategory;
    private DatabaseHelper dbHelper;
    private boolean isEditMode = false;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);

        View mainRoot = findViewById(R.id.mainRoot);
        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        TextView tvHeader = findViewById(R.id.tvFormHeader);
        edtBookId = findViewById(R.id.edtBookId);
        edtTitle = findViewById(R.id.edtTitle);
        edtAuthor = findViewById(R.id.edtAuthor);
        edtCategory = findViewById(R.id.edtCategory);
        Button btnSave = findViewById(R.id.btnSaveBook);
        Button btnCancel = findViewById(R.id.btnCancelBook);
        View btnCancelHeader = findViewById(R.id.btnCancelBookHeader);
        bottomNav = findViewById(R.id.bottomNav);

        if (getIntent().hasExtra("EXTRA_BOOK_ID")) {
            isEditMode = true;
            tvHeader.setText("Chỉnh sửa thông tin sách");
            edtBookId.setText(getIntent().getStringExtra("EXTRA_BOOK_ID"));
            edtBookId.setEnabled(false);
            edtTitle.setText(getIntent().getStringExtra("EXTRA_TITLE"));
            edtAuthor.setText(getIntent().getStringExtra("EXTRA_AUTHOR"));
            edtCategory.setText(getIntent().getStringExtra("EXTRA_CATEGORY"));
        }

        if (bottomNav != null) {
            if (!isEditMode) {
                bottomNav.setSelectedItemId(R.id.nav_add_book);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_books);
            }
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_books) {
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_loans) {
                    Intent intent = new Intent(AddEditBookActivity.this, LoanManagementActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                }
                return true;
            });
        }

        if (btnCancelHeader != null) {
            btnCancelHeader.setOnClickListener(v -> finish());
        }

        btnSave.setOnClickListener(v -> saveBook());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> finish());
    }

    private void saveBook() {
        String id = edtBookId.getText().toString().trim();
        String title = edtTitle.getText().toString().trim();
        String author = edtAuthor.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();

        if (TextUtils.isEmpty(id)) { edtBookId.setError("Vui lòng nhập mã sách"); return; }
        if (TextUtils.isEmpty(title)) { edtTitle.setError("Vui lòng nhập tên sách"); return; }
        if (TextUtils.isEmpty(author)) { edtAuthor.setError("Vui lòng nhập tác giả"); return; }
        if (TextUtils.isEmpty(category)) { edtCategory.setError("Vui lòng nhập thể loại"); return; }

        Book book = new Book(id, title, author, category, false);

        if (isEditMode) {
            if (dbHelper.updateBook(book)) {
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            if (dbHelper.isBookIdExists(id)) {
                edtBookId.setError("Mã sách đã tồn tại!");
                return;
            }
            if (dbHelper.addBook(book)) {
                Toast.makeText(this, "Thêm sách thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}