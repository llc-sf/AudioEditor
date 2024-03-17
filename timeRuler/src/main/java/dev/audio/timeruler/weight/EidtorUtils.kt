package dev.audio.timeruler.weight

fun Float.pixel2Time(unitMsPixel: Float): Long {
    return (this / unitMsPixel).toLong()
}

fun Long.time2Pixel(unitMsPixel: Float): Float {
    return (this * unitMsPixel)
}