package dev.audio.timeruler.utils

import android.graphics.Paint
import java.text.SimpleDateFormat
import java.util.Date


fun Paint.getTextHeight(): Float {
    val fontMetrics = fontMetrics
    return fontMetrics.bottom - fontMetrics.top
}

fun Paint.getTopY(): Float {
    return return getTextHeight() / 2 + fontMetrics.descent
}