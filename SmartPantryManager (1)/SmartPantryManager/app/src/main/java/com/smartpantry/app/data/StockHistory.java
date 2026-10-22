package com.smartpantry.app.data;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * One point in an ingredient's stock history: "item X had quantity Q at time T".
 * A quantity of 0 is recorded when an item is deleted, so the dashboard chart can
 * work out how many items were in the pantry on any given day.
 */
@Entity(tableName = "stock_history")
public class StockHistory {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int itemId;

    private double quantity;

    /** Epoch millis. */
    private long recordedAt;

    public StockHistory() { }

    @Ignore
    public StockHistory(int itemId, double quantity, long recordedAt) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.recordedAt = recordedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public long getRecordedAt() { return recordedAt; }
    public void setRecordedAt(long recordedAt) { this.recordedAt = recordedAt; }
}
