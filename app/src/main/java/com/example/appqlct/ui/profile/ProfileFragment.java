package com.example.appqlct.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.FragmentProfileBinding;
import com.example.appqlct.ui.auth.LoginActivity;
import com.example.appqlct.ui.categories.CategoriesActivity;
import com.example.appqlct.ui.chat.AIChatActivity;
import com.example.appqlct.ui.reports.ReportsActivity;
import com.example.appqlct.ui.wallets.WalletsActivity;
import com.example.appqlct.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        binding.tvProfileName.setText(sessionManager.getUsername());
        binding.tvProfileEmail.setText(sessionManager.getEmail());

        binding.menuWallets.setOnClickListener(v -> startActivity(new Intent(requireContext(), WalletsActivity.class)));
        binding.menuCategories.setOnClickListener(v -> startActivity(new Intent(requireContext(), CategoriesActivity.class)));
        binding.menuReports.setOnClickListener(v -> startActivity(new Intent(requireContext(), ReportsActivity.class)));
        binding.menuAiChat.setOnClickListener(v -> startActivity(new Intent(requireContext(), AIChatActivity.class)));

        binding.menuChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        sessionManager.logout();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void showChangePasswordDialog() {
        EditText etNewPass = new EditText(requireContext());
        etNewPass.setHint("Nhập mật khẩu mới (tối thiểu 6 ký tự)");
        etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(etNewPass)
                .setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
                    String pass = etNewPass.getText().toString().trim();
                    if (pass.length() < 6) {
                        Toast.makeText(requireContext(), "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ok = dbHelper.updatePassword(sessionManager.getUserId(), pass);
                    if (ok) {
                        Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Không thể cập nhật mật khẩu.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
