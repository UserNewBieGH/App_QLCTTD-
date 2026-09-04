package com.example.appqlct.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appqlct.R;
import com.example.appqlct.models.Budget;
import com.example.appqlct.utils.CurrencyFormatter;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public interface OnBudgetActionListener {
        void onDelete(Budget budget);
    }

    private final Context context;
    private List<Budget> list;
    private final OnBudgetActionListener listener;

    public BudgetAdapter(Context context, List<Budget> list, OnBudgetActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<Budget> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget b = list.get(position);

        holder.tvCategory.setText(b.getCategoryName());
        holder.ivIcon.setImageResource(TransactionAdapter.getIconDrawable(b.getCategoryIcon()));

        try {
            if (b.getCategoryColor() != null && !b.getCategoryColor().isEmpty()) {
                int c = Color.parseColor(b.getCategoryColor());
                holder.iconContainer.getBackground().mutate().setTint(c);
            }
        } catch (Exception ignored) {}

        String amountsStr = "Đã chi: " + CurrencyFormatter.format(b.getSpentAmount()) + " / " + CurrencyFormatter.format(b.getLimitAmount());
        holder.tvAmounts.setText(amountsStr);

        int pct = b.getPercentage();
        holder.tvPct.setText(pct + "%");
        holder.progress.setProgress(Math.min(pct, 100));

        int color;
        if (pct >= 100) {
            color = ContextCompat.getColor(context, R.color.expense_red);
        } else if (pct >= 80) {
            color = ContextCompat.getColor(context, R.color.warning_amber);
        } else {
            color = ContextCompat.getColor(context, R.color.income_green);
        }

        holder.tvPct.setTextColor(color);
        holder.progress.setIndicatorColor(color);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(b);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView ivIcon;
        TextView tvCategory;
        TextView tvAmounts;
        TextView tvPct;
        LinearProgressIndicator progress;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.budget_icon_container);
            ivIcon = itemView.findViewById(R.id.iv_budget_icon);
            tvCategory = itemView.findViewById(R.id.tv_budget_category);
            tvAmounts = itemView.findViewById(R.id.tv_budget_amounts);
            tvPct = itemView.findViewById(R.id.tv_budget_pct);
            progress = itemView.findViewById(R.id.progress_budget);
            btnDelete = itemView.findViewById(R.id.btn_delete_budget);
        }
    }
}
