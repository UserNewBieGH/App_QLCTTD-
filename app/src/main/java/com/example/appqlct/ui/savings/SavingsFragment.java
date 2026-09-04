package com.example.appqlct.ui.savings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appqlct.R;
import com.example.appqlct.adapters.SavingsAdapter;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.FragmentSavingsBinding;
import com.example.appqlct.models.SavingsGoal;
import com.example.appqlct.models.Wallet;
import com.example.appqlct.utils.CurrencyFormatter;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SavingsFragment extends Fragment {

    private FragmentSavingsBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private SavingsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSavingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupRecyclerView();

        binding.btnAddSavingsGoal.setOnClickListener(v -> showAddGoalDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavings();
    }

    private void setupRecyclerView() {
        adapter = new SavingsAdapter(requireContext(), new ArrayList<>(), new SavingsAdapter.OnSavingsActionListener() {
            @Override
            public void onDeposit(SavingsGoal goal) {
                showDepositDialog(goal);
            }

            @Override
            public void onDelete(SavingsGoal goal) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa mục tiêu '" + goal.getName() + "'?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            boolean ok = dbHelper.deleteSavingsGoal(goal.getId(), sessionManager.getUserId());
                            if (ok) {
                                Toast.makeText(requireContext(), "Đã xóa mục tiêu tiết kiệm!", Toast.LENGTH_SHORT).show();
                                loadSavings();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.rvSavings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSavings.setAdapter(adapter);
    }

    private void loadSavings() {
        int userId = sessionManager.getUserId();
        List<SavingsGoal> list = dbHelper.getSavingsGoals(userId);

        if (list.isEmpty()) {
            binding.tvEmptySavings.setVisibility(View.VISIBLE);
            binding.rvSavings.setVisibility(View.GONE);
        } else {
            binding.tvEmptySavings.setVisibility(View.GONE);
            binding.rvSavings.setVisibility(View.VISIBLE);
            adapter.updateData(list);
        }
    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_savings, null);
        EditText etName = dialogView.findViewById(R.id.et_savings_name);
        EditText etTarget = dialogView.findViewById(R.id.et_savings_target);
        EditText etDeadline = dialogView.findViewById(R.id.et_savings_deadline);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Tạo mục tiêu", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String targetStr = etTarget.getText() != null ? etTarget.getText().toString().trim() : "";
                    String deadline = etDeadline.getText() != null ? etDeadline.getText().toString().trim() : "";

                    if (name.isEmpty() || targetStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập tên mục tiêu và số tiền!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double target = Double.parseDouble(targetStr);
                        if (target <= 0) {
                            Toast.makeText(requireContext(), "Số tiền mục tiêu phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        dbHelper.addSavingsGoal(sessionManager.getUserId(), name, target, deadline);
                        Toast.makeText(requireContext(), "Tạo mục tiêu tiết kiệm thành công!", Toast.LENGTH_SHORT).show();
                        loadSavings();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDepositDialog(SavingsGoal goal) {
        int userId = sessionManager.getUserId();
        List<Wallet> wallets = dbHelper.getWallets(userId);
        if (wallets.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn chưa có ví tiền khả dụng!", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_deposit_savings, null);
        TextView tvTitle = dialogView.findViewById(R.id.tv_deposit_title);
        tvTitle.setText("Nạp tiền: " + goal.getName());

        Spinner spinner = dialogView.findViewById(R.id.spinner_deposit_wallet);
        EditText etAmount = dialogView.findViewById(R.id.et_deposit_amount);

        List<String> walletLabels = new ArrayList<>();
        for (Wallet w : wallets) {
            walletLabels.add(w.getName() + " (" + CurrencyFormatter.format(w.getBalance()) + ")");
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, walletLabels);
        spinner.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Nạp ngay", (dialog, which) -> {
                    String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
                    if (amountStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập số tiền nạp!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            Toast.makeText(requireContext(), "Số tiền nạp phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Wallet chosenWallet = wallets.get(spinner.getSelectedItemPosition());
                        if (chosenWallet.getBalance() < amount) {
                            Toast.makeText(requireContext(), "Ví " + chosenWallet.getName() + " không đủ số dư!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean ok = dbHelper.depositToGoal(goal.getId(), userId, chosenWallet.getId(), amount);
                        if (ok) {
                            Toast.makeText(requireContext(), "Nạp tiền thành công! Tiến độ mục tiêu đã tăng.", Toast.LENGTH_SHORT).show();
                            loadSavings();
                        } else {
                            Toast.makeText(requireContext(), "Lỗi khi nạp tiền.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
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
