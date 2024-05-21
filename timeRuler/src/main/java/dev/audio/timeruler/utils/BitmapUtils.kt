package dev.audio.timeruler.utils

import android.graphics.Bitmap
import android.graphics.Rect
import android.view.MotionEvent


/**
 * 扩展函数：裁剪位图的中间三分之一宽度
 */
fun Bitmap.cropMiddleThirdWidth(): Bitmap {
    val width = this.width
    val height = this.height

    // 计算从中间开始截取的区域
    val startX = width / 3 - 30
    val cropWidth = width / 3 + 60

    // 创建新的位图
    return Bitmap.createBitmap(this, startX, 0, cropWidth, height)
}