package com.android.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.provider.Settings;

import java.util.Locale;

/**
 * Created by lisao on 2020/11/18
 */
public class AppProvider {
    public static Application context;

    static {
        try {
            context = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                context = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        }
    }

    public static Application get() {
        return context;
    }


    //判断是否开启了不保留活动
    public static boolean isNotKeepActivity() {
        int value = 0;
        try {
            value = android.provider.Settings.Global.getInt(context.getContentResolver(), android.provider.Settings.Global.ALWAYS_FINISH_ACTIVITIES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value != 0;
    }

    /**
     * 判断是否开启了开发者模式
     *
     * @return
     */
    public static boolean isDeveloperMode() {
        int value = 0;
        try {
            value = android.provider.Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value != 0;
    }

    /**
     * 是否支持rtl
     *
     * @return
     */
    public static boolean isSupportRTL() {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        boolean hasRtlSupport = (applicationInfo.flags & ApplicationInfo.FLAG_SUPPORTS_RTL) == ApplicationInfo.FLAG_SUPPORTS_RTL;
        return hasRtlSupport;
    }
}
