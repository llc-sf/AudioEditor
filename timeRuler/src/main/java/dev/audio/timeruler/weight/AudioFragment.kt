package dev.audio.timeruler.weight

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.core.content.ContextCompat
import com.android.app.AppProvider
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.ffmpeglib.tool.TimeUtil
import dev.audio.timeruler.BuildConfig
import dev.audio.timeruler.R
import dev.audio.timeruler.weight.BaseAudioEditorView.Companion.long_press_tag
import dev.audio.timeruler.weight.BaseAudioEditorView.Companion.time_line_tag
import dev.audio.timeruler.bean.Ref
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.weight.BaseAudioEditorView.Companion.wave_tag
import kotlin.math.roundToInt

/**
 * 波形片段
 */
open class AudioFragment(var audioEditorView: BaseAudioEditorView) {
    companion object {

        const val DEFAULT_WAVE_HEIGHT = 150f
        const val DEFAULT_WAVE_VERTICAL_POSITION = 200f
        const val DEFAULT_WAVE_COLOR = Color.RED
        const val WAVE_STEP = 20
        const val WAVE_STEP_CONTINUOUS = 400
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
    var endTimestamp: Long = 0
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

    //波形高度的一半
    var maxHalfWaveHeight: Float = DEFAULT_WAVE_HEIGHT

    private var waveStep = WAVE_STEP
    private var waveStepContinuous = WAVE_STEP_CONTINUOUS

    //波形宽度
    private val waveViewWidth
        get() = (duration * unitMsPixel).toInt().apply {
            Log.i(wave_tag, "waveViewWidth width=$this duration:$duration unitMsPixel:$unitMsPixel")
        }

    //每毫秒对应的像素
    val unitMsPixel by Ref { audioEditorView.unitMsPixel }


    //当前指示标的位置（元素）
    private val cursorPosition by Ref { audioEditorView.cursorPosition }

    val screenWithDuration by Ref { audioEditorView.screenWithDuration }

    val baselinePosition by Ref { audioEditorView.baselinePosition }

    /**
     *
     * 当前坐标轴的时间游标同步
     *
     */
    val cursorValue by Ref { audioEditorView.cursorValue }

    val waveStartYInParent by Ref { audioEditorView.waveStartYInParent }


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

    var path: String? = null

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
        this.color = ContextCompat.getColor(AppProvider.context,R.color.color_333333)
        this.style = Paint.Style.FILL
    }


    /**
     * 波形中心Y坐标
     * 多轨道时，获取轨道的Y坐标
     *
     * 裁剪模式下，单独算
     */
    open fun getTrackYPosition(): Float { //        return waveVerticalPosition + ((((currentTouchY.toDouble() - startY) / waveVerticalPosition).roundToInt() * waveVerticalPosition).toInt())
        return waveVerticalLongPressTempPosition.apply {
            Log.i(long_press_tag, "index:${index} ondraw waveVerticalPositionLongPress: $this")
        }
    }

