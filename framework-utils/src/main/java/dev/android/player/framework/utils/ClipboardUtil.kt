package dev.android.player.framework.utils

import android.content.ClipboardManager
import android.content.Context

object ClipboardUtil {
    /**
     * 获取剪贴板的文本
     *
     * @return 剪贴板的文本
     */
    fun copyText(context: Context): String? {
        //8.1系统，要在主线程获取ClipboardManager，否则报错：Can't create handler inside thread that has not called Looper.prepare()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        return if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(context).toString()
        } else null
    }
}