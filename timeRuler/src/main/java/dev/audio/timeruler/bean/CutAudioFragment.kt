package dev.audio.timeruler.bean

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import dev.audio.ffmpeglib.tool.ScreenUtil


/**
 * 波形片段
 *
 * 带裁剪模式
 */
class CutAudioFragment : AudioFragment() {

    companion object {
        //裁剪竖线的狂赌
        const val strokeWidth_cut = 5f
    }

    private var startTimestampPosition: Float = 20f
    private var endTimestampPosition: Float = 100f


    private val timestampHandlerRadius = 20f
    private val timestampHandlerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = strokeWidth_cut
        style = Paint.Style.STROKE
    }
    private val timestampLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = strokeWidth_cut
    }


    /**
     * 是否选中  裁剪
     */
    override fun isSelected(x: Float): Boolean {
        return x in startTimestampPosition..endTimestampPosition
    }

    override fun drawWave(canvas: Canvas): Boolean {
        // 开启图层
        var saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
        super.drawWave(canvas)

        // 绘制代表开始和结束时间戳的线，线的终止位置应在圆圈的下缘
        canvas.drawLine(
            startTimestampPosition,
            timestampHandlerRadius * 2,
            startTimestampPosition,
//            height.toFloat(),
            200f,
            timestampLinePaint
        )
        canvas.drawLine(
            endTimestampPosition,
            timestampHandlerRadius * 2,
            endTimestampPosition,
            //            height.toFloat(),
            200f,
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
        drawCutFragment(canvas)
        //恢复图层
        canvas.restoreToCount(saved)
        return true

    }

    private fun drawCutFragment(canvas: Canvas) {
        // 假设已经有了一个Bitmap和Canvas，并且波形已经绘制完成
        val paint = Paint()
        paint.color = Color.YELLOW
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        // 创建覆盖两条竖线中间区域的矩形
        val rect =
            Rect(
                startTimestampPosition.toInt() + strokeWidth_cut.toInt(),
                (rect?.top ?: 0) + strokeWidth.toInt(),
                endTimestampPosition.toInt() - strokeWidth_cut.toInt(),
                ((rect?.bottom ?: 0) - strokeWidth.toInt())
            )

        // 在波形图上绘制这个矩形
        canvas.drawRect(rect, paint)
        // 移除Xfermode
        paint.xfermode = null
    }

    private var isMovingStart: Boolean = false
    private var isMovingEnd: Boolean = false
    private var lastTouchXProcess: Float = 0f
    fun onTouchEvent(context: Context, view: View, event: MotionEvent?): Boolean {
        if (event == null) return false
        if (!isTarget(event)) {
            return false
        }
        var width = ScreenUtil.getScreenWidth(context)
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMovingStart = Math.abs(x - startTimestampPosition) <= timestampHandlerRadius
                isMovingEnd = Math.abs(x - endTimestampPosition) <= timestampHandlerRadius
                lastTouchXProcess = x
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 检查控件是否已经初始化宽度
                if (width > 0) {
                    val dx = x - lastTouchXProcess
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
                    lastTouchXProcess = x
                    view.invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isMovingStart = false
                isMovingEnd = false
                return true
            }
        }
        return view.onTouchEvent(event)
    }

    private fun isTarget(event: MotionEvent): Boolean {
        // 为开始时间戳的圆球创建一个矩形
        val startRect = Rect(
            startTimestampPosition.toInt(),
            timestampHandlerRadius.toInt(),
            startTimestampPosition.toInt() + (timestampHandlerRadius * 2).toInt(),
            timestampHandlerRadius.toInt() + 200
        )

        // 为结束时间戳的圆球创建一个矩形
        val endRect = Rect(
            endTimestampPosition.toInt(),
            timestampHandlerRadius.toInt(),
            endTimestampPosition.toInt() + (timestampHandlerRadius * 2).toInt(),
            timestampHandlerRadius.toInt() + 200
        )

        // 检查触摸点是否落在开始或结束的矩形内
        if (startRect.contains(event.x.toInt(), event.y.toInt()) ||
            endRect.contains(event.x.toInt(), event.y.toInt())
        ) {
            return true
        }

        return false
    }

}