package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Handles unit differences for the strict-matching rule.
 * Every unit belongs to a Dimension (mass, volume or count) and can be converted
 * to a base unit of that dimension: grams, millilitres or pieces.
 * Units from different dimensions are never compared (1 kg of flour is not "3 pieces").
 */
public final class UnitConverter {

    public enum Dimension { MASS, VOLUME, COUNT }

    /** Canonical units offered in the app's unit spinner. */
    public static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "piece"};

    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        String[][] pairs = {
                {"gram", "g"}, {"grams", "g"}, {"gr", "g"},
                {"kilogram", "kg"}, {"kilograms", "kg"}, {"kilo", "kg"}, {"kilos", "kg"}, {"kgs", "kg"},
                {"milliliter", "ml"}, {"milliliters", "ml"}, {"millilitre", "ml"}, {"millilitres", "ml"}, {"mls", "ml"},
                {"liter", "l"}, {"liters", "l"}, {"litre", "l"}, {"litres", "l"},
                {"teaspoon", "tsp"}, {"teaspoons", "tsp"}, {"tsps", "tsp"},
                {"tablespoon", "tbsp"}, {"tablespoons", "tbsp"}, {"tbs", "tbsp"}, {"tbsps", "tbsp"},
                {"cups", "cup"},
                {"pieces", "piece"}, {"pc", "piece"}, {"pcs", "piece"}, {"each", "piece"},
                {"unit", "piece"}, {"units", "piece"}, {"slice", "piece"}, {"slices", "piece"},
                {"clove", "piece"}, {"cloves", "piece"}, {"can", "piece"}, {"cans", "piece"},
                {"tin", "piece"}, {"tins", "piece"}, {"whole", "piece"}, {"item", "piece"}, {"items", "piece"}
        };
        for (String[] p : pairs) {
            ALIASES.put(p[0], p[1]);
        }
        for (String u : UNITS) {
            ALIASES.put(u, u);
        }
    }

    private UnitConverter() { }

    /** Maps free text such as "Grams" or "tbs" to a canonical unit; unknown text becomes "piece". */
    public static String normalizeUnit(String raw) {
        if (raw == null) {
            return "piece";
        }
        String key = raw.trim().toLowerCase(Locale.ROOT);
        if (key.endsWith(".")) {
            key = key.substring(0, key.length() - 1);
        }
        String canonical = ALIASES.get(key);
        return canonical != null ? canonical : "piece";
    }

    public static Dimension dimensionOf(String unit) {
        switch (normalizeUnit(unit)) {
            case "g":
            case "kg":
                return Dimension.MASS;
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return Dimension.VOLUME;
            default:
                return Dimension.COUNT;
        }
    }

    /** Converts a quantity to the base unit of its dimension (g, ml or piece). */
    public static double toBase(double quantity, String unit) {
        switch (normalizeUnit(unit)) {
            case "kg":
            case "l":
                return quantity * 1000.0;
            case "tsp":
                return quantity * 5.0;
            case "tbsp":
                return quantity * 15.0;
            case "cup":
                return quantity * 240.0;
            default: // g, ml, piece
                return quantity;
        }
    }
}
