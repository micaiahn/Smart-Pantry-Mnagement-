package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import com.smartpantry.app.data.StockHistory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns raw stock history rows into the numbers drawn by the dashboard chart:
 * "how many ingredients were in the pantry at the end of each of the last N days".
 */
public final class StockHistoryCalculator {

    private StockHistoryCalculator() { }

    /** Last millisecond of the day that was {@code daysAgo} days before {@code now} (0 = today). */
    public static long endOfDay(long now, int daysAgo) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(TextFormat.startOfDay(now));
        c.add(Calendar.DAY_OF_YEAR, 1 - daysAgo);
        return c.getTimeInMillis() - 1;
    }

    /**
     * Number of ingredients with a quantity above zero at the end of each day,
     * oldest day first; the last entry is today.
     */
    public static int[] itemCountsPerDay(List<StockHistory> history, int days, long now) {
        List<StockHistory> sorted = new ArrayList<>(history);
        Collections.sort(sorted, (a, b) -> Long.compare(a.getRecordedAt(), b.getRecordedAt()));

        int[] counts = new int[days];
        Map<Integer, Double> latest = new HashMap<>();
        int index = 0;
        for (int i = 0; i < days; i++) {
            long dayEnd = endOfDay(now, days - 1 - i);
            while (index < sorted.size() && sorted.get(index).getRecordedAt() <= dayEnd) {
                latest.put(sorted.get(index).getItemId(), sorted.get(index).getQuantity());
                index++;
            }
            int count = 0;
            for (double quantity : latest.values()) {
                if (quantity > 0) {
                    count++;
                }
            }
            counts[i] = count;
        }
       
        return counts;
    }
}
