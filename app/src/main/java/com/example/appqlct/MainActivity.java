package com.example.appqlct;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.ViewGroup;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appqlct.databinding.ActivityMainBinding;
import com.example.appqlct.ui.auth.LoginActivity;
import com.example.appqlct.ui.budgets.BudgetsFragment;
import com.example.appqlct.ui.chat.AIChatActivity;
import com.example.appqlct.ui.dashboard.DashboardFragment;
import com.example.appqlct.ui.profile.ProfileFragment;
import com.example.appqlct.ui.savings.SavingsFragment;
import com.example.appqlct.ui.transactions.AddEditTransactionActivity;
import com.example.appqlct.ui.transactions.TransactionsFragment;
import com.example.appqlct.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainCoordinator, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            binding.bottomNavigation.post(() -> {
                int navHeight = binding.bottomNavigation.getHeight();
                int margin = navHeight + (int) (16 * getResources().getDisplayMetrics().density);

                if (binding.fabAddTransaction.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams lpAdd = (ViewGroup.MarginLayoutParams) binding.fabAddTransaction.getLayoutParams();
                    lpAdd.bottomMargin = margin;
                    binding.fabAddTransaction.setLayoutParams(lpAdd);
                }

                if (binding.fabAiAssistant.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams lpAi = (ViewGroup.MarginLayoutParams) binding.fabAiAssistant.getLayoutParams();
                    lpAi.bottomMargin = margin;
                    binding.fabAiAssistant.setLayoutParams(lpAi);
                }
            });

            return insets;
        });

        // Default to Dashboard
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        setupBottomNavigation();
        setupFabButtons();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment());
                binding.fabAddTransaction.show();
                return true;
            } else if (id == R.id.nav_transactions) {
                loadFragment(new TransactionsFragment());
                binding.fabAddTransaction.show();
                return true;
            } else if (id == R.id.nav_budgets) {
                loadFragment(new BudgetsFragment());
                binding.fabAddTransaction.hide();
                return true;
            } else if (id == R.id.nav_savings) {
                loadFragment(new SavingsFragment());
                binding.fabAddTransaction.hide();
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                binding.fabAddTransaction.hide();
                return true;
            }
            return false;
        });
    }

    private void setupFabButtons() {
        binding.fabAddTransaction.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddEditTransactionActivity.class));
        });

        binding.fabAiAssistant.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AIChatActivity.class));
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    public void navigateToTab(int menuItemId) {
        binding.bottomNavigation.setSelectedItemId(menuItemId);
    }
}