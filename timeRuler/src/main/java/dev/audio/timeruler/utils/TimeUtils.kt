package dev.audio.timeruler.utils

import java.text.SimpleDateFormat
import java.util.Date


// 扩展Long类型，添加转换为特定时区时间字符串的方法


// Date类的扩展方法，用于格式化日期
fun Date.formatToCursorDateString(): String {
    val cursorDateFormat = SimpleDateFormat("MM月dd日 HH:mm:ss:SSS")
    return cursorDateFormat.format(this)
}


fun Long.formatToCursorDateString(): String {
    val cursorDateFormat = SimpleDateFormat("MM月dd日 HH:mm:ss:SSS")
    return cursorDateFormat.format(this)
}