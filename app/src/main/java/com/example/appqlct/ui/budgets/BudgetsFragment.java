package com.example.appqlct.ui.budgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appqlct.R;
import com.example.appqlct.adapters.BudgetAdapter;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.FragmentBudgetsBinding;
import com.example.appqlct.models.Budget;
import com.example.appqlct.models.Category;
import com.example.appqlct.utils.DateUtils;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class BudgetsFragment extends Fragment {

    private FragmentBudgetsBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private BudgetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();
        binding.tvBudgetMonth.setText("Tháng " + (month < 10 ? "0" + month : month) + "/" + year);

        setupRecyclerView();

        binding.btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter(requireContext(), new ArrayList<>(), budget -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa ngân sách này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean ok = dbHelper.deleteBudget(budget.getId(), sessionManager.getUserId());
                        if (ok) {
                            Toast.makeText(requireContext(), "Đã xóa ngân sách!", Toast.LENGTH_SHORT).show();
                            loadBudgets();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        binding.rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgets.setAdapter(adapter);
    }

    private void loadBudgets() {
        int userId = sessionManager.getUserId();
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();

        List<Budget> list = dbHelper.getBudgets(userId, month, year);
        if (list.isEmpty()) {
            binding.tvEmptyBudgets.setVisibility(View.VISIBLE);
            binding.rvBudgets.setVisibility(View.GONE);
        } else {
            binding.tvEmptyBudgets.setVisibility(View.GONE);
            binding.rvBudgets.setVisibility(View.VISIBLE);
            adapter.updateData(list);
        }
    }

    private void showAddBudgetDialog() {
        int userId = sessionManager.getUserId();
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();

        List<Category> expenseCats = dbHelper.getCategories(userId, "expense");
        if (expenseCats.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có danh mục chi tiêu nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_budget, null);
        Spinner spinner = dialogView.findViewById(R.id.spinner_budget_cat);
        EditText etLimit = dialogView.findViewById(R.id.et_budget_limit);

        List<String> names = new ArrayList<>();
        for (Category c : expenseCats) {
            names.add(c.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
        spinner.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String limitStr = etLimit.getText() != null ? etLimit.getText().toString().trim() : "";
                    if (limitStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập hạn mức!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        double limit = Double.parseDouble(limitStr);
                        if (limit <= 0) {
                            Toast.makeText(requireContext(), "Hạn mức phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int catId = expenseCats.get(spinner.getSelectedItemPosition()).getId();
                        dbHelper.setBudget(userId, catId, limit, month, year);
                        Toast.makeText(requireContext(), "Thiết lập ngân sách thành công!", Toast.LENGTH_SHORT).show();
                        loadBudgets();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
