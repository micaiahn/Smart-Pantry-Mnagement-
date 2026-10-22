package com.smartpantry.app.data;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StockHistoryDao {

    @Insert
    void insert(StockHistory entry);

    /** Whole history, oldest first. Observed by the dashboard chart. */
    @Query("SELECT * FROM stock_history ORDER BY recordedAt ASC")
    LiveData<List<StockHistory>> observeAll();

    /** History of one ingredient, oldest first (background thread). */
    @Query("SELECT * FROM stock_history WHERE itemId = :itemId ORDER BY recordedAt ASC")
    List<StockHistory> getForItem(int itemId);
}
