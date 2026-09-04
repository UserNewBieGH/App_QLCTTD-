package com.example.appqlct.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appqlct.R;
import com.example.appqlct.models.Transaction;
import com.example.appqlct.utils.CurrencyFormatter;
import com.example.appqlct.utils.DateUtils;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
        void onItemLongClick(Transaction transaction);
    }

    private final Context context;
    private List<Transaction> list;
    private final OnItemClickListener listener;

    public TransactionAdapter(Context context, List<Transaction> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<Transaction> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = list.get(position);

        holder.tvCategoryName.setText(t.getCategoryName());

        String dateFormatted = DateUtils.formatToDisplay(t.getTransactionDate());
        String subText = (t.getWalletName() != null ? t.getWalletName() : "") + " • " + dateFormatted;
        holder.tvSub.setText(subText);

        if (t.getNote() != null && !t.getNote().trim().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(t.getNote());
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        boolean isIncome = "income".equalsIgnoreCase(t.getType());
        holder.tvAmount.setText(CurrencyFormatter.formatWithSign(t.getAmount(), t.getType()));
        holder.tvAmount.setTextColor(ContextCompat.getColor(context, isIncome ? R.color.income_green : R.color.expense_red));

        // Category icon & color
        int iconRes = getIconDrawable(t.getCategoryIcon());
        holder.ivIcon.setImageResource(iconRes);

        try {
            if (t.getCategoryColor() != null && !t.getCategoryColor().isEmpty()) {
                int color = Color.parseColor(t.getCategoryColor());
                holder.iconContainer.getBackground().mutate().setTint(color);
                holder.ivIcon.setColorFilter(Color.WHITE);
            }
        } catch (Exception ignored) {}

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(t);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(t);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static int getIconDrawable(String iconName) {
        if (iconName == null) return R.drawable.ic_category_other;
        switch (iconName) {
            case "ic_category_food":
            case "bi-cup-hot":
                return R.drawable.ic_category_food;
            case "ic_category_transport":
            case "bi-car-front":
                return R.drawable.ic_category_transport;
            case "ic_category_shopping":
            case "bi-bag":
                return R.drawable.ic_category_shopping;
            case "ic_category_entertainment":
            case "bi-controller":
                return R.drawable.ic_category_entertainment;
            case "ic_category_bill":
            case "bi-receipt":
                return R.drawable.ic_category_bill;
            case "ic_category_health":
            case "bi-heart-pulse":
                return R.drawable.ic_category_health;
            case "ic_category_education":
            case "bi-book":
                return R.drawable.ic_category_education;
            case "ic_category_salary":
            case "bi-cash-stack":
                return R.drawable.ic_category_salary;
            case "ic_category_bonus":
            case "bi-gift":
                return R.drawable.ic_category_bonus;
            case "ic_category_investment":
            case "bi-graph-up-arrow":
                return R.drawable.ic_category_investment;
            default:
                return R.drawable.ic_category_other;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView ivIcon;
        TextView tvCategoryName;
        TextView tvSub;
        TextView tvNote;
        TextView tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.icon_container);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvSub = itemView.findViewById(R.id.tv_transaction_sub);
            tvNote = itemView.findViewById(R.id.tv_transaction_note);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
