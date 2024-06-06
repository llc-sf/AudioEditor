package dev.android.player.framework.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Stack;

import dev.android.player.framework.utils.impl.ServiceCenter;
import dev.android.player.framework.utils.service.AppService;
import dev.android.player.framework.utils.service.PlayerService;


/**
 * Activity 堆栈管理
 */
public class ActivityStackManager {
    private static final String TAG = "ActivityStackManager";

    private static ActivityStackManager instance;
    private Stack<Activity> activityStack = new Stack<>();
    private WeakReference<Activity> mStartedActivity;

    private ActivityStackManager() {
    }

    public static ActivityStackManager getInstance() {
        if (instance == null) {
            instance = new ActivityStackManager();
        }
        return instance;
    }


    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * 移除Activity
     */
    public void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        return getStartedActivity();
    }



    @Nullable
    public Activity getTopActivity() {
        if (activityStack == null || activityStack.isEmpty()) {
            return null;
        }
        return activityStack.lastElement();
    }

    @Nullable
    public Activity getStartedActivity() {
        return mStartedActivity != null ? mStartedActivity.get() : null;
    }

    private void setStartedActivity(Activity activity) {
        mStartedActivity = activity != null ? new WeakReference<>(activity) : null;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        if (activityStack == null || activityStack.isEmpty()) {
            return;
        } else {
            Activity activity = activityStack.lastElement();
            finishActivity(activity);
        }
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        try {
            for (Activity activity : activityStack) {
                if (activity.getClass().equals(cls)) {
                    finishActivity(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }


    public boolean isAppRunning() {
        return mStartedActivity != null && mStartedActivity.get() != null;
    }

    /**
     * 获取堆栈中Activity的个数
     */
    public int getActivityCount() {
        return activityStack.size();
    }

    public void init(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                Log.d(TAG, "onActivityCreated() called with: activity = [" + activity + "], savedInstanceState = [" + savedInstanceState + "]");
                addActivity(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                Log.d(TAG, "onActivityStarted() called with: activity = [" + activity + "]");
                setStartedActivity(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                Log.d(TAG, "onActivityResumed() called with: activity = [" + activity + "]");
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                Log.d(TAG, "onActivityPaused() called with: activity = [" + activity + "]");
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                Log.d(TAG, "onActivityStopped() called with: activity = [" + activity + "]");
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                Log.d(TAG, "onActivityDestroyed() called with: activity = [" + activity + "]");
                removeActivity(activity);
                if (activityStack == null || activityStack.isEmpty()) {
                    setStartedActivity(null);
                }
                Log.d(TAG, "activityStack.size() = [" + activityStack.size() + "]");
//                ServiceCenter.INSTANCE.getService(PlayerService.class).stop();
                ServiceCenter.INSTANCE.getService(PlayerService.class).clearProgress();
            }
        });
    }
}
