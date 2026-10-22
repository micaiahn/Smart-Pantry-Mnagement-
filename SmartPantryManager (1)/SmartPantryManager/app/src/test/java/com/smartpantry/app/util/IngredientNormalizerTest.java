package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IngredientNormalizerTest {

    @Test
    public void pluralsBecomeSingular() {
        assertEquals("tomato", IngredientNormalizer.normalize("Tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalize("potatoes"));
        assertEquals("egg", IngredientNormalizer.normalize("EGGS"));
        assertEquals("cherry", IngredientNormalizer.normalize("cherries"));
        assertEquals("peach", IngredientNormalizer.normalize("peaches"));
        assertEquals("onion", IngredientNormalizer.normalize("onions"));
    }

    @Test
    public void wordsThatLookPluralAreKept() {
        assertEquals("hummus", IngredientNormalizer.normalize("hummus"));
        assertEquals("asparagus", IngredientNormalizer.normalize("Asparagus"));
        assertEquals("molasses", IngredientNormalizer.normalize("molasses"));
    }

    @Test
    public void whitespaceCaseAndFillerWordsAreIgnored() {
        assertEquals("tomato", IngredientNormalizer.normalize("  Fresh   TOMATO "));
        assertEquals("onion", IngredientNormalizer.normalize("chopped onions"));
    }

    @Test
    public void synonymsMapToOneCanonicalName() {
        assertEquals("zucchini", IngredientNormalizer.normalize("Baby Marrows"));
        assertEquals("pasta", IngredientNormalizer.normalize("spaghetti"));
        assertEquals("yogurt", IngredientNormalizer.normalize("yoghurt"));
        assertEquals("cooking oil", IngredientNormalizer.normalize("Sunflower oil"));
        assertEquals("garlic", IngredientNormalizer.normalize("garlic cloves"));
        assertEquals("lentil", IngredientNormalizer.normalize("red lentils"));
    }

    @Test
    public void emptyOrSymbolOnlyInputGivesEmptyString() {
        assertEquals("", IngredientNormalizer.normalize(null));
        assertEquals("", IngredientNormalizer.normalize("   "));
        assertEquals("", IngredientNormalizer.normalize("123 !!"));
    }
}
