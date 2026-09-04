package com.example.appqlct.ui.reports;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appqlct.R;
import com.example.appqlct.adapters.TransactionAdapter;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.ActivityReportsBinding;
import com.example.appqlct.models.Transaction;
import com.example.appqlct.utils.CurrencyFormatter;
import com.example.appqlct.utils.DateUtils;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private ActivityReportsBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        binding.btnBackReports.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        loadReports();
    }

    private void loadReports() {
        int userId = sessionManager.getUserId();
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();

        double income = dbHelper.getMonthlyTotal(userId, "income", month, year);
        double expense = dbHelper.getMonthlyTotal(userId, "expense", month, year);
        double net = income - expense;

        binding.tvRepIncome.setText("+ " + CurrencyFormatter.format(income));
        binding.tvRepExpense.setText("- " + CurrencyFormatter.format(expense));
        binding.tvRepNet.setText(CurrencyFormatter.format(net));

        // Breakdown categories
        List<Transaction> expenses = dbHelper.getTransactions(userId, "expense", month, year, null, 0);

        Map<String, CategoryStat> statMap = new HashMap<>();
        for (Transaction t : expenses) {
            String name = t.getCategoryName();
            if (!statMap.containsKey(name)) {
                statMap.put(name, new CategoryStat(name, t.getCategoryIcon(), t.getCategoryColor(), 0));
            }
            statMap.get(name).amount += t.getAmount();
        }

        List<CategoryStat> statList = new ArrayList<>(statMap.values());
        Collections.sort(statList, (a, b) -> Double.compare(b.amount, a.amount));

        binding.layoutBreakdownContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (statList.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Chưa có chi tiêu nào trong tháng này");
            tv.setTextColor(getColor(R.color.text_muted));
            tv.setTextSize(14);
            tv.setPadding(60, 40, 60, 40);
            binding.layoutBreakdownContainer.addView(tv);
            return;
        }

        for (CategoryStat stat : statList) {
            View card = inflater.inflate(R.layout.item_budget, binding.layoutBreakdownContainer, false);

            TextView tvCategory = card.findViewById(R.id.tv_budget_category);
            TextView tvAmounts = card.findViewById(R.id.tv_budget_amounts);
            TextView tvPct = card.findViewById(R.id.tv_budget_pct);
            ProgressBar progress = card.findViewById(R.id.progress_budget);
            FrameLayout iconContainer = card.findViewById(R.id.budget_icon_container);
            ImageView ivIcon = card.findViewById(R.id.iv_budget_icon);
            View btnDelete = card.findViewById(R.id.btn_delete_budget);
            btnDelete.setVisibility(View.GONE);

            tvCategory.setText(stat.name);
            tvAmounts.setText("Chi: " + CurrencyFormatter.format(stat.amount));

            int pct = expense > 0 ? (int) Math.round((stat.amount / expense) * 100) : 0;
            tvPct.setText(pct + "%");
            progress.setProgress(pct);

            ivIcon.setImageResource(TransactionAdapter.getIconDrawable(stat.icon));

            try {
                if (stat.color != null && !stat.color.isEmpty()) {
                    int c = Color.parseColor(stat.color);
                    iconContainer.getBackground().mutate().setTint(c);
                    progress.setProgressTintList(ColorStateList.valueOf(c));
                }
            } catch (Exception ignored) {}

            binding.layoutBreakdownContainer.addView(card);
        }
    }

    private static class CategoryStat {
        String name;
        String icon;
        String color;
        double amount;

        CategoryStat(String name, String icon, String color, double amount) {
            this.name = name;
            this.icon = icon;
            this.color = color;
            this.amount = amount;
        }
    }
}
