package dev.android.player.framework.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatDrawableManager;

import java.util.Locale;

/**
 * Created by robotinthesun on 24/10/2017.
 */

public class ViewUtils {

    public static void setImageViewVectorResource(Context context, RemoteViews remoteViews, int id, int vector) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteViews.setImageViewResource(id, vector);
        } else {
            Drawable d = AppCompatDrawableManager.get().getDrawable(context, vector);
            Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(),
                    d.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            d.setBounds(0, 0, c.getWidth(), c.getHeight());
            d.draw(c);
            remoteViews.setImageViewBitmap(id, b);
        }
    }

    public static void setImageViewVectorResource(Context context, ImageView imageView, @DrawableRes int vector) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setImageResource(vector);
        } else {
            Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, vector);
            imageView.setImageDrawable(drawable);
        }
    }

    public static void removeViewFromParent(View view) {
        if (view == null) {
            return;
        }

        ViewParent parent = view.getParent();
        if (parent == null) {
            return;
        }
        ViewGroup viewgroup = (ViewGroup) parent;
        viewgroup.removeView(view);
    }

    public static void setTranslucent(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = activity.getWindow().getDecorView();
            int option = android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void addView(View view, ViewGroup parentView, ViewGroup.LayoutParams layoutParams) {
        if (view == null) {
            return;
        }

        ViewParent parent = view.getParent();
        if (parent == parentView) {
            return;
        }
        if (parent != null) {
            removeViewFromParent((ViewGroup) parent);
        }

        parentView.addView(view, layoutParams);
    }


    /**
     * 主动测量View的宽高
     *
     * @param view
     * @return
     */
    public static int[] onMeasure(View view) {
        int widthMeasureSpec = android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return new int[]{view.getMeasuredWidth(), view.getMeasuredHeight()};
    }

    public static boolean isRtl(View view) {
        return view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL || isRtl2(view);
    }

    public static boolean isRtl2(View view) {
        Context context = view.getContext();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        boolean hasRtlSupport = (applicationInfo.flags & ApplicationInfo.FLAG_SUPPORTS_RTL) == ApplicationInfo.FLAG_SUPPORTS_RTL;
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage().toLowerCase();
        return hasRtlSupport && (language.startsWith("ar") || language.startsWith("fa") || language.startsWith("he") || language.startsWith("ur"));
    }


    /**
     * @param solid
     * @param border
     * @param width
     * @param mask
     * @param radius
     * @return
     */
    public static final Drawable getRippleDrawable(int solid, int border, int width, int mask, float radius) {
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(solid);
        normal.setStroke(width, border);
        normal.setCornerRadius(radius);

        GradientDrawable ripple = new GradientDrawable();
        ripple.setColor(mask);
        ripple.setCornerRadius(radius);

        ColorStateList colors = ColorStateList.valueOf(mask);

        return new RippleDrawable(colors, normal, ripple);

    }

    public static int[] onMeasureAtMost(View view) {
        int maxWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int maxHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return new int[]{view.getMeasuredWidth(), view.getMeasuredHeight()};
    }

}
