package com.smartpantry.app.util;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.content.Context;
import android.content.SharedPreferences;

/** Wrapper around SharedPreferences for the Settings screen. */
public final class AppPrefs {

    public static final int EXPIRY_WARNING_DAYS = 3;

    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_ALERTS = "expiring_alerts";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    private final SharedPreferences prefs;

    public AppPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public boolean isExpiringAlertsEnabled() {
        return prefs.getBoolean(KEY_ALERTS, true);
    }

    public void setExpiringAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ALERTS, enabled).apply();
    }

    public String getDefaultUnit() {
        return prefs.getString(KEY_DEFAULT_UNIT, "g");
    }

    public void setDefaultUnit(String unit) {
        prefs.edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}
