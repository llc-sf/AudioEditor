package dev.audio.timeruler.weight

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import dev.audio.ffmpeglib.tool.TimeUtil
import dev.audio.timeruler.weight.BaseAudioEditorView.Companion.long_press_tag
import dev.audio.timeruler.weight.BaseAudioEditorView.Companion.time_line_tag
import dev.audio.timeruler.bean.Ref
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.weight.BaseAudioEditorView.Companion.wave_tag
import kotlin.math.roundToInt

/**
 * 波形片段
 */
open class AudioFragment(private var audioEditorView: BaseAudioEditorView) {
    companion object {

        const val DEFAULT_WAVE_HEIGHT = 150f
        const val DEFAULT_WAVE_VERTICAL_POSITION = 200f
        const val DEFAULT_WAVE_COLOR = Color.RED
        const val WAVE_STEP = 20
    }

    /**
     * 起始时间
     *
     * 波形图在时间坐标轴上的起始时间戳
     * （范围：时间角度）
     */
    var startTimestamp: Long = 0

    /**
     * 结束时间
     * 波形图在时间坐标轴上的结束时间戳
     * （范围：时间角度）
     */
    private var endTimestamp: Long = 0
        get() {
            return startTimestamp + duration
        }


    /**
     * 波形绘制的坐标区域 屏幕坐标
     * （范围：位置角度）
     */
    var rect: Rect? = null

    //波形数据
    var waveform: Waveform? = null

    //波形高度
    var maxWaveHeight: Float = DEFAULT_WAVE_HEIGHT

    var waveStep = WAVE_STEP

    //波形宽度
    private val waveViewWidth
        get() = (duration * unitMsPixel).toInt().apply {
            Log.i(wave_tag, "waveViewWidth width=$this duration:$duration unitMsPixel:$unitMsPixel")
        }

    //每毫秒对应的像素
    val unitMsPixel by Ref { audioEditorView.unitMsPixel }


    //当前指示标的位置（元素）
    private val cursorPosition by Ref { audioEditorView.cursorPosition }


