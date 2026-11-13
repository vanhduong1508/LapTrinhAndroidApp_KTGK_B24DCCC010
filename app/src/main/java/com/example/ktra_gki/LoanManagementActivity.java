package com.example.ktra_gki;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

public class LoanManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout layoutLoanList;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_management);

        View mainRoot = findViewById(R.id.mainRoot);
        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        dbHelper = new DatabaseHelper(this);
        layoutLoanList = findViewById(R.id.layoutLoanList);
        Button btnCreateLoan = findViewById(R.id.btnCreateLoan);
        bottomNav = findViewById(R.id.bottomNav);

        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_loans).setChecked(true);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_books) {
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_add_book) {
                    Intent intent = new Intent(LoanManagementActivity.this, AddEditBookActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0, 0);
                    return true;
                }
                return true;
            });
        }

        btnCreateLoan.setOnClickListener(v ->
                startActivity(new Intent(this, CreateLoanActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_loans).setChecked(true);
        }
        loadActiveLoans();
    }

    private void loadActiveLoans() {
        layoutLoanList.removeAllViews();
        List<Loan> loans = dbHelper.getActiveLoans();

        if (loans.isEmpty()) {
            LinearLayout emptyLayout = new LinearLayout(this);
            emptyLayout.setOrientation(LinearLayout.VERTICAL);
            emptyLayout.setGravity(Gravity.CENTER);
            emptyLayout.setPadding(0, 100, 0, 100);

            ImageView ivEmpty = new ImageView(this);
            ivEmpty.setImageResource(R.drawable.ic_empty_box);
            ivEmpty.setColorFilter(ContextCompat.getColor(this, R.color.text_muted));
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(100, 100);
            ivEmpty.setLayoutParams(imgParams);

            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Hiện không có phiếu mượn nào đang hiệu lực.");
            tvEmpty.setTextSize(14f);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvEmpty.setPadding(0, 12, 0, 0);

            emptyLayout.addView(ivEmpty);
            emptyLayout.addView(tvEmpty);

            layoutLoanList.addView(emptyLayout);
            return;
        }

        for (Loan loan : loans) {
            View itemView = getLayoutInflater().inflate(R.layout.item_loan, layoutLoanList, false);

            TextView tvBookTitle = itemView.findViewById(R.id.tvLoanBookTitle);
            TextView tvBookId = itemView.findViewById(R.id.tvLoanBookId);
            TextView tvBorrowerName = itemView.findViewById(R.id.tvBorrowerName);
            TextView tvBorrowerPhone = itemView.findViewById(R.id.tvBorrowerPhone);
            TextView tvBorrowDate = itemView.findViewById(R.id.tvBorrowDate);
            TextView tvDueDate = itemView.findViewById(R.id.tvDueDate);
            Button btnReturn = itemView.findViewById(R.id.btnReturnBook);

            tvBookTitle.setText(loan.getBookTitle());
            tvBookId.setText("Mã sách: " + loan.getBookId());
            tvBorrowerName.setText("Người mượn: " + loan.getBorrowerName());
            tvBorrowerPhone.setText("SĐT: " + loan.getBorrowerPhone());
            tvBorrowDate.setText("Mượn: " + loan.getBorrowDate());
            tvDueDate.setText("Hạn trả: " + loan.getDueDate());

            btnReturn.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận trả sách")
                        .setMessage("Xác nhận \"" + loan.getBorrowerName() + "\" trả sách \"" + loan.getBookTitle() + "\"?")
                        .setPositiveButton("Trả sách", (d, w) -> {
                            dbHelper.returnBook(loan.getLoanId(), loan.getBookId());
                            loadActiveLoans();
                            Toast.makeText(this, "Đã trả sách, cập nhật trạng thái về Có sẵn", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            layoutLoanList.addView(itemView);
        }
    }
}