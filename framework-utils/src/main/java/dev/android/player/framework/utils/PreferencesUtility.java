/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package dev.android.player.framework.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.zjsoft.simplecache.SharedPreferenceV2;

public final class PreferencesUtility {

    public static final String HAS_RATED_US = "has_rated_us";
    public static final String DELAY_RATE_TIME = "delay_rate_time";
    public static final String LAST_CHECK_RATE_PLAY_TIME = "last_check_rate_play_time";


    private static final String TOGGLE_HEADPHONE_PAUSE = "toggle_headphone_pause";
    private static final String LANGUAGE = "Language";
    private static final String START_VERSION = "StartVersion";
    private static final String DOCUMENT_TREE_URI = "document_tree_uri";
    private static final String SUBSCRIPTION_USER = "NoAdUser";
    private static final String VIP_USER = "VIP_User";
    private static final String SPLASH_HAS_OPEN = "splash_has_open";
    private static final String INSTALL_TIME = "app_install_time";


    public static final String KEY_NEW_USER = "new_user";//判断是否是新用户


    private static final String LIBRARY_VP_INDEX = "library_vp_index";//音乐库页面的索引
    private static final String MAIN_VP_INDEX = "main_vp_index";//主页面的索引

    private static final String APP_OPEN_COUNT = "app_open_count";//app打开次数

    private static final String KEY_IS_SET_SPEED = "is_set_speed";//是否设置过速度

    private volatile static PreferencesUtility sInstance;
    private static SharedPreferences mPreferences;

    public PreferencesUtility(final Context context) {
        mPreferences = new SharedPreferenceV2(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static final PreferencesUtility getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (PreferencesUtility.class) {
                if (sInstance == null) {
                    sInstance = new PreferencesUtility(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public void registerAsSharedPreferenceChangeListener(SharedPreferences sharedPreferences) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener((SharedPreferenceV2) mPreferences);
    }

    public void unregisterAsSharedPreferenceChangeListener(SharedPreferences sharedPreferences) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener((SharedPreferenceV2) mPreferences);
    }

    public void setOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean pauseEnabledOnDetach() {
        return mPreferences.getBoolean(TOGGLE_HEADPHONE_PAUSE, true);
    }

    public int getLibraryViewPagerIndex() {
        return mPreferences.getInt(LIBRARY_VP_INDEX, 0);
    }

    public void setLibraryViewPagerIndex(final int index) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(LIBRARY_VP_INDEX, index);
        editor.apply();
    }

    public int getMainViewPagerIndex() {
        return mPreferences.getInt(MAIN_VP_INDEX, 0);
    }

    public void setMainViewPagerIndex(final int index) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(MAIN_VP_INDEX, index);
        editor.apply();
    }


    public boolean hasRatedUs() {
        return mPreferences.getBoolean(HAS_RATED_US, false);
    }

    public void setRatedUs(boolean rated) {
        mPreferences.edit().putBoolean(HAS_RATED_US, rated).commit();
    }

    public long shouldRateTime() {
        long count = delayRateTime() - 1;
        if (count == 0) {
            return 3 * 60;
        }
        long rate;
        long pow = 4;
        if (count > pow) {
            rate = (long) (Math.pow(2, pow - 1) * (count - pow + 1));
        } else {
            rate = (long) Math.pow(2, count - 1);
        }
        return 30 * 60 * rate;
    }

    private long delayRateTime() {
        long count = mPreferences.getLong(DELAY_RATE_TIME, 1);
        if (count < 1) {
            count = 1;
        }
        return count;
    }

    public void setDelayRateUs() {
        mPreferences.edit().putLong(DELAY_RATE_TIME, delayRateTime() + 1).commit();
    }

    public long getLastCheckRatePlayTime() {
        return mPreferences.getLong(LAST_CHECK_RATE_PLAY_TIME, 0);
    }

    public void setLastCheckRatePlayTime(long time) {
        mPreferences.edit().putLong(LAST_CHECK_RATE_PLAY_TIME, time).commit();
    }


    public SharedPreferences getPreference() {
        return mPreferences;
    }

    public long getAppInstallTime() {
        return mPreferences.getLong(INSTALL_TIME, 0);
    }

    public void setAppInstallTime(long time) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(INSTALL_TIME, time);
        editor.apply();
    }

    /**
     * 判断是否是新用户 在MainActivity 之后的页面调用
     *
     * @return
     */
    public boolean isNewUser() {
        return mPreferences.getBoolean(KEY_NEW_USER, true);
    }

    /**
     * 在MainActivity 之前的页面调用，判断是否是新用户
     * 判断逻辑：如果包含KEY_NEW_USER，说明是老用户，否则是新用户
     *
     * @return
     */
    public boolean isNewUserBefore() {
        return !mPreferences.contains(KEY_NEW_USER);
    }

    /**
     * 是否是升级用户
     * 升级到最新
     *
     * @param nowVersion
     * @return
     */
    public boolean isUpdateVersionUser(int nowVersion) {
        return nowVersion > getStartVersion();
    }

