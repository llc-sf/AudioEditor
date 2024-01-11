package com.masoudss.lib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import com.masoudss.lib.R

class CustomWaveformSeekBar(context: Context, attrs: AttributeSet) :
    WaveformSeekBar(context, attrs) {
    private var startTimestampPosition: Float = 0f
    private var endTimestampPosition: Float = 0f
    private var lastTouchX: Float = 0f
    private var isMovingStart: Boolean = false
    private var isMovingEnd: Boolean = false
    private val timestampLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 5f
    }
    private val timestampHandlerRadius = 20f
    private val timestampHandlerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }


    override fun refreshPosition() {
        startTimestampPosition = sample?.size?.let { it * 0.25f } ?: 0f
        endTimestampPosition = sample?.size?.let { it * 0.75f } ?: 0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制代表开始和结束时间戳的线，线的终止位置应在圆圈的下缘
        canvas.drawLine(
            startTimestampPosition,
            timestampHandlerRadius * 2,
            startTimestampPosition,
            height.toFloat(),
            timestampLinePaint
        )
        canvas.drawLine(
            endTimestampPosition,
            timestampHandlerRadius * 2,
            endTimestampPosition,
            height.toFloat(),
            timestampLinePaint
        )

        // 绘制圆圈标记在直线的顶端
        canvas.drawCircle(
            startTimestampPosition,
            timestampHandlerRadius,
            timestampHandlerRadius,
            timestampHandlerPaint
        )
        canvas.drawCircle(
            endTimestampPosition,
            timestampHandlerRadius,
            timestampHandlerRadius,
            timestampHandlerPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMovingStart = Math.abs(x - startTimestampPosition) <= timestampHandlerRadius
                isMovingEnd = Math.abs(x - endTimestampPosition) <= timestampHandlerRadius
                lastTouchX = x
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 检查控件是否已经初始化宽度
                if (width > 0) {
                    val dx = x - lastTouchX
                    if (isMovingStart) {
                        startTimestampPosition += dx
                        if (startTimestampPosition < 0) startTimestampPosition = 0f
                        if (startTimestampPosition > endTimestampPosition - timestampHandlerRadius * 2) {
                            startTimestampPosition =
                                endTimestampPosition - timestampHandlerRadius * 2
                        }
                    } else if (isMovingEnd) {
                        endTimestampPosition += dx
                        if (endTimestampPosition > width.toFloat()) endTimestampPosition =
                            width.toFloat()
                        if (endTimestampPosition < startTimestampPosition + timestampHandlerRadius * 2) {
                            endTimestampPosition =
                                startTimestampPosition + timestampHandlerRadius * 2
                        }
                    }
                    lastTouchX = x
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isMovingStart = false
                isMovingEnd = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

}
