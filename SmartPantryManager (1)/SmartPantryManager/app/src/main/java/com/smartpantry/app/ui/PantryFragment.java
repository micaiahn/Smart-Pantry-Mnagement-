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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.smartpantry.app.R;
import com.smartpantry.app.data.AppDatabase;
import com.smartpantry.app.data.PantryDao;
import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.data.PantryRepository;
import com.smartpantry.app.util.AppPrefs;
import com.smartpantry.app.util.TextFormat;

import java.util.ArrayList;
import java.util.List;

/** Pantry list screen: shows every ingredient and lets the user add, edit and delete. */
public class PantryFragment extends Fragment implements PantryAdapter.Listener {

    private AppDatabase db;
    private PantryDao dao;
    private AppPrefs prefs;
    private PantryAdapter adapter;
    private TextView tvEmpty;
    private TextView tvBanner;

    private List<PantryItem> currentItems = new ArrayList<>();
    private boolean loaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());
        dao = db.pantryDao();
        prefs = new AppPrefs(requireContext());

        tvEmpty = view.findViewById(R.id.tv_empty);
        tvBanner = view.findViewById(R.id.tv_expiry_banner);
        RecyclerView recycler = view.findViewById(R.id.recycler_pantry);
        FloatingActionButton fab = view.findViewById(R.id.fab_add);

        adapter = new PantryAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        // Intent: open the Add screen (no item id extra = "create new")
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditIngredientActivity.class)));

        // The LiveData re-delivers the list after every insert/update/delete.
        dao.observeAll().observe(getViewLifecycleOwner(), items -> {
            currentItems = items;
            loaded = true;
            render();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        render();   // the alerts setting may have changed on the Settings screen
    }

    private void render() {
        if (!loaded || adapter == null) {
            return;
        }
        boolean alerts = prefs.isExpiringAlertsEnabled();
        adapter.setAlertsEnabled(alerts);
        adapter.submitList(new ArrayList<>(currentItems));
        tvEmpty.setVisibility(currentItems.isEmpty() ? View.VISIBLE : View.GONE);

        int soon = 0;
        if (alerts) {
            long now = System.currentTimeMillis();
            for (PantryItem item : currentItems) {
                if (item.hasExpiry()) {
                    int days = TextFormat.daysUntil(item.getExpiryDate(), now);
                    if (days >= 0 && days <= AppPrefs.EXPIRY_WARNING_DAYS) {
                        soon++;
                    }
                }
            }
        }
        if (soon > 0) {
            tvBanner.setText(getString(R.string.expiring_banner, soon, AppPrefs.EXPIRY_WARNING_DAYS));
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            tvBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(PantryItem item) {
        // Intent with extra: the Add/Edit screen loads this id and switches to "edit" mode
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(final PantryItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    AppDatabase.EXECUTOR.execute(() -> PantryRepository.delete(db, item));
                    Snackbar.make(requireView(), getString(R.string.item_deleted, item.getName()),
                                    Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v ->
                                    AppDatabase.EXECUTOR.execute(() -> PantryRepository.insert(db, item)))
                            .show();
                })
                .show();
    }
}
