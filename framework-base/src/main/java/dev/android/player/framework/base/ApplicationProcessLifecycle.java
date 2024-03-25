package dev.android.player.framework.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.util.LinkedList;
import java.util.List;

/**
 * Application 前后台管理
 */
public class ApplicationProcessLifecycle implements DefaultLifecycleObserver {

    private static final String TAG = "ApplicationProcess";

    private static final ApplicationProcessLifecycle instance = new ApplicationProcessLifecycle();

    private final List<ProcessLifecycleListener> listeners;

    private boolean isAppInForeground = false;

    private ApplicationProcessLifecycle() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        listeners = new LinkedList<>();
    }

    private final Handler mHandler = new ResponseHandler(Looper.getMainLooper());

    public static ApplicationProcessLifecycle get() {
        return instance;
    }


    public boolean isAppInForeground() {
        return isAppInForeground;
    }

    public void addListener(ProcessLifecycleListener listener) {
        listeners.add(listener);
    }

    /**
     * 这里说明一下为什么用onResume 而不用onStart
     * 通过Widget 打开 APP时，会启动一个透明的 Activity，如果使用onStart的回调，
     * 那么会导致应用进入前台时获取到的是透明的 Activity。因为 透明的 Activity 的 onDestroy 会在onResume后执行
     * 其实onStart 和  onResume 都可以，只是onResume的回调会晚一点，获取的是当前正在显示的 Activity
     *
     * @param owner the component, whose state was changed
     */
    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        isAppInForeground = true;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(ResponseHandler.MSG_APP_ENTER_FOREGROUND, 100);
    }


    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        isAppInForeground = false;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(ResponseHandler.MSG_APP_ENTER_BACKGROUND, 100);
    }


    private class ResponseHandler extends Handler {

        public static final int MSG_APP_ENTER_FOREGROUND = 1;
        public static final int MSG_APP_ENTER_BACKGROUND = 2;


        private boolean isAppInForegroundMethodCalled = false;

        private boolean isAppInBackgroundMethodCalled = false;

        public ResponseHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_APP_ENTER_FOREGROUND:
                    dispatchAppEnterForeground();
                    break;
                case MSG_APP_ENTER_BACKGROUND:
                    dispatchAppEnterBackground();
                    break;
            }
        }

        /**
         * 应用进入前台¬
         */
        private void dispatchAppEnterForeground() {
            if (!isAppInForegroundMethodCalled) {
                Log.d(TAG, "onAppEnterForeground() called with: owner = []");
                synchronized (listeners) {
                    for (ProcessLifecycleListener listener : listeners) {
                        listener.onAppEnterForeground();
                    }
                }
                isAppInForegroundMethodCalled = true;
                isAppInBackgroundMethodCalled = false;
            }
        }

        /**
         * 应用进入后台
         */
        private void dispatchAppEnterBackground() {
            if (!isAppInBackgroundMethodCalled) {
                Log.d(TAG, "onAppEnterBackground() called with: owner = []");
                synchronized (listeners) {
                    for (ProcessLifecycleListener listener : listeners) {
                        listener.onAppEnterBackground();
                    }
                }
                isAppInBackgroundMethodCalled = true;
                isAppInForegroundMethodCalled = false;
            }
        }
    }


    public interface ProcessLifecycleListener {
        void onAppEnterForeground();

        void onAppEnterBackground();
    }
}
