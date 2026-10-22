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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.smartpantry.app.R;
import com.smartpantry.app.data.AppDatabase;
import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.data.RecipeWithIngredients;
import com.smartpantry.app.data.StockHistory;
import com.smartpantry.app.ui.widget.LineChartView;
import com.smartpantry.app.ui.widget.ThinProgressView;
import com.smartpantry.app.util.AppPrefs;
import com.smartpantry.app.util.RecipeMatcher;
import com.smartpantry.app.util.StockHistoryCalculator;
import com.smartpantry.app.util.TextFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Dashboard (UI redesign by Micaiah Nhamburo): quick actions, inventory statistics, low-stock and expiring alert cards,
 * and the stock history chart. Everything is recalculated whenever the pantry, the recipes
 * or the stock history change (LiveData), so it is always up to date.
 */
public class DashboardFragment extends Fragment {

    private static final int HISTORY_DAYS = 14;
    private static final int MAX_CARDS = 5;

    private AppPrefs prefs;

    private List<PantryItem> pantry = new ArrayList<>();
    private List<RecipeWithIngredients> recipes = new ArrayList<>();
    private List<StockHistory> history = new ArrayList<>();
    private boolean pantryLoaded = false;
    private boolean recipesLoaded = false;
    private boolean historyLoaded = false;

    private View statTotal;
    private View statLow;
    private View statExpiring;
    private View statReady;
    private LinearLayout containerLow;
    private LinearLayout containerExpiring;
    private TextView tvLowEmpty;
    private TextView tvExpiringEmpty;
    private TextView tvExpiringSubtitle;
    private View sectionExpiring;
    private TextView tvHistorySubtitle;
    private TextView tvHistoryEmpty;
    private LineChartView chart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new AppPrefs(requireContext());

        statTotal = view.findViewById(R.id.stat_total);
        statLow = view.findViewById(R.id.stat_low);
        statExpiring = view.findViewById(R.id.stat_expiring);
        statReady = view.findViewById(R.id.stat_ready);
        containerLow = view.findViewById(R.id.container_low_stock);
        containerExpiring = view.findViewById(R.id.container_expiring);
        tvLowEmpty = view.findViewById(R.id.tv_low_empty);
        tvExpiringEmpty = view.findViewById(R.id.tv_expiring_empty);
        tvExpiringSubtitle = view.findViewById(R.id.tv_expiring_subtitle);
        sectionExpiring = view.findViewById(R.id.section_expiring);
        tvHistorySubtitle = view.findViewById(R.id.tv_history_subtitle);
        tvHistoryEmpty = view.findViewById(R.id.tv_history_empty);
        chart = view.findViewById(R.id.chart_history);

        configureStat(statTotal, R.drawable.ic_box, R.string.stat_total);
        configureStat(statLow, R.drawable.ic_warning, R.string.stat_low);
        configureStat(statExpiring, R.drawable.ic_timer, R.string.stat_expiring);
        configureStat(statReady, R.drawable.ic_recipes, R.string.stat_ready);

