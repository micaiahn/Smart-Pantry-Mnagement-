package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.data.RecipeIngredient;
import com.smartpantry.app.data.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE STRICT-MATCHING RULE (core business logic) - designed and implemented by Micaiah Nhamburo.
 *
 * A recipe is "suggested" only if EVERY ingredient it requires is present in the pantry
 * in at least the required quantity. One missing or insufficient ingredient excludes it.
 *
 * Robustness to real-world messiness:
 *  - names are normalised ("Tomatoes" == "tomato", "Spaghetti" == "pasta")
 *  - quantities are converted to a base unit (1 kg covers a recipe that needs 500 g)
 *  - several pantry entries of the same ingredient are added together
 */
public final class RecipeMatcher {

    private RecipeMatcher() { }

    /** The outcome of checking one recipe: which ingredients are not covered by the pantry. */
    public static final class Result {
        public final RecipeWithIngredients recipe;
        public final List<String> missing;

        Result(RecipeWithIngredients recipe, List<String> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }

        /** Strict match: nothing missing (and the recipe actually has ingredients). */
        public boolean isStrictMatch() {
            return missing.isEmpty() && !recipe.ingredients.isEmpty();
        }

        /** Optional bonus list: exactly one ingredient short. */
        public boolean isAlmostThere() {
            return missing.size() == 1;
        }
    }

    /** Key combining the canonical name and the unit dimension, e.g. "flour|MASS". */
    static String key(String normalizedName, String unit) {
        return normalizedName + "|" + UnitConverter.dimensionOf(unit);
    }

    /** Total pantry quantity per ingredient, in base units (g, ml or pieces). */
    public static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String k = key(item.getNormalizedName(), item.getUnit());
            double base = UnitConverter.toBase(item.getQuantity(), item.getUnit());
            Double existing = totals.get(k);
            totals.put(k, existing == null ? base : existing + base);
        }
        return totals;
    }

    /** Returns the display names of recipe ingredients the pantry cannot cover. */
    public static List<String> findMissing(RecipeWithIngredients recipe, Map<String, Double> pantryTotals) {
        List<String> missing = new ArrayList<>();
        for (RecipeIngredient needed : recipe.ingredients) {
            Double have = pantryTotals.get(key(needed.getNormalizedName(), needed.getUnit()));
            double need = UnitConverter.toBase(needed.getQuantity(), needed.getUnit());
            // small tolerance so 0.1 + 0.2 style floating point sums do not cause false misses
            if (have == null || have + 1e-9 < need) {
                missing.add(needed.getName());
            }
        }
        return missing;
    }

    /** Evaluates every recipe against the pantry. */
    public static List<Result> evaluateAll(List<RecipeWithIngredients> recipes, List<PantryItem> pantry) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        List<Result> results = new ArrayList<>();
        for (RecipeWithIngredients r : recipes) {
            results.add(new Result(r, findMissing(r, totals)));
        }
        return results;
    }

    /** Only the recipes the user can cook right now. This feeds the main Suggested list. */
    public static List<Result> strictMatches(List<RecipeWithIngredients> recipes, List<PantryItem> pantry) {
        List<Result> out = new ArrayList<>();
        for (Result r : evaluateAll(recipes, pantry)) {
            if (r.isStrictMatch()) {
                out.add(r);
            }
        }
        return out;
    }

    /** Bonus list: recipes missing exactly one ingredient. Always shown separately. */
    public static List<Result> almostThere(List<RecipeWithIngredients> recipes, List<PantryItem> pantry) {
        List<Result> out = new ArrayList<>();
        for (Result r : evaluateAll(recipes, pantry)) {
            if (r.isAlmostThere() && !r.recipe.ingredients.isEmpty()) {
                out.add(r);
            }
        }
        return out;
    }
}
