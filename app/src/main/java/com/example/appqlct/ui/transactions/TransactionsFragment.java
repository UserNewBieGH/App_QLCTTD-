package com.example.appqlct.ui.transactions;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appqlct.R;
import com.example.appqlct.adapters.TransactionAdapter;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.FragmentTransactionsBinding;
import com.example.appqlct.models.Transaction;
import com.example.appqlct.utils.CurrencyFormatter;
import com.example.appqlct.utils.DateUtils;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TransactionAdapter adapter;
    private String currentTypeFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupRecyclerView();
        setupFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(requireContext(), new ArrayList<>(), new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {
                Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                intent.putExtra("transaction_id", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Transaction transaction) {
                showDeleteDialog(transaction);
            }
        });

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_expense)) {
                currentTypeFilter = "expense";
            } else if (checkedIds.contains(R.id.chip_income)) {
                currentTypeFilter = "income";
            } else {
                currentTypeFilter = "all";
            }
            loadTransactions();
        });

        binding.etSearchTransactions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadTransactions();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadTransactions() {
        int userId = sessionManager.getUserId();
        String query = binding.etSearchTransactions.getText() != null ? binding.etSearchTransactions.getText().toString() : "";

        List<Transaction> list = dbHelper.getTransactions(userId, currentTypeFilter, null, null, query, 0);

        if (list.isEmpty()) {
            binding.tvEmptyTransactions.setVisibility(View.VISIBLE);
            binding.rvTransactions.setVisibility(View.GONE);
        } else {
            binding.tvEmptyTransactions.setVisibility(View.GONE);
            binding.rvTransactions.setVisibility(View.VISIBLE);
            adapter.updateData(list);
        }

        // Calculate summary
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();
        double sumIncome = dbHelper.getMonthlyTotal(userId, "income", month, year);
        double sumExpense = dbHelper.getMonthlyTotal(userId, "expense", month, year);

        binding.tvSumIncome.setText("+ " + CurrencyFormatter.format(sumIncome));
        binding.tvSumExpense.setText("- " + CurrencyFormatter.format(sumExpense));
    }

    private void showDeleteDialog(Transaction t) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này? Số dư ví sẽ được hoàn lại tự động.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean ok = dbHelper.deleteTransaction(t.getId(), sessionManager.getUserId());
                    if (ok) {
                        Toast.makeText(requireContext(), "Đã xóa giao dịch thành công!", Toast.LENGTH_SHORT).show();
                        loadTransactions();
                    } else {
                        Toast.makeText(requireContext(), "Không thể xóa giao dịch.", Toast.LENGTH_SHORT).show();
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
