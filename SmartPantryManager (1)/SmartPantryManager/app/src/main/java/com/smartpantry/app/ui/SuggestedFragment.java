package com.smartpantry.app.ui;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.smartpantry.app.R;
import com.smartpantry.app.data.AppDatabase;
import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.data.RecipeWithIngredients;
import com.smartpantry.app.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen. It re-runs the strict matcher whenever the pantry or the
 * recipe table changes, so adding or removing one ingredient updates the list instantly.
 * The optional "Almost there" list is kept clearly separate from the strict list.
 */
public class SuggestedFragment extends Fragment implements RecipeAdapter.Listener {

    private RecipeAdapter adapter;
    private TextView tvEmpty;

    private List<RecipeWithIngredients> recipes = new ArrayList<>();
    private List<PantryItem> pantry = new ArrayList<>();
    private boolean recipesLoaded = false;
    private boolean pantryLoaded = false;
    private boolean showAlmost = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmpty = view.findViewById(R.id.tv_empty);
        RecyclerView recycler = view.findViewById(R.id.recycler_recipes);
        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggle_mode);

        adapter = new RecipeAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showAlmost = (checkedId == R.id.btn_almost);
                refresh();
            }
        });
        toggle.check(R.id.btn_ready);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.recipeDao().observeAllWithIngredients().observe(getViewLifecycleOwner(), list -> {
            recipes = list;
            recipesLoaded = true;
            refresh();
        });
        db.pantryDao().observeAll().observe(getViewLifecycleOwner(), items -> {
            pantry = items;
            pantryLoaded = true;
            refresh();
        });
    }

    private void refresh() {
        if (!recipesLoaded || !pantryLoaded || adapter == null) {
            return;     // wait until both tables have been read once
        }
        List<RecipeAdapter.Row> rows = new ArrayList<>();
        if (showAlmost) {
            for (RecipeMatcher.Result r : RecipeMatcher.almostThere(recipes, pantry)) {
                rows.add(new RecipeAdapter.Row(r.recipe.recipe.getId(), r.recipe.recipe.getName(),
                        getString(R.string.recipe_subtitle_almost, r.missing.get(0)),
                        (r.recipe.ingredients.size() - 1f) / r.recipe.ingredients.size()));
            }
        } else {
            for (RecipeMatcher.Result r : RecipeMatcher.strictMatches(recipes, pantry)) {
                rows.add(new RecipeAdapter.Row(r.recipe.recipe.getId(), r.recipe.recipe.getName(),
                        getString(R.string.recipe_subtitle_ready, r.recipe.ingredients.size()),
                        1f));
            }
        }
        adapter.submitList(rows);

        // Feedback instead of a blank screen when nothing matches
        tvEmpty.setText(showAlmost ? R.string.no_almost_match : R.string.no_recipes_match);
        tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRecipeClick(int recipeId) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipeId);
        startActivity(intent);
    }
}
