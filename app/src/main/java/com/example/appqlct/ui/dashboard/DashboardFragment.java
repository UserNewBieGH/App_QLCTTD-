package com.example.appqlct.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appqlct.MainActivity;
import com.example.appqlct.R;
import com.example.appqlct.adapters.TransactionAdapter;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.FragmentDashboardBinding;
import com.example.appqlct.models.Budget;
import com.example.appqlct.models.Transaction;
import com.example.appqlct.ui.categories.CategoriesActivity;
import com.example.appqlct.ui.chat.AIChatActivity;
import com.example.appqlct.ui.reports.ReportsActivity;
import com.example.appqlct.ui.transactions.AddEditTransactionActivity;
import com.example.appqlct.ui.wallets.WalletsActivity;
import com.example.appqlct.utils.CurrencyFormatter;
import com.example.appqlct.utils.DateUtils;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TransactionAdapter transactionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupViews();
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void setupViews() {
        binding.tvUsernameDashboard.setText(sessionManager.getUsername());

        transactionAdapter = new TransactionAdapter(requireContext(), new ArrayList<>(), new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction transaction) {
                Intent intent = new Intent(requireContext(), AddEditTransactionActivity.class);
                intent.putExtra("transaction_id", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Transaction transaction) {
                // optional delete prompt
            }
        });

        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        binding.btnQuickWallets.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), WalletsActivity.class));
        });

        binding.btnQuickCategories.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CategoriesActivity.class));
        });

        binding.btnQuickReports.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ReportsActivity.class));
        });

        binding.tvSeeAllTransactions.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_transactions);
            }
        });
    }

    private void loadDashboardData() {
        int userId = sessionManager.getUserId();
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();

        // 1. Total Balance
        double totalBalance = dbHelper.getTotalBalance(userId);
        binding.tvDashboardTotalBalance.setText(CurrencyFormatter.format(totalBalance));

        // 2. Month Income & Expense
        double income = dbHelper.getMonthlyTotal(userId, "income", month, year);
        double expense = dbHelper.getMonthlyTotal(userId, "expense", month, year);
        binding.tvDashboardIncome.setText("+ " + CurrencyFormatter.format(income));
        binding.tvDashboardExpense.setText("- " + CurrencyFormatter.format(expense));

        // 3. Recent Transactions (limit 5)
        List<Transaction> recents = dbHelper.getTransactions(userId, null, null, null, null, 5);
        if (recents.isEmpty()) {
            binding.tvEmptyRecent.setVisibility(View.VISIBLE);
            binding.rvRecentTransactions.setVisibility(View.GONE);
        } else {
            binding.tvEmptyRecent.setVisibility(View.GONE);
            binding.rvRecentTransactions.setVisibility(View.VISIBLE);
            transactionAdapter.updateData(recents);
        }

        // 4. Budget Warning
        List<Budget> budgets = dbHelper.getBudgets(userId, month, year);
        Budget highestExceeded = null;
        for (Budget b : budgets) {
            if (b.getPercentage() >= 80) {
                if (highestExceeded == null || b.getPercentage() > highestExceeded.getPercentage()) {
                    highestExceeded = b;
                }
            }
        }

        if (highestExceeded != null) {
            binding.cardBudgetWarning.setVisibility(View.VISIBLE);
            String msg = "Cảnh báo: Danh mục [" + highestExceeded.getCategoryName() + "] đã chi " +
                    highestExceeded.getPercentage() + "% hạn mức ngân sách tháng!";
            binding.tvBudgetWarningMsg.setText(msg);
        } else {
            binding.cardBudgetWarning.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
