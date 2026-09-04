package com.example.appqlct.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appqlct.R;
import com.example.appqlct.models.Wallet;
import com.example.appqlct.utils.CurrencyFormatter;

import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    public interface OnWalletActionListener {
        void onEdit(Wallet wallet);
        void onDelete(Wallet wallet);
    }

    private final Context context;
    private List<Wallet> list;
    private final OnWalletActionListener listener;

    public WalletAdapter(Context context, List<Wallet> list, OnWalletActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<Wallet> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet w = list.get(position);

        holder.tvWalletName.setText(w.getName());
        holder.tvWalletBalance.setText(CurrencyFormatter.format(w.getBalance()));
        holder.tvIsDefault.setVisibility(w.isDefault() ? View.VISIBLE : View.GONE);

        try {
            if (w.getColor() != null && !w.getColor().isEmpty()) {
                int c = Color.parseColor(w.getColor());
                holder.iconContainer.getBackground().mutate().setTint(c);
                holder.ivIcon.setColorFilter(Color.WHITE);
            }
        } catch (Exception ignored) {}

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(w);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(w);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView ivIcon;
        TextView tvWalletName;
        TextView tvIsDefault;
        TextView tvWalletBalance;
        ImageButton btnEdit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.wallet_icon_container);
            ivIcon = itemView.findViewById(R.id.iv_wallet_icon);
            tvWalletName = itemView.findViewById(R.id.tv_wallet_name);
            tvIsDefault = itemView.findViewById(R.id.tv_is_default);
            tvWalletBalance = itemView.findViewById(R.id.tv_wallet_balance);
            btnEdit = itemView.findViewById(R.id.btn_edit_wallet);
        }
    }
}
