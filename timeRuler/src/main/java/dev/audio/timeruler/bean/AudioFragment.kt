package dev.audio.timeruler.bean

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.roundToInt

/**
 * 波形片段
 */
class AudioFragment {
    companion object {

        const val DEFAULT_WAVE_HEIGHT = 50f
        const val DEFAULT_WAVE_VERTICAL_POSITION = 200f
        const val DEFAULT_WAVE_COLOR = Color.RED
    }

    //波形数据
    var waveform: Waveform? = null

    //波形高度
    var maxWaveHeight: Float = DEFAULT_WAVE_HEIGHT

    //波形宽度
    var waveViewWidth = 0
        get() = (duration * unitMsPixel).toInt()

    //每毫秒对应的像素
    var unitMsPixel: Float = 0f

    //波形垂直位置
    var waveVerticalPosition: Float = DEFAULT_WAVE_VERTICAL_POSITION

    //起始时间
    var startValue: Long = 0

    //当前指示的时间
    var cursorValue: Long = 0

    //当前指示标的位置（元素）
    var cursorPosition: Float = 0f

//    var offsetX: Float = 0f
//        get() = ((cursorValue - (startValue ?: 0)) * unitMsPixel - cursorPosition)

    //总时长
    var duration: Long = 0

    //颜色
    var color: Int = DEFAULT_WAVE_COLOR
        set(value) {
            field = value
            mWavePaint.color = value
        }

    //当前手指的Y坐标 长按移动用
    var currentTouchY: Int = 0

    //长按时 Y的坐标
    var startY = 0f

    //前一次水平方向上的坐标
    var lastTouchX: Int = 0

    //长按移动，手指抬起时  与原来起点的cursorValue差值，惯性滑动用
    var offsetUpTouchX: Long = 0


    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.RED
        this.style = Paint.Style.FILL
    }


    private fun getTrackYPosition(): Float {
        return waveVerticalPosition + ((((currentTouchY.toDouble() - startY) / waveVerticalPosition).roundToInt() * waveVerticalPosition).toInt())
    }

    fun draw(
        canvas: Canvas
    ): Boolean {
        var wf = waveform
        val samples = wf?.amplitudes ?: return true
        val centerY = getTrackYPosition() // 使用新变量设置垂直位置
        //            val centerY = waveVerticalPosition // 使用新变量设置垂直位置
        val maxAmplitude = (samples.maxOrNull() ?: 1).toFloat()

        // 使用 maxWaveHeight 变量来控制波形高度
        val amplitudeScale = maxWaveHeight

        val path = Path()
        val upperPoints = mutableListOf<Pair<Float, Float>>()

        var offsetX = -((cursorValue - (startValue)) * unitMsPixel - cursorPosition)

        for (i in samples.indices step 400) { // 步长设置为400，可根据需要调整
            val x = (waveViewWidth * (i / samples.size.toFloat())) + offsetX
            val sampleValue = (samples[i] / maxAmplitude) * amplitudeScale
            val y = centerY - sampleValue
            upperPoints.add(Pair(x, y))
        }

        upperPoints.add(Pair(waveViewWidth + offsetX, centerY))

        path.moveTo(0f + offsetX, centerY)
        for (i in 0 until upperPoints.size - 1) {
            val (x1, y1) = upperPoints[i]
            val (x2, y2) = upperPoints[i + 1]
            val midX = (x1 + x2) / 2
            val midY = (y1 + y2) / 2
            path.quadTo(x1, y1, midX, midY)
        }

        val (lastUpperX, lastUpperY) = upperPoints.last()
        path.lineTo(lastUpperX, lastUpperY)

        for (i in upperPoints.size - 2 downTo 0) {
            val (x1, y1) = upperPoints[i]
            val y = 2 * centerY - y1
            if (i > 0) {
                val (x2, y2) = upperPoints[i - 1]
                val midX = (x1 + x2) / 2
                val midY = 2 * centerY - (y1 + y2) / 2
                path.quadTo(x1, y, midX, midY)
            } else {
                path

                // 最后连接回到起始点的中心线
                path.lineTo(x1, y)
            }
        }

        // 闭合路径
        path.lineTo(0f + offsetX, centerY)

        // 绘制路径
        canvas.drawPath(path, mWavePaint)
        return false
    }

    fun refreshCursorValueByComputeScroll(currX: Int) {
        cursorValue =
            startValue + offsetUpTouchX + (currX / unitMsPixel).toLong()
    }

    fun refreshCursorValueByHandleHorizontalMove(deltaX: Float) {
        cursorValue -= (deltaX / unitMsPixel).toLong()
    }

    fun refreshCursorValueByOnScroll(courseIncrement: Long) {
        cursorValue += courseIncrement
    }

    fun refreshOffsetUpTouchX(oriCursorValue: Long) {
        offsetUpTouchX = cursorValue - oriCursorValue
    }

    fun refreshCurrentTouchY(currentY: Int) {
        currentTouchY = currentY
    }

    fun refreshStartY(startY: Float){
        this.startY = startY
    }
}