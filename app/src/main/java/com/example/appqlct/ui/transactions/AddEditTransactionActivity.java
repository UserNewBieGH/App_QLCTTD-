package com.example.appqlct.ui.transactions;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appqlct.R;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.ActivityAddEditTransactionBinding;
import com.example.appqlct.models.Category;
import com.example.appqlct.models.Transaction;
import com.example.appqlct.models.Wallet;
import com.example.appqlct.utils.DateUtils;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddEditTransactionActivity extends AppCompatActivity {

    private ActivityAddEditTransactionBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private String currentType = "expense";
    private int transactionId = -1;
    private String selectedDateDb;

    private List<Category> currentCategories = new ArrayList<>();
    private List<Wallet> currentWallets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        selectedDateDb = DateUtils.getTodayDbFormat();
        binding.tvSelectedDate.setText(DateUtils.formatToDisplay(selectedDateDb));

        transactionId = getIntent().getIntExtra("transaction_id", -1);

        setupTypeSelector();
        setupDatePicker();
        setupWallets();
        setupCategories();

        if (transactionId != -1) {
            loadExistingTransaction();
        }

        binding.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnSaveTransaction.setOnClickListener(v -> saveTransaction());
        binding.btnDeleteAction.setOnClickListener(v -> deleteCurrentTransaction());
    }

    private void setupTypeSelector() {
        binding.btnTypeExpense.setOnClickListener(v -> {
            currentType = "expense";
            binding.btnTypeExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.expense_red));
            binding.btnTypeExpense.setTextColor(Color.WHITE);

            binding.btnTypeIncome.setBackgroundColor(Color.TRANSPARENT);
            binding.btnTypeIncome.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            binding.etAmount.setTextColor(ContextCompat.getColor(this, R.color.expense_red));
            setupCategories();
        });

        binding.btnTypeIncome.setOnClickListener(v -> {
            currentType = "income";
            binding.btnTypeIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.income_green));
            binding.btnTypeIncome.setTextColor(Color.WHITE);

            binding.btnTypeExpense.setBackgroundColor(Color.TRANSPARENT);
            binding.btnTypeExpense.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            binding.etAmount.setTextColor(ContextCompat.getColor(this, R.color.income_green));
            setupCategories();
        });
    }

    private void setupDatePicker() {
        binding.layoutDatePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String mStr = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
                String dStr = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                selectedDateDb = year + "-" + mStr + "-" + dStr;
                binding.tvSelectedDate.setText(DateUtils.formatToDisplay(selectedDateDb));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });
    }

    private void setupWallets() {
        currentWallets = dbHelper.getWallets(sessionManager.getUserId());
        List<String> walletNames = new ArrayList<>();
        for (Wallet w : currentWallets) {
            walletNames.add(w.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, walletNames);
        binding.spinnerWallet.setAdapter(adapter);
    }

    private void setupCategories() {
        currentCategories = dbHelper.getCategories(sessionManager.getUserId(), currentType);
        List<String> catNames = new ArrayList<>();
        for (Category c : currentCategories) {
            catNames.add(c.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, catNames);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void loadExistingTransaction() {
        binding.tvTitleAction.setText(R.string.edit_transaction);
        binding.btnDeleteAction.setVisibility(View.VISIBLE);

        List<Transaction> list = dbHelper.getTransactions(sessionManager.getUserId(), null, null, null, null, 0);
        for (Transaction t : list) {
            if (t.getId() == transactionId) {
                currentType = t.getType();
                if ("income".equalsIgnoreCase(currentType)) {
                    binding.btnTypeIncome.performClick();
                } else {
                    binding.btnTypeExpense.performClick();
                }

                binding.etAmount.setText(String.valueOf((long) t.getAmount()));
                if (t.getNote() != null) binding.etNote.setText(t.getNote());
                selectedDateDb = t.getTransactionDate();
                binding.tvSelectedDate.setText(DateUtils.formatToDisplay(selectedDateDb));

                // Select category in spinner
                for (int i = 0; i < currentCategories.size(); i++) {
                    if (currentCategories.get(i).getId() == t.getCategoryId()) {
                        binding.spinnerCategory.setSelection(i);
                        break;
                    }
                }

                // Select wallet in spinner
                for (int i = 0; i < currentWallets.size(); i++) {
                    if (currentWallets.get(i).getId() == t.getWalletId()) {
                        binding.spinnerWallet.setSelection(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void saveTransaction() {
        String amountStr = binding.etAmount.getText() != null ? binding.etAmount.getText().toString().trim() : "";
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentCategories.isEmpty() || currentWallets.isEmpty()) {
            Toast.makeText(this, "Chưa có danh mục hoặc ví khả dụng!", Toast.LENGTH_SHORT).show();
            return;
        }

        int catIndex = binding.spinnerCategory.getSelectedItemPosition();
        int walletIndex = binding.spinnerWallet.getSelectedItemPosition();
        int categoryId = currentCategories.get(catIndex).getId();
        int walletId = currentWallets.get(walletIndex).getId();
        String note = binding.etNote.getText() != null ? binding.etNote.getText().toString().trim() : "";

        // Check budget warning for expense
        if ("expense".equalsIgnoreCase(currentType)) {
            String warning = dbHelper.checkBudgetWarning(sessionManager.getUserId(), categoryId, amount, selectedDateDb);
            if (warning != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Cảnh báo ngân sách")
                        .setMessage(warning + "\n\nBạn có muốn tiếp tục lưu giao dịch này không?")
                        .setPositiveButton("Tiếp tục lưu", (dialog, which) -> executeSave(categoryId, walletId, amount, note))
                        .setNegativeButton("Hủy", null)
                        .show();
                return;
            }
        }

        executeSave(categoryId, walletId, amount, note);
    }

    private void executeSave(int categoryId, int walletId, double amount, String note) {
        boolean success;
        if (transactionId == -1) {
            success = dbHelper.addTransaction(sessionManager.getUserId(), categoryId, walletId, currentType, amount, note, selectedDateDb);
        } else {
            success = dbHelper.updateTransaction(transactionId, sessionManager.getUserId(), categoryId, walletId, currentType, amount, note, selectedDateDb);
        }

        if (success) {
            Toast.makeText(this, "Lưu giao dịch thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu giao dịch.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCurrentTransaction() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này? Số dư ví sẽ được hoàn lại tự động.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean ok = dbHelper.deleteTransaction(transactionId, sessionManager.getUserId());
                    if (ok) {
                        Toast.makeText(this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