    public void setNewUser(boolean isNewUser) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(KEY_NEW_USER, isNewUser);
        editor.apply();
    }

//
//    /**
//     * 获取用户当前使用语言的索引
//     */
//    public int getLanguageIndex() {
//        return mPreferences.getInt(LANGUAGE, -1);
//    }
//
//    /**
//     * 设置当前语言的索引
//     */
//    public void setLanguageIndex(int index) {
//        mPreferences.edit().putInt(LANGUAGE, index).apply();
//    }


    public int getStartVersion() {
        return mPreferences.getInt(START_VERSION, 0);
    }

    public void setStartVersion(int startVersion) {
        mPreferences.edit().putInt(START_VERSION, startVersion).apply();
    }


    public String getDocumentTreeUri() {
        return mPreferences.getString(DOCUMENT_TREE_URI, null);
    }

    public void setDocumentTreeUri(String documentTreeUri) {
        mPreferences.edit().putString(DOCUMENT_TREE_URI, documentTreeUri).apply();
    }

    public boolean isNoAdUser() {
        return isSubscriptionUser() || isVIPUser();
    }

    public boolean isSubscriptionUser() {
        return mPreferences.getBoolean(SUBSCRIPTION_USER, false);
    }

    public void setSubscriptionUser(boolean value) {
        mPreferences.edit().putBoolean(SUBSCRIPTION_USER, value).apply();
    }

    public boolean isVIPUser() {
        return mPreferences.getBoolean(VIP_USER, false);
    }

    public void setVIPUser(boolean value) {
        mPreferences.edit().putBoolean(VIP_USER, value).apply();
    }

//    /**
//     * 过时方法 请参考 MediaStoreFilterConfig
//     *
//     * @param value
//     */
//    @Deprecated
//    public void setIgnoreShortFile(boolean value) {
//        mPreferences.edit().putBoolean(IGNORE_SHORT_FILE, value).apply();
//    }
//
//    /**
//     * 过时方法 请参考 MediaStoreFilterConfig
//     *
//     * @return
//     */
//    @Deprecated
//    public boolean ignoreShortFile() {
//        return mPreferences.getBoolean(IGNORE_SHORT_FILE, false);
//    }
//
//    /**
//     * 过时方法 请参考 MediaStoreFilterConfig
//     *
//     * @param value
//     */
//    @Deprecated
//    public void setIgnoreShortSong(boolean value) {
//        mPreferences.edit().putBoolean(IGNORE_SHORT_SONG, value).apply();
//    }
//
//    /**
//     * 过时方法 请参考 MediaStoreFilterConfig
//     *
//     * @return
//     */
//    @Deprecated
//    public boolean ignoreShortSong() {
//        return mPreferences.getBoolean(IGNORE_SHORT_SONG, false);
//    }

    public boolean hasOpenSplashPage() {
        return mPreferences.getBoolean(SPLASH_HAS_OPEN, false);
    }


    /**
     * 判断是否设置了指定Key的值
     *
     * @param key
     * @return
     */
    public boolean hasValue(String key) {
        return mPreferences.contains(key);
    }


    public void clear(String key) {
        mPreferences.edit().remove(key).apply();
    }

    /**
     * 隐私权限弹窗展示说明
     */
    public static final String IS_TERMS_SERVICE_SHOWED = "is_terms_service_showed";//隐私弹窗是否展示过

    public boolean isTermsServiceShowed() {
        return mPreferences.getBoolean(IS_TERMS_SERVICE_SHOWED, false);
    }

    public void setIsTermsServiceShowed(boolean isShowed) {
        mPreferences.edit()
                .putBoolean(IS_TERMS_SERVICE_SHOWED, isShowed)
                .apply();
    }

    /**
     * 获取 App 打开次数
     *
     * @return
     */
    public long getAppOpenCount() {
        if (hasValue(APP_OPEN_COUNT) || getStartVersion() == 0) {//获取安装时的版本,如果是0，说明是新用户，否则是老用户
            return mPreferences.getLong(APP_OPEN_COUNT, 1);
        } else {//以前版本没记录打开次数，所以默认值设置大一点，避免影响老用户
            return mPreferences.getLong(APP_OPEN_COUNT, 5);
        }
    }

    /**
     * 更新 App 打开次数
     */
    public void updateAppOpenCount() {
        long count = getAppOpenCount();
        mPreferences.edit()
                .putLong(APP_OPEN_COUNT, count + 1)
                .apply();
    }

    /**
     * 设置过播放速度
     */
    public void setSpeed() {
        mPreferences.edit()
                .putBoolean(KEY_IS_SET_SPEED, true)
                .apply();
    }

    /**
     * 判断是否设置过播放速度
     */
    public boolean hasSetSpeed() {
        return mPreferences.getBoolean(KEY_IS_SET_SPEED, false);
    }
}

