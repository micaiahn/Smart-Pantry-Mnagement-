package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns the messy text a user types ("Tomatoes", " fresh Tomato ") into one canonical form
 * so the strict matcher compares like with like. This is deliberately a small rule-based
 * approach (not NLP): lower-case, drop filler words, singularise the last word, apply synonyms.
 */
public final class IngredientNormalizer {

    private static final Set<String> NOISE = new HashSet<>(Arrays.asList(
            "fresh", "ripe", "large", "medium", "small", "chopped", "diced", "sliced",
            "minced", "organic", "tinned", "canned", "dried", "grated"));

    /** Words that look plural but must not be changed. */
    private static final Set<String> UNCHANGED = new HashSet<>(Arrays.asList(
            "molasses", "hummus", "asparagus", "couscous", "swiss", "watercress", "citrus"));

    /** Words ending in "ies" that are plurals of "-ie" words rather than "-y" words. */
    private static final Set<String> IES_TO_IE = new HashSet<>(Arrays.asList(
            "cookies", "brownies", "smoothies", "veggies", "pies"));

    private static final Map<String, String> IRREGULAR = new HashMap<>();
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("knives", "knife");

        String[][] syn = {
                {"aubergine", "eggplant"}, {"courgette", "zucchini"}, {"baby marrow", "zucchini"},
                {"capsicum", "bell pepper"}, {"green onion", "spring onion"}, {"scallion", "spring onion"},
                {"yoghurt", "yogurt"}, {"plain yoghurt", "yogurt"}, {"plain yogurt", "yogurt"},
                {"all purpose flour", "flour"}, {"plain flour", "flour"}, {"cake flour", "flour"},
                {"wheat flour", "flour"}, {"bread flour", "flour"},
                {"oil", "cooking oil"}, {"vegetable oil", "cooking oil"}, {"sunflower oil", "cooking oil"},
                {"canola oil", "cooking oil"},
                {"caster sugar", "sugar"}, {"white sugar", "sugar"}, {"granulated sugar", "sugar"},
                {"spaghetti", "pasta"}, {"penne", "pasta"}, {"macaroni", "pasta"}, {"fusilli", "pasta"},
                {"garlic clove", "garlic"},
                {"whole milk", "milk"}, {"full cream milk", "milk"}, {"skim milk", "milk"},
                {"table salt", "salt"},
                {"white bread", "bread"}, {"brown bread", "bread"}, {"wholewheat bread", "bread"},
                {"cheddar", "cheese"}, {"cheddar cheese", "cheese"},
                {"baby spinach", "spinach"}, {"cherry tomato", "tomato"},
                {"red kidney bean", "kidney bean"}, {"rolled oat", "oat"}, {"porridge oat", "oat"},
                {"chilli powder", "chili powder"},
                {"red lentil", "lentil"}, {"brown lentil", "lentil"}, {"green lentil", "lentil"},
                {"jasmine rice", "rice"}, {"basmati rice", "rice"}, {"white rice", "rice"},
                {"brown rice", "rice"}, {"long grain rice", "rice"}
        };
        for (String[] s : syn) {
            SYNONYMS.put(s[0], s[1]);
        }
    }

    private IngredientNormalizer() { }

    /** Returns the canonical form of an ingredient name, or "" if nothing usable was typed. */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.toLowerCase(Locale.ROOT)
                .replace("'", "")
                .replace("\u2019", "")
                .replaceAll("[^a-z]+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }

        List<String> tokens = new ArrayList<>(Arrays.asList(cleaned.split("\\s+")));
        List<String> meaningful = new ArrayList<>();
        for (String t : tokens) {
            if (!NOISE.contains(t)) {
                meaningful.add(t);
            }
        }
        if (!meaningful.isEmpty()) {
            tokens = meaningful;
        }

        int last = tokens.size() - 1;
        tokens.set(last, singularize(tokens.get(last)));

        String joined = String.join(" ", tokens);
        String mapped = SYNONYMS.get(joined);
        return mapped != null ? mapped : joined;
    }

    /** Very small singulariser covering the plural patterns found in ingredient names. */
    static String singularize(String word) {
        int n = word.length();
        if (n <= 3 || UNCHANGED.contains(word)) {
            return word;
        }
        if (IRREGULAR.containsKey(word)) {
            return IRREGULAR.get(word);
        }
        if (IES_TO_IE.contains(word)) {
            return word.substring(0, n - 1);
        }
        if (word.endsWith("ies") && n > 4) {                       // cherries -> cherry
            return word.substring(0, n - 3) + "y";
        }
        if (word.endsWith("oes")                                   // tomatoes -> tomato
                || word.endsWith("ches") || word.endsWith("shes")  // peaches -> peach
                || word.endsWith("sses") || word.endsWith("xes")) {
            return word.substring(0, n - 2);
        }
        if (word.endsWith("ss") || word.endsWith("us")) {          // cress, hummus
            return word;
        }
        if (word.endsWith("s")) {                                  // eggs -> egg
            return word.substring(0, n - 1);
        }
        return word;
    }
}
