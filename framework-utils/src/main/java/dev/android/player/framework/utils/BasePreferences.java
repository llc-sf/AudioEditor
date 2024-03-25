package dev.android.player.framework.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by robotinthesun on 13/11/2017.
 */

public abstract class BasePreferences {
    protected SharedPreferences mPreferences;

    protected BasePreferences(Context context) {
        initPreferences(context);
    }

    protected abstract void initPreferences(Context context);

    public SharedPreferences.Editor editor() {
        return mPreferences.edit();
    }

    protected int getInt(String key, int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }



    protected SharedPreferences.Editor putInt(String key, int value) {
        return editor().putInt(key, value);
    }

    protected String getString(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    protected SharedPreferences.Editor putString(String key, String value) {
        return editor().putString(key, value);
    }

    protected boolean getBoolean(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key, defaultValue);
    }

    protected SharedPreferences.Editor putBoolean(String key, boolean value) {
        return editor().putBoolean(key, value);
    }

    protected Long getLong(String key, long defaultValue) {
        return mPreferences.getLong(key, defaultValue);
    }

    protected SharedPreferences.Editor putLong(String key, long value) {
        return editor().putLong(key, value);
    }
}
