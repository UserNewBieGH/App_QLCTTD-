package com.example.appqlct.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appqlct.MainActivity;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.ActivityRegisterBinding;
import com.example.appqlct.models.User;
import com.example.appqlct.utils.SessionManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        binding.btnRegister.setOnClickListener(v -> handleRegister());

        binding.tvGotoLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String username = binding.etRegUsername.getText() != null ? binding.etRegUsername.getText().toString().trim() : "";
        String email = binding.etRegEmail.getText() != null ? binding.etRegEmail.getText().toString().trim() : "";
        String password = binding.etRegPassword.getText() != null ? binding.etRegPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etRegConfirmPassword.getText() != null ? binding.etRegConfirmPassword.getText().toString().trim() : "";

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = dbHelper.registerUser(username, email, password);
        if (userId != -1) {
            sessionManager.createLoginSession((int) userId, username, email);
            Toast.makeText(this, "Đăng ký thành công! Đã tạo ví và danh mục mẫu cho bạn.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email này đã được sử dụng! Vui lòng chọn email khác.", Toast.LENGTH_SHORT).show();
        }
    }
}
