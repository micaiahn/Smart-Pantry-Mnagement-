package com.smartpantry.app.data;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The pre-loaded recipe collection (20 recipes) inserted on first run.
 * Ingredient specs are written as "name|quantity|unit" to keep the data compact.
 */
public final class RecipeSeeder {

    private RecipeSeeder() { }

    /** Inserts every seed recipe and its ingredients in one transaction. */
    public static void seed(AppDatabase db) {
        final RecipeDao dao = db.recipeDao();
        final List<RecipeWithIngredients> all = buildSeedRecipes();
        db.runInTransaction(() -> {
            for (RecipeWithIngredients r : all) {
                int recipeId = (int) dao.insertRecipe(r.recipe);
                for (RecipeIngredient ri : r.ingredients) {
                    ri.setRecipeId(recipeId);
                }
                dao.insertIngredients(r.ingredients);
            }
        });
    }

    public static List<RecipeWithIngredients> buildSeedRecipes() {
        List<RecipeWithIngredients> list = new ArrayList<>();

        add(list, "Scrambled Eggs on Toast",
                "Toast the bread.\nWhisk the eggs with the salt.\nMelt the butter in a pan over low heat.\n"
                        + "Pour in the eggs and stir gently until just set.\nServe on the toast.",
                "egg|3|piece", "bread|2|piece", "butter|10|g", "salt|0.5|tsp");

        add(list, "Cheese Omelette",
                "Beat the eggs with the salt.\nMelt the butter in a non-stick pan.\nPour in the eggs and let the base set.\n"
                        + "Sprinkle over the cheese, fold the omelette in half and cook for one more minute.",
                "egg|3|piece", "cheese|50|g", "butter|10|g", "salt|0.5|tsp");

        add(list, "Tomato Pasta",
                "Boil the pasta in salted water until it gets  tender enough .\nFry the chopped onion and garlic in the olive oil.\n"
                        + "Add the chopped tomatoes and simmer for 10 minutes.\nSeason, toss with the drained pasta and serve.",
                "pasta|200|g", "tomato|3|piece", "onion|1|piece", "garlic|2|piece", "olive oil|2|tbsp", "salt|1|tsp");

        add(list, "Egg Fried Rice",
                "Cook the rice and let it cool.\nStir-fry the diced carrot and onion in the oil.\nPush to the side and scramble the eggs.\n"
                        + "Add the rice and soy sauce and fry for 3 to 4 minutes.",
                "rice|200|g", "egg|2|piece", "carrot|1|piece", "onion|1|piece", "soy sauce|2|tbsp", "cooking oil|1|tbsp");

        add(list, "Fluffy Pancakes",
                "Mix the flour and sugar in a bowl.\nWhisk in the milk and egg to make a smooth batter.\n"
                        + "Melt a little butter in a pan.\nCook spoonfuls of batter for 2 minutes per side until golden.",
                "flour|150|g", "milk|250|ml", "egg|1|piece", "sugar|1|tbsp", "butter|20|g");

        add(list, "Creamy Potato Soup",
                "Peel and cube the potatoes, and chop the onion.\nSoften the onion in the butter.\n"
                        + "Add the potatoes and enough water to cover; simmer for 20 minutes.\n"
                        + "Mash roughly, stir in the milk and salt, and heat through.",
                "potato|4|piece", "onion|1|piece", "butter|20|g", "milk|250|ml", "salt|1|tsp");

        add(list, "Cheese Toastie",
                "Butter the outside of both bread slices.\nPlace the cheese between the slices.\n"
                        + "Toast in a pan for 2 to 3 minutes per side until golden and melted.",
                "bread|2|piece", "cheese|60|g", "butter|10|g");

        add(list, "Lentil Curry",
                "Rinse the lentils.\nFry the onion and garlic in the oil, then stir in the curry powder.\n"
                        + "Add the lentils, chopped tomatoes and 500 ml water.\nSimmer for 25 minutes, season and serve.",
                "lentil|200|g", "onion|1|piece", "tomato|2|piece", "garlic|2|piece",
                "curry powder|2|tsp", "cooking oil|1|tbsp", "salt|1|tsp");

        add(list, "Chickpea Salad",
                "Drain and rinse the chickpeas.\nDice the tomatoes and cucumber.\n"
                        + "Whisk the lemon juice, olive oil and salt.\nToss everything together and serve cold.",
                "chickpea|240|g", "tomato|2|piece", "cucumber|1|piece", "lemon|1|piece", "olive oil|2|tbsp", "salt|0.5|tsp");

        add(list, "Banana Oat Porridge",
                "Simmer the oats and milk for 5 minutes, stirring.\nSlice the banana.\n"
                        + "Serve the porridge topped with banana and a drizzle of honey.",
                "oat|80|g", "milk|300|ml", "banana|1|piece", "honey|1|tbsp");

        add(list, "Garlic Butter Noodles",
                "Cook the noodles and drain.\nMelt the butter and gently fry the crushed garlic for 1 minute.\n"
                        + "Toss the noodles in the garlic butter and season with salt.",
                "noodle|200|g", "butter|30|g", "garlic|3|piece", "salt|0.5|tsp");

        add(list, "Veggie Stir-Fry",
                "Slice the carrots, bell pepper and onion.\nHeat the oil in a wok and add the garlic.\n"
                        + "Stir-fry the vegetables for 5 minutes.\nAdd the soy sauce and cook for 1 more minute.",
                "carrot|2|piece", "bell pepper|1|piece", "onion|1|piece", "garlic|2|piece",
                "soy sauce|2|tbsp", "cooking oil|1|tbsp");

        add(list, "Stovetop Mac and Cheese",
                "Boil the pasta until tender and drain.\nMelt the butter, stir in the flour and cook for 1 minute.\n"
                        + "Whisk in the milk until thickened.\nStir in the cheese until melted and mix with the pasta.",
                "pasta|200|g", "cheese|150|g", "milk|200|ml", "butter|20|g", "flour|1|tbsp");

        add(list, "French Toast",
                "Whisk the eggs and milk with the sugar in a shallow dish.\nSoak each bread slice for 20 seconds per side.\n"
                        + "Fry in the butter until golden on both sides.",
                "bread|4|piece", "egg|2|piece", "milk|100|ml", "sugar|1|tsp", "butter|10|g");

        add(list, "Tuna Pasta",
                "Boil the pasta until tender.\nSoften the chopped onion in the olive oil.\n"
                        + "Flake in the tuna and warm through.\nToss with the pasta and season.",
                "pasta|200|g", "tuna|1|piece", "onion|1|piece", "olive oil|1|tbsp");

        add(list, "Loaded Baked Potatoes",
                "Prick the potatoes and bake at 200 C for about 1 hour.\nSplit open and fluff the insides.\n"
                        + "Mix in the butter, cheese and salt and return to the oven for 5 minutes.",
                "potato|2|piece", "cheese|60|g", "butter|20|g", "salt|0.5|tsp");

        add(list, "Simple Guacamole",
                "Mash the avocados in a bowl.\nStir in the lemon juice and salt.\n"
                        + "Fold in the diced tomato and chopped onion.\nServe immediately.",
                "avocado|2|piece", "lemon|1|piece", "tomato|1|piece", "onion|0.5|piece", "salt|0.5|tsp");

        add(list, "Spinach Egg Scramble",
                "Soften the onion in the oil.\nAdd the spinach and cook until wilted.\n"
                        + "Pour in the beaten eggs with the salt and stir until set.",
                "spinach|100|g", "egg|3|piece", "onion|1|piece", "cooking oil|1|tbsp", "salt|0.5|tsp");

        add(list, "Quick Bean Chilli",
                "Fry the onion and garlic in the oil.\nStir in the chilli powder.\n"
                        + "Add the beans and chopped tomatoes and simmer for 20 minutes.",
                "kidney bean|400|g", "tomato|3|piece", "onion|1|piece", "garlic|2|piece",
                "chili powder|1|tsp", "cooking oil|1|tbsp");

        add(list, "Yogurt Fruit Bowl",
                "Spoon the yogurt into a bowl.\nSlice the banana on top.\nSprinkle with oats and drizzle with honey.",
                "yogurt|200|g", "banana|1|piece", "honey|1|tbsp", "oat|30|g");

        return list;
    }

    private static void add(List<RecipeWithIngredients> list, String name, String steps, String... specs) {
        RecipeWithIngredients r = new RecipeWithIngredients();
        r.recipe = new Recipe(name, steps);
        r.ingredients = new ArrayList<>();
        for (String spec : specs) {
            String[] parts = spec.split("\\|");
            r.ingredients.add(new RecipeIngredient(
                    capitalise(parts[0]), Double.parseDouble(parts[1]), parts[2]));
        }
        list.add(r);
    }

    private static String capitalise(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
