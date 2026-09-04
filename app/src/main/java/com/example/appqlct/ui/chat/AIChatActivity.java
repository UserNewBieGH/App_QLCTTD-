package com.example.appqlct.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appqlct.adapters.ChatAdapter;
import com.example.appqlct.databinding.ActivityAiChatBinding;
import com.example.appqlct.models.ChatMessage;
import com.example.appqlct.utils.AICoinsVisionEngine;
import com.example.appqlct.utils.DateUtils;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;

public class AIChatActivity extends AppCompatActivity {

    private ActivityAiChatBinding binding;
    private SessionManager sessionManager;
    private ChatAdapter chatAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);

        setupRecyclerView();
        setupQuickChips();

        binding.btnBackChat.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnClearChat.setOnClickListener(v -> {
            chatAdapter.clearMessages();
            sendWelcomeMessage();
        });

        binding.btnSendChat.setOnClickListener(v -> sendMessage());

        binding.etChatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        sendWelcomeMessage();
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(this, new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(chatAdapter);
    }

    private void setupQuickChips() {
        binding.chipQueryBalance.setOnClickListener(v -> handleQuery("Số dư tài khoản của tôi hiện tại bao nhiêu?"));
        binding.chipQueryStats.setOnClickListener(v -> handleQuery("Tổng thu chi tháng này của tôi thế nào?"));
        binding.chipQueryTop.setOnClickListener(v -> handleQuery("Tôi chi tiêu nhiều nhất vào danh mục nào?"));
        binding.chipQueryBudget.setOnClickListener(v -> handleQuery("Tình trạng ngân sách tháng này ra sao?"));
        binding.chipQueryTips.setOnClickListener(v -> handleQuery("Cho tôi lời khuyên tiết kiệm tiền hiệu quả"));
    }

    private void sendWelcomeMessage() {
        String welcome = "Xin chào " + sessionManager.getUsername() + " 👋 Tôi là **Trợ lý AI Coins Vision**.\n\n" +
                "Tôi có thể giúp bạn giải đáp thắc mắc về số dư, thu chi tháng, phân tích danh mục chi nhiều nhất và tư vấn tiết kiệm.\n" +
                "Hãy chọn câu hỏi gợi ý bên dưới hoặc nhập câu hỏi của bạn nhé!";
        chatAdapter.addMessage(new ChatMessage(welcome, false, DateUtils.getCurrentTime()));
    }

    private void handleQuery(String query) {
        binding.etChatInput.setText(query);
        sendMessage();
    }

    private void sendMessage() {
        String text = binding.etChatInput.getText() != null ? binding.etChatInput.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        // Add user message
        chatAdapter.addMessage(new ChatMessage(text, true, DateUtils.getCurrentTime()));
        binding.etChatInput.setText("");
        binding.rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

        // Simulate intelligent AI thinking delay (300ms)
        handler.postDelayed(() -> {
            String reply = AICoinsVisionEngine.getReply(AIChatActivity.this, sessionManager.getUserId(), text);
            chatAdapter.addMessage(new ChatMessage(reply, false, DateUtils.getCurrentTime()));
            binding.rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }, 300);
    }
}
