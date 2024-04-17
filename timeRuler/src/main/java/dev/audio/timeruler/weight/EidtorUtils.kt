package dev.audio.timeruler.weight


/**
 * 像素转时间
 * 相对
 */
fun Float.pixel2Time(unitMsPixel: Float): Long {
    return (this / unitMsPixel).toLong()
}

/**
 * 时间转像素
 * 相对
 */
fun Long.time2Pixel(unitMsPixel: Float): Float {
    return (this * unitMsPixel)
}


/**
 * 时间转像素
 * 绝对
 * @param start 开始时间
 * Long  timeInLine
 */
fun Long.time2PixelInTimeLine(start: Long, unitMsPixel: Float): Float {
    return ((this - start) * unitMsPixel)
}