    open fun onDraw(canvas: Canvas) {
        var customWaveHeightScale = 0.8
        val centerY = getTrackYPosition() // 使用新变量设置垂直位置
        waveRect(centerY, canvas)
        val wf = waveform
        val samples = wf?.amplitudes ?: return // 确保最大振幅不为0
        var maxAmplitude = (samples.maxOrNull() ?: 0).toFloat()
        Log.i("llc_wave", "maxAmplitude:$maxAmplitude")
        if (maxAmplitude < 5f) {
            maxAmplitude = 5f // 防止除以0，设置一个默认值
        }

        // 补充的最小振幅值
        val minAmplitudeValue = 0.05f * maxAmplitude // 最小振幅值

        val fixedBarWidth = 8f // 固定的矩形宽度
        val fixedGapWidth = 8f // 固定的间隙宽度
        val cornerRadius = fixedBarWidth / 2 // 圆角的半径
        val totalWidthNeeded = fixedBarWidth + fixedGapWidth // 每个矩形条加间隙所需的总宽度

        val barsToFit = (waveViewWidth / totalWidthNeeded).toInt()
        val step = (samples.size / barsToFit).coerceAtLeast(1)

        // 如果数据点不足，补全最小值
        val paddedSamples = if (samples.size < barsToFit) {
            val newSamples = mutableListOf<Int>()
            val repeats = barsToFit / samples.size
            for (sample in samples) {
                for (i in 0 until repeats) {
                    newSamples.add(sample)
                }
            }
            // 处理余数部分
            val remaining = barsToFit - newSamples.size
            if (remaining > 0) {
                newSamples.addAll(samples.take(remaining))
            }
            newSamples
        } else {
            samples
        }

        // 获取屏幕宽度
        val screenWidth = ScreenUtil.getScreenWidth(audioEditorView.context)
        for (i in 0 until barsToFit) {
            val xPosition = i * totalWidthNeeded + x
            if (xPosition < 0) {
                continue
            }
            val sampleIndex = i * step
            var sampleValue = if (sampleIndex < paddedSamples.size) paddedSamples[sampleIndex] else minAmplitudeValue
            Log.i("llc_wave", "before sampleValue:$sampleValue")
            if (sampleValue == 0) {
                sampleValue = minAmplitudeValue
            }
            Log.i("llc_wave", "after sampleValue:$sampleValue")
            val scaledSampleValue = ((sampleValue.toInt() / maxAmplitude) * maxHalfWaveHeight*customWaveHeightScale).toInt() // 使用安全的除法
            val barHeight = scaledSampleValue * 2

            val top = centerY - (barHeight / 2)
            val bottom = centerY + (barHeight / 2)

            val rectF = RectF(xPosition, top, xPosition + fixedBarWidth, bottom)
            Log.i("llc_wave", "xPosition:$xPosition")
            // 如果 xPosition 超过屏幕宽度，就结束循环
            if (xPosition > screenWidth) {
                break
            }
            if (xPosition > 0 && xPosition < ScreenUtil.getScreenWidth(audioEditorView.context)) {
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mWavePaint)
            }
        }
    }

    open fun onDrawEndDeal(canvas: Canvas) {
        var wf = waveform
        val samples = wf?.amplitudes ?: return
        val centerY = getTrackYPosition()
        val maxAmplitude = (samples.maxOrNull() ?: 1).toFloat()

        val fixedBarWidth = 8f
        val fixedGapWidth = 8f
        val cornerRadius = fixedBarWidth / 2
        val totalWidthNeeded = fixedBarWidth + fixedGapWidth

        // 调整最后一个矩形的间隔以填充整个宽度
        val adjustedWaveViewWidth = waveViewWidth - fixedGapWidth // 确保最后一个矩形条后没有多余的间隔
        val barsToFit = (adjustedWaveViewWidth / totalWidthNeeded).toInt()
        val actualWidthUsed = barsToFit * totalWidthNeeded
        val remainingWidth = adjustedWaveViewWidth - actualWidthUsed
        val additionalGap = if (barsToFit > 0) remainingWidth / barsToFit else 0f

        val step = (samples.size.toFloat() / (barsToFit + 1)).toInt().coerceAtLeast(1)

        for (i in 0 until barsToFit) {
            val sampleIndex = i * step
            if (sampleIndex >= samples.size) break

            val scaledSampleValue = (samples[sampleIndex] / maxAmplitude) * maxHalfWaveHeight
            val barHeight = scaledSampleValue * 2

            val xPosition = i * (totalWidthNeeded + additionalGap) + x
            val top = centerY - (barHeight / 2)
            val bottom = centerY + (barHeight / 2)

            val rectF = RectF(xPosition, top, xPosition + fixedBarWidth, bottom)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mWavePaint)
        }

        waveRect(centerY, canvas)

    }

    private fun waveRect(centerY: Float, canvas: Canvas) {
        rect = Rect((0f + x).toInt(), (centerY - maxHalfWaveHeight).toInt(), ((0f + x) + waveViewWidth).toInt(), (maxHalfWaveHeight + centerY).toInt()).apply {
            Log.i(long_press_tag, "index:${index} draw: $this")
        }

        //画空心rect
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.RED
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        if (BuildConfig.DEBUG) {
            canvas.drawRect(rect!!, rectPaint)
        }
    }


    open fun onDrawContinuous(canvas: Canvas) {
        var wf = waveform
        val samples = wf?.amplitudes ?: return
        val centerY = getTrackYPosition() // 使用新变量设置垂直位置 //            val centerY = waveVerticalPosition // 使用新变量设置垂直位置
        val maxAmplitude = (samples.maxOrNull() ?: 1).toFloat()

        // 使用 maxWaveHeight 变量来控制波形高度
        val amplitudeScale = maxHalfWaveHeight

        val path = Path()
        val upperPoints = mutableListOf<Pair<Float, Float>>()

        Log.i(time_line_tag, "drawWave index=$index offsetCursorValue = ${cursorValue - cursorValue}")

        for (i in samples.indices step waveStepContinuous) { // 步长设置为400，可根据需要调整
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
        rect = Rect((0f + x).toInt(), (centerY - maxHalfWaveHeight).toInt(), ((0f + x) + waveViewWidth).toInt(), (maxHalfWaveHeight + centerY).toInt()).apply {
            Log.i(long_press_tag, "index:${index} draw: $this")
        } //画空心rect
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.RED
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        canvas.drawRect(rect!!, rectPaint)
        Log.i(time_line_tag, "timeline drawWave index=$index cursorValueTotal:${
            TimeUtil.getDetailTime(cursorValue)
        },cursorValue:${TimeUtil.getDetailTime(cursorValue)},startValue:${
            TimeUtil.getDetailTime(startTimestamp)
        }")
        Log.i(time_line_tag, "timeline drawWave index=$index currentTime:${cursorValue - startTimestamp} [${startTimestamp},${endTimestamp}]")
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

    fun moveStartByPixel(distance: Float) {
        audioEditorView.moveStartByPixel(distance)
    }

    fun moveRightByTime(time: Long) {
        audioEditorView.moveRightByTime(time)
    }


    fun moveWave(stepTimeValue: Long) {
        var tempCursorValue = cursorValue - stepTimeValue
        if (tempCursorValue <= startTimestamp) {
            audioEditorView.cursorValue = startTimestamp
            return
        }
        if (tempCursorValue + screenWithDuration >= endTimestamp) {
            audioEditorView.cursorValue = endTimestamp - screenWithDuration
            return
        }
        audioEditorView.cursorValue = tempCursorValue
    }

}