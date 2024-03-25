package com.android.language;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 多语言切换工具
 */
public class GlobalLanguages {

    private static final List<LanguageItem> LANGUAGES = new ArrayList<>();

    private static final Map<String, Locale> LOCALS = new LinkedHashMap<>();


    public static String DEFAULT_SYSTEM = "System";//这个标记不要乱改
    public static String LANGUAGE_US = "us";
    public static String LANGUAGE_ES = "es";
    public static String LANGUAGE_IN = "in";
    public static String LANGUAGE_PT = "pt";
    public static String LANGUAGE_RU = "ru";
    public static String LANGUAGE_TR = "tr";
    public static String LANGUAGE_DE = "de";
    public static String LANGUAGE_FR = "fr";
    public static String LANGUAGE_IT = "it";
    public static String LANGUAGE_JA = "ja";
    public static String LANGUAGE_PL = "pl";
    public static String LANGUAGE_AR = "ar";
    public static String LANGUAGE_CS = "cs";
    public static String LANGUAGE_TH = "th";
    public static String LANGUAGE_KO = "ko";
    public static String LANGUAGE_CN = "cn";
    public static String LANGUAGE_TW = "tw";
    public static String LANGUAGE_HI = "hi";
    public static String LANGUAGE_VI = "vi";
    public static String LANGUAGE_MS = "ms";
    public static String LANGUAGE_FA = "fa";
    public static String LANGUAGE_UK = "uk";
    public static String LANGUAGE_SV = "sv";
    public static String LANGUAGE_BN = "bn";


    static {
        LANGUAGES.add(new LanguageItem(DEFAULT_SYSTEM, DEFAULT_SYSTEM, getSystemLocal()));//系统默认语言
        LANGUAGES.add(new LanguageItem(LANGUAGE_US, "English", Locale.ENGLISH)); //英语-en
        LANGUAGES.add(new LanguageItem(LANGUAGE_ES, "Español", new Locale(LANGUAGE_ES)));//西班牙-es
        LANGUAGES.add(new LanguageItem(LANGUAGE_IN, "Bahasa Indonesia", new Locale(LANGUAGE_IN)));//印尼-in
        LANGUAGES.add(new LanguageItem(LANGUAGE_PT, "Português do Brasil", new Locale(LANGUAGE_PT)));//葡萄牙语(西班牙)-pt
        LANGUAGES.add(new LanguageItem(LANGUAGE_RU, "Русский", new Locale(LANGUAGE_RU)));//俄语ru
        LANGUAGES.add(new LanguageItem(LANGUAGE_TR, "Türkçe", new Locale(LANGUAGE_TR)));//土耳其-tr
        LANGUAGES.add(new LanguageItem(LANGUAGE_DE, "Deutsch", new Locale(LANGUAGE_DE)));//德语-de
        LANGUAGES.add(new LanguageItem(LANGUAGE_FR, "Français", new Locale(LANGUAGE_FR)));//法语-fr
        LANGUAGES.add(new LanguageItem(LANGUAGE_IT, "Italiano", new Locale(LANGUAGE_IT)));//意大利语-it
        LANGUAGES.add(new LanguageItem(LANGUAGE_JA, "日本語", new Locale(LANGUAGE_JA)));//日语-ja
        LANGUAGES.add(new LanguageItem(LANGUAGE_PL, "Polski", new Locale(LANGUAGE_PL)));//波兰语-pl
        LANGUAGES.add(new LanguageItem(LANGUAGE_AR, "العربية", new Locale(LANGUAGE_AR)));//阿拉伯语-ar
        LANGUAGES.add(new LanguageItem(LANGUAGE_CS, "čeština", new Locale(LANGUAGE_CS)));//捷克语-cs
        LANGUAGES.add(new LanguageItem(LANGUAGE_TH, "ไทย", new Locale(LANGUAGE_TH)));//泰语-th
        LANGUAGES.add(new LanguageItem(LANGUAGE_KO, "한국어", new Locale(LANGUAGE_KO)));//韩语-ko
        LANGUAGES.add(new LanguageItem(LANGUAGE_CN, "简体中文", Locale.SIMPLIFIED_CHINESE));//简体中文-zh-CN
        LANGUAGES.add(new LanguageItem(LANGUAGE_TW, "繁體中文", Locale.TRADITIONAL_CHINESE));//繁体中文-zh-TW
        LANGUAGES.add(new LanguageItem(LANGUAGE_HI, "हिन्दी", new Locale(LANGUAGE_HI)));//印地语-hi
        LANGUAGES.add(new LanguageItem(LANGUAGE_VI, "Tiếng Việt", new Locale(LANGUAGE_VI)));//越南语-vi
        LANGUAGES.add(new LanguageItem(LANGUAGE_MS, "Bahasa Melayu", new Locale(LANGUAGE_MS)));//马来西亚语-ms
        LANGUAGES.add(new LanguageItem(LANGUAGE_FA, "فارسی", new Locale(LANGUAGE_FA)));//波斯语-fa
        LANGUAGES.add(new LanguageItem(LANGUAGE_UK, "українська", new Locale(LANGUAGE_UK)));//乌克兰语-uk
        LANGUAGES.add(new LanguageItem(LANGUAGE_SV, "Svenska", new Locale(LANGUAGE_SV)));//瑞典语-sv
        LANGUAGES.add(new LanguageItem(LANGUAGE_BN, "বাংলা", new Locale(LANGUAGE_BN)));//孟加拉语-bn

        for (LanguageItem item : LANGUAGES) {
            LOCALS.put(item.name, item.locale);
        }

    }


    private static boolean isFirst = true;

    public static String CurrentLanguage = DEFAULT_SYSTEM;

    private static SharedPreferences mSharedPreferences;

    private static final String SP_NAME = "app_language";//sp名称、
    private static final String SP_KEY_LANGUAGE = "language";//sp key

    /**
     * 设置当前语言
     *
     * @param context
     * @param key
     */
    public static synchronized void setLanguageLocal(Context context, String key) {
        CurrentLanguage = key;
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        mSharedPreferences.edit().putString(SP_KEY_LANGUAGE, key).apply();
    }

    public static void setLanguageLocal(Context context, int position) {
        setLanguageLocal(context, LANGUAGES.get(position).name);
    }

    /**
     * @param context
     * @return
     */
    public static Locale getLanguageLocal(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        String key = mSharedPreferences.getString(SP_KEY_LANGUAGE, DEFAULT_SYSTEM);
        return getLocale(key);
    }

    private static String getCurrentLanguage(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return mSharedPreferences.getString(SP_KEY_LANGUAGE, DEFAULT_SYSTEM);
    }


    /**
     * 获取当前语言 索引
     *
     * @return
     */
    public static int getLanguageIndex() {
        for (int i = LANGUAGES.size() - 1; i >= 0; i--) {
            if (LANGUAGES.get(i).name.equals(CurrentLanguage)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 获取语言名称列表
     *
     * @return
     */
    @Deprecated
    public static String[] getLangNameList() {
        List<String> result = new ArrayList<>();
        for (LanguageItem language : LANGUAGES) {
            result.add(language.name);
        }
        return result.toArray(new String[]{});
    }

    /**
     * 是否是右对齐语言
     *
     * @return
     */
    public static boolean isRtlLanguage(Context context) {
        try {
            Locale locale = context.getResources().getConfiguration().locale;
            String language = locale.getLanguage().toLowerCase();
            if (language.equals("ar") || language.equals("iw") || language.equals("fa") || language.equals("ur")) {
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<LanguageItem> getLanguages() {
        return LANGUAGES;
    }


    /**
     * 根据名称获取Local
     *
     * @param name getLangNameList 返回的名称
     * @return
     */
    public static Locale getLocale(String name) {
        if (TextUtils.equals(name, DEFAULT_SYSTEM)) {
            return getSystemLocal();
        } else if (LOCALS.containsKey(name)) {
            return LOCALS.get(name);
        } else {
            return Locale.getDefault();
        }
    }

    /**
     * 获取系统语言国家
     *
     * @return
     */
    private static Locale getSystemLocal() {
        Locale locale = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = Resources.getSystem().getConfiguration().getLocales().get(0);
            } else {
                locale = Resources.getSystem().getConfiguration().locale;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        //如果应用程序不支持，则返回英文国家
        if (isAppSupportLocal(locale)) {
            return locale;
        } else {
            return Locale.ENGLISH;
        }
    }

    /**
     * 判断应用程序是否支持该语言
     *
     * @param locale
     * @return
     */
    private static boolean isAppSupportLocal(Locale locale) {
        return LANGUAGES.stream().filter(item -> !TextUtils.equals(DEFAULT_SYSTEM, item.name)).anyMatch(item -> TextUtils.equals(item.locale.getLanguage(), locale.getLanguage()));
    }


    /**
     * 根据位置获取local
     *
     * @param index LANGUAGES 中的顺序
     * @return
     */
    public static Locale getLocale(int index) {
        if (index < 0 || index > LANGUAGES.size()) {
            return Locale.getDefault();
        } else {
            return LANGUAGES.get(index).locale;
        }
    }

    public static Context getLocaleContext(Context base) {
        //第一次进入时，读取Sp配置。
        if (isFirst) {
            CurrentLanguage = getCurrentLanguage(base);
            isFirst = false;
        }
        return getLocaleContext(base, CurrentLanguage);
    }

    public static Context getLocaleContext(Context base, String name) {
        return getLocaleContext(base, getLocale(name));
    }

    @Deprecated
    public static Context getLocaleContext(Context base, int index) {
        return getLocaleContext(base, getLocale(index));
    }

    public static Context getLocaleContext(Context base, Locale locale) {
        try {
            Configuration configuration = base.getResources().getConfiguration();
            DisplayMetrics dm = base.getResources().getDisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleList list = new LocaleList(locale);
                configuration.setLocales(list);
                LocaleList.setDefault(list);
                base = base.createConfigurationContext(configuration);
            } else {
                configuration.setLocale(locale);
                configuration.locale = locale;
            }
            Locale.setDefault(locale);
            base.getResources().updateConfiguration(configuration, dm);
            Log.e("GlobalLanguages", "changeLanguage success " + base.getResources().getConfiguration().locale.getLanguage());
        } catch (Exception e) {
            Log.e("GlobalLanguages", "changeLanguage", e);
        }
        return base;
    }

    public static void onConfigurationChanged(Context context) {
        try {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            Configuration configuration = context.getResources().getConfiguration();
            Locale locale = getLanguageLocal(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleList list = new LocaleList(locale);
                configuration.setLocales(list);
                LocaleList.setDefault(list);
            } else {
                configuration.setLocale(locale);
                configuration.locale = locale;
            }
            Locale.setDefault(locale);
            context.getResources().updateConfiguration(configuration, dm);
            Log.d("GlobalLanguages", "onConfigurationChanged success " + context.getResources().getConfiguration().locale.getLanguage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("GlobalLanguages", "onConfigurationChanged: ", e);
        }
    }


    public static boolean isDefault() {
        return CurrentLanguage.equals(DEFAULT_SYSTEM);
    }


    public static class LanguageItem {

        public final String key;
        public final Locale locale;
        public final String name;

        public LanguageItem(String key, String name, Locale locale) {
            this.key = key;
            this.name = name;
            this.locale = locale;
        }
    }



    public static String getLanguage(Context context, int id, String code) {
        Configuration newConfig = new Configuration(context.getResources().getConfiguration());
        newConfig.setLocale(new Locale(code));
        Resources koResources = context.createConfigurationContext(newConfig).getResources();
        String welcomeMessage = koResources.getString(id);
        return welcomeMessage;
    }

    public static boolean isRtlLanguage(String languageCode) {

        try {
            languageCode.toLowerCase();
            if (languageCode.equals("ar") || languageCode.equals("iw") || languageCode.equals("fa") || languageCode.equals("ur")) {
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getLanguage(Context context, int id, String code1, String code2) {
        Configuration newConfig = new Configuration(context.getResources().getConfiguration());
        newConfig.setLocale(new Locale(code1, code2));
        Resources koResources = context.createConfigurationContext(newConfig).getResources();
        String welcomeMessage = koResources.getString(id);
        return welcomeMessage;
    }
}
