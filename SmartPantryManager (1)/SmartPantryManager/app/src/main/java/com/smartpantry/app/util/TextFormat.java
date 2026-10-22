package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Small helpers for showing quantities and expiry dates. */
public final class TextFormat {

    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    private TextFormat() { }

    /** 200.0 -> "200", 0.5 -> "0.5", 1.25 -> "1.25". */
    public static String formatQuantity(double quantity) {
        if (quantity == Math.rint(quantity)) {
            return String.valueOf((long) quantity);
        }
        String text = String.format(Locale.US, "%.2f", quantity);
        if (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    /** Short date for chart labels, e.g. "29 Mar". */
    public static String formatShortDate(long millis) {
        return new SimpleDateFormat("d MMM", Locale.getDefault()).format(new Date(millis));
    }

    /** Midnight (local time) at the start of the day containing the given instant. */
    public static long startOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** Whole days from today until the expiry date (negative = already expired). */
    public static int daysUntil(long expiryMillis, long nowMillis) {
        long diff = startOfDay(expiryMillis) - startOfDay(nowMillis);
        return (int) Math.round((double) diff / DAY_MS);
    }
}
