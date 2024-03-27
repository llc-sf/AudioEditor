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

fun Long.format2Duration(): String {
    val hours = this / 3600000 // 将毫秒转换为小时
    val minutes = (this % 3600000) / 60000 // 计算剩余的分钟
    val seconds = (this % 60000) / 1000 // 计算剩余的秒
    val milliseconds = this % 1000 // 获取毫秒

    return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds)
}