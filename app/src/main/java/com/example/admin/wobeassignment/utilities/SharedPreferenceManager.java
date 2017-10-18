package com.example.admin.wobeassignment.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Admin on 19-09-2017.
 */

public class SharedPreferenceManager {
    private static SharedPreferences mPreference;
    public static SharedPreferenceManager preferenceManager;
    private static SharedPreferences.Editor editor;
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    private SharedPreferenceManager() {

    }

    private static synchronized void initPreference(Context context) {
        if (mPreference == null) {
            mPreference = context.getSharedPreferences("wobe-preferences", Context.MODE_PRIVATE);
            editor = mPreference.edit();
        }
    }

    public static synchronized SharedPreferenceManager getInstance(Context context) {
        if (preferenceManager == null) {
            preferenceManager = new SharedPreferenceManager();
        }
        initPreference(context);
        return preferenceManager;
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return mPreference.getBoolean(IS_FIRST_TIME_LAUNCH, false);
    }

    public void saveData(String key, String data) {
        mPreference.edit().putString(key, data).apply();
    }

    public void removeData(String key) {
        mPreference.edit().remove(key).apply();
    }

    public void saveData(String key, int data) {
        mPreference.edit().putInt(key, data).apply();
    }

    public void saveData(String key, long data) {
        mPreference.edit().putLong(key, data).apply();
    }

    public void saveData(String key, boolean data) {
        mPreference.edit().putBoolean(key, data).apply();
    }

    public String getString(String key) {
        return mPreference.getString(key, null);
    }

    public int getInt(String key) {
        return mPreference.getInt(key, 0);
    }

    public long getLong(String key) {
        return mPreference.getLong(key, 0);
    }

    public boolean getBoolean(String key) {
        return mPreference.getBoolean(key, false);
    }

    public boolean contains(String key) {
        return mPreference.contains(key);
    }

    public void clearData() {
        mPreference.edit().clear().apply();
    }
}

