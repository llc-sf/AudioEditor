package dev.android.player.framework.utils

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Activity 的扩展函数 当 Activity 获取焦点的时候，执行回调
 * @param isInit 是否是初始化 用于判断是否是新用户第一次进入
 * @param func 回调
 */
fun AppCompatActivity.whenFocused(isInit: Boolean, func: () -> Unit) {

    val MSG_HANDLER_WINDOW_FOCUS_CHANGED = 1//WindowFocusChanged
    val MSG_HANDLER_CALL_BACK = 2//执行回调
    var handler: Handler? = null
    //是否是 OPPO 或者 VIVO
    val isOppoOrVIVO = AndroidUtil.isOppoDevice() || AndroidUtil.isVIVODevice()
    //是否是 10 - 12 版本
    val isVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2

    val listener = ViewTreeObserver.OnWindowFocusChangeListener {
        val msg = Message.obtain()
        msg.what = MSG_HANDLER_WINDOW_FOCUS_CHANGED
        msg.obj = it
        handler?.removeCallbacksAndMessages(null)
        handler?.sendMessage(msg)
    }

    //Handler 的回调
    val callback: Handler.Callback = Handler.Callback {
        when (it.what) {
            MSG_HANDLER_WINDOW_FOCUS_CHANGED -> {
                val hasFocus = it.obj as Boolean
                handler?.removeMessages(MSG_HANDLER_CALL_BACK)
                if (hasFocus && isOppoOrVIVO && isVersion && isInit) {
                    handler?.sendEmptyMessageDelayed(MSG_HANDLER_CALL_BACK, 2000)
                } else if (hasFocus && isInit) { //其他机型 操蛋的机型，不知道会不会有弹窗的各种问题，所以统一延迟500ms
                    handler?.sendEmptyMessageDelayed(MSG_HANDLER_CALL_BACK, 500)
                } else if (hasFocus) {
                    handler?.sendEmptyMessageDelayed(MSG_HANDLER_CALL_BACK, 50)
                }
            }

            MSG_HANDLER_CALL_BACK -> {
                func()
            }
        }
        true
    }


    /**
     * Activity 销毁的时候，需要移除所有的消息
     */
    fun onActivityDestroy() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
        window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
    }
    handler = Handler(Looper.getMainLooper(), callback)
    this.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            onActivityDestroy()
        }
    })
    //触发一次
    val msg = Message.obtain()
    msg.what = MSG_HANDLER_WINDOW_FOCUS_CHANGED
    msg.obj = hasWindowFocus()
    handler?.sendMessage(msg)
    window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(listener)
}