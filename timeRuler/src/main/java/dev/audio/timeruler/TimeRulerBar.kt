package dev.audio.timeruler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import dev.audio.timeruler.BaseScaleBar.TickMarkStrategy
import dev.audio.timeruler.bean.AudioFragment
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.utils.SizeUtils
import java.text.SimpleDateFormat
import kotlin.reflect.KProperty

open class TimeRulerBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseScaleBar(context, attrs), TickMarkStrategy {
    private var mTickPaint: Paint? = null
    private var mColorCursorPaint: Paint? = null
    private val mTriangleHeight = 10f
    private val tickValueColor: Int
    private val tickValueSize: Float
    private val cursorBackgroundColor: Int
    private val cursorValueSize: Float
    private val colorScaleBackground: Int
    private val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
    private var tickValueBoundOffsetH = 20f
    private val videoAreaHeight: Float
    private var videoAreaOffset: Float
    private var drawCursorContent: Boolean


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeRulerBar)
        videoAreaHeight = typedArray.getDimension(
            R.styleable.TimeRulerBar_videoAreaHeight,
            SizeUtils.sp2px(getContext(), 20f).toFloat()
        )
        videoAreaOffset = typedArray.getDimension(
            R.styleable.TimeRulerBar_videoAreaOffset,
            SizeUtils.sp2px(getContext(), 0f).toFloat()
        )
        tickValueColor = typedArray.getColor(R.styleable.TimeRulerBar_tickValueColor, Color.BLACK)
        tickValueSize = typedArray.getDimension(
            R.styleable.TimeRulerBar_tickValueSize,
            SizeUtils.sp2px(getContext(), 8f).toFloat()
        )
        cursorBackgroundColor =
            typedArray.getColor(R.styleable.TimeRulerBar_cursorBackgroundColor, Color.RED)
        cursorValueSize = typedArray.getDimension(
            R.styleable.TimeRulerBar_cursorValueSize,
            SizeUtils.sp2px(getContext(), 10f).toFloat()
        )
        colorScaleBackground =
            typedArray.getColor(R.styleable.TimeRulerBar_colorScaleBackground, Color.WHITE)
        drawCursorContent = typedArray.getBoolean(R.styleable.TimeRulerBar_drawCursorContent, true)
        typedArray.recycle()
        init()
    }

    private fun init() {
        tickValueBoundOffsetH = SizeUtils.dp2px(context, 6f).toFloat()
        mTickPaint = Paint()
        mTickPaint!!.color = tickValueColor
        mTickPaint!!.isAntiAlias = true
        mTickPaint!!.style = Paint.Style.FILL_AND_STROKE
        mTickPaint!!.textAlign = Paint.Align.CENTER
        mTickPaint!!.textSize = tickValueSize
        mTickPaint!!.strokeWidth = 1f
        mTickPaint!!.isDither = true
        mColorCursorPaint = Paint()
        mColorCursorPaint!!.style = Paint.Style.FILL_AND_STROKE
        mColorCursorPaint!!.isDither = true
        setTickMarkStrategy(this)
    }


    // 设置波形数据的方法
    fun setWaveform(waveform: Waveform) {
        audioFragments.add(AudioFragment().apply {
            index = 0
            duration = 1000 * 60 * 2
            maxWaveHeight = 50f
            waveVerticalPosition = 200f
            color = Color.RED
            cursorPosition = mCursorPosition
            startValue = mScaleInfo?.startValue ?: 0
            this.unitMsPixel = unitPixel
            this.waveform = waveform
            cursorValue = mCursorValue
        })
        audioFragments.add(AudioFragment().apply {
            index = 1
            duration = 1000 * 60 * 2
            maxWaveHeight = 50f
            waveVerticalPosition = 400f
            color = Color.RED
            cursorPosition = mCursorPosition
            startValue = mScaleInfo?.startValue ?: 0
            this.unitMsPixel = unitPixel
            this.waveform = waveform
            cursorValue = mCursorValue
        })
        audioFragments.add(AudioFragment().apply {
            index = 2
            duration = 1000 * 60 * 2
            maxWaveHeight = 50f
            waveVerticalPosition = 600f
            color = Color.RED
            cursorPosition = mCursorPosition
            startValue = mScaleInfo?.startValue ?: 0
            this.unitMsPixel = unitPixel
            this.waveform = waveform
            cursorValue = mCursorValue
        })
        invalidate() // 触发重新绘制
    }


    private var audioFragments = mutableListOf<AudioFragment>()

    override fun drawWaveformSeekBar(canvas: Canvas) {
        super.drawWaveformSeekBar(canvas)
        audioFragments.forEach { audioFragment ->
            if (draw(audioFragment, canvas)) return
        }
    }

    private fun draw(
        audioFragment: AudioFragment,
        canvas: Canvas
    ): Boolean {
        return audioFragment.draw(canvas)
    }


    fun setMode(@Mode mode: String) {
        setMode(mode, true)
    }

    override fun setRange(start: Long, end: Long) {
        super.setRange(start, end)
        setMode(mMode, true)
    }

    fun setScreenSpanValue(screenSpanValue: Long) {
        minScreenSpanValue = screenSpanValue
    }

    /**
     * 1、手动直接设置
     * 2、放大缩小设置
     * @param m
     * @param setScaleRatio
     */
    private fun setMode(
        @Mode m: String,
        setScaleRatio: Boolean,
        isRefreshUnitPixel: Boolean = true
    ) {
        val spanValue: Long
        var index = 0
        when (m) {
            MODE_ARRAY[0] -> {
                index = 0
                mMode = m
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                spanValue = MODE_UINT_VALUE_ARRAY[index]
            }

            MODE_ARRAY[1] -> {
                mMode = m
                index = 1
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                spanValue = MODE_UINT_VALUE_ARRAY[1]
            }

            MODE_ARRAY[2] -> {
                mMode = m
                index = 2
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                spanValue = MODE_UINT_VALUE_ARRAY[index]
            }

            MODE_ARRAY[3] -> {
                mMode = m
                index = 3
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                spanValue = MODE_UINT_VALUE_ARRAY[index]
            }

            MODE_ARRAY[4] -> {
                mMode = m
                index = 4
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                spanValue = MODE_UINT_VALUE_ARRAY[index]
            }

            MODE_ARRAY[5] -> {
                mMode = m
                index = 5
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                spanValue = MODE_UINT_VALUE_ARRAY[index]
            }

            else -> throw RuntimeException("not support mode: $m")
        }
        if (isRefreshUnitPixel) {
            unitPixel = (width * 1f / spanValue)
        }
        Log.e("TAG", "unitPixel: ${unitPixel}")
        if (setScaleRatio) {
            setScaleRatio(minScreenSpanValue * 1.0f / spanValue)
        }
        invalidate()
    }

    override fun unitPixelChange(prop: KProperty<*>, old: Float, new: Float) {
        super.unitPixelChange(prop, old, new)
        audioFragments.forEach {
            it.unitMsPixel = new
        }
    }

    override fun cursorPositionPixelChange(prop: KProperty<*>, old: Float, new: Float) {
        super.cursorPositionPixelChange(prop, old, new)
        audioFragments.forEach {
            it.cursorPosition = new
            invalidate()
        }
    }

    override fun disPlay(scaleValue: Long, keyScale: Boolean): Boolean {
        return keyScale
    }

    override fun getScaleValue(scaleValue: Long, keyScale: Boolean): String {
        val formattedTime = simpleDateFormat.format(scaleValue)
        // 解析天、小时、分钟和秒
        val parts = formattedTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val seconds = parts[2].toInt()
        // 转换为秒
        return (hours * 3600 + minutes * 60 + seconds).toString() + "s"
    }

    override fun getColor(scaleValue: Long, keyScale: Boolean): Int {
        return tickValueColor
    }

    override fun getSize(scaleValue: Long, keyScale: Boolean, maxScaleValueSize: Float): Float {
        return tickValueSize
    }

    override fun onEndTickDraw(canvas: Canvas?) {
        val startLimit = scrollX
        val endLimit = scrollX + width
        val startY = videoAreaOffset
        val endY = videoAreaHeight + videoAreaOffset
        // ① 绘制背景
        mColorCursorPaint!!.color = colorScaleBackground
        canvas!!.drawRect(
            startLimit.toFloat(),
            startY,
            endLimit.toFloat(),
            endY,
            mColorCursorPaint!!
        )
        //  绘制颜色刻度尺
        if (null != mColorScale) {
            val size = mColorScale!!.size
            val rect = RectF()
            rect.top = startY
            rect.bottom = endY
            var startValue: Long
            var endValue: Long
            var startPiexl: Float
            var endPiexl: Float
            // ② 绘制颜色刻度
            for (i in 0 until size) {
                startValue = mColorScale!!.getStart(i)
                endValue = mColorScale!!.getEnd(i)
                startPiexl = mCursorPosition + (startValue - cursorValue) * unitPixel
                endPiexl = mCursorPosition + (endValue - cursorValue) * unitPixel
                if (endPiexl < startLimit) {
                    continue
                }
                if (startPiexl > endLimit) {
                    continue
                }
                rect.left = startPiexl
                rect.right = endPiexl
                mColorCursorPaint!!.color = mColorScale!!.getColor(i)
                canvas.drawRect(rect, mColorCursorPaint!!)
            }
        }
    }

    override fun calcContentHeight(baselinePositionProportion: Float): Int {
        val contentHeight = super.calcContentHeight(baselinePositionProportion)
        mColorCursorPaint!!.textSize = cursorValueSize
        val fontMetrics = mColorCursorPaint!!.fontMetrics
        val ceil = Math.ceil((fontMetrics.bottom - fontMetrics.top).toDouble())
        val cursorValueHeight = (ceil + mTriangleHeight + tickValueBoundOffsetH).toInt() + 5
        val cursorContentHeight =
            ((keyTickHeight + cursorValueHeight) / baselinePositionProportion + 0.5f).toInt()
        return Math.max(contentHeight, cursorContentHeight)
    }

    override fun onScale(info: ScaleMode?, unitPixel: Float) {
        val width = width
        // 计算一屏刻度值跨度
        val screenSpanValue = width / unitPixel
        updateMode(screenSpanValue)
    }

    fun setShowCursor(isShowCursorContent: Boolean) {
        drawCursorContent = isShowCursorContent
        invalidate()
    }

    fun setVideoAreaOffset(progress: Int) {
        videoAreaOffset = progress.toFloat()
        invalidate()
    }

    private fun updateMode(screenSpanValue: Float) {
        Log.i("TAG", "updateMode: $screenSpanValue")
        if (screenSpanValue >= MODE_UINT_VALUE_ARRAY[5]) {
            setMode(MODE_ARRAY[5], setScaleRatio = false, isRefreshUnitPixel = false)
        } else if (screenSpanValue >= MODE_UINT_VALUE_ARRAY[4]) {
            setMode(MODE_ARRAY[4], setScaleRatio = false, isRefreshUnitPixel = false)
        } else if (screenSpanValue >= MODE_UINT_VALUE_ARRAY[3]) {
            setMode(MODE_ARRAY[3], setScaleRatio = false, isRefreshUnitPixel = false)
        } else if (screenSpanValue >= MODE_UINT_VALUE_ARRAY[2]) {
            setMode(MODE_ARRAY[2], setScaleRatio = false, isRefreshUnitPixel = false)
        } else if (screenSpanValue >= MODE_UINT_VALUE_ARRAY[1]) {
            setMode(MODE_ARRAY[1], setScaleRatio = false, isRefreshUnitPixel = false)
        } else {
            setMode(MODE_ARRAY[0], setScaleRatio = false, isRefreshUnitPixel = false)
        }
    }

    var cursorDateFormat = SimpleDateFormat("HH:mm:ss")

    /*可自行绘制浮标*/
    override fun drawCursor(canvas: Canvas, cursorPosition: Float, cursorValue: Long) {
        super.drawCursor(canvas, cursorPosition, cursorValue)
        if (!drawCursorContent) return
        val keyTickHeight = keyTickHeight
        val baselinePosition = baselinePosition
        // ①绘制倒三角
        val path = Path()
        val statY = baselinePosition - keyTickHeight
        // 倒三角形顶边的 y
        val topSidePosition = statY - mTriangleHeight
        path.moveTo(cursorPosition, statY)
        path.lineTo(cursorPosition - 3.5f, topSidePosition)
        path.lineTo(cursorPosition + 3.5f, topSidePosition)
        path.close()
        mTickPaint!!.color = cursorBackgroundColor
        canvas.drawPath(path, mTickPaint!!)
        val content = cursorDateFormat.format(cursorValue)
        val textBound = Rect()
        mTickPaint!!.textSize = cursorValueSize
        // 测量内容大小
        mTickPaint!!.getTextBounds(content, 0, content.length, textBound)

        // ②绘制内容背景
        // 创建包裹内容的背景大小
        val rectF = RectF(
            0f,
            0f,
            (textBound.width() + 20).toFloat(),
            textBound.height() + tickValueBoundOffsetH
        )
        // 背景位置
        // x方向： 关于游标居中  y方向:在倒三角形上边
        rectF.offset(cursorPosition - rectF.width() * 0.5f, topSidePosition + 0.5f - rectF.height())
        val rx = rectF.width() * 0.5f
        mTickPaint!!.color = cursorBackgroundColor
        canvas.drawRoundRect(rectF, rx, rx, mTickPaint!!)
        mTickPaint!!.color = tickValueColor
        // ③ 绘制内容
        // 使内容绘制在背景内,达到包裹效果
        val textY = rectF.centerY() + textBound.height() * 0.5f
        canvas.drawText(content, cursorPosition, textY, mTickPaint!!)
    }

    private var mColorScale: ColorScale? = null


    fun setColorScale(scale: ColorScale?) {
        mColorScale = scale
    }

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


    override fun onLongPressTrackIndex(y: Int): Int {
        //view 在屏幕上的y坐标
        audioFragments.forEachIndexed { index, audioFragment ->
            if (y > audioFragment.rect?.top ?: 0 && y < audioFragment.rect?.bottom ?: 0) {
                return index.apply {
                    Log.i(long_press_tag, "TimeRulerBar onLongPressTrackIndex touchy=$y,index=$this,rect=${audioFragment.rect}")
                }
            }
        }
        return 0.apply {
            Log.i(long_press_tag, "TimeRulerBar onLongPressTrackIndex touchy=$y,index=$this")
        }
    }

    override fun refreshLongPressStartY(startY: Float) {
        Log.i(long_press_tag, "TimeRulerBar refreshLongPressStartY startY=$startY,longTouchIndex=$longTouchIndex")
        audioFragments[longTouchIndex]?.let {
            it.refreshLongPressStartY(startY)
        }
    }

    override fun refreshLongPressCurrentTouchY(currentY: Int) {
        audioFragments[longTouchIndex]?.let {
            it.refreshLongPressCurrentTouchY(currentY)
        }
    }

    override fun refreshCursorValueByComputeScroll(currX: Int) {
        audioFragments.forEach {
            it.refreshCursorValueByComputeScroll(currX)
        }
    }

    override fun refreshCursorValueByLongPressHandleHorizontalMove(deltaX: Float) {
        Log.i(long_press_tag, "TimeRulerBar refreshCursorValueByLongPressHandleHorizontalMove: $deltaX")
        audioFragments[longTouchIndex]?.let {
            it.refreshCursorValueByHandleHorizontalMove(deltaX)
        }
    }

    override fun refreshCursorValueByOnScroll(courseIncrement: Long) {
        audioFragments.forEach {
            it.refreshCursorValueByOnScroll(courseIncrement)
        }
    }

    override fun refreshOffsetUpTouchX(oriCursorValue: Long) {
        audioFragments[longTouchIndex]?.let {
            it.refreshOffsetUpTouchX(oriCursorValue)
        }
    }

    override fun onLongPressTouchUpEvent() {
        super.onLongPressTouchUpEvent()
        audioFragments[longTouchIndex]?.let {
            it.onLongPressTouchUpEvent()
        }
    }

}