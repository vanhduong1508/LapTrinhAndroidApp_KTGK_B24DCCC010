package com.example.ktra_gki;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout layoutBookList;
    private EditText edtSearch;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View mainRoot = findViewById(R.id.mainRoot);
        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        edtSearch = findViewById(R.id.edtSearch);
        layoutBookList = findViewById(R.id.layoutBookList);
        bottomNav = findViewById(R.id.bottomNav);

        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_books).setChecked(true);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_add_book) {
                    Intent intent = new Intent(MainActivity.this, AddEditBookActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_loans) {
                    Intent intent = new Intent(MainActivity.this, LoanManagementActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return true;
            });
        }

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadBooks(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_books).setChecked(true);
        }
        loadBooks(edtSearch.getText().toString());
    }

    private void loadBooks(String keyword) {
        layoutBookList.removeAllViews();
        List<Book> books = dbHelper.searchBooks(keyword);

        if (books.isEmpty()) {
            LinearLayout emptyLayout = new LinearLayout(this);
            emptyLayout.setOrientation(LinearLayout.VERTICAL);
            emptyLayout.setGravity(Gravity.CENTER);
            emptyLayout.setPadding(0, 80, 0, 80);

            ImageView ivEmpty = new ImageView(this);
            ivEmpty.setImageResource(R.drawable.ic_empty_box);
            ivEmpty.setColorFilter(ContextCompat.getColor(this, R.color.text_muted));
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(100, 100);
            ivEmpty.setLayoutParams(imgParams);

            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không tìm thấy cuốn sách nào.");
            tvEmpty.setTextSize(14f);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvEmpty.setPadding(0, 12, 0, 0);

            emptyLayout.addView(ivEmpty);
            emptyLayout.addView(tvEmpty);

            layoutBookList.addView(emptyLayout);
            return;
        }

        for (Book book : books) {
            View itemView = getLayoutInflater().inflate(R.layout.item_book, layoutBookList, false);

            TextView tvTitle = itemView.findViewById(R.id.tvBookTitle);
            TextView tvId = itemView.findViewById(R.id.tvBookId);
            TextView tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            TextView tvCategory = itemView.findViewById(R.id.tvBookCategory);
            TextView tvStatus = itemView.findViewById(R.id.tvBookStatus);
            Button btnEdit = itemView.findViewById(R.id.btnEditBook);
            Button btnDelete = itemView.findViewById(R.id.btnDeleteBook);

            tvTitle.setText(book.getTitle());
            tvId.setText("Mã sách: " + book.getBookId());
            tvAuthor.setText("Tác giả: " + book.getAuthor());
            tvCategory.setText(book.getCategory());

            if (book.isBorrowed()) {
                tvStatus.setText("Đang mượn");
                tvStatus.setBackgroundResource(R.drawable.bg_badge_borrowed);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_borrowed_text));
            } else {
                tvStatus.setText("Có sẵn");
                tvStatus.setBackgroundResource(R.drawable.bg_badge_available);
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
            }

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddEditBookActivity.class);
                intent.putExtra("EXTRA_BOOK_ID", book.getBookId());
                intent.putExtra("EXTRA_TITLE", book.getTitle());
                intent.putExtra("EXTRA_AUTHOR", book.getAuthor());
                intent.putExtra("EXTRA_CATEGORY", book.getCategory());
                startActivity(intent);
            });

            btnDelete.setOnClickListener(v -> {
                if (book.isBorrowed()) {
                    Toast.makeText(MainActivity.this, "Không thể xóa sách đang được mượn!", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Xóa sách \"" + book.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            dbHelper.deleteBook(book.getBookId());
                            loadBooks(edtSearch.getText().toString());
                            Toast.makeText(MainActivity.this, "Đã xóa sách", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            layoutBookList.addView(itemView);
        }
    }
}