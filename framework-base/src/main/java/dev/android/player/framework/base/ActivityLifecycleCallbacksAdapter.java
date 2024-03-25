package dev.android.player.framework.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * 生命周期监听
 */
public class ActivityLifecycleCallbacksAdapter implements Application.ActivityLifecycleCallbacks,
        ApplicationProcessLifecycle.ProcessLifecycleListener {

    private final List<Activity> mStackActivities;

    private WeakReference<Activity> mResumedActivity;

    private WeakReference<Activity> mStoppedActivity;


    private final HashMap<String, WeakReference<Activity>> mActivityMap = new HashMap<>();

    public ActivityLifecycleCallbacksAdapter() {
        mStackActivities = new Stack<>();
        ApplicationProcessLifecycle.get().addListener(this);
    }

    private boolean isAppInForeground = false;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mStackActivities.add(activity);
        mActivityMap.put(activity.getClass().getSimpleName(), new WeakReference<>(activity));
    }


    @CallSuper
    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        mResumedActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        mStoppedActivity = new WeakReference<>(activity);
    }


    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        mStackActivities.remove(activity);
        mActivityMap.remove(activity.getClass().getSimpleName());
        if (mStackActivities.size() == 0) {
            onAppExit(activity);
            mStoppedActivity = null;
            mResumedActivity = null;
        }
    }

    @Nullable
    protected Activity getActivityByName(String name) {
        WeakReference<Activity> reference = mActivityMap.get(name);
        return reference != null ? reference.get() : null;
    }


    /**
     * 应用完全退出
     *
     * @param activity
     */
    protected void onAppExit(@NonNull Activity activity) {

    }

    protected boolean isAppInForeground() {
        return isAppInForeground;
    }

    protected void onAppEnterForeground(@Nullable Activity activity) {

    }

    protected void onAppEnterBackground(@Nullable Activity activity) {

    }

    @Override
    public void onAppEnterForeground() {
        this.isAppInForeground = true;
        onAppEnterForeground(mResumedActivity != null ? mResumedActivity.get() : null);
    }

    @Override
    public void onAppEnterBackground() {
        this.isAppInForeground = false;
        onAppEnterBackground(mStoppedActivity != null ? mStoppedActivity.get() : null);
    }
}