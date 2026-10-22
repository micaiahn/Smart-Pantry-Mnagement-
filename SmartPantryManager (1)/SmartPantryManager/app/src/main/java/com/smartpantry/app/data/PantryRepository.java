package com.smartpantry.app.data;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

/**
 * Single place that changes pantry rows AND records the matching stock history.
 * Every method touches the database, so call them from AppDatabase.EXECUTOR (never the main thread).
 */
public final class PantryRepository {

    private PantryRepository() { }

    /** Create (also used by "Undo" after a delete: the item keeps its old id). */
    public static void insert(final AppDatabase db, final PantryItem item) {
        db.runInTransaction(() -> {
            long id = db.pantryDao().insert(item);
            item.setId((int) id);
            record(db, item.getId(), item.getQuantity());
        });
    }

    /** Update; history is only recorded when the quantity actually changed. */
    public static void update(final AppDatabase db, final PantryItem item) {
        db.runInTransaction(() -> {
            PantryItem previous = db.pantryDao().getById(item.getId());
            db.pantryDao().update(item);
            if (previous == null || previous.getQuantity() != item.getQuantity()) {
                record(db, item.getId(), item.getQuantity());
            }
        });
    }

    /** Delete; a zero quantity is recorded so charts know the item is gone. */
    public static void delete(final AppDatabase db, final PantryItem item) {
        db.runInTransaction(() -> {
            db.pantryDao().delete(item);
            record(db, item.getId(), 0);
        });
    }

    private static void record(AppDatabase db, int itemId, double quantity) {
        db.stockHistoryDao().insert(new StockHistory(itemId, quantity, System.currentTimeMillis()));
    }
}
