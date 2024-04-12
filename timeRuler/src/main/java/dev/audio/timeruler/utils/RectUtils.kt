package dev.audio.timeruler.utils

import android.graphics.Rect
import android.view.MotionEvent


fun Rect.isTouch(event: MotionEvent): Boolean {
    return event.x >= left && event.x <= right && event.y >= top && event.y <= bottom
}
