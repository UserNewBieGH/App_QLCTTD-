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

import com.example.appqlct.R;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.FragmentProfileBinding;
import com.example.appqlct.ui.auth.LoginActivity;
import com.example.appqlct.ui.categories.CategoriesActivity;
import com.example.appqlct.ui.chat.AIChatActivity;
import com.example.appqlct.ui.reports.ReportsActivity;
import com.example.appqlct.ui.wallets.WalletsActivity;
import com.example.appqlct.utils.CurrencyFormatter;
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

        loadProfileData();

        binding.menuEditProfile.setOnClickListener(v -> showEditProfileDialog());
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

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        int userId = sessionManager.getUserId();
        binding.tvProfileName.setText(sessionManager.getUsername());
        binding.tvProfileEmail.setText(sessionManager.getEmail());

        // Load Thống kê hoạt động thực tế
        int txCount = dbHelper.getTotalTransactionCount(userId);
        double totalIncome = dbHelper.getTotalIncomeAllTime(userId);
        double totalExpense = dbHelper.getTotalExpenseAllTime(userId);

        binding.tvProfileTxCount.setText(String.valueOf(txCount));
        binding.tvProfileTotalIncome.setText("+" + CurrencyFormatter.format(totalIncome));
        binding.tvProfileTotalExpense.setText("-" + CurrencyFormatter.format(totalExpense));
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText etUsername = dialogView.findViewById(R.id.et_edit_profile_username);
        EditText etEmail = dialogView.findViewById(R.id.et_edit_profile_email);

        etUsername.setText(sessionManager.getUsername());
        etEmail.setText(sessionManager.getEmail());

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Lưu thay đổi", (dialog, which) -> {
                    String newName = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
                    String newEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

                    if (newName.length() < 2) {
                        Toast.makeText(requireContext(), "Tên hiển thị phải có ít nhất 2 ký tự!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        Toast.makeText(requireContext(), "Địa chỉ email không hợp lệ!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int res = dbHelper.updateUserProfile(sessionManager.getUserId(), newName, newEmail);
                    if (res == -1) {
                        Toast.makeText(requireContext(), "Email này đã được sử dụng bởi tài khoản khác!", Toast.LENGTH_LONG).show();
                    } else if (res == 1) {
                        sessionManager.updateUserSession(newName, newEmail);
                        loadProfileData();
                        Toast.makeText(requireContext(), "Cập nhật thông tin cá nhân thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Không thể cập nhật thông tin.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        EditText etCurrent = dialogView.findViewById(R.id.et_pass_current);
        EditText etNew = dialogView.findViewById(R.id.et_pass_new);
        EditText etConfirm = dialogView.findViewById(R.id.et_pass_confirm);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
                    String currentPass = etCurrent.getText() != null ? etCurrent.getText().toString().trim() : "";
                    String newPass = etNew.getText() != null ? etNew.getText().toString().trim() : "";
                    String confirmPass = etConfirm.getText() != null ? etConfirm.getText().toString().trim() : "";

                    if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ các trường!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int userId = sessionManager.getUserId();
                    boolean isCurrentMatch = dbHelper.verifyPassword(userId, currentPass);
                    if (!isCurrentMatch) {
                        Toast.makeText(requireContext(), "Mật khẩu hiện tại không chính xác!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (newPass.length() < 6) {
                        Toast.makeText(requireContext(), "Mật khẩu mới phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(requireContext(), "Xác nhận mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean ok = dbHelper.updatePassword(userId, newPass);
                    if (ok) {
                        Toast.makeText(requireContext(), "Thay đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
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
