package com.smartpantry.app.ui;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah T Nhamburo
 */

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartpantry.app.R;
import com.smartpantry.app.data.AppDatabase;

/**
 * Host Activity. It owns the header (title + settings button) and the bottom navigation bar
 * and swaps three Fragments (Dashboard, Pantry, Suggested Recipes) into the container.
 * Add/Edit Ingredient, Recipe Detail and Settings are separate Activities opened with Intents.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTitle = findViewById(R.id.tv_title);
        findViewById(R.id.btn_settings).setOnClickListener(
                v -> startActivity(new Intent(this, SettingsActivity.class)));

        // First run: pre-load the recipe collection. Does nothing once recipes exist.
        AppDatabase.seedIfEmpty(this);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            showScreen(item.getItemId());
             return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);   // triggers the listener above
        } else {
            updateTitle(bottomNav.getSelectedItemId());        // Fragment is restored by the system
        }
    }

    /** Lets a Fragment (e.g. the dashboard quick action) switch tabs. */
    public void selectTab(int menuItemId) {
        bottomNav.setSelectedItemId(menuItemId);
    }

    private void showScreen(int menuItemId) {
        Fragment fragment;
        if (menuItemId == R.id.nav_recipes) {
            fragment = new SuggestedFragment();
        } else if (menuItemId == R.id.nav_pantry) {
            fragment = new PantryFragment();
        } else {
            fragment = new DashboardFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        updateTitle(menuItemId);
    }

    private void updateTitle(int menuItemId) {
        int title;
        if (menuItemId == R.id.nav_recipes) {
            title = R.string.title_recipes;
        } else if (menuItemId == R.id.nav_pantry) {
            title = R.string.title_pantry;
        } else {
            title = R.string.title_dashboard;
        }
        tvTitle.setText(title);
    }
}