        // Quick actions: both use Intents / tab switches that already exist elsewhere in the app
        view.findViewById(R.id.card_quick_add).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditIngredientActivity.class)));
        view.findViewById(R.id.card_quick_recipes).setOnClickListener(v ->
                ((MainActivity) requireActivity()).selectTab(R.id.nav_recipes));

        AppDatabase db = AppDatabase.getInstance(requireContext());
        db.pantryDao().observeAll().observe(getViewLifecycleOwner(), items -> {
            pantry = items;
            pantryLoaded = true;
            render();
        });
        db.recipeDao().observeAllWithIngredients().observe(getViewLifecycleOwner(), list -> {
            recipes = list;
            recipesLoaded = true;
            render();
        });
        db.stockHistoryDao().observeAll().observe(getViewLifecycleOwner(), rows -> {
            history = rows;
            historyLoaded = true;
            render();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        render();   // the alerts setting may have changed on the Settings screen
    }

    private void configureStat(View stat, @DrawableRes int icon, int label) {
        ((ImageView) stat.findViewById(R.id.iv_stat_icon)).setImageResource(icon);
        ((TextView) stat.findViewById(R.id.tv_stat_label)).setText(label);
    }

    private void setStatValue(View stat, int value) {
        ((TextView) stat.findViewById(R.id.tv_stat_value)).setText(String.valueOf(value));
    }

    private void render() {
        if (!pantryLoaded || !recipesLoaded || !historyLoaded || getView() == null) {
            return;     // wait until all three tables have been read once
        }
        final long now = System.currentTimeMillis();
        final boolean alertsOn = prefs.isExpiringAlertsEnabled();

        // ---- work out the low-stock and expiring lists ----
        List<PantryItem> low = new ArrayList<>();
        List<PantryItem> expiring = new ArrayList<>();
        for (PantryItem item : pantry) {
            if (item.isLowStock()) {
                low.add(item);
            }
            if (item.hasExpiry()
                    && TextFormat.daysUntil(item.getExpiryDate(), now) <= AppPrefs.EXPIRY_WARNING_DAYS) {
                expiring.add(item);
            }
        }
        Collections.sort(low, new Comparator<PantryItem>() {
            @Override
            public int compare(PantryItem a, PantryItem b) {
                return Double.compare(a.getQuantity() / a.getMinQuantity(),
                        b.getQuantity() / b.getMinQuantity());      // emptiest first
            }
        });
        Collections.sort(expiring, (a, b) -> Long.compare(a.getExpiryDate(), b.getExpiryDate()));

        // ---- statistics ----
        setStatValue(statTotal, pantry.size());
        setStatValue(statLow, low.size());
        setStatValue(statExpiring, expiring.size());
        setStatValue(statReady, RecipeMatcher.strictMatches(recipes, pantry).size());

        // ---- low stock cards ----
        containerLow.removeAllViews();
        for (int i = 0; i < Math.min(MAX_CARDS, low.size()); i++) {
            containerLow.addView(buildLowCard(low.get(i)));
        }
        tvLowEmpty.setVisibility(low.isEmpty() ? View.VISIBLE : View.GONE);

        // ---- expiring cards (only when alerts are switched on) ----
        sectionExpiring.setVisibility(alertsOn ? View.VISIBLE : View.GONE);
        tvExpiringSubtitle.setText(getString(R.string.expiring_subtitle, AppPrefs.EXPIRY_WARNING_DAYS));
        containerExpiring.removeAllViews();
        for (int i = 0; i < Math.min(MAX_CARDS, expiring.size()); i++) {
            containerExpiring.addView(buildExpiringCard(expiring.get(i), now));
        }
        tvExpiringEmpty.setVisibility(expiring.isEmpty() ? View.VISIBLE : View.GONE);

        // ---- stock history chart ----
        tvHistorySubtitle.setText(getString(R.string.history_subtitle, HISTORY_DAYS));
        if (history.isEmpty()) {
            chart.setVisibility(View.GONE);
            tvHistoryEmpty.setVisibility(View.VISIBLE);
        } else {
            int[] counts = StockHistoryCalculator.itemCountsPerDay(history, HISTORY_DAYS, now);
            float[] values = new float[counts.length];
            for (int i = 0; i < counts.length; i++) {
                values[i] = counts[i];
            }
            long firstDay = StockHistoryCalculator.endOfDay(now, HISTORY_DAYS - 1);
            chart.setData(values, TextFormat.formatShortDate(firstDay), getString(R.string.chart_today));
            chart.setVisibility(View.VISIBLE);
            tvHistoryEmpty.setVisibility(View.GONE);
        }
    }

    /** Alert card for an item at or below its low-stock level. */
    private View buildLowCard(final PantryItem item) {
        View card = inflateCard(containerLow);
        ((TextView) card.findViewById(R.id.tv_card_label)).setText(R.string.label_low_stock);
        ((TextView) card.findViewById(R.id.tv_card_pill)).setText(R.string.pill_low);
        ((TextView) card.findViewById(R.id.tv_card_name)).setText(item.getName());
        ((TextView) card.findViewById(R.id.tv_card_detail)).setText(getString(R.string.card_detail_low,
                TextFormat.formatQuantity(item.getQuantity()), item.getUnit(),
                TextFormat.formatQuantity(item.getMinQuantity())));

        // Thin red bar: how much of the alert level is left
        ThinProgressView bar = card.findViewById(R.id.progress_card);
        bar.setFillColor(ContextCompat.getColor(requireContext(), R.color.pantry_red));
        bar.setFraction((float) (item.getQuantity() / item.getMinQuantity()));
        card.setOnClickListener(v -> openEdit(item));
        return card;
    }

    /** Alert card for an item that expires soon (or has already expired). */
    private View buildExpiringCard(final PantryItem item, long now) {
        View card = inflateCard(containerExpiring);
        int days = TextFormat.daysUntil(item.getExpiryDate(), now);

        String pill;
        if (days < 0) {
            pill = getString(R.string.pill_expired);
        } else if (days == 0) {
            pill = getString(R.string.pill_today);
        } else if (days == 1) {
            pill = getString(R.string.pill_one_day);
        } else {
            pill = getString(R.string.pill_days_left, days);
        }
        ((TextView) card.findViewById(R.id.tv_card_label)).setText(R.string.label_expiring);
        ((TextView) card.findViewById(R.id.tv_card_pill)).setText(pill);
        ((TextView) card.findViewById(R.id.tv_card_name)).setText(item.getName());
        ((TextView) card.findViewById(R.id.tv_card_detail)).setText(getString(R.string.card_detail_expiry,
                TextFormat.formatQuantity(item.getQuantity()), item.getUnit(),
                TextFormat.formatDate(item.getExpiryDate())));

        ThinProgressView bar = card.findViewById(R.id.progress_card);
        bar.setFillColor(ContextCompat.getColor(requireContext(), R.color.pantry_red));
        bar.setFraction(Math.max(0f, days) / (float) AppPrefs.EXPIRY_WARNING_DAYS);
        card.setOnClickListener(v -> openEdit(item));
        return card;
    }

    private View inflateCard(ViewGroup parent) {
        return LayoutInflater.from(requireContext()).inflate(R.layout.item_alert_card, parent, false);
    }

    private void openEdit(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }
}
