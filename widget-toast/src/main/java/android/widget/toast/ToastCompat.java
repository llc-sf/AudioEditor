package android.widget.toast;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

/**
 * 支持配置两种Toast,一种正常的Toast 另一种是可以点击的Toast
 */
public class ToastCompat implements Runnable {

    private static WeakReference<ToastWrapper> sLastToast;

    public static ToastCompat makeText(Context context, @StringRes int resId)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId));
    }

    public static ToastCompat makeText(Context context, CharSequence text)
            throws Resources.NotFoundException {
        return makeText(context, text, Toast.LENGTH_SHORT);
    }

    /**
     * 显示没有图标的Toast
     */
    public static ToastCompat makeTextNoIcon(Context context, @StringRes int resId)
            throws Resources.NotFoundException {
        return makeTextNoIcon(false, context, context.getString(resId), Toast.LENGTH_SHORT);
    }

    /**
     * 显示没有图标的Toast
     */
    public static ToastCompat makeTextNoIcon(Context context, CharSequence text)
            throws Resources.NotFoundException {
        return makeTextNoIcon(false, context, text, Toast.LENGTH_SHORT);
    }

    public static ToastCompat makeText(Context context, boolean isSuccess, @StringRes int resId)
            throws Resources.NotFoundException {
        return makeText(context, isSuccess, context.getResources().getText(resId));
    }

    public static ToastCompat makeText(Context context, boolean isSuccess, CharSequence text)
            throws Resources.NotFoundException {
        return makeText(context, isSuccess, text, Toast.LENGTH_SHORT);
    }

    public static ToastCompat makeText(Context context, @StringRes int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    public static ToastCompat makeText(Context context, CharSequence text, int duration)
            throws Resources.NotFoundException {
        return makeText(context, true, text, duration);
    }

    public static ToastCompat makeText(Context context, CharSequence text, int iconRes, int duration)
            throws Resources.NotFoundException {
        return makeText(context, true, text,iconRes, duration);
    }

    public static ToastCompat makeText(Context context, CharSequence text, Boolean isRtl)
            throws Resources.NotFoundException {
        return makeTextWithAnchor(true, context, true, text, Toast.LENGTH_SHORT, 0f, isRtl);
    }

    public static ToastCompat makeText(Context context, boolean isSuccess, @StringRes int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, isSuccess, context.getResources().getText(resId), duration);
    }

    public static ToastCompat makeTextWithAnchor(boolean isSystem, Context context, boolean isSuccess, CharSequence text, int duration, float anchor, boolean ltrLayout) {
        ToastView view = new ToastView(context);
        view.setLayoutDirection(ltrLayout ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        view.setTitle(text);
        view.setSuccess(isSuccess);
        if (isSystem) {
            if (sLastToast != null && sLastToast.get() != null) {
                sLastToast.get().cancel();
            }
            ToastWrapper wrapper = ToastWrapper.makeText(context, text, duration);
            wrapper.setView(view);
            int y = getDefaultPosition(context);
            if (anchor > 0 && anchor < 1) {
                y = (int) (context.getResources().getDisplayMetrics().heightPixels * anchor) + getNavigationBarHeight(context);
            }
            wrapper.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, y);
            sLastToast = new WeakReference<ToastWrapper>(wrapper);
            return new ToastCompat(wrapper);
        } else {
            return new ToastCompat(context, isSuccess, text, duration);
        }

    }

    public static ToastCompat makeText(Context context, boolean isSuccess, CharSequence text, int duration) {
        ToastView view = new ToastView(context);
        view.setTitle(text);
        view.setSuccess(isSuccess);

        if (sLastToast != null && sLastToast.get() != null) {
            sLastToast.get().cancel();
        }
        ToastWrapper wrapper = ToastWrapper.makeText(context, text, duration);
        wrapper.setView(view);
        int y = getDefaultPosition(context);
        wrapper.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, y);
        sLastToast = new WeakReference<ToastWrapper>(wrapper);
        return new ToastCompat(wrapper);
    }

    public static ToastCompat makeText(Context context, boolean isSuccess, CharSequence text, int iconRes, int duration) {
        ToastView view = new ToastView(context);
        view.setTitle(text);
        view.setSuccess(isSuccess);
        view.setIcon(iconRes);

        if (sLastToast != null && sLastToast.get() != null) {
            sLastToast.get().cancel();
        }
        ToastWrapper wrapper = ToastWrapper.makeText(context, text, duration);
        wrapper.setView(view);
        int y = getDefaultPosition(context);
        wrapper.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, y);
        sLastToast = new WeakReference<ToastWrapper>(wrapper);
        return new ToastCompat(wrapper);
    }

    /**
     * @param text     显示的文本
     * @param duration 显示的时间
     */
    public static ToastCompat makeTextNoIcon(boolean isSystem, Context context, CharSequence text, int duration) {
        if (isSystem) {
            ToastView view = new ToastView(context);
            view.setTitle(text);
            view.showIcon(false);
            if (sLastToast != null && sLastToast.get() != null) {
                sLastToast.get().cancel();
            }
            ToastWrapper wrapper = ToastWrapper.makeText(context, text, duration);
            wrapper.setView(view);
            int y = getDefaultPosition(context);
            wrapper.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, y);
            sLastToast = new WeakReference<ToastWrapper>(wrapper);
            return new ToastCompat(wrapper);
        } else {
            return new ToastCompat(context, false, text, duration, false);
        }
    }


    public static ToastCompat makeLinkText(Context context, CharSequence text) {
        return makeLinkText(context, text, Toast.LENGTH_SHORT);
    }

    public static ToastCompat makeLinkText(Context context, CharSequence text, int duration) {
        return makeLinkText(context, true, text, duration);
    }

    /**
     * 显示可点击的Toast
     *
     * @param context
     * @param isSuccess
     * @param text
     * @param duration
     * @return
     */
    public static ToastCompat makeLinkText(Context context, boolean isSuccess, CharSequence text, int duration) {
        return new ToastCompat(context, isSuccess, text, duration);
    }


    private ToastView mContainer = null;
    private WeakReference<Context> mContextRef;

    private static WeakReference<ToastView> sViewRef;

    private int mDuration;

    private WindowManager manager = null;

    private ToastWrapper mWrapper;

    //使用基础的Toast
    private ToastCompat(ToastWrapper wrapper) {
        mWrapper = wrapper;
    }


    /**
     * 使用WindowManager 添加窗口
     *
     * @param context
     * @param success
     * @param text
     * @param duration
     */
    public ToastCompat(Context context, boolean success, CharSequence text, int duration) {
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getLifecycle().addObserver(new LifeCycle());
        }

        mContainer = new ToastView(context);
        mContainer.setTitle(text);
        mContainer.setSuccess(success);

        this.mDuration = duration;
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mContextRef = new WeakReference<>(context);
    }

    /**
     * 使用WindowManager 添加窗口
     */
    public ToastCompat(Context context, boolean success, CharSequence text, int duration, boolean showIcon) {
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).getLifecycle().addObserver(new LifeCycle());
        }

        mContainer = new ToastView(context);
        mContainer.setTitle(text);
        mContainer.setSuccess(success);
        mContainer.showIcon(showIcon);

        this.mDuration = duration;
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mContextRef = new WeakReference<>(context);
    }

    public void show() {
        if (mWrapper != null) {//这个也有两个问题。一个 重复点击会出现多次Toast. 另一个是Toast无法点击
            mWrapper.show();
        } else {//下面的方法有两个问题，一个Activity关闭时，会导致Toast消失，另一个是Toast展示时容易发生内存泄漏
            Context context = mContextRef.get();
            if (context != null) {
                int w = 0;//屏幕宽度
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    w = manager.getCurrentWindowMetrics().getBounds().width();
                } else {
                    w = manager.getDefaultDisplay().getWidth();
                }
                int w_m = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.AT_MOST);
                int h_m = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                mContainer.measure(w_m, h_m);
                int width = mContainer.getMeasuredWidth();
                int margin = (int) mContainer.getScreenMargin();
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.width = Math.min(width, w - margin * 2);
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.format = PixelFormat.TRANSLUCENT;
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
                params.windowAnimations = android.R.style.Animation_Toast;

                params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                params.y = getDefaultPosition(context);
                params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

                if (sViewRef != null && sViewRef.get() != null) {
                    onRemoveOldToast(manager, sViewRef.get());
                }
                sViewRef = new WeakReference<>(mContainer);
                try {
                    if (context instanceof Activity) {
                        params.token = ((Activity) context).getWindow().getDecorView().getWindowToken();
                        if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                            manager.addView(mContainer, params);
                            mContainer.postDelayed(this, getDuration());
                        }
                    } else {
                        manager.addView(mContainer, params);
                        mContainer.postDelayed(this, getDuration());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取导航栏高度
     *
     * @param context
     * @return
     */
    private static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * 获取默认的位置
     *
     * @param context
     * @return 默认位置
     */
    public static int getDefaultPosition(Context context) {
        if (context == null) {
            return 0;
        }
        float position;//默认位置
        if (context instanceof IToastScreenPosition) {
            position = ((IToastScreenPosition) context).getPosition();
        } else {
            int[] attrs = new int[]{R.attr.toast_screen_position};
            TypedArray array = context.obtainStyledAttributes(null, attrs, R.attr.CustomToastCompatStyle, R.style.ToastCompatStyle);
            position = array.getFloat(0, 0.22f);
            array.recycle();
        }
        int screen_height = context.getResources().getDisplayMetrics().heightPixels;
        if (isScreenHasNavigationBar(context)) {
            int navigation_bar_height = getNavigationBarHeight(context);
            return (int) (screen_height * position) + navigation_bar_height;
        } else {
            return (int) (screen_height * position);
        }
    }

    /**
     * 判断是否有导航栏
     *
     * @param context
     * @return
     */
    private static boolean isScreenHasNavigationBar(Context context) {
        if (context == null) {
            return false;
        }
        boolean hasNavigationBar = false;
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager == null) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = manager.getCurrentWindowMetrics().getWindowInsets();
            hasNavigationBar = insets.isVisible(WindowInsets.Type.navigationBars());//判断是否有导航栏
        } else {
            Resources resources = context.getResources();
            int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
            if (id > 0) {
                hasNavigationBar = resources.getBoolean(id);
            }
        }
        return hasNavigationBar;
    }


    private long getDuration() {
        if (mDuration == Toast.LENGTH_SHORT) {
            return 3000;
        } else if (mDuration == Toast.LENGTH_LONG) {
            return 5000;
        } else {
            return mDuration;
        }
    }


    private void onRemoveOldToast(WindowManager manager, View view) {
        try {
            sViewRef.clear();
            if (view != null && view.isAttachedToWindow()) {
                manager.removeView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        onRemoveOldToast(manager, mContainer);
    }

    private class LifeCycle implements DefaultLifecycleObserver {
        @Override
        public void onPause(@NonNull LifecycleOwner owner) {
            onRemoveOldToast(manager, sViewRef.get());
        }

        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            owner.getLifecycle().removeObserver(this);
        }
    }
}
