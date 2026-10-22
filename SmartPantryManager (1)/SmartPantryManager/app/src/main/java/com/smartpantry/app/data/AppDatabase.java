package com.smartpantry.app.data;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.content.Context;
import android.content*;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The single on-device SQLite database (via Room).
 * Room generates the SQL and the SQLiteOpenHelper for us; the file lives in the app's
 * private storage, so the data survives closing and reopening the app.
 *
 * Version 2 adds the low-stock alert level and the stock history table. The migration keeps
 * all existing pantry data (nothing is wiped on upgrade).
 */
@Database(
        entities = {PantryItem.class, Recipe.class, RecipeIngredient.class, StockHistory.class},
        version = 2,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "smart_pantry.db";
    private static volatile AppDatabase instance;

    /** Version 1 -> 2: new column, new table, and a starting history point for existing items. */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE pantry_items ADD COLUMN minQuantity REAL NOT NULL DEFAULT 0");
            db.execSQL("CREATE TABLE IF NOT EXISTS stock_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "itemId INTEGER NOT NULL, "
                    + "quantity REAL NOT NULL, "
                    + "recordedAt INTEGER NOT NULL)");
            db.execSQL("INSERT INTO stock_history (itemId, quantity, recordedAt) "
                    + "SELECT id, quantity, " + System.currentTimeMillis() + " FROM pantry_items");
        }
    };

    /**
     * Room forbids database work on the main thread. A single background thread runs all
     * writes in order, which also stops the first-run seeding from running twice.
     */
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public abstract PantryDao pantryDao();

    public abstract RecipeDao recipeDao();

    public abstract StockHistoryDao stockHistoryDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(), AppDatabase.class, DB_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }

    /** Pre-loads the recipe collection the first time the app runs (when the table is empty). */
    public static void seedIfEmpty(Context context) {
        final Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            AppDatabase db = getInstance(appContext);
            if (db.recipeDao().count() == 0) {
                RecipeSeeder.seed(db);
            }
        });
    }
}
