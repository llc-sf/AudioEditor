package dev.android.player.framework.utils

import android.os.Handler
import android.os.Looper

object ThreadUtils {

    //主线程执行
    fun runOnMainThread(call: () -> Unit) {

        Handler(Looper.getMainLooper()).post {
            call.invoke()
        }
    }
}