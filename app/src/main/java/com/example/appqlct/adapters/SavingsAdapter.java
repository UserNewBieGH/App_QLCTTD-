package com.example.appqlct.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appqlct.R;
import com.example.appqlct.models.SavingsGoal;
import com.example.appqlct.utils.CurrencyFormatter;
import com.example.appqlct.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class SavingsAdapter extends RecyclerView.Adapter<SavingsAdapter.ViewHolder> {

    public interface OnSavingsActionListener {
        void onDeposit(SavingsGoal goal);
        void onDelete(SavingsGoal goal);
    }

    private final Context context;
    private List<SavingsGoal> list;
    private final OnSavingsActionListener listener;

    public SavingsAdapter(Context context, List<SavingsGoal> list, OnSavingsActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<SavingsGoal> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_savings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavingsGoal g = list.get(position);

        holder.tvName.setText(g.getName());

        boolean isCompleted = "completed".equalsIgnoreCase(g.getStatus()) || g.getCurrentAmount() >= g.getTargetAmount();
        holder.tvStatus.setText(isCompleted ? "Đã hoàn thành" : "Đang thực hiện");
        holder.tvStatus.setTextColor(ContextCompat.getColor(context, isCompleted ? R.color.income_green : R.color.secondary));

        String amounts = CurrencyFormatter.format(g.getCurrentAmount()) + " / " + CurrencyFormatter.format(g.getTargetAmount()) + " (" + g.getPercentage() + "%)";
        holder.tvAmounts.setText(amounts);

        holder.progress.setProgress(g.getPercentage());
        if (isCompleted) {
            holder.progress.setIndicatorColor(ContextCompat.getColor(context, R.color.income_green));
        }

        if (g.getDeadline() != null && !g.getDeadline().isEmpty()) {
            holder.tvDeadline.setText("Hạn chót: " + DateUtils.formatToDisplay(g.getDeadline()));
        } else {
            holder.tvDeadline.setText("Không có hạn chót");
        }

        if (isCompleted) {
            holder.btnDeposit.setEnabled(false);
            holder.btnDeposit.setText("Hoàn thành");
        } else {
            holder.btnDeposit.setEnabled(true);
            holder.btnDeposit.setText("Nạp tiền");
            holder.btnDeposit.setOnClickListener(v -> {
                if (listener != null) listener.onDeposit(g);
            });
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(g);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvStatus;
        TextView tvAmounts;
        LinearProgressIndicator progress;
        TextView tvDeadline;
        MaterialButton btnDeposit;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_goal_name);
            tvStatus = itemView.findViewById(R.id.tv_goal_status);
            tvAmounts = itemView.findViewById(R.id.tv_goal_amounts);
            progress = itemView.findViewById(R.id.progress_goal);
            tvDeadline = itemView.findViewById(R.id.tv_goal_deadline);
            btnDeposit = itemView.findViewById(R.id.btn_deposit);
            btnDelete = itemView.findViewById(R.id.btn_delete_goal);
        }
    }
}
