package com.example.appqlct.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appqlct.R;
import com.example.appqlct.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final Context context;
    private final List<ChatMessage> list;

    public ChatAdapter(Context context, List<ChatMessage> list) {
        this.context = context;
        this.list = list;
    }

    public void addMessage(ChatMessage msg) {
        list.add(msg);
        notifyItemInserted(list.size() - 1);
    }

    public void clearMessages() {
        list.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = list.get(position);

        if (msg.isUser()) {
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.layoutAi.setVisibility(View.GONE);
            holder.tvUserText.setText(msg.getText());
            holder.tvUserTime.setText(msg.getTimestamp());
        } else {
            holder.layoutUser.setVisibility(View.GONE);
            holder.layoutAi.setVisibility(View.VISIBLE);
            holder.tvAiText.setText(msg.getText());
            holder.tvAiTime.setText(msg.getTimestamp());
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUser;
        TextView tvUserText;
        TextView tvUserTime;

        LinearLayout layoutAi;
        TextView tvAiText;
        TextView tvAiTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUser = itemView.findViewById(R.id.layout_user_msg);
            tvUserText = itemView.findViewById(R.id.tv_user_text);
            tvUserTime = itemView.findViewById(R.id.tv_user_time);

            layoutAi = itemView.findViewById(R.id.layout_ai_msg);
            tvAiText = itemView.findViewById(R.id.tv_ai_text);
            tvAiTime = itemView.findViewById(R.id.tv_ai_time);
        }
    }
}
