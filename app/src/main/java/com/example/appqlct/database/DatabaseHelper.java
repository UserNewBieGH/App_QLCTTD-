package com.example.appqlct.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.appqlct.models.Budget;
import com.example.appqlct.models.Category;
import com.example.appqlct.models.SavingsGoal;
import com.example.appqlct.models.Transaction;
import com.example.appqlct.models.User;
import com.example.appqlct.models.Wallet;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton pattern
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Table USERS
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "avatar_url TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

        // 2. Table WALLETS
        db.execSQL("CREATE TABLE wallets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "icon TEXT DEFAULT 'ic_wallet', " +
                "balance REAL DEFAULT 0, " +
                "color TEXT DEFAULT '#10B981', " +
                "is_default INTEGER DEFAULT 0, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");

        // 3. Table CATEGORIES
        db.execSQL("CREATE TABLE categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "icon TEXT DEFAULT 'ic_tag', " +
                "color TEXT DEFAULT '#6B7280', " +
                "type TEXT NOT NULL, " +
                "is_default INTEGER DEFAULT 0, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");

        // 4. Table TRANSACTIONS
        db.execSQL("CREATE TABLE transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "wallet_id INTEGER NOT NULL, " +
                "type TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "note TEXT, " +
                "transaction_date TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id), " +
                "FOREIGN KEY(category_id) REFERENCES categories(id), " +
                "FOREIGN KEY(wallet_id) REFERENCES wallets(id))");

        // 5. Table BUDGETS
        db.execSQL("CREATE TABLE budgets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "limit_amount REAL NOT NULL, " +
                "month INTEGER NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(category_id) REFERENCES categories(id))");

        // 6. Table SAVINGS_GOALS
        db.execSQL("CREATE TABLE savings_goals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "target_amount REAL NOT NULL, " +
                "current_amount REAL DEFAULT 0, " +
                "deadline TEXT, " +
                "status TEXT DEFAULT 'in_progress', " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS savings_goals");
        db.execSQL("DROP TABLE IF EXISTS budgets");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS wallets");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ================= USER & AUTH =================

    public long registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("email", email.toLowerCase().trim());
        cv.put("password_hash", password); // Simple hash or plain for local demo

        long userId = db.insert("users", null, cv);
        if (userId != -1) {
            // Seed default categories and default wallet for new user
            seedUserData((int) userId);
        }
        return userId;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password_hash = ?",
                new String[]{email.toLowerCase().trim(), password});

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow("password_hash")));
            user.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow("avatar_url")));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            cursor.close();
        }
        return user;
    }

    public boolean verifyPassword(int userId, String currentPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE id = ? AND password_hash = ?",
                new String[]{String.valueOf(userId), currentPassword});
        boolean match = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return match;
    }

    public boolean updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password_hash", newPassword);
        return db.update("users", cv, "id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    public int updateUserProfile(int userId, String username, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Check if email already exists for another user
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ? AND id != ?",
                new String[]{email.toLowerCase().trim(), String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return -1; // Email already in use
        }
        if (cursor != null) cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("email", email.toLowerCase().trim());
        boolean success = db.update("users", cv, "id = ?", new String[]{String.valueOf(userId)}) > 0;
        return success ? 1 : 0;
    }

    public int getTotalTransactionCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(id) FROM transactions WHERE user_id = ?", new String[]{String.valueOf(userId)});
        int count = 0;
        if (c != null && c.moveToFirst()) {
            count = c.getInt(0);
            c.close();
        }
        return count;
    }

    public double getTotalIncomeAllTime(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'income'", new String[]{String.valueOf(userId)});
        double sum = 0;
        if (c != null && c.moveToFirst()) {
            sum = c.getDouble(0);
            c.close();
        }
        return sum;
    }

    public double getTotalExpenseAllTime(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE user_id = ? AND type = 'expense'", new String[]{String.valueOf(userId)});
        double sum = 0;
        if (c != null && c.moveToFirst()) {
            sum = c.getDouble(0);
            c.close();
        }
        return sum;
    }

    public void seedUserData(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Default Wallet
        ContentValues walletCv = new ContentValues();
        walletCv.put("user_id", userId);
        walletCv.put("name", "Tiền mặt");
        walletCv.put("icon", "ic_wallet");
        walletCv.put("balance", 2000000);
        walletCv.put("color", "#10B981");
        walletCv.put("is_default", 1);
        long walletId = db.insert("wallets", null, walletCv);

        // Bank Wallet
        ContentValues bankCv = new ContentValues();
        bankCv.put("user_id", userId);
        bankCv.put("name", "Tài khoản Ngân hàng");
        bankCv.put("icon", "ic_wallet");
        bankCv.put("balance", 15000000);
        bankCv.put("color", "#3B82F6");
        bankCv.put("is_default", 0);
        long bankWalletId = db.insert("wallets", null, bankCv);

        // 2. Default Expense Categories
        String[][] expenseCategories = {
                {"Ăn uống", "ic_category_food", "#EF4444"},
                {"Di chuyển", "ic_category_transport", "#F59E0B"},
                {"Mua sắm", "ic_category_shopping", "#8B5CF6"},
                {"Giải trí", "ic_category_entertainment", "#EC4899"},
                {"Hóa đơn & Tiện ích", "ic_category_bill", "#3B82F6"},
                {"Sức khỏe", "ic_category_health", "#14B8A6"},
                {"Giáo dục", "ic_category_education", "#F97316"},
                {"Chi tiêu khác", "ic_category_other", "#6B7280"}
        };

        long foodCatId = -1;
        for (String[] cat : expenseCategories) {
            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            cv.put("name", cat[0]);
            cv.put("icon", cat[1]);
            cv.put("color", cat[2]);
            cv.put("type", "expense");
            cv.put("is_default", 1);
            long id = db.insert("categories", null, cv);
            if (cat[0].equals("Ăn uống")) foodCatId = id;
        }

        // 3. Default Income Categories
        String[][] incomeCategories = {
                {"Lương", "ic_category_salary", "#10B981"},
                {"Thưởng", "ic_category_bonus", "#06B6D4"},
                {"Đầu tư", "ic_category_investment", "#6366F1"},
                {"Thu nhập khác", "ic_category_other", "#6B7280"}
        };

        for (String[] cat : incomeCategories) {
            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            cv.put("name", cat[0]);
            cv.put("icon", cat[1]);
            cv.put("color", cat[2]);
            cv.put("type", "income");
            cv.put("is_default", 1);
            db.insert("categories", null, cv);
        }

        // 4. Default Budget for current month
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int curMonth = cal.get(java.util.Calendar.MONTH) + 1;
        int curYear = cal.get(java.util.Calendar.YEAR);

        if (foodCatId != -1) {
            ContentValues bCv = new ContentValues();
            bCv.put("user_id", userId);
            bCv.put("category_id", foodCatId);
            bCv.put("limit_amount", 3000000);
            bCv.put("month", curMonth);
            bCv.put("year", curYear);
            db.insert("budgets", null, bCv);
        }

        // 5. Default Savings Goal
        ContentValues sCv = new ContentValues();
        sCv.put("user_id", userId);
        sCv.put("name", "Quỹ dự phòng khẩn cấp");
        sCv.put("target_amount", 10000000);
        sCv.put("current_amount", 2500000);
        sCv.put("deadline", curYear + "-12-31");
        sCv.put("status", "in_progress");
        db.insert("savings_goals", null, sCv);
    }

    // ================= WALLETS =================

    public List<Wallet> getWallets(int userId) {
        List<Wallet> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM wallets WHERE user_id = ? ORDER BY is_default DESC, name ASC",
                new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Wallet w = new Wallet();
                w.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                w.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                w.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                w.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("icon")));
                w.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow("balance")));
                w.setColor(cursor.getString(cursor.getColumnIndexOrThrow("color")));
                w.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1);
                list.add(w);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Wallet getWalletById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM wallets WHERE id = ?", new String[]{String.valueOf(id)});
        Wallet w = null;
        if (cursor != null && cursor.moveToFirst()) {
            w = new Wallet();
            w.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            w.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            w.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            w.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("icon")));
            w.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow("balance")));
            w.setColor(cursor.getString(cursor.getColumnIndexOrThrow("color")));
            w.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1);
            cursor.close();
        }
        return w;
    }

    public long addWallet(int userId, String name, String icon, double balance, String color, boolean isDefault) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isDefault) {
            db.execSQL("UPDATE wallets SET is_default = 0 WHERE user_id = ?", new Object[]{userId});
        }
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("name", name);
        cv.put("icon", icon);
        cv.put("balance", balance);
        cv.put("color", color);
        cv.put("is_default", isDefault ? 1 : 0);
        return db.insert("wallets", null, cv);
    }

    public boolean updateWallet(int id, int userId, String name, String icon, String color, boolean isDefault) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isDefault) {
            db.execSQL("UPDATE wallets SET is_default = 0 WHERE user_id = ?", new Object[]{userId});
        }
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("icon", icon);
        cv.put("color", color);
        cv.put("is_default", isDefault ? 1 : 0);
        return db.update("wallets", cv, "id = ? AND user_id = ?", new String[]{String.valueOf(id), String.valueOf(userId)}) > 0;
    }

    public boolean deleteWallet(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Check if there are transactions associated
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM transactions WHERE wallet_id = ?", new String[]{String.valueOf(id)});
        if (c != null && c.moveToFirst()) {
            int count = c.getInt(0);
            c.close();
            if (count > 0) return false; // Cannot delete wallet with transactions
        }
        return db.delete("wallets", "id = ? AND user_id = ?", new String[]{String.valueOf(id), String.valueOf(userId)}) > 0;
    }

    public double getTotalBalance(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(balance) FROM wallets WHERE user_id = ?", new String[]{String.valueOf(userId)});
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    // ================= CATEGORIES =================

    public List<Category> getCategories(int userId, String type) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM categories WHERE user_id = ? " +
                (type != null ? "AND type = ? " : "") +
                "ORDER BY name ASC";
        String[] args = type != null ? new String[]{String.valueOf(userId), type} : new String[]{String.valueOf(userId)};

        Cursor cursor = db.rawQuery(query, args);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Category c = new Category();
                c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                c.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                c.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                c.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("icon")));
                c.setColor(cursor.getString(cursor.getColumnIndexOrThrow("color")));
                c.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                c.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1);
                list.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Category getCategoryById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM categories WHERE id = ?", new String[]{String.valueOf(id)});
        Category c = null;
        if (cursor != null && cursor.moveToFirst()) {
            c = new Category();
            c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            c.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            c.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            c.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("icon")));
            c.setColor(cursor.getString(cursor.getColumnIndexOrThrow("color")));
            c.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
            c.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1);
            cursor.close();
        }
        return c;
    }

    public long addCategory(int userId, String name, String icon, String color, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("name", name);
        cv.put("icon", icon);
        cv.put("color", color);
        cv.put("type", type);
        cv.put("is_default", 0);
        return db.insert("categories", null, cv);
    }

    public boolean updateCategory(int id, int userId, String name, String icon, String color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("icon", icon);
        cv.put("color", color);
        return db.update("categories", cv, "id = ? AND user_id = ?", new String[]{String.valueOf(id), String.valueOf(userId)}) > 0;
    }

    public boolean deleteCategory(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM transactions WHERE category_id = ?", new String[]{String.valueOf(id)});
        if (c != null && c.moveToFirst()) {
            int count = c.getInt(0);
            c.close();
            if (count > 0) return false; // Cannot delete category used in transactions
        }
        return db.delete("categories", "id = ? AND user_id = ?", new String[]{String.valueOf(id), String.valueOf(userId)}) > 0;
    }

    // ================= TRANSACTIONS =================

    public boolean addTransaction(int userId, int categoryId, int walletId, String type, double amount, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            cv.put("category_id", categoryId);
            cv.put("wallet_id", walletId);
            cv.put("type", type);
            cv.put("amount", amount);
            cv.put("note", note);
            cv.put("transaction_date", date);
            long id = db.insert("transactions", null, cv);
            if (id == -1) return false;

            // Update wallet balance
            double balanceChange = type.equals("income") ? amount : -amount;
            db.execSQL("UPDATE wallets SET balance = balance + ? WHERE id = ?", new Object[]{balanceChange, walletId});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateTransaction(int id, int userId, int newCategoryId, int newWalletId, String newType, double newAmount, String newNote, String newDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Get old transaction
            Cursor c = db.rawQuery("SELECT wallet_id, type, amount FROM transactions WHERE id = ? AND user_id = ?",
                    new String[]{String.valueOf(id), String.valueOf(userId)});
            if (c == null || !c.moveToFirst()) return false;

            int oldWalletId = c.getInt(0);
            String oldType = c.getString(1);
            double oldAmount = c.getDouble(2);
            c.close();

            // Revert old wallet
            double oldBalanceChange = oldType.equals("income") ? -oldAmount : oldAmount;
            db.execSQL("UPDATE wallets SET balance = balance + ? WHERE id = ?", new Object[]{oldBalanceChange, oldWalletId});

            // 2. Update transaction
            ContentValues cv = new ContentValues();
            cv.put("category_id", newCategoryId);
            cv.put("wallet_id", newWalletId);
            cv.put("type", newType);
            cv.put("amount", newAmount);
            cv.put("note", newNote);
            cv.put("transaction_date", newDate);
            db.update("transactions", cv, "id = ?", new String[]{String.valueOf(id)});

            // 3. Apply to new wallet
            double newBalanceChange = newType.equals("income") ? newAmount : -newAmount;
            db.execSQL("UPDATE wallets SET balance = balance + ? WHERE id = ?", new Object[]{newBalanceChange, newWalletId});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteTransaction(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT wallet_id, type, amount FROM transactions WHERE id = ? AND user_id = ?",
                    new String[]{String.valueOf(id), String.valueOf(userId)});
            if (c == null || !c.moveToFirst()) return false;

            int walletId = c.getInt(0);
            String type = c.getString(1);
            double amount = c.getDouble(2);
            c.close();

            // Revert wallet balance
            double balanceChange = type.equals("income") ? -amount : amount;
            db.execSQL("UPDATE wallets SET balance = balance + ? WHERE id = ?", new Object[]{balanceChange, walletId});

            db.delete("transactions", "id = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<Transaction> getTransactions(int userId, String typeFilter, Integer month, Integer year, String searchQuery, int limit) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder("SELECT t.*, c.name as category_name, c.icon as category_icon, c.color as category_color, w.name as wallet_name " +
                "FROM transactions t " +
                "JOIN categories c ON t.category_id = c.id " +
                "JOIN wallets w ON t.wallet_id = w.id " +
                "WHERE t.user_id = ? ");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));

        if (typeFilter != null && !typeFilter.isEmpty() && !typeFilter.equals("all")) {
            sql.append("AND t.type = ? ");
            args.add(typeFilter);
        }
        if (month != null && year != null) {
            String monthStr = month < 10 ? "0" + month : String.valueOf(month);
            sql.append("AND strftime('%m', t.transaction_date) = ? AND strftime('%Y', t.transaction_date) = ? ");
            args.add(monthStr);
            args.add(String.valueOf(year));
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND t.note LIKE ? ");
            args.add("%" + searchQuery.trim() + "%");
        }

        sql.append("ORDER BY t.transaction_date DESC, t.id DESC ");
        if (limit > 0) {
            sql.append("LIMIT ").append(limit);
        }

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                t.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                t.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                t.setWalletId(cursor.getInt(cursor.getColumnIndexOrThrow("wallet_id")));
                t.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                t.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
                t.setTransactionDate(cursor.getString(cursor.getColumnIndexOrThrow("transaction_date")));
                t.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
                t.setCategoryIcon(cursor.getString(cursor.getColumnIndexOrThrow("category_icon")));
                t.setCategoryColor(cursor.getString(cursor.getColumnIndexOrThrow("category_color")));
                t.setWalletName(cursor.getString(cursor.getColumnIndexOrThrow("wallet_name")));
                list.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public double getMonthlyTotal(int userId, String type, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = month < 10 ? "0" + month : String.valueOf(month);
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions " +
                        "WHERE user_id = ? AND type = ? " +
                        "AND strftime('%m', transaction_date) = ? AND strftime('%Y', transaction_date) = ?",
                new String[]{String.valueOf(userId), type, monthStr, String.valueOf(year)});
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    // ================= BUDGETS =================

    public List<Budget> getBudgets(int userId, int month, int year) {
        List<Budget> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = month < 10 ? "0" + month : String.valueOf(month);

        String sql = "SELECT b.*, c.name as category_name, c.icon as category_icon, c.color as category_color, " +
                "(SELECT IFNULL(SUM(t.amount), 0) FROM transactions t " +
                " WHERE t.user_id = b.user_id AND t.category_id = b.category_id AND t.type = 'expense' " +
                " AND strftime('%m', t.transaction_date) = ? AND strftime('%Y', t.transaction_date) = ?) as spent " +
                "FROM budgets b " +
                "JOIN categories c ON b.category_id = c.id " +
                "WHERE b.user_id = ? AND b.month = ? AND b.year = ? " +
                "ORDER BY c.name ASC";

        Cursor cursor = db.rawQuery(sql, new String[]{monthStr, String.valueOf(year), String.valueOf(userId), String.valueOf(month), String.valueOf(year)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Budget b = new Budget();
                b.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                b.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                b.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                b.setLimitAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("limit_amount")));
                b.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow("month")));
                b.setYear(cursor.getInt(cursor.getColumnIndexOrThrow("year")));
                b.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
                b.setCategoryIcon(cursor.getString(cursor.getColumnIndexOrThrow("category_icon")));
                b.setCategoryColor(cursor.getString(cursor.getColumnIndexOrThrow("category_color")));
                b.setSpentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("spent")));
                list.add(b);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public long setBudget(int userId, int categoryId, double limitAmount, int month, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("category_id", categoryId);
        cv.put("limit_amount", limitAmount);
        cv.put("month", month);
        cv.put("year", year);

        // check exists
        Cursor c = db.rawQuery("SELECT id FROM budgets WHERE user_id = ? AND category_id = ? AND month = ? AND year = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId), String.valueOf(month), String.valueOf(year)});
        if (c != null && c.moveToFirst()) {
            int id = c.getInt(0);
            c.close();
            db.update("budgets", cv, "id = ?", new String[]{String.valueOf(id)});
            return id;
        }
        if (c != null) c.close();
        return db.insert("budgets", null, cv);
    }

    public boolean deleteBudget(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("budgets", "id = ? AND user_id = ?", new String[]{String.valueOf(id), String.valueOf(userId)}) > 0;
    }

    public String checkBudgetWarning(int userId, int categoryId, double amount, String dateStr) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Parse month and year from dateStr (YYYY-MM-DD)
        String[] parts = dateStr.split("-");
        if (parts.length < 2) return null;
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        String monthStr = parts[1];

        Cursor bCursor = db.rawQuery(
                "SELECT b.limit_amount, c.name FROM budgets b JOIN categories c ON b.category_id = c.id " +
                        "WHERE b.user_id = ? AND b.category_id = ? AND b.month = ? AND b.year = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId), String.valueOf(month), String.valueOf(year)});

        if (bCursor == null || !bCursor.moveToFirst()) {
            if (bCursor != null) bCursor.close();
            return null; // No budget
        }

        double limit = bCursor.getDouble(0);
        String catName = bCursor.getString(1);
        bCursor.close();

        Cursor sCursor = db.rawQuery(
                "SELECT IFNULL(SUM(amount), 0) FROM transactions " +
                        "WHERE user_id = ? AND category_id = ? AND type = 'expense' " +
                        "AND strftime('%m', transaction_date) = ? AND strftime('%Y', transaction_date) = ?",
                new String[]{String.valueOf(userId), String.valueOf(categoryId), monthStr, String.valueOf(year)});

        double currentSpent = 0;
        if (sCursor != null && sCursor.moveToFirst()) {
            currentSpent = sCursor.getDouble(0);
            sCursor.close();
        }

        double newSpent = currentSpent + amount;
        if (newSpent > limit) {
            return "Cảnh báo: Bạn đã chi vượt mức ngân sách tháng của danh mục [" + catName + "] (" +
                    com.example.appqlct.utils.CurrencyFormatter.format(newSpent) + " / " +
                    com.example.appqlct.utils.CurrencyFormatter.format(limit) + ")!";
        } else if (newSpent >= limit * 0.8) {
            int pct = (int) Math.round((newSpent / limit) * 100);
            return "Cảnh báo: Chi tiêu danh mục [" + catName + "] đã đạt " + pct + "% hạn mức ngân sách tháng!";
        }
        return null;
    }

    // ================= SAVINGS GOALS =================

    public List<SavingsGoal> getSavingsGoals(int userId) {
        List<SavingsGoal> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM savings_goals WHERE user_id = ? ORDER BY status ASC, created_at DESC",
                new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                SavingsGoal g = new SavingsGoal();
                g.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                g.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                g.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                g.setTargetAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("target_amount")));
                g.setCurrentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("current_amount")));
                g.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
                g.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                list.add(g);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public long addSavingsGoal(int userId, String name, double targetAmount, String deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("name", name);
        cv.put("target_amount", targetAmount);
        cv.put("current_amount", 0);
        cv.put("deadline", deadline);
        cv.put("status", "in_progress");
        return db.insert("savings_goals", null, cv);
    }

    public boolean updateSavingsGoal(int id, int userId, String name, double targetAmount, String deadline, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("target_amount", targetAmount);
        cv.put("deadline", deadline);
        cv.put("status", status);
        return db.update("savings_goals", cv, "id = ? AND user_id = ?", new String[]{String.valueOf(id), String.valueOf(userId)}) > 0;
    }

    public boolean depositToGoal(int goalId, int userId, int walletId, double depositAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Check wallet balance
            Cursor wCur = db.rawQuery("SELECT balance FROM wallets WHERE id = ? AND user_id = ?",
                    new String[]{String.valueOf(walletId), String.valueOf(userId)});
            if (wCur == null || !wCur.moveToFirst()) return false;
            double walletBal = wCur.getDouble(0);
            wCur.close();

            if (walletBal < depositAmount) return false; // Not enough balance

            // 2. Get goal info
            Cursor gCur = db.rawQuery("SELECT target_amount, current_amount FROM savings_goals WHERE id = ? AND user_id = ?",
                    new String[]{String.valueOf(goalId), String.valueOf(userId)});
            if (gCur == null || !gCur.moveToFirst()) return false;
            double target = gCur.getDouble(0);
            double current = gCur.getDouble(1);
            gCur.close();

            double newCurrent = current + depositAmount;
            String newStatus = newCurrent >= target ? "completed" : "in_progress";

            // 3. Deduct wallet
            db.execSQL("UPDATE wallets SET balance = balance - ? WHERE id = ?", new Object[]{depositAmount, walletId});

            // 4. Update goal
            ContentValues gCv = new ContentValues();
            gCv.put("current_amount", newCurrent);
            gCv.put("status", newStatus);
            db.update("savings_goals", gCv, "id = ?", new String[]{String.valueOf(goalId)});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteSavingsGoal(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("savings_goals", "id = ? AND user_id = ?", new String[]{String.valueOf(id), String.valueOf(userId)}) > 0;
    }
}
