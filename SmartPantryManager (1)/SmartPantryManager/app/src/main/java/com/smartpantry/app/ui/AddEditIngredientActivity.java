package com.smartpantry.app.ui;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartpantry.app.R;
import com.smartpantry.app.data.AppDatabase;
import com.smartpantry.app.data.PantryDao;
import com.smartpantry.app.data.PantryItem;
import com.smartpantry.app.data.PantryRepository;
import com.smartpantry.app.data.StockHistory;
import com.smartpantry.app.ui.widget.LineChartView;
import com.smartpantry.app.util.AppPrefs;
import com.smartpantry.app.util.IngredientNormalizer;
import com.smartpantry.app.util.TextFormat;
import com.smartpantry.app.util.UnitConverter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Add / Edit Ingredient screen (one form used for both).
 * If the launching Intent carries EXTRA_ITEM_ID the form edits that row; otherwise it creates one.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private static final String STATE_EXPIRY = "state_expiry";
    private static final String NAME_PATTERN = "^[A-Za-z][A-Za-z '\u2019-]*$";
    private static final double MAX_QUANTITY = 100000;

    private TextInputLayout tilName;
    private TextInputLayout tilQuantity;
    private TextInputLayout tilMinQuantity;
    private TextInputLayout tilExpiry;
    private TextInputEditText etName;
    private TextInputEditText etQuantity;
    private TextInputEditText etMinQuantity;
    private TextInputEditText etExpiry;
    private View cardItemHistory;
    private LineChartView chartItem;
    private Spinner spinnerUnit;
    private Button btnSave;

    private AppDatabase db;
    private PantryDao dao;
    private int itemId = -1;
    private long expiryMillis = 0;      // 0 = no expiry date
    private final List<String> units = Arrays.asList(UnitConverter.UNITS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = AppDatabase.getInstance(this);
        dao = db.pantryDao();
        itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(itemId == -1
                    ? R.string.title_add_ingredient : R.string.title_edit_ingredient);
        }

        tilName = findViewById(R.id.til_name);
        tilQuantity = findViewById(R.id.til_quantity);
        tilMinQuantity = findViewById(R.id.til_min_quantity);
        tilExpiry = findViewById(R.id.til_expiry);
        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etMinQuantity = findViewById(R.id.et_min_quantity);
        etExpiry = findViewById(R.id.et_expiry);
        cardItemHistory = findViewById(R.id.card_item_history);
        chartItem = findViewById(R.id.chart_item);
        spinnerUnit = findViewById(R.id.spinner_unit);
        btnSave = findViewById(R.id.btn_save);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, UnitConverter.UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        etExpiry.setOnClickListener(v -> showDatePicker());
        tilExpiry.setEndIconOnClickListener(v -> setExpiry(0));
        btnSave.setOnClickListener(v -> onSaveClicked());

        if (savedInstanceState != null) {
            // The system restores the text fields itself; we only restore our own variable.
            setExpiry(savedInstanceState.getLong(STATE_EXPIRY, 0));
        } else if (itemId == -1) {
            spinnerUnit.setSelection(Math.max(0, units.indexOf(new AppPrefs(this).getDefaultUnit())));
        } else {
            loadExistingItem();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_EXPIRY, expiryMillis);
    }

    /** Edit mode: read the row on a background thread, then fill the form. */
    private void loadExistingItem() {
        AppDatabase.EXECUTOR.execute(() -> {
            final PantryItem item = dao.getById(itemId);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (item == null) {
                    Toast.makeText(this, R.string.error_item_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                etName.setText(item.getName());
                etQuantity.setText(TextFormat.formatQuantity(item.getQuantity()));
                spinnerUnit.setSelection(Math.max(0, units.indexOf(item.getUnit())));
                if (item.getMinQuantity() > 0) {
                    etMinQuantity.setText(TextFormat.formatQuantity(item.getMinQuantity()));
                }
                setExpiry(item.getExpiryDate());
            });
        });
        loadItemHistory();
    }

    /** Edit mode: draw this ingredient's quantity over time. */
    private void loadItemHistory() {
        AppDatabase.EXECUTOR.execute(() -> {
            final List<StockHistory> rows = db.stockHistoryDao().getForItem(itemId);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || rows.isEmpty()) {
                    return;
                }
                float[] values = new float[rows.size()];
                for (int i = 0; i < rows.size(); i++) {
                    values[i] = (float) rows.get(i).getQuantity();
                }
                chartItem.setData(values,
                        TextFormat.formatShortDate(rows.get(0).getRecordedAt()),
                        TextFormat.formatShortDate(rows.get(rows.size() - 1).getRecordedAt()));
                cardItemHistory.setVisibility(View.VISIBLE);
            });
        });
    }

    private void setExpiry(long millis) {
        expiryMillis = millis;
        etExpiry.setText(millis > 0 ? TextFormat.formatDate(millis) : "");
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        if (expiryMillis > 0) {
            c.setTimeInMillis(expiryMillis);
        }
        new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, day, 0, 0, 0);
            chosen.set(Calendar.MILLISECOND, 0);
            setExpiry(chosen.getTimeInMillis());
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void onSaveClicked() {
        // ---- Input validation (Micaiah Nhamburo): every problem is shown on the field it belongs to ----
        tilName.setError(null);
        tilQuantity.setError(null);
        tilMinQuantity.setError(null);
        boolean valid = true;

        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_required));
            valid = false;
        } else if (name.length() < 2 || name.length() > 40) {
            tilName.setError(getString(R.string.error_name_length));
            valid = false;
        } else if (!name.matches(NAME_PATTERN) || IngredientNormalizer.normalize(name).isEmpty()) {
            tilName.setError(getString(R.string.error_name_invalid));
            valid = false;
        }

        String qtyText = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();
        double quantity = 0;
        if (qtyText.isEmpty()) {
            tilQuantity.setError(getString(R.string.error_quantity_required));
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText.replace(',', '.'));
                if (Double.isNaN(quantity) || quantity <= 0 || quantity > MAX_QUANTITY) {
                    tilQuantity.setError(getString(R.string.error_quantity_range));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError(getString(R.string.error_quantity_invalid));
                valid = false;
            }
        }

        // Optional low-stock alert level: empty means "no alert", otherwise 0..MAX
        String minText = etMinQuantity.getText() == null ? "" : etMinQuantity.getText().toString().trim();
        double minQuantity = 0;
        if (!minText.isEmpty()) {
            try {
                minQuantity = Double.parseDouble(minText.replace(',', '.'));
                if (Double.isNaN(minQuantity) || minQuantity < 0 || minQuantity > MAX_QUANTITY) {
                    tilMinQuantity.setError(getString(R.string.error_min_invalid));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                tilMinQuantity.setError(getString(R.string.error_min_invalid));
                valid = false;
            }
        }
        if (!valid) {
            return;
        }

        // ---- Save (Create or Update) on the background thread, then close the screen ----
        btnSave.setEnabled(false);
        final String unit = UnitConverter.UNITS[spinnerUnit.getSelectedItemPosition()];
        final PantryItem item = new PantryItem(name, quantity, unit, expiryMillis);
        item.setMinQuantity(minQuantity);
        AppDatabase.EXECUTOR.execute(() -> {
            if (itemId == -1) {
                PantryRepository.insert(db, item);
            } else {
                item.setId(itemId);
                PantryRepository.update(db, item);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
