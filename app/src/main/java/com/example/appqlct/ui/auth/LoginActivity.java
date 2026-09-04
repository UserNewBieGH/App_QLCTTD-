package com.example.appqlct.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appqlct.MainActivity;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.ActivityLoginBinding;
import com.example.appqlct.models.User;
import com.example.appqlct.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = DatabaseHelper.getInstance(this);

        binding.btnLogin.setOnClickListener(v -> handleLogin());

        binding.btnQuickDemo.setOnClickListener(v -> handleDemoLogin());

        binding.tvGotoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.loginUser(email, password);
        if (user != null) {
            sessionManager.createLoginSession(user.getId(), user.getUsername(), user.getEmail());
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Email hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDemoLogin() {
        // Find or create demo user
        User user = dbHelper.loginUser("demo@gmail.com", "123456");
        if (user == null) {
            long id = dbHelper.registerUser("Người dùng Mẫu", "demo@gmail.com", "123456");
            user = dbHelper.loginUser("demo@gmail.com", "123456");
        }

        if (user != null) {
            sessionManager.createLoginSession(user.getId(), user.getUsername(), user.getEmail());
            Toast.makeText(this, "Đăng nhập thành công với tài khoản mẫu!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
