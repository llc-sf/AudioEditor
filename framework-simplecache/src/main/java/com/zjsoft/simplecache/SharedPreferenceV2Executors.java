package com.zjsoft.simplecache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedPreferenceV2Executors {

    public static ExecutorService getExecutors() {
        return Holder.SERVICE;
    }

    private static class Holder {
        private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
        //核心线程 默认为cpu核心数，
        //最多200个等待队列
        private static final ExecutorService SERVICE = new ThreadPoolExecutor(CORE_COUNT, CORE_COUNT * 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(200),
                new SharedPreferenceV2Factory(), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private static class SharedPreferenceV2Factory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        SharedPreferenceV2Factory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "SharedPreferenceV2-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
