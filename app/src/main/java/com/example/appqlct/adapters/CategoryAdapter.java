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
import com.example.appqlct.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryActionListener {
        void onDelete(Category category);
        void onSelect(Category category);
    }

    private final Context context;
    private List<Category> list;
    private final OnCategoryActionListener listener;

    public CategoryAdapter(Context context, List<Category> list, OnCategoryActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<Category> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category c = list.get(position);

        holder.tvName.setText(c.getName());
        holder.ivIcon.setImageResource(TransactionAdapter.getIconDrawable(c.getIcon()));

        try {
            if (c.getColor() != null && !c.getColor().isEmpty()) {
                int col = Color.parseColor(c.getColor());
                holder.iconContainer.getBackground().mutate().setTint(col);
            }
        } catch (Exception ignored) {}

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(c);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(c);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView ivIcon;
        TextView tvName;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.cat_icon_container);
            ivIcon = itemView.findViewById(R.id.iv_cat_icon);
            tvName = itemView.findViewById(R.id.tv_cat_name);
            btnDelete = itemView.findViewById(R.id.btn_delete_cat);
        }
    }
}
