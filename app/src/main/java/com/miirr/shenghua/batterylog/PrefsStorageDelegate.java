package com.miirr.shenghua.batterylog;

import android.content.SharedPreferences;

/**
 * Created by shenghua on 12/4/15.
 */
public class PrefsStorageDelegate {

    public static final String PREFS_NAME = "BatteryLogData";
    private static SharedPreferences prefs;

    private static final String USER_NAME = "UserName";
    private static final String PASSWORD = "Password";

    private static final String PLUGIN_POWER = "PluginPower";
    private static final String PLUGIN_TIME = "PluginTime";

    private static final String LANGUAGE_INDEX = "LanguageIndex";

    public static boolean initialized() {
        return prefs != null;
    }

    public static void initialize(SharedPreferences p) {
        if (prefs == null) prefs = p;
    }

    public static String getUsername() {
        return prefs.getString(USER_NAME, "");
    }

    public static void setUsername(String username) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_NAME, username);
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

    public static String getStringValue(String key) {
        return prefs.getString(key, "");
    }

    public static void setStringValue(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static int getPluginPower() {
        return prefs.getInt(PLUGIN_POWER, -1);
    }

    public static long getPluginTime() {
        return prefs.getLong(PLUGIN_TIME, 0);
    }

    public static void setPluginPower(int power) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PLUGIN_POWER, power);
        editor.commit();
    }

    public static void setPluginTime(long time) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PLUGIN_TIME, time);
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
