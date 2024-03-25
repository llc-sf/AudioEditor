package dev.android.player.framework.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

/**
 * 多个条件值同时满足，弹窗弹出优先级处理问题
 */
object MultiProcessPriorityTask {

    private var mInstant: Instant? = null

    @JvmStatic
    fun onAttachApp(app: Application) {
        app.registerActivityLifecycleCallbacks(Lifecycle())
    }

    /**
     * 执行任务
     */
    @JvmStatic
    fun run(task: PriorityTask) {
        mInstant?.run(task)
    }

    private fun init() {
        mInstant = Instant()
    }

    private fun release() {
        mInstant?.onDestroy()
        mInstant = null
    }

    private class Lifecycle : Application.ActivityLifecycleCallbacks {
        private val size = AtomicInteger(0)
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (size.get() == 0) {
                init()
            }
            size.getAndIncrement()
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (size.getAndDecrement() == 0) {
                release()
            }
        }
    }


}

private class Instant {
    private val mLock = Semaphore(1)

    private val mMultiProcessThread: HandlerThread = HandlerThread("MultiProcessPriorityTask")

    private val mMultiProcessHandler = Handler(mMultiProcessThread.apply { start() }.looper) {
        mLock.acquire()
        when (it.what) {
            HANDLER_TASK_PROCESS -> {
                mMainHandler.post(TaskWrapper(mCurrentTask))
            }
        }
        true
    }

    private val mMainHandler = Handler(Looper.getMainLooper())

    private var mCurrentTask: PriorityTask? = null

    private val HANDLER_TASK_PROCESS = 0x0001

    /**
     * 执行任务
     * 500毫秒内只需要高优先级任务执行。其他任务不需要执行
     */
    fun run(task: PriorityTask) {
        try {
            if (mCurrentTask?.getPriority() ?: Int.MIN_VALUE < task.getPriority()) {
                mCurrentTask = task
                //移除旧的需要执行的任务，再次发送一遍事件
                mMultiProcessHandler.removeMessages(HANDLER_TASK_PROCESS)
                mMultiProcessHandler.sendEmptyMessageDelayed(HANDLER_TASK_PROCESS, 800)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mLock.release()
        }
    }

    fun onDestroy() {
        try {
            mLock.release()
            mMultiProcessHandler.removeCallbacksAndMessages(null)
            mMultiProcessThread.quitSafely()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 执行完毕后释放锁
     */
    private inner class TaskWrapper(private val task: PriorityTask?) : Runnable {
        override fun run() {
            task?.run()
            mCurrentTask = null
            //执行完毕后释放锁
            mLock.release()
        }
    }
}


interface PriorityTask : Runnable {
    //任务优先级
    fun getPriority(): Int
}


