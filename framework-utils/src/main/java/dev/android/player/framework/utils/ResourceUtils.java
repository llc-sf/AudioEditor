package dev.android.player.framework.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;

import java.util.Locale;

/**
 * 资源工具
 */
public class ResourceUtils {

    public static String makeLabel(final Context context, final int pluralInt,
                                   final int number) {
        String temp = context.getResources().getQuantityString(pluralInt, number);
        //资源格式中有%s占位符,也有%d占位符，那么对于这种需要做一下判断，如果是%s占位符，不能直接用，需要先把数字转一下
        if (temp.contains("%s")) {
            return String.format(temp, String.format(Locale.getDefault(), "%d", number));
        } else {
            return String.format(temp, number);
        }
    }

    /**
     * 通过名称获取资源id
     */
    public static int getResourceId(Context context, String name) {
        return context.getResources().getIdentifier(name, "id", context.getPackageName());
    }

    /**
     * 获取指定国家的语言资源
     *
     * @param context
     * @param locale
     * @return
     */
    public static Resources getLocalResource(Context context, Locale locale) {
        Resources resources = context.getResources();
        try {
            PackageManager pm = context.getPackageManager();
            String name = context.getPackageName();
            resources = pm.getResourcesForApplication(name);
            Configuration config = resources.getConfiguration();
            config.locale = locale;
            resources.updateConfiguration(config, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resources;
    }


    public static int getAttrColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static int getAttrDrawable(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.resourceId;
    }

}
