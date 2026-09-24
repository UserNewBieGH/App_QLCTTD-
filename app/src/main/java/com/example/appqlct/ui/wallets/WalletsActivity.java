package com.example.appqlct.ui.wallets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appqlct.R;
import com.example.appqlct.adapters.WalletAdapter;
import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.databinding.ActivityWalletsBinding;
import com.example.appqlct.models.Wallet;
import com.example.appqlct.utils.CurrencyFormatter;
import com.example.appqlct.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class WalletsActivity extends AppCompatActivity {

    private ActivityWalletsBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private WalletAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        setupRecyclerView();

        binding.btnBackWallets.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.btnAddWallet.setOnClickListener(v -> showAddEditWalletDialog(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWallets();
    }

    private void setupRecyclerView() {
        adapter = new WalletAdapter(this, new ArrayList<>(), new WalletAdapter.OnWalletActionListener() {
            @Override
            public void onEdit(Wallet wallet) {
                showAddEditWalletDialog(wallet);
            }

            @Override
            public void onDelete(Wallet wallet) {
                new AlertDialog.Builder(WalletsActivity.this)
                        .setTitle("Xác nhận xóa ví")
                        .setMessage("Bạn có chắc chắn muốn xóa ví '" + wallet.getName() + "'? Lưu ý: Không thể xóa ví nếu đã có giao dịch liên kết.")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            boolean ok = dbHelper.deleteWallet(wallet.getId(), sessionManager.getUserId());
                            if (ok) {
                                Toast.makeText(WalletsActivity.this, "Đã xóa ví thành công!", Toast.LENGTH_SHORT).show();
                                loadWallets();
                            } else {
                                Toast.makeText(WalletsActivity.this, "Không thể xóa ví đã liên kết với giao dịch hoặc ví duy nhất!", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.rvWallets.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWallets.setAdapter(adapter);
    }

    private void loadWallets() {
        int userId = sessionManager.getUserId();
        List<Wallet> wallets = dbHelper.getWallets(userId);
        adapter.updateData(wallets);

        double total = dbHelper.getTotalBalance(userId);
        binding.tvWalletsTotalBalance.setText(CurrencyFormatter.format(total));
    }

    private void showAddEditWalletDialog(Wallet wallet) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_wallet, null);
        TextView tvTitle = dialogView.findViewById(R.id.tv_wallet_dialog_title);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name_input);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance_input);
        CheckBox cbDefault = dialogView.findViewById(R.id.cb_wallet_default);
        View layoutBalance = dialogView.findViewById(R.id.layout_wallet_balance);

        boolean isEdit = (wallet != null);
        if (isEdit) {
            tvTitle.setText("Chỉnh sửa thông tin ví");
            etName.setText(wallet.getName());
            layoutBalance.setVisibility(View.GONE); // balance updated through transactions
            cbDefault.setChecked(wallet.isDefault());
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên ví!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isDefault = cbDefault.isChecked();
                    int userId = sessionManager.getUserId();

                    if (!isEdit) {
                        String balStr = etBalance.getText() != null ? etBalance.getText().toString().trim() : "0";
                        balStr = balStr.replace(".", "").replace(",", "").trim();
                        double balance = 0;
                        try {
                            if (!balStr.isEmpty()) balance = Double.parseDouble(balStr);
                        } catch (NumberFormatException ignored) {}

                        dbHelper.addWallet(userId, name, "ic_wallet", balance, "#10B981", isDefault);
                        Toast.makeText(this, "Tạo ví mới thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.updateWallet(wallet.getId(), userId, name, wallet.getIcon(), wallet.getColor(), isDefault);
                        Toast.makeText(this, "Cập nhật ví thành công!", Toast.LENGTH_SHORT).show();
                    }
                    loadWallets();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