    /**
     *
     * 当前坐标轴的时间游标同步
     *
     */
    private val cursorValue by Ref { audioEditorView.cursorValue }


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
            Log.i(long_press_tag, "index:${index} setValue waveVerticalLongPressTempPosition: $value")
        }

    //波形垂直间隔
    private var waveVerticalInterval: Float = DEFAULT_WAVE_VERTICAL_POSITION


    /**
     * 偏移量
     *
     * start 位置距离  时间坐标轴0 的偏移量（时间）
     */
    var cursorOffsetTime: Long = 0


    //距离View起始点的偏移量 像素（屏幕最左边）
    private val x: Float
        get() {
            return -((cursorValue - (startTimestamp)) * unitMsPixel - cursorPosition - cursorOffsetTime * unitMsPixel)
        }


    //竖直轨道
    var index = 0

    //波形时长
    var duration: Long = 0
        set(value) {
            field = value
            initCutFragment()
        }

    open fun initCutFragment() {
    }

    //颜色
    var color: Int = DEFAULT_WAVE_COLOR
        set(value) {
            field = value
            mWavePaint.color = value
        }

    //当前手指的Y坐标 长按移动用
    private var currentTouchY: Int = 0

    //长按时 Y的坐标
    private var startY = 0f

    //前一次水平方向上的坐标
    var lastTouchX: Int = 0

    //长按移动，手指抬起时  与原来起点的cursorValue差值，惯性滑动用
    private var offsetUpTouchX: Long = 0


    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.RED
        this.style = Paint.Style.FILL
    }


    private fun getTrackYPosition(): Float { //        return waveVerticalPosition + ((((currentTouchY.toDouble() - startY) / waveVerticalPosition).roundToInt() * waveVerticalPosition).toInt())
        return waveVerticalLongPressTempPosition.apply {
            Log.i(long_press_tag, "index:${index} ondraw waveVerticalPositionLongPress: $this")
        }

    }

    open fun onDraw(canvas: Canvas) {
        var wf = waveform
        val samples = wf?.amplitudes ?: return
        val centerY = getTrackYPosition() // 使用新变量设置垂直位置
        val maxAmplitude = (samples.maxOrNull() ?: 1).toFloat()

        // 设置固定的矩形宽度和间隙宽度
        val fixedBarWidth = 8f // 固定的矩形宽度
        val fixedGapWidth = 8f // 固定的间隙宽度
        val cornerRadius = fixedBarWidth / 2 // 圆角的半径
        val totalWidthNeeded = fixedBarWidth + fixedGapWidth // 每个矩形条加间隙所需的总宽度

        // 计算能够绘制的矩形条数量
        val barsToFit = (waveViewWidth / totalWidthNeeded).toInt()

        // 计算步长，用于在samples中跳过一定数量的样本
        val step = (samples.size / barsToFit).coerceAtLeast(1)

        // 循环遍历并绘制圆角矩形条
        for (i in 0 until barsToFit) { // 计算当前样本索引
            val sampleIndex = i * step // 确保不会越界
            if (sampleIndex >= samples.size) break

            val xPosition = i * totalWidthNeeded + x // 当前矩形的X位置
            val scaledSampleValue = (samples[sampleIndex] / maxAmplitude) * maxWaveHeight // 根据波形的最大振幅来缩放样本值
            val barHeight = scaledSampleValue * 2 // 矩形的高度是波峰到波谷的距离

            // 计算绘制的顶部和底部位置
            val top = centerY - (barHeight / 2)
            val bottom = centerY + (barHeight / 2)

            // 绘制带有圆角的矩形条
            val rectF = RectF(xPosition, top, xPosition + fixedBarWidth, bottom)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mWavePaint)
        }

        rect = Rect((0f + x).toInt(), (centerY - maxWaveHeight).toInt(), ((0f + x) + waveViewWidth).toInt(), (maxWaveHeight + centerY).toInt()).apply {
            Log.i(long_press_tag, "index:${index} draw: $this")
        }

        //画空心rect
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.RED
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        canvas.drawRect(rect!!, rectPaint)

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
    fun refreshCursorValueByComputeScroll(currX: Int) { //cursorValue = startValue + offsetUpTouchX + (currX / unitMsPixel).toLong()
        Log.i(long_press_tag, "index:${index}  refreshCursorValueByComputeScroll cursorValue: ${
            TimeUtil.getDetailTime(cursorValue)
        }")
    }

    /**
     * 触摸滑动时间轴松手惯性滑动
     *
     * distanceX 惯性滑动的距离
     * courseIncrement 惯性滑动的时间
     */
    fun refreshCursorValueByOnScroll(distanceX: Float,
                                     courseIncrement: Long) { //cursorValue += courseIncrement

    }

    /**
     * 长按移动时，水平方向的移动
     */
    fun refreshCursorValueByLongPressHandleHorizontalMove(deltaX: Float) {
        cursorOffsetTime += (deltaX / unitMsPixel).toLong() //时间戳转换成时间
        Log.i(long_press_tag, "index:${index}  refreshCursorValueByLongPressHandleHorizontalMove cursorValue: ${
            TimeUtil.getDetailTime(cursorValue)
        }")
    }


    fun refreshOffsetUpTouchX(oriCursorValue: Long) {
        offsetUpTouchX = cursorValue - oriCursorValue
    }

    fun refreshLongPressCurrentTouchY(currentY: Int) {
        currentTouchY = currentY
        waveVerticalLongPressTempPosition = waveVerticalPosition + ((((currentTouchY.toDouble() - startY) / waveVerticalInterval).roundToInt() * waveVerticalInterval).toInt())
        Log.i(long_press_tag, "index:${index} refreshLongPressCurrentTouchY waveVerticalLongPressTempPosition: $waveVerticalLongPressTempPosition,startY=$startY")
    }

    fun refreshLongPressStartY(startY: Float) {
        Log.i(long_press_tag, "index:${index} refreshLongPressStartY waveVerticalPosition: $waveVerticalPosition,startY=$startY")
        waveVerticalLongPressTempPosition = waveVerticalPosition
        this.startY = startY
    }

    fun onLongPressTouchUpEvent() {
        Log.i(long_press_tag, "index:${index} onLongPressTouchUpEvent waveVerticalPosition: $waveVerticalPosition,waveVerticalLongPressTempPosition=$waveVerticalLongPressTempPosition")
        waveVerticalPosition = waveVerticalLongPressTempPosition
        startY = 0f
        currentTouchY = 0
    }

    fun time2PositionInTimeline(timeStamp: Long): Float {
        return (cursorPosition + (timeStamp - cursorValue) * unitMsPixel)
    }

    fun moveRightByPixel(distance: Float) {
        audioEditorView.moveRightByPixel(distance)
    }

    fun moveRightByTime(time: Long) {
        audioEditorView.moveRightByTime(time)
    }


}