package com.example.appqlct.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {
    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        formatter = new DecimalFormat("#,###", symbols);
    }

    public static String format(double amount) {
        return formatter.format(amount) + " đ";
    }

    public static String formatWithSign(double amount, String type) {
        if ("income".equalsIgnoreCase(type)) {
            return "+ " + formatter.format(amount) + " đ";
        } else {
            return "- " + formatter.format(amount) + " đ";
        }
    }
}
