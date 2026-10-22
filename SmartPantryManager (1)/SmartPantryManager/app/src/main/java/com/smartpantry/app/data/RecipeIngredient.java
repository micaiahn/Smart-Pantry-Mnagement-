package com.smartpantry.app.data;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.smartpantry.app.util.IngredientNormalizer;
import com.smartpantry.app.util.UnitConverter;

/** One ingredient line of a recipe, e.g. "Pasta 200 g". Deleted automatically with its recipe. */
@Entity(
        tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipeId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("recipeId")})
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int recipeId;

    @NonNull
    private String name = "";

    @NonNull
    private String normalizedName = "";

    private double quantity;

    @NonNull
    private String unit = "g";

    public RecipeIngredient() { }

    @Ignore
    public RecipeIngredient(@NonNull String name, double quantity, @NonNull String unit) {
        this.name = name;
        this.normalizedName = IngredientNormalizer.normalize(name);
        this.quantity = quantity;
        this.unit = UnitConverter.normalizeUnit(unit);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

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
}
