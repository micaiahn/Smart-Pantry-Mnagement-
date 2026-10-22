package com.smartpantry.app.data;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/** Data access object for the pantry: the C, R, U and D of CRUD. */
@Dao
public interface PantryDao {

    @Insert
    long insert(PantryItem item);                       // Create

    /** Observed by the UI: re-delivers the list whenever the table changes. */
    @Query("SELECT * FROM pantry_items ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<PantryItem>> observeAll();            // Read (live)

    @Query("SELECT * FROM pantry_items")
    List<PantryItem> getAllNow();                       // Read (one-off, background thread)

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    PantryItem getById(int id);                         // Read (single row)

    @Update
    void update(PantryItem item);                       // Update

    @Delete
    void delete(PantryItem item);                       // Delete
}
