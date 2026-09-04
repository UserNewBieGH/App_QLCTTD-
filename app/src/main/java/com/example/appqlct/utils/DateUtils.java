package com.example.appqlct.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat DB_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static String getTodayDbFormat() {
        return DB_FORMAT.format(new Date());
    }

    public static String formatToDisplay(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) return "";
        try {
            Date date = DB_FORMAT.parse(dbDate);
            if (date != null) return DISPLAY_FORMAT.format(date);
        } catch (ParseException e) {
            // fallback
        }
        return dbDate;
    }

    public static String formatToDb(Date date) {
        return DB_FORMAT.format(date);
    }

    public static String formatToDisplay(Date date) {
        return DISPLAY_FORMAT.format(date);
    }

    public static String getCurrentTime() {
        return TIME_FORMAT.format(new Date());
    }

    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1; // 1-12
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static String getFirstDayOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return DB_FORMAT.format(cal.getTime());
    }

    public static String getLastDayOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return DB_FORMAT.format(cal.getTime());
    }
}
