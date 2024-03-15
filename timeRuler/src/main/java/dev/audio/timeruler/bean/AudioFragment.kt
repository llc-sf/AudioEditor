package dev.audio.timeruler.bean

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import dev.audio.ffmpeglib.tool.TimeUtil
import dev.audio.timeruler.BaseMultiTrackAudioEditorView.Companion.long_press_tag
import dev.audio.timeruler.BaseMultiTrackAudioEditorView.Companion.time_line_tag
import kotlin.math.roundToInt

/**
 * 波形片段
 */
open class AudioFragment {
    companion object {

        const val DEFAULT_WAVE_HEIGHT = 50f
        const val DEFAULT_WAVE_VERTICAL_POSITION = 200f
        const val DEFAULT_WAVE_COLOR = Color.RED
    }

    //波形图在时间坐标轴上的起始时间戳
    var startTimestamp: Long = 0
        set(value) {
            field = value
        }

    //波形图在时间坐标轴上的结束时间戳
    var endTimestamp: Long = 0
        set(value) {
            field = value
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

    //选中的一圈矩形宽度
    var strokeWidth = 2f

    //波形垂直位置
    var waveVerticalPosition: Float = DEFAULT_WAVE_VERTICAL_POSITION
        set(value) {
            field = value
            waveVerticalLongPressTempPosition = value
        }

    /**
     * 长按波形图 临时的垂直位置
     */
    private var waveVerticalLongPressTempPosition = waveVerticalPosition
        set(value) {
            field = value
            Log.i(
                long_press_tag,
                "index:${index} setValue waveVerticalLongPressTempPosition: $value"
            )
        }

    //波形垂直间隔
    private var waveVerticalInterval: Float = DEFAULT_WAVE_VERTICAL_POSITION

    //起始时间
    var startValue: Long = 0


    /**
     * 当前游标指示的时间
     *
     * 与坐标轴的时间游标同步
     *
     */
    var cursorValue: Long = 0
        set(value) {
            field = value
            startTimestamp = cursorValue + cursorOffsetTime
            endTimestamp = startTimestamp + duration
        }

    /**
     * 偏移量
     *
     * start 位置距离  时间坐标轴0 的偏移量（时间）
     */
    private var cursorOffsetTime: Long = 0

    //当前指示标的位置（元素）
    var cursorPosition: Float = 0f

    //距离View起始点的偏移量 像素（屏幕最左边）
    private var x: Float = 0f
        get() {
            return -((cursorValue - (startValue)) * unitMsPixel - cursorPosition - cursorOffsetTime * unitMsPixel)
        }

    //波形绘制的坐标区域(相对于TimeRulerBar)
    var rect: Rect? = null

    var index = 0


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
//        return waveVerticalPosition + ((((currentTouchY.toDouble() - startY) / waveVerticalPosition).roundToInt() * waveVerticalPosition).toInt())
        return waveVerticalLongPressTempPosition.apply {
            Log.i(long_press_tag, "index:${index} ondraw waveVerticalPositionLongPress: $this")
        }

    }

    open fun drawWave(
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

        Log.i(
            time_line_tag,
            "drawWave index=$index offsetCursorValue = ${cursorValue - cursorValue}"
        )

        for (i in samples.indices step 400) { // 步长设置为400，可根据需要调整
            val x = (waveViewWidth * (i / samples.size.toFloat())) + x
            val sampleValue = (samples[i] / maxAmplitude) * amplitudeScale
            val y = centerY - sampleValue
            upperPoints.add(Pair(x, y))
        }

        upperPoints.add(Pair(waveViewWidth + x, centerY))

        path.moveTo(0f + x, centerY)
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
        path.lineTo(0f + x, centerY)

        // 绘制路径
        canvas.drawPath(path, mWavePaint)
        rect = Rect(
            (0f + x).toInt(),
            (centerY - maxWaveHeight).toInt(),
            ((0f + x) + waveViewWidth).toInt(),
            (maxWaveHeight + centerY).toInt()
        ).apply {
            Log.i(long_press_tag, "index:${index} draw: $this")
        }
        //画空心rect
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.RED
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        canvas.drawRect(rect!!, rectPaint)
        Log.i(
            time_line_tag, "timeline drawWave index=$index cursorValueTotal:${
                TimeUtil.getDetailTime(cursorValue)
            },cursorValue:${TimeUtil.getDetailTime(cursorValue)},startValue:${
                TimeUtil.getDetailTime(
                    startValue
                )
            }"
        )
        Log.i(
            time_line_tag,
            "timeline drawWave index=$index currentTime:${cursorValue - startValue} [${startTimestamp},${endTimestamp}]"
        )
        return false
    }


    /**
     * 是否是选中状态
     */
    open fun isSelected(x: Float): Boolean {
        return false
    }


    /**
     * 触摸滑动时间轴时
     * currX  当前的手指触摸的X坐标
     */
    fun refreshCursorValueByComputeScroll(currX: Int) {
        //cursorValue = startValue + offsetUpTouchX + (currX / unitMsPixel).toLong()
        Log.i(
            long_press_tag,
            "index:${index}  refreshCursorValueByComputeScroll cursorValue: ${
                TimeUtil.getDetailTime(cursorValue)
            }"
        )
    }

    /**
     * 触摸滑动时间轴松手惯性滑动
     *
     * distanceX 惯性滑动的距离
     * courseIncrement 惯性滑动的时间
     */
    fun refreshCursorValueByOnScroll(distanceX: Float, courseIncrement: Long) {
        //cursorValue += courseIncrement

    }

    /**
     * 长按移动时，水平方向的移动
     */
    fun refreshCursorValueByLongPressHandleHorizontalMove(deltaX: Float) {
        cursorOffsetTime += (deltaX / unitMsPixel).toLong()
        //时间戳转换成时间
        Log.i(
            long_press_tag,
            "index:${index}  refreshCursorValueByLongPressHandleHorizontalMove cursorValue: ${
                TimeUtil.getDetailTime(cursorValue)
            }"
        )
    }


    fun refreshOffsetUpTouchX(oriCursorValue: Long) {
        offsetUpTouchX = cursorValue - oriCursorValue
    }

    fun refreshLongPressCurrentTouchY(currentY: Int) {
        currentTouchY = currentY
        waveVerticalLongPressTempPosition =
            waveVerticalPosition + ((((currentTouchY.toDouble() - startY) / waveVerticalInterval).roundToInt() * waveVerticalInterval).toInt())
        Log.i(
            long_press_tag,
            "index:${index} refreshLongPressCurrentTouchY waveVerticalLongPressTempPosition: $waveVerticalLongPressTempPosition,startY=$startY"
        )
    }

    fun refreshLongPressStartY(startY: Float) {
        Log.i(
            long_press_tag,
            "index:${index} refreshLongPressStartY waveVerticalPosition: $waveVerticalPosition,startY=$startY"
        )
        waveVerticalLongPressTempPosition = waveVerticalPosition
        this.startY = startY
    }

    fun onLongPressTouchUpEvent() {
        Log.i(
            long_press_tag,
            "index:${index} onLongPressTouchUpEvent waveVerticalPosition: $waveVerticalPosition,waveVerticalLongPressTempPosition=$waveVerticalLongPressTempPosition"
        )
        waveVerticalPosition = waveVerticalLongPressTempPosition
        startY = 0f
        currentTouchY = 0
    }
}