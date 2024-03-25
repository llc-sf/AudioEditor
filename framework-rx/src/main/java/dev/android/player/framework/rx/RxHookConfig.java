package dev.android.player.framework.rx;

import android.util.Log;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.internal.schedulers.RxThreadFactory;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by lisao on 12/15/20
 * RxPlugin Hook
 */
public class RxHookConfig {

    public static void hook() {
    }

    public static class ThreadHook {

        private static final Executor NewThreadExecutor = new ThreadPoolExecutor(4,
                4,
                180,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200), new RxThreadFactory("RxHook-NewThread", Thread.NORM_PRIORITY, true));

        private static final Scheduler NewScheduler = Schedulers.from(NewThreadExecutor);


        static {
            RxJavaPlugins.setInitNewThreadSchedulerHandler(callable -> {
                Log.d("RxHookConfig", "ThreadHook.setInitNewThreadSchedulerHandler apply");
                return NewScheduler;
            });
            RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> {
                Log.d("RxHookConfig", "ThreadHook.setNewThreadSchedulerHandler apply");
                return NewScheduler;
            });
        }

        public static void hook() {
        }
    }

    public static class OnErrorHook {
        static {
            RxJavaPlugins.setErrorHandler(e -> {
                if (e instanceof UndeliverableException) {
                    e = e.getCause();
                }
                if ((e instanceof IOException) || (e instanceof SocketException)) {
                    // fine, irrelevant network problem or API that throws on cancellation
                    return;
                }
                if (e instanceof InterruptedException) {
                    // fine, some blocking code was interrupted by a dispose call
                    return;
                }
                if (e != null) {
                    String msg = e.getMessage();
                    Log.e("OnErrorHook", "msg = " + msg);
                }
            });
        }

        public static void hook() {
        }
    }

    /**
     * 自定义日常上报
     */
    private static class RxHookUndeliverableErrorException extends Exception {
        public RxHookUndeliverableErrorException(Throwable cause) {
            super(cause);
        }
    }
}
