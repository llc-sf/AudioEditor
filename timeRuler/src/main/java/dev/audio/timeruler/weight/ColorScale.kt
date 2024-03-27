package dev.audio.timeruler.weight

import androidx.annotation.ColorInt

interface ColorScale {
    /*需要绘制的颜色数量*/
    val size: Int

    /*开始时间*/
    fun getStart(index: Int): Long

    /*结束时间*/
    fun getEnd(index: Int): Long

    /*绘制的颜色*/
    @ColorInt
    fun getColor(index: Int): Int
}