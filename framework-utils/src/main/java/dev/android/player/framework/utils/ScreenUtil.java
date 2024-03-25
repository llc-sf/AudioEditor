package dev.android.player.framework.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;


public class ScreenUtil {
    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取到当前屏幕的宽度Dp
     *
     * @param context
     */
    public static int getScreenWidthDp(Context context) {
        return px2dp(context, getScreenWidth(context));
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }


    public static final float RATIO_4_3 = 4f / 3f;
    public static final float RATIO_16_9 = 16f / 9f;
    public static final float RATIO_18_9 = 18f / 9f;

    /**
     * 获取当前屏幕高度与宽度的比例
     *
     * @param context
     * @return
     */
    public static float getScreenRatio(Context context) {
        //4:3 = 4/3 = 1.3333333333333333
        //16:9 = 16/9 = 1.7777777777777777
        //18:9 = 18/9 = 2
        return getScreenHeight(context) * 1.0f / getScreenWidth(context);
    }

    /**
     * 获取屏幕密度
     *
     * @param context
     * @return
     */
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 获取屏幕密度DPI
     *
     * @param context
     * @return
     */
    public static int getScreenDensityDpi(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * 获取状态栏高度
     * ？？？？？？？？高度不对 todo
     * @param context
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 获取导航栏高度
     *
     * @param context
     */
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    /**
     * 获取屏幕的宽高
     */
    public static int[] getScreenSize(Context context) {
        int[] size = new int[2];
        size[0] = context.getResources().getDisplayMetrics().widthPixels;
        size[1] = context.getResources().getDisplayMetrics().heightPixels;
        return size;
    }

    /**
     * 获取屏幕比例
     */
    public static float getScreenRate(Context context) {
        float screenRate = 0;
        int[] screenSize = getScreenSize(context);
        screenRate = screenSize[0] * 1.0f / screenSize[1];
        return screenRate;
    }

    /**
     * 当前Activity 是否显示NavigationBar
     *
     * @param activity
     * @return
     */
    public static boolean isShowNavigationBar(Activity activity) {
        boolean isShow = false;
        try {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            isShow = realSize.y != size.y;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isShow;
    }


    public static int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5F);
    }

    public static int px2dp(Context context, float px) {
        return (int) (px / context.getResources().getDisplayMetrics().density + 0.5F);
    }


    @interface ScreenRatio {

    }

}
