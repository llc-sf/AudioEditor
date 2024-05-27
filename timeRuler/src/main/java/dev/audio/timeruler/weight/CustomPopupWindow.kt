package dev.audio.timeruler.weight

import android.content.Context
import android.view.ViewGroup
import android.widget.PopupWindow

/**
 * 解决5.0不展示问题
 */
class CustomPopupWindow(context: Context) : PopupWindow(context) {

    init {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
