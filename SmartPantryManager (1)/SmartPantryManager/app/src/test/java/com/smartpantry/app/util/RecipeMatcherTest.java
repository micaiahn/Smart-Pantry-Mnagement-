package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.data.Recipe;
import com.smartpantry.app.data.RecipeIngredient;
import com.smartpantry.app.data.RecipeSeeder;
import com.smartpantry.app.data.RecipeWithIngredients;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Unit tests for the strict-matching rule (Section 2.3 of the brief). Run with: ./gradlew test */
public class RecipeMatcherTest {

    private static RecipeWithIngredients recipe(String name, RecipeIngredient... ingredients) {
        RecipeWithIngredients r = new RecipeWithIngredients();
        r.recipe = new Recipe(name, "step");
        r.ingredients = new ArrayList<>(Arrays.asList(ingredients));
        return r;
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static PantryItem have(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit, 0);
    }

    private static RecipeWithIngredients fiveIngredientRecipe() {
        return recipe("Five",
                need("Egg", 2, "piece"), need("Flour", 200, "g"), need("Milk", 250, "ml"),
                need("Butter", 20, "g"), need("Sugar", 1, "tbsp"));
    }

    @Test
    public void recipeWithAllFiveIngredientsIsSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 6, "piece"), have("Flour", 1, "kg"), have("Milk", 1, "l"),
                have("Butter", 250, "g"), have("Sugar", 5, "tbsp"));
        assertEquals(1, RecipeMatcher.strictMatches(Arrays.asList(fiveIngredientRecipe()), pantry).size());
    }

    @Test
    public void recipeNeedingFiveWithOnlyFourInPantryIsExcluded() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 6, "piece"), have("Flour", 1, "kg"), have("Milk", 1, "l"),
                have("Butter", 250, "g"));     // sugar missing
        List<RecipeWithIngredients> recipes = Arrays.asList(fiveIngredientRecipe());
        assertTrue(RecipeMatcher.strictMatches(recipes, pantry).isEmpty());
        // ...but it does qualify for the separate, optional "almost there" list
        assertEquals(1, RecipeMatcher.almostThere(recipes, pantry).size());
        assertEquals("Sugar", RecipeMatcher.almostThere(recipes, pantry).get(0).missing.get(0));
    }

    @Test
    public void insufficientQuantityIsExcluded() {
        RecipeWithIngredients r = recipe("Pancakes", need("Flour", 150, "g"), need("Egg", 1, "piece"));
        List<PantryItem> pantry = Arrays.asList(have("Flour", 100, "g"), have("Egg", 2, "piece"));
        assertTrue(RecipeMatcher.strictMatches(Arrays.asList(r), pantry).isEmpty());
    }

    @Test
    public void exactQuantityIsEnough() {
        RecipeWithIngredients r = recipe("Pancakes", need("Flour", 150, "g"), need("Egg", 1, "piece"));
        List<PantryItem> pantry = Arrays.asList(have("Flour", 150, "g"), have("Egg", 1, "piece"));
        assertEquals(1, RecipeMatcher.strictMatches(Arrays.asList(r), pantry).size());
    }

    @Test
    public void singularAndPluralNamesMatch() {
        RecipeWithIngredients r = recipe("Salsa", need("Tomato", 3, "piece"), need("Onion", 1, "piece"));
        List<PantryItem> pantry = Arrays.asList(have("Tomatoes", 4, "piece"), have("onions", 2, "pcs"));
        assertEquals(1, RecipeMatcher.strictMatches(Arrays.asList(r), pantry).size());
    }

    @Test
    public void differentUnitsOfTheSameDimensionAreConverted() {
        RecipeWithIngredients r = recipe("Bread", need("Flour", 500, "g"));
        assertEquals(1, RecipeMatcher.strictMatches(Arrays.asList(r),
                Arrays.asList(have("Flour", 1, "kg"))).size());
        assertTrue(RecipeMatcher.strictMatches(Arrays.asList(r),
                Arrays.asList(have("Flour", 0.4, "kg"))).isEmpty());
    }

    @Test
    public void severalPantryEntriesOfTheSameIngredientAreAddedTogether() {
        RecipeWithIngredients r = recipe("Cake", need("Flour", 250, "g"));
        List<PantryItem> pantry = Arrays.asList(have("Flour", 150, "g"), have("Flour", 150, "g"));
        assertEquals(1, RecipeMatcher.strictMatches(Arrays.asList(r), pantry).size());
    }

    @Test
    public void incompatibleUnitDimensionsDoNotMatch() {
        RecipeWithIngredients r = recipe("Bread", need("Flour", 100, "g"));
        assertTrue(RecipeMatcher.strictMatches(Arrays.asList(r),
                Arrays.asList(have("Flour", 3, "piece"))).isEmpty());
    }

    @Test
    public void emptyPantryMatchesNothing() {
        assertTrue(RecipeMatcher.strictMatches(RecipeSeeder.buildSeedRecipes(),
                new ArrayList<PantryItem>()).isEmpty());
    }

    @Test
    public void recipeWithoutIngredientsIsNeverSuggested() {
        RecipeWithIngredients r = recipe("Empty");
        assertTrue(RecipeMatcher.strictMatches(Arrays.asList(r), new ArrayList<PantryItem>()).isEmpty());
    }

    @Test
    public void seedDataHasAtLeastFifteenRecipesEachWithIngredientsAndSteps() {
        List<RecipeWithIngredients> seeds = RecipeSeeder.buildSeedRecipes();
        assertTrue(seeds.size() >= 15);
        for (RecipeWithIngredients r : seeds) {
            assertFalse(r.recipe.getName() + " has no ingredients", r.ingredients.isEmpty());
            assertFalse(r.recipe.getName() + " has no steps", r.recipe.getSteps().isEmpty());
        }
    }

    @Test
    public void addingAndRemovingOneIngredientTogglesASeededRecipe() {
        List<RecipeWithIngredients> seeds = RecipeSeeder.buildSeedRecipes();
        List<PantryItem> pantry = new ArrayList<>(Arrays.asList(
                have("Bread", 2, "slices"), have("Cheese", 100, "g")));
        assertFalse(containsRecipe(RecipeMatcher.strictMatches(seeds, pantry), "Cheese Toastie")); // no butter yet

        pantry.add(have("Butter", 50, "g"));
        assertTrue(containsRecipe(RecipeMatcher.strictMatches(seeds, pantry), "Cheese Toastie"));

        pantry.remove(pantry.size() - 1);
        assertFalse(containsRecipe(RecipeMatcher.strictMatches(seeds, pantry), "Cheese Toastie"));
    }

    private static boolean containsRecipe(List<RecipeMatcher.Result> results, String name) {
        for (RecipeMatcher.Result r : results) {
            if (r.recipe.recipe.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
