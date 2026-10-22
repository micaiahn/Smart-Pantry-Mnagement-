package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.data.StockHistory;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** Tests for the dashboard chart numbers and the low-stock rule. */
public class StockHistoryCalculatorTest {

    private static final long NOW = System.currentTimeMillis();

    private static StockHistory entry(int itemId, double qty, int daysAgo) {
        return new StockHistory(itemId, qty, StockHistoryCalculator.endOfDay(NOW, daysAgo));
    }

    @Test
    public void countsItemsInPantryAtTheEndOfEachDay() {
        List<StockHistory> history = new ArrayList<>();
        history.add(entry(1, 5, 3));   // item 1 added 3 days ago
        history.add(entry(2, 3, 1));   // item 2 added yesterday
        history.add(entry(1, 0, 0));   // item 1 deleted today

        int[] counts = StockHistoryCalculator.itemCountsPerDay(history, 5, NOW);

        assertEquals(5, counts.length);
        assertEquals(0, counts[0]);    // 4 days ago
        assertEquals(1, counts[1]);    // 3 days ago
        assertEquals(1, counts[2]);    // 2 days ago
        assertEquals(2, counts[3]);    // yesterday
        assertEquals(1, counts[4]);    // today
    }

    @Test
    public void laterQuantityReplacesEarlierOneForTheSameItem() {
        List<StockHistory> history = new ArrayList<>();
        history.add(entry(1, 2, 2));
        history.add(entry(1, 0, 1));   // used up yesterday
        history.add(entry(1, 4, 0));   // restocked today

        int[] counts = StockHistoryCalculator.itemCountsPerDay(history, 3, NOW);

        assertEquals(1, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(1, counts[2]);
    }

    @Test
    public void emptyHistoryGivesAllZeros() {
        int[] counts = StockHistoryCalculator.itemCountsPerDay(new ArrayList<StockHistory>(), 14, NOW);
        assertEquals(14, counts.length);
        for (int c : counts) {
            assertEquals(0, c);
        }
    }

    @Test
    public void lowStockOnlyWhenAnAlertLevelIsSetAndReached() {
        PantryItem item = new PantryItem("Rice", 300, "g", 0);
        assertFalse(item.isLowStock());            // no alert level set

        item.setMinQuantity(500);
        assertTrue(item.isLowStock());             // 300 <= 500

        item.setQuantity(500);
        assertTrue(item.isLowStock());             // exactly at the level counts

        item.setQuantity(800);
        assertFalse(item.isLowStock());            // above the level
    }
}
