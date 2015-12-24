package com.shenghua.battery;

import android.content.SharedPreferences;

/**
 * Created by shenghua on 12/4/15.
 */
public class PrefsStorageDelegate {

    public static final String PREFS_NAME = "BatteryLogData";
    private static SharedPreferences prefs;

    private static final String EMAIL = "Email";
    private static final String PASSWORD = "Password";

    private static final String CHARGE_TYPE = "ChargeType";
    private static final String PLUGIN_POWER = "PluginPower";
    private static final String PLUGIN_TIME = "PluginTime";
    private static final String CHARGE_FULL_TIME = "ChargeFullTime";

    private static final String LANGUAGE_INDEX = "LanguageIndex";

    public static boolean initialized() {
        return prefs != null;
    }

    public static void initialize(SharedPreferences p) {
        if (prefs == null) prefs = p;
    }

    public static String getEmail() {
        return prefs.getString(EMAIL, "");
    }

    public static void setEmail(String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(EMAIL, email);
        editor.commit();
    }

    public static String getPassword() {
        return prefs.getString(PASSWORD, "");
    }

    public static void setPassword(String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PASSWORD, password);
        editor.commit();
    }

    public static boolean getBooleanValue(String key) {
        return prefs.getBoolean(key, false);
    }

    public static void setBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static String getStringValue(String key) {
        return prefs.getString(key, "");
    }

    public static void setStringValue(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static int getChargeType() {
        return prefs.getInt(CHARGE_TYPE, BatteryLogService.BATTERY_UNDEFINED_CHARGESTATUS);
    }

    public static void setChargeType(int type) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(CHARGE_TYPE, type);
        editor.commit();
    }

    public static int getPluginPower() {
        return prefs.getInt(PLUGIN_POWER, BatteryLogService.BATTERY_POWER_UNKNOWN);
    }

    public static void setPluginPower(int power) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PLUGIN_POWER, power);
        editor.commit();
    }

    public static long getPluginTime() {
        return prefs.getLong(PLUGIN_TIME, BatteryLogService.BATTERY_TIME_UNDEFINED);
    }

    public static void setPluginTime(long time) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PLUGIN_TIME, time);
        editor.commit();
    }

    public static long getChargeFullTime() {
        return prefs.getLong(CHARGE_FULL_TIME, BatteryLogService.BATTERY_TIME_UNDEFINED);
    }

    public static void setChargeFullTime(long time) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(CHARGE_FULL_TIME, time);
        editor.commit();
    }

    public static int getLanguageIndex() {
        return prefs.getInt(LANGUAGE_INDEX, 0);
    }

    public static void setLanguageIndex(int languageIndex) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LANGUAGE_INDEX, languageIndex);
        editor.commit();
    }
}
