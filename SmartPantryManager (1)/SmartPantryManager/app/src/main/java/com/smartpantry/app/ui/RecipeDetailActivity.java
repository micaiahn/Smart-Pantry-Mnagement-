package com.smartpantry.app.ui;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartpantry.app.R;
import com.smartpantry.app.data.AppDatabase;
import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.data.RecipeIngredient;
import com.smartpantry.app.data.RecipeWithIngredients;
import com.smartpantry.app.util.RecipeMatcher;
import com.smartpantry.app.util.TextFormat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shows the full ingredient list and method for one recipe (id arrives through the Intent). */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private TextView tvTitle;
    private TextView tvIngredients;
    private TextView tvSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_recipe_detail);
        }

        tvTitle = findViewById(R.id.tv_detail_title);
        tvIngredients = findViewById(R.id.tv_detail_ingredients);
        tvSteps = findViewById(R.id.tv_detail_steps);

        final int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        final AppDatabase db = AppDatabase.getInstance(this);

        // Database reads happen off the main thread, then we hop back to update the UI.
        AppDatabase.EXECUTOR.execute(() -> {
            final RecipeWithIngredients recipe = db.recipeDao().getById(recipeId);
            final List<PantryItem> pantry = db.pantryDao().getAllNow();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                bind(recipe, pantry);
            });
        });
    }

    private void bind(RecipeWithIngredients recipe, List<PantryItem> pantry) {
        if (recipe == null || recipe.recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvTitle.setText(recipe.recipe.getName());

        // Tick (in pantry) or cross (missing / not enough) for each ingredient
        Set<String> missing = new HashSet<>(
                RecipeMatcher.findMissing(recipe, RecipeMatcher.buildPantryTotals(pantry)));
        int green = ContextCompat.getColor(this, R.color.pantry_green);
        int red = ContextCompat.getColor(this, R.color.pantry_red);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (RecipeIngredient ing : recipe.ingredients) {
            boolean isMissing = missing.contains(ing.getName());
            String line = (isMissing ? "\u2717 " : "\u2713 ") + ing.getName() + " - "
                    + TextFormat.formatQuantity(ing.getQuantity()) + " " + ing.getUnit() + "\n";
            int start = builder.length();
            builder.append(line);
            builder.setSpan(new ForegroundColorSpan(isMissing ? red : green),
                    start, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvIngredients.setText(builder);

        // Method: number each line
        String[] steps = recipe.recipe.getSteps().split("\n");
        StringBuilder method = new StringBuilder();
        for (int i = 0; i < steps.length; i++) {
            method.append(i + 1).append(". ").append(steps[i].trim()).append("\n\n");
        }
        tvSteps.setText(method.toString().trim());
        tvSteps.setTextColor(Color.parseColor("#1F2A1F"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
