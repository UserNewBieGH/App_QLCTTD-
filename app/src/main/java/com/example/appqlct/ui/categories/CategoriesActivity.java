package com.example.appqlct.ui.categories;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appqlct.R;
import com.example.appqlct.adapters.CategoryAdapter;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.ActivityCategoriesBinding;
import com.example.appqlct.models.Category;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private ActivityCategoriesBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private CategoryAdapter adapter;
    private String currentType = "expense";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        setupTabs();
        setupRecyclerView();

        binding.btnBackCategories.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    private void setupTabs() {
        binding.btnTabExpense.setOnClickListener(v -> {
            currentType = "expense";
            binding.btnTabExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.expense_red));
            binding.btnTabExpense.setTextColor(Color.WHITE);

            binding.btnTabIncome.setBackgroundColor(Color.TRANSPARENT);
            binding.btnTabIncome.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            loadCategories();
        });

        binding.btnTabIncome.setOnClickListener(v -> {
            currentType = "income";
            binding.btnTabIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.income_green));
            binding.btnTabIncome.setTextColor(Color.WHITE);

            binding.btnTabExpense.setBackgroundColor(Color.TRANSPARENT);
            binding.btnTabExpense.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            loadCategories();
        });
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(this, new ArrayList<>(), new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onDelete(Category category) {
                new AlertDialog.Builder(CategoriesActivity.this)
                        .setTitle("Xác nhận xóa danh mục")
                        .setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.getName() + "'? Lưu ý: Không thể xóa danh mục đã được dùng trong các giao dịch.")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            boolean ok = dbHelper.deleteCategory(category.getId(), sessionManager.getUserId());
                            if (ok) {
                                Toast.makeText(CategoriesActivity.this, "Đã xóa danh mục thành công!", Toast.LENGTH_SHORT).show();
                                loadCategories();
                            } else {
                                Toast.makeText(CategoriesActivity.this, "Không thể xóa danh mục đang có giao dịch liên kết!", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onSelect(Category category) {}
        });

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCategories.setAdapter(adapter);
    }

    private void loadCategories() {
        List<Category> list = dbHelper.getCategories(sessionManager.getUserId(), currentType);
        adapter.updateData(list);
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.et_cat_name_input);
        RadioButton rbExpense = dialogView.findViewById(R.id.rb_cat_expense);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Tạo mới", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên danh mục!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String type = rbExpense.isChecked() ? "expense" : "income";
                    String color = type.equals("expense") ? "#EF4444" : "#10B981";
                    String icon = type.equals("expense") ? "ic_category_other" : "ic_category_other";

                    dbHelper.addCategory(sessionManager.getUserId(), name, icon, color, type);
                    Toast.makeText(this, "Thêm danh mục mới thành công!", Toast.LENGTH_SHORT).show();
                    loadCategories();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
