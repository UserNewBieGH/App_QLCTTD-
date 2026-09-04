package com.example.appqlct.utils;

import android.content.Context;

import com.example.appqlct.database.DatabaseHelper;
import com.example.appqlct.models.Budget;
import com.example.appqlct.models.Category;
import com.example.appqlct.models.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AICoinsVisionEngine {

    public static String getReply(Context context, int userId, String userQuery) {
        String q = userQuery.toLowerCase(Locale.getDefault()).trim();
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();

        double totalBalance = db.getTotalBalance(userId);
        double monthlyIncome = db.getMonthlyTotal(userId, "income", month, year);
        double monthlyExpense = db.getMonthlyTotal(userId, "expense", month, year);
        double netSaving = monthlyIncome - monthlyExpense;

        if (q.contains("số dư") || q.contains("tiền còn") || q.contains("bao nhiêu tiền")) {
            return "📊 Tổng số dư khả dụng trên tất cả các ví của bạn hiện tại là **" +
                    CurrencyFormatter.format(totalBalance) + "**.\n\nHãy phân bổ chi tiêu hợp lý để duy trì nguồn tài chính an toàn nhé!";
        }

        if (q.contains("thu chi") || q.contains("tháng này") || q.contains("tổng quan")) {
            StringBuilder sb = new StringBuilder();
            sb.append("📈 **Báo cáo tình hình tài chính tháng ").append(month).append("/").append(year).append("**:\n\n");
            sb.append("• Tổng thu nhập: **").append(CurrencyFormatter.format(monthlyIncome)).append("**\n");
            sb.append("• Tổng chi tiêu: **").append(CurrencyFormatter.format(monthlyExpense)).append("**\n");
            sb.append("• Tiết kiệm ròng: **").append(CurrencyFormatter.format(netSaving)).append("**\n\n");

            if (monthlyIncome > 0) {
                double rate = (monthlyExpense / monthlyIncome) * 100;
                sb.append(String.format(Locale.getDefault(), "Bạn đã chi tiêu khoảng **%.1f%%** thu nhập trong tháng. ", rate));
                if (rate > 80) {
                    sb.append("⚠️ Tỷ lệ chi tiêu đang khá cao, bạn nên cân nhắc cắt giảm các khoản không thiết yếu!");
                } else {
                    sb.append("✅ Bạn đang quản lý chi tiêu rất tốt!");
                }
            }
            return sb.toString();
        }

        if (q.contains("nhiều nhất") || q.contains("tiêu vào đâu") || q.contains("danh mục")) {
            List<Transaction> txList = db.getTransactions(userId, "expense", month, year, null, 100);
            if (txList.isEmpty()) {
                return "Bạn chưa có khoản chi tiêu nào được ghi nhận trong tháng này.";
            }

            Map<String, Double> catMap = new HashMap<>();
            for (Transaction t : txList) {
                String cat = t.getCategoryName();
                catMap.put(cat, catMap.getOrDefault(cat, 0.0) + t.getAmount());
            }

            String topCat = "";
            double maxAmount = 0;
            for (Map.Entry<String, Double> entry : catMap.entrySet()) {
                if (entry.getValue() > maxAmount) {
                    maxAmount = entry.getValue();
                    topCat = entry.getKey();
                }
            }

            return "💡 Trong tháng " + month + "/" + year + ", danh mục bạn chi nhiều nhất là **" +
                    topCat + "** với tổng số tiền là **" + CurrencyFormatter.format(maxAmount) +
                    "**.\n\nHãy kiểm tra xem đây có phải là chi phí cố định hay có thể tối ưu thêm không nhé!";
        }

        if (q.contains("ngân sách") || q.contains("hạn mức")) {
            List<Budget> budgets = db.getBudgets(userId, month, year);
            if (budgets.isEmpty()) {
                return "Bạn chưa thiết lập ngân sách nào cho tháng này. Hãy vào mục 'Ngân sách' để đặt giới hạn chi tiêu và kiểm soát ví tiền tốt hơn nhé!";
            }

            StringBuilder sb = new StringBuilder("🎯 **Tình trạng ngân sách tháng " + month + "/" + year + "**:\n\n");
            for (Budget b : budgets) {
                sb.append("• ").append(b.getCategoryName()).append(": ")
                        .append(CurrencyFormatter.format(b.getSpentAmount())).append(" / ")
                        .append(CurrencyFormatter.format(b.getLimitAmount()))
                        .append(" (").append(b.getPercentage()).append("%)\n");
            }
            return sb.toString();
        }

        if (q.contains("tiết kiệm") || q.contains("lời khuyên") || q.contains("mẹo") || q.contains("tư vấn")) {
            return "💡 **3 Lời khuyên tài chính từ Coins Vision**:\n\n" +
                    "1. **Quy tắc 50/30/20**: 50% thu nhập cho nhu cầu thiết yếu (ăn uống, hóa đơn), 30% cho sở thích cá nhân, 20% tích lũy vào mục tiêu tiết kiệm hoặc quỹ khẩn cấp.\n" +
                    "2. **Ghi chép giao dịch tức thì**: Mỗi khi phát sinh giao dịch, hãy tạo ngay trên ứng dụng để không bỏ sót khoản nhỏ lẻ nào.\n" +
                    "3. **Đặt ngân sách cho từng danh mục**: Luôn duy trì cảnh báo ngân sách dưới 80% để chủ động dòng tiền cuối tháng.";
        }

        if (q.contains("xin chào") || q.contains("hello") || q.contains("hi") || q.contains("ơi")) {
            return "Xin chào bạn! 👋 Tôi là **Trợ lý AI Coins Vision**.\n\nTôi có thể giúp bạn kiểm tra số dư ví, phân tích thu chi tháng, tìm danh mục chi nhiều nhất, hoặc tư vấn kế hoạch tiết kiệm. Bạn muốn tôi hỗ trợ điều gì hôm nay?";
        }

        return "Cảm ơn bạn đã trò chuyện cùng Trợ lý AI Coins Vision! Bạn có thể chọn các câu hỏi gợi ý nhanh bên dưới (Số dư ví, Thu chi tháng này, Chi tiêu nhiều nhất) hoặc hỏi về cách quản lý tài chính cá nhân nhé.";
    }
}
