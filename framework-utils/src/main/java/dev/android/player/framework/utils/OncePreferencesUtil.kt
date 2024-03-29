package dev.android.player.framework.utils

import android.content.Context
import android.content.SharedPreferences
import com.android.app.AppProvider
import com.zjsoft.simplecache.SharedPreferenceV2

object OncePreferencesUtil {

    const val key = "once_preferences"


    var mPreferences: SharedPreferences = SharedPreferenceV2(AppProvider.context.getSharedPreferences(key, Context.MODE_PRIVATE))


    fun get(key: String): Boolean {
        return mPreferences.getBoolean(key, false)
    }

    fun set(key: String) {
        mPreferences.edit().putBoolean(key, true).apply()
    }

    fun clear(key: String) {
        mPreferences.edit().putBoolean(key, false).apply()
    }

}