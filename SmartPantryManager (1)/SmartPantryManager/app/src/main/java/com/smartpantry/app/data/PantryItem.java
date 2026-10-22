package com.smartpantry.app.data;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.smartpantry.app.util.IngredientNormalizer;
import com.smartpantry.app.util.UnitConverter;

/** One ingredient the user currently has at home (a row in the pantry_items table). */
@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** The name exactly as the user typed it (shown in the UI). */
    @NonNull
    private String name = "";

    /** Canonical form used by the matcher, e.g. "Tomatoes" -> "tomato". */
    @NonNull
    private String normalizedName = "";

    private double quantity;

    @NonNull
    private String unit = "g";

    /** Expiry date as epoch millis at local midnight; 0 means "no expiry date". */
    private long expiryDate;

    /**
     * Low-stock alert level in the same unit as quantity (0 = no alert set).
     * When quantity falls to this level the item appears in the dashboard's low-stock alerts.
     */
    @ColumnInfo(defaultValue = "0")
    private double minQuantity;

    /** Used by Room. */
    public PantryItem() { }

    @Ignore
    public PantryItem(@NonNull String name, double quantity, @NonNull String unit, long expiryDate) {
        this.name = name.trim();
        this.normalizedName = IngredientNormalizer.normalize(name);
        this.quantity = quantity;
        this.unit = UnitConverter.normalizeUnit(unit);
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull
    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(@NonNull String normalizedName) { this.normalizedName = normalizedName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    @NonNull
    public String getUnit() { return unit; }
    public void setUnit(@NonNull String unit) { this.unit = unit; }

    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }

    public double getMinQuantity() { return minQuantity; }
    public void setMinQuantity(double minQuantity) { this.minQuantity = minQuantity; }

    public boolean hasExpiry() { return expiryDate > 0; }

    /** True when the user set an alert level and the stock has fallen to (or below) it. */
    public boolean isLowStock() { return minQuantity > 0 && quantity <= minQuantity; }
}
