package com.smartpantry.app.ui;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.smartpantry.app.R;
import com.smartpantry.app.util.AppPrefs;
import com.smartpantry.app.util.UnitConverter;

import java.util.Arrays;

/** Settings screen: expiring-soon alerts toggle and the default unit for new ingredients. */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final AppPrefs prefs = new AppPrefs(requireContext());

        MaterialSwitch alertSwitch = view.findViewById(R.id.switch_alerts);
        alertSwitch.setChecked(prefs.isExpiringAlertsEnabled());
        alertSwitch.setOnCheckedChangeListener((button, checked) -> prefs.setExpiringAlertsEnabled(checked));

        Spinner unitSpinner = view.findViewById(R.id.spinner_default_unit);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, UnitConverter.UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setSelection(Arrays.asList(UnitConverter.UNITS).indexOf(prefs.getDefaultUnit()));
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                prefs.setDefaultUnit(UnitConverter.UNITS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
