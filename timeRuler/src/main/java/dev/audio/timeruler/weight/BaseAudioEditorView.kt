package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.StringDef
import androidx.core.view.GestureDetectorCompat
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.ffmpeglib.tool.TimeUtil
import dev.audio.timeruler.R
import dev.audio.timeruler.listener.OnScaleChangeListener
import dev.audio.timeruler.utils.SizeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.reflect.KProperty

/**
 * 概念解释
 * 坐标轴：
 * 坐标轴游标：中间的一个竖线
 *
 */
abstract class BaseAudioEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) :
    View(context, attrs), ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener {

    companion object {

        const val TAG = "BMTAEdV"
        const val long_press_tag = "long_press_tag"
        const val touch_tag = "touch_tag"
        const val time_line_tag = "time_line_tag"
        const val cut_tag = "cut_tag"

        /**
         * updateScaleInfo(500ms, 100ms);
         */
        const val MODE_UINT_100_MS = "unit 100 ms"

        /**
         * updateScaleInfo(2.5s, 500ms);
         */
        const val MODE_UINT_500_MS = "unit 500 ms"

        /**
         * updateScaleInfo(5s, 1s);
         */
        const val MODE_UINT_1000_MS = "unit 1000 ms"

        /**
         * updateScaleInfo(10s, 2s);
         */
        const val MODE_UINT_2000_MS = "unit 2000 ms"

        /**
         * updateScaleInfo(15s, 3s);
         */
        const val MODE_UINT_3000_MS = "unit 3000 ms"

        /**
         * updateScaleInfo(30s, 6s);
         */
        const val MODE_UINT_6000_MS = "unit 6000 ms"

        //数组管理
        val MODE_ARRAY = arrayOf(
            MODE_UINT_100_MS,
            MODE_UINT_500_MS,
            MODE_UINT_1000_MS,
            MODE_UINT_2000_MS,
            MODE_UINT_3000_MS,
            MODE_UINT_6000_MS
        )


        private const val VALUE_100_MS: Long = 100
        private const val VALUE_500_MS: Long = 500
        private const val VALUE_1000_MS: Long = 1000
        private const val VALUE_2000_MS: Long = 2000
        private const val VALUE_3000_MS: Long = 3000
        private const val VALUE_6000_MS: Long = 6000
        val VALUE_ARRAY = arrayOf(
            VALUE_100_MS,
            VALUE_500_MS,
            VALUE_1000_MS,
            VALUE_2000_MS,
            VALUE_3000_MS,
            VALUE_6000_MS
        )

        /**
         *
         * 一屏显示多长时间
         *
         * 6个档位
         * 屏幕宽度分成60份
         *
         * 一屏显示的时间 = 一份的时间 * 60
         */
        private const val SCREEN_SECTIONS: Long = 60
        private const val SCREEN_WIDTH_TIME_100_MS_VALUE = VALUE_100_MS * SCREEN_SECTIONS
        private const val SCREEN_WIDTH_TIME_500_MS_VALUE = VALUE_500_MS * SCREEN_SECTIONS
        private const val SCREEN_WIDTH_TIME_1000_MS_VALUE = VALUE_1000_MS * SCREEN_SECTIONS
        private const val SCREEN_WIDTH_TIME_2000_MS_VALUE = VALUE_2000_MS * SCREEN_SECTIONS
        private const val SCREEN_WIDTH_TIME_3000_MS_VALUE = VALUE_3000_MS * SCREEN_SECTIONS
        private const val SCREEN_WIDTH_TIME_6000_MS_VALUE = VALUE_6000_MS * SCREEN_SECTIONS
        val SCREEN_WIDTH_TIME_VALUE_ARRAY = arrayOf(
            SCREEN_WIDTH_TIME_100_MS_VALUE,
            SCREEN_WIDTH_TIME_500_MS_VALUE,
            SCREEN_WIDTH_TIME_1000_MS_VALUE,
            SCREEN_WIDTH_TIME_2000_MS_VALUE,
            SCREEN_WIDTH_TIME_3000_MS_VALUE,
            SCREEN_WIDTH_TIME_6000_MS_VALUE
        )

        /**
         * 默认状态
         */
        const val STATUS_NONE = 0

        /**
         * 按下
         */
        const val STATUS_DOWN = 1

        /**
         * 拖拽滚动
         */
        const val STATUS_SCROLL = STATUS_DOWN + 1

        /**
         * 甩动滚动(惯性)
         */
        const val STATUS_SCROLL_FLING = STATUS_SCROLL + 1

        /**
         * 缩放
         */
        const val STATUS_ZOOM = STATUS_SCROLL_FLING + 1

    }


    @StringDef(
        MODE_UINT_100_MS,
        MODE_UINT_500_MS,
        MODE_UINT_1000_MS,
        MODE_UINT_2000_MS,
        MODE_UINT_3000_MS,
        MODE_UINT_6000_MS
    )
    annotation class Mode

    @JvmField
    @Mode
    protected var mMode = MODE_UINT_1000_MS

    /* 缩放比例*/
    private var mScaleRatio = 1.0f

    /* 关键刻度高度*/
    protected val keyTickHeight: Float

    /* 刻度高度*/
    private var mTickHeight = 0f

    /* 普通刻度线与关键刻度线的比*/
    private val mNormalTickAndKeyTickRatio: Float

    /* 刻度间距*/
    private var mTickSpacing = 0f
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mScalePaint: Paint? = null
    protected var mScaleInfo: ScaleMode? = null

    /*刻度线颜色*/
    val tickValueColor: Int

    /*刻度字体大小*/
    val tickValueSize: Float

    /*每毫秒多少像素*/
    var unitMsPixel: Float = 0f


    open fun cursorPositionPixelChange(prop: KProperty<*>, old: Float, new: Float) {
        invalidate()
    }


    open fun cursorValueChange(prop: KProperty<*>, old: Long, new: Long) {
        invalidate()
    }

    /*一个屏幕宽度最少显示多少毫秒  8s*/
    @JvmField
    protected var minScreenSpanValue: Long = 0

    /*一个屏幕宽度最多显示多少毫秒  80s*/
    private var maxScreenSpanValue: Long = 0

    /*一毫秒最多占多少像素*/
    private var maxUnitPixel = 0f

    /*一毫秒最少占多少像素*/
    private var minUnitPixel = 0f


    /*时间戳*/
    var cursorValue: Long by ObservableProperty(0L) { prop, old, new ->
        println("${prop.name} changed from $old to $new")
        cursorValueChange(prop, old, new)
    }

    /**
     *
     * 游标的绝对位置 x坐标
     */
    var cursorPosition: Float by ObservableProperty(0f) { prop, old, new ->
        println("${prop.name} changed from $old to $new")
        cursorPositionPixelChange(prop, old, new)
    }

    /**
     *
     * 游标相对view width的位置  0～ 1
     */
    private var mCursorPositionProportion = 0.5f

    // 刻度尺横线位置
    private var mBaselinePosition = 0f
    private var mBaselinePositionProportion = 0.01f
    private val tickColor: Int
    private var tickDirectionUp //刻度线方向
            : Boolean
    private val cursorLineColor: Int
    private val showCursorLine: Boolean
    private val showTickValue: Boolean
    private val showTickLine: Boolean
    private var mScroller: Scroller? = null

    /**
     * 状态
     */
    private var status = STATUS_NONE

    /* 刻度值 最大规格*/
    private val maxScaleValueSize: Float

    /*文字偏移量*/
    private val tickValueOffset: Float

    private var mGestureDetectorCompat: GestureDetectorCompat? = null
    private var scrollHappened = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseScaleBar)
        tickValueColor = typedArray.getColor(R.styleable.TimeRulerBar_tickValueColor, Color.WHITE)
        tickValueSize = typedArray.getDimension(
            R.styleable.TimeRulerBar_tickValueSize,
            SizeUtils.sp2px(getContext(), 8f).toFloat()
        )
        keyTickHeight = typedArray.getDimension(
            R.styleable.BaseScaleBar_keyTickHeight,
            SizeUtils.dp2px(getContext(), 10f).toFloat()
        )
        tickValueOffset = typedArray.getDimension(
            R.styleable.BaseScaleBar_tickValueOffset,
            SizeUtils.dp2px(getContext(), -30f).toFloat()
        )
        tickDirectionUp = typedArray.getBoolean(R.styleable.BaseScaleBar_tickDirectionUp, true)
        mNormalTickAndKeyTickRatio =
            typedArray.getFloat(R.styleable.BaseScaleBar_normalTickRatio, 0.67f)
        tickColor = typedArray.getColor(R.styleable.BaseScaleBar_tickColor, Color.BLACK)
        cursorLineColor =
            typedArray.getColor(R.styleable.BaseScaleBar_cursorLineColor, Color.YELLOW)
        showCursorLine = typedArray.getBoolean(R.styleable.BaseScaleBar_showCursorLine, true)
        showTickValue = typedArray.getBoolean(R.styleable.BaseScaleBar_showCursorLine, true)
        showTickLine = typedArray.getBoolean(R.styleable.BaseScaleBar_showTickLine, true)
        maxScaleValueSize = typedArray.getDimension(
            R.styleable.BaseScaleBar_maxScaleValueSize,
            SizeUtils.sp2px(getContext(), 15f).toFloat()
        )
        var position = typedArray.getFloat(R.styleable.BaseScaleBar_cursorPosition, 0.5f)
        mCursorPositionProportion = position
        position = typedArray.getFloat(R.styleable.BaseScaleBar_baselinePosition, 0.67f)
        mBaselinePositionProportion = position
        // 释放
        typedArray.recycle()
        init()
    }


    private fun init() {
        mScalePaint = Paint()
        mScalePaint!!.isAntiAlias = true
        mScalePaint!!.strokeWidth = 1.0f
        mScalePaint!!.isDither = true
        mScalePaint!!.style = Paint.Style.FILL_AND_STROKE
        minScreenSpanValue = SCREEN_WIDTH_TIME_VALUE_ARRAY[0]
        maxScreenSpanValue = SCREEN_WIDTH_TIME_VALUE_ARRAY[SCREEN_WIDTH_TIME_VALUE_ARRAY.size - 1]

        mScaleInfo = ScaleMode()
        mScaleInfo!!.unitValue = 60000
        mScaleInfo!!.keyScaleRange = (5 * 60 * 1000).toLong()
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        mScaleInfo!!.startValue = calendar.timeInMillis

        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        mScaleInfo!!.endValue = calendar.timeInMillis

        cursorValue = mScaleInfo!!.startValue
    }


    class ObservableProperty<T>(
        var value: T,
        val onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

        operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
            val oldValue = value
            value = newValue
            onChange(property, oldValue, newValue)
        }
    }

    protected fun setScaleRatio(
        @FloatRange(
            from = 0.0,
            to = 1.0,
            fromInclusive = false
        ) scaleRatio: Float
    ) {
        mScaleRatio = scaleRatio
    }


    /**
     * 设置 坐标轴 游标的时间值
     */
    fun setCursorTimeValue(cursorValue: Long) {
        this.cursorValue = cursorValue
    }

    /**
     * 获取 坐标轴 游标的时间值
     */
    fun getCursorTimeValue(): Long {
        return cursorValue
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mTickHeight = keyTickHeight * mNormalTickAndKeyTickRatio
        cursorPosition = w * mCursorPositionProportion
        mBaselinePosition = h * mBaselinePositionProportion
        maxUnitPixel = w * 1.0f / minScreenSpanValue
        minUnitPixel = w * 1.0f / maxScreenSpanValue
        unitMsPixel = maxUnitPixel * mScaleRatio
        mTickSpacing = mScaleInfo!!.unitValue * unitMsPixel
    }

    open fun setRange(start: Long, end: Long) {
        if (start >= end) {
            return
        }
        mScaleInfo!!.startValue = start
        mScaleInfo!!.endValue = end
        cursorValue = start
        setMode(mMode, true)
        invalidate()
    }

    fun print(timestamp: Long) {
        // 假设这是你的时间戳
        // 创建一个日期对象
        val date = Date(timestamp)
        // 创建SimpleDateFormat对象，定义日期和时间的格式
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        // 使用SimpleDateFormat对象格式化日期对象
        val formattedDate = sdf.format(date)
        // 打印格式化后的日期和时间
        Log.i("llc_date", "formattedDate:$formattedDate")
    }

    /**
     * @param keyScaleRange 关键刻度值f
     * @param unitValue     每个刻度的值
     */
    protected fun updateScaleInfo(keyScaleRange: Long, unitValue: Long) {
        mScaleInfo!!.keyScaleRange = keyScaleRange
        mScaleInfo!!.unitValue = unitValue
    }

    protected val baselinePosition: Float
        protected get() = mBaselinePosition + 10

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), getHeightSize(
                suggestedMinimumHeight, heightMeasureSpec
            )
        )
    }

    private fun getHeightSize(size: Int, heightMeasureSpec: Int): Int {
        var result = size
        val contentHeight = calcContentHeight(mBaselinePositionProportion)
        val specMode = MeasureSpec.getMode(heightMeasureSpec)
        val specSize = MeasureSpec.getSize(heightMeasureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED -> result = if (size > contentHeight) size else contentHeight
            MeasureSpec.AT_MOST -> result = contentHeight
            MeasureSpec.EXACTLY -> result = specSize
        }
        return result
    }

    /**
     * 计算内容高度
     * todo
     *
     * @param baselinePositionProportion
     * @return
     */
    protected open fun calcContentHeight(baselinePositionProportion: Float): Int {
        var tickValueHeight = 0
        if (showTickValue && null != mTickMarkStrategy) {
            mScalePaint!!.textSize = maxScaleValueSize
            val fontMetrics = mScalePaint!!.fontMetrics
            val ceil = Math.ceil((fontMetrics.bottom - fontMetrics.top).toDouble())
            tickValueHeight = (ceil + tickValueOffset).toInt()
        }
        return ((keyTickHeight + tickValueHeight) / baselinePositionProportion + 0.5f).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        Log.i(long_press_tag, "onDraw cursorValue=${TimeUtil.getDetailTime(cursorValue)}")
        val baselinePosition = baselinePosition
        mScalePaint!!.color = tickColor
        if (showTickLine) {
            canvas.drawLine(
                scrollX.toFloat(),
                baselinePosition,
                (scrollX + width).toFloat(),
                baselinePosition,
                mScalePaint!!
            )
        }
        val leftRange = cursorValue - mScaleInfo!!.startValue
        val leftNeighborOffset = leftRange % mScaleInfo!!.unitValue
        val leftNeighborTickValue = cursorValue - leftNeighborOffset
        val leftNeighborPosition = cursorPosition - leftNeighborOffset * unitMsPixel
        val leftCount = (cursorPosition / mTickSpacing + 0.5f).toInt()
        var onDrawTickPosition: Float
        var onDrawTickValue: Long
        for (i in 0 until leftCount) {
            onDrawTickValue = leftNeighborTickValue - mScaleInfo!!.unitValue * i
            if (onDrawTickValue < mScaleInfo!!.startValue) {
                break
            }
            onDrawTickPosition = leftNeighborPosition - mTickSpacing * i
            if (tickDirectionUp) {
                if ((onDrawTickValue - mScaleInfo!!.startValue) % mScaleInfo!!.keyScaleRange == 0L) {
                    canvas.drawLine(
                        onDrawTickPosition,
                        baselinePosition,
                        onDrawTickPosition,
                        baselinePosition - keyTickHeight,
                        mScalePaint!!
                    )
                    drawTickValue(
                        canvas,
                        onDrawTickPosition,
                        baselinePosition - keyTickHeight,
                        onDrawTickValue,
                        true
                    )
                } else {
                    canvas.drawLine(
                        onDrawTickPosition,
                        baselinePosition - mTickHeight,
                        onDrawTickPosition,
                        baselinePosition,
                        mScalePaint!!
                    )
                    drawTickValue(
                        canvas,
                        onDrawTickPosition,
                        baselinePosition - mTickHeight,
                        onDrawTickValue,
                        false
                    )
                }
            } else {
                if ((onDrawTickValue - mScaleInfo!!.startValue) % mScaleInfo!!.keyScaleRange == 0L) {
                    canvas.drawLine(
                        onDrawTickPosition,
                        baselinePosition,
                        onDrawTickPosition,
                        baselinePosition + keyTickHeight,
                        mScalePaint!!
                    )
                    if ((onDrawTickValue - mScaleInfo!!.startValue) % (mScaleInfo!!.keyScaleRange * 2) == 0L) {
                        drawTickValue(
                            canvas,
                            onDrawTickPosition,
                            baselinePosition - keyTickHeight,
                            onDrawTickValue,
                            true
                        )
                    }
                } else {
                    canvas.drawLine(
                        onDrawTickPosition,
                        baselinePosition,
                        onDrawTickPosition,
                        baselinePosition + mTickHeight,
                        mScalePaint!!
                    )
                    drawTickValue(
                        canvas,
                        onDrawTickPosition,
                        baselinePosition - mTickHeight,
                        onDrawTickValue,
                        false
                    )
                }
            }
        }
        val rightNeighborTickValue = leftNeighborTickValue + mScaleInfo!!.unitValue
        val rightNeighborPosition = leftNeighborPosition + mTickSpacing
        val rightCount = ((width - cursorPosition) / mTickSpacing + 0.5f).toInt()
        for (i in 0 until rightCount) {
            onDrawTickValue = rightNeighborTickValue + mScaleInfo!!.unitValue * i
            if (onDrawTickValue > mScaleInfo!!.endValue) {
                break
            }
            onDrawTickPosition = rightNeighborPosition + mTickSpacing * i
            if (tickDirectionUp) {
                if ((onDrawTickValue - mScaleInfo!!.startValue) % mScaleInfo!!.keyScaleRange == 0L) {
                    canvas.drawLine(
                        onDrawTickPosition,
                        baselinePosition,
                        onDrawTickPosition,
                        baselinePosition - keyTickHeight,
                        mScalePaint!!
                    )
                    drawTickValue(
                        canvas,
                        onDrawTickPosition,
                        baselinePosition - keyTickHeight,
                        onDrawTickValue,
                        true
                    )
                } else {
                    canvas.drawLine(
                        onDrawTickPosition,
                        baselinePosition,
                        onDrawTickPosition,
                        baselinePosition - mTickHeight,
                        mScalePaint!!
                    )
                    drawTickValue(
                        canvas,
                        onDrawTickPosition,
                        baselinePosition - mTickHeight,
                        onDrawTickValue,
                        false
                    )
                }
            } else {
                if ((onDrawTickValue - mScaleInfo!!.startValue) % mScaleInfo!!.keyScaleRange == 0L) {
                    canvas.drawLine(
                        onDrawTickPosition,
                        baselinePosition,
                        onDrawTickPosition,
                        baselinePosition + keyTickHeight,
                        mScalePaint!!
                    )
                    if ((onDrawTickValue - mScaleInfo!!.startValue) % (mScaleInfo!!.keyScaleRange * 2) == 0L) {
                        drawTickValue(
                            canvas,
                            onDrawTickPosition,
                            baselinePosition - keyTickHeight,
                            onDrawTickValue,
                            true
                        )
                    }
                } else {
                    canvas.drawLine(
                        onDrawTickPosition,
                        baselinePosition,
                        onDrawTickPosition,
                        baselinePosition + mTickHeight,
                        mScalePaint!!
                    )
                    drawTickValue(
                        canvas,
                        onDrawTickPosition,
                        baselinePosition - mTickHeight,
                        onDrawTickValue,
                        false
                    )
                }
            }
        }
        drawWaveformSeekBar(canvas)
    }

    protected open fun drawWaveformSeekBar(canvas: Canvas) {}

    protected open fun onEndTickDraw(canvas: Canvas?) {}

    /**
     * 绘制游标
     *
     * @param canvas
     * @param cursorPosition
     */
    protected open fun drawCursor(canvas: Canvas, cursorPosition: Float, cursorValue: Long) {
        if (showCursorLine) {
            mScalePaint!!.color = cursorLineColor
            canvas.drawLine(cursorPosition, 0f, cursorPosition, height.toFloat(), mScalePaint!!)
        }
    }

    /**
     * 绘制刻度线描述
     *
     * @param canvas
     * @param x
     * @param y
     * @param scaleValue
     * @param keyScale
     */
    private fun drawTickValue(
        canvas: Canvas,
        x: Float,
        y: Float,
        scaleValue: Long,
        keyScale: Boolean
    ) {
        if (showTickValue) {
            if (null != mTickMarkStrategy) {
                if (mTickMarkStrategy!!.disPlay(scaleValue, keyScale)) {
                    mScalePaint!!.color = mTickMarkStrategy!!.getColor(scaleValue, keyScale)
                    mScalePaint!!.textAlign = Paint.Align.CENTER
                    var size = mTickMarkStrategy!!.getSize(scaleValue, keyScale, maxScaleValueSize)
                    size = Math.min(maxScaleValueSize, size)
                    mScalePaint!!.textSize = size
                    canvas.drawText(
                        mTickMarkStrategy!!.getScaleValue(scaleValue, keyScale),
                        x,
                        y - tickValueOffset,
                        mScalePaint!!
                    )
                }
            }
        }
    }

    private val scaleGestureDetect: ScaleGestureDetector
        get() {
            if (null == mScaleGestureDetector) {
                mScaleGestureDetector = ScaleGestureDetector(context, this)
            }
            return mScaleGestureDetector!!
        }
    private val gestureDetectorCompat: GestureDetectorCompat
        get() {
            if (null == mGestureDetectorCompat) {
                mGestureDetectorCompat = GestureDetectorCompat(context, this)
            }
            return mGestureDetectorCompat!!
        }
    private val scroller: Scroller
        get() {
            if (null == mScroller) {
                mScroller = Scroller(context)
            }
            return mScroller!!
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetect.onTouchEvent(event)
        gestureDetectorCompat.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_UP -> {
                if (scrollHappened && status != STATUS_SCROLL_FLING) {
                    if (null != mOnCursorListener) {
                        mOnCursorListener!!.onStopTrackingTouch(cursorValue)
                    }
                }
                scrollHappened = false
                if (status == STATUS_DOWN || status == STATUS_SCROLL || status == STATUS_ZOOM) {
                    status = STATUS_NONE
                }
            }
        }
        return true
    }

    open fun onLongPressTouchUpEvent() {
    }


    override fun onScale(detector: ScaleGestureDetector): Boolean {
        var scaleFactor = detector.scaleFactor
        Log.d(TAG, "onScalescaleFactor: $scaleFactor")
        status = STATUS_ZOOM
        unitMsPixel *= scaleFactor
        if (unitMsPixel > maxUnitPixel) {
            unitMsPixel = maxUnitPixel
            scaleFactor = 1.0f
        } else if (unitMsPixel < minUnitPixel) {
            unitMsPixel = minUnitPixel
            scaleFactor = 1.0f
        }
        onScale(mScaleInfo, unitMsPixel)
        mScaleRatio *= scaleFactor
        mTickSpacing = mScaleInfo!!.unitValue * unitMsPixel
        Log.d(TAG, mScaleRatio.toString() + "onScale:mTickSpacing " + mTickSpacing)
        invalidate()
        return unitMsPixel < maxUnitPixel || unitMsPixel > minUnitPixel
    }

    private var lastScale = 0f

    /*0~1000之间*/
    fun setScale(scale: Float) {
        var scaleFactor = 0.9f + scale / 10000
        if (lastScale < scale) {
            scaleFactor = 1 + scale / 10000
        }
        status = STATUS_ZOOM
        unitMsPixel *= scaleFactor
        if (unitMsPixel > maxUnitPixel) {
            unitMsPixel = maxUnitPixel
            scaleFactor = 1.0f
        } else if (unitMsPixel < minUnitPixel) {
            unitMsPixel = minUnitPixel
            scaleFactor = 1.0f
        }
        Log.d(TAG, unitMsPixel.toString() + "onScalescaleFactor: " + scaleFactor)
        if (scale == 1000f) {
            scaleFactor = 1f
            unitMsPixel = (6.0f * Math.pow(10.0, -4.0)).toFloat()
        }
        if (scale == 0f) {
            scaleFactor = 1f
            unitMsPixel = (1.25f * Math.pow(10.0, -5.0)).toFloat()
        }
        onScale(mScaleInfo, unitMsPixel)
        mScaleRatio *= scaleFactor
        mTickSpacing = mScaleInfo!!.unitValue * unitMsPixel
        Log.d(TAG, mScaleRatio.toString() + "onScale:mTickSpacing " + mTickSpacing)
        invalidate()
        lastScale = scale
    }


    private fun onScale(info: ScaleMode?, unitPixel: Float) {
        val width = width
        // 计算一屏刻度值跨度
        val screenSpanValue = width / unitPixel
        updateMode(screenSpanValue)
    }


    private fun updateMode(screenSpanValue: Float) {
        Log.i("TAG", "updateMode: $screenSpanValue")
        if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[5]) {
            setMode(MODE_ARRAY[5], setScaleRatio = false, isRefreshUnitPixel = false)
        } else if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[4]) {
            setMode(MODE_ARRAY[4], setScaleRatio = false, isRefreshUnitPixel = false)
        } else if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[3]) {
            setMode(MODE_ARRAY[3], setScaleRatio = false, isRefreshUnitPixel = false)
        } else if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[2]) {
            setMode(MODE_ARRAY[2], setScaleRatio = false, isRefreshUnitPixel = false)
        } else if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[1]) {
            setMode(MODE_ARRAY[1], setScaleRatio = false, isRefreshUnitPixel = false)
        } else {
            setMode(MODE_ARRAY[0], setScaleRatio = false, isRefreshUnitPixel = false)
        }
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {}
    override fun onDown(e: MotionEvent): Boolean {
        Log.i(touch_tag, "onDown")
        if (status == STATUS_SCROLL_FLING) {
            scroller.forceFinished(true)
        } else {
            scrollHappened = false
        }
        status = STATUS_DOWN
        // 返回出 拦截事件
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        // do nothing
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        // do nothing
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.i(touch_tag, "onScroll")
        if (e2.pointerCount > 1) {
            return false
        }
        if (scaleGestureDetect.isInProgress) {
            return false
        }
        // TODO: 处理第一次触发滚动产生的距离过大,呈现滚动突兀不友好体验问题 ------ 待优化
        if (!scrollHappened) {
            scrollHappened = true
            if (null != mOnCursorListener) {
                mOnCursorListener!!.onStartTrackingTouch(cursorValue)
            }
            return true
        }
        status = STATUS_SCROLL
        // 游标刻度值增量
        val courseIncrement = (distanceX / unitMsPixel).toLong()
        Log.i(TAG, "unitPixel: $unitMsPixel")
        cursorValue += courseIncrement
//        mCursorValue1 += courseIncrement
        refreshCursorValueByOnScroll(distanceX, courseIncrement)
        var result = true
        if (cursorValue < mScaleInfo!!.startValue) {
            cursorValue = mScaleInfo!!.startValue
            result = false
        } else if (cursorValue > mScaleInfo!!.endValue) {
            cursorValue = mScaleInfo!!.endValue
            result = false
        }
        if (null != mOnCursorListener) {
            mOnCursorListener!!.onProgressChanged(cursorValue, true)
        }
        invalidate()
        return result
    }


    open fun onLongPressTrackIndex(e: MotionEvent): Int {
        return 0
    }


    override fun computeScroll() {
        Log.i(touch_tag, "computeScroll")
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            cursorValue = mScaleInfo!!.startValue + (currX / unitMsPixel).toLong()
//            mCursorValue1 =
//                mScaleInfo!!.startValue + offsetOriCurrentValue1 + (currX / unitPixel).toLong()
            refreshCursorValueByComputeScroll(currX)
            if (cursorValue < mScaleInfo!!.startValue) {
                cursorValue = mScaleInfo!!.startValue
            } else if (cursorValue > mScaleInfo!!.endValue) {
                cursorValue = mScaleInfo!!.endValue
            }
            invalidate()
        } else {
            if (status == STATUS_SCROLL_FLING) {
                status = STATUS_NONE
                if (null != mOnCursorListener) {
                    mOnCursorListener!!.onStopTrackingTouch(cursorValue)
                }
            }
        }
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.i(touch_tag, "onFling")
        status = STATUS_SCROLL_FLING
        val startX = ((cursorValue - mScaleInfo!!.startValue) * unitMsPixel).toInt()
        val maX = ((mScaleInfo!!.endValue - mScaleInfo!!.startValue) * unitMsPixel).toInt()
        scroller.fling(
            startX,
            0,
            -velocityX.toInt(),
            0,
            0,
            maX,
            0,
            0
        )
        invalidate()
        return true
    }

    fun setTickDirection(isUP: Boolean) {
        tickDirectionUp = isUP
        invalidate()
    }


    private var mOnCursorListener: OnCursorListener? = null
    fun setOnCursorListener(l: OnCursorListener?) {
        mOnCursorListener = l
    }

    interface OnCursorListener {
        fun onStartTrackingTouch(cursorValue: Long)
        fun onProgressChanged(cursorValue: Long, isFromUser: Boolean)
        fun onStopTrackingTouch(cursorValue: Long)
    }

    protected var scaleChangeListener: OnScaleChangeListener? = null

    fun setOnScaleChangeListener(onScaleChangeListenerListener: OnScaleChangeListener?) {
        this.scaleChangeListener = onScaleChangeListenerListener
    }

    private var mTickMarkStrategy: TickMarkStrategy? = null

    fun setTickMarkStrategy(tickMarkStrategy: TickMarkStrategy?) {
        mTickMarkStrategy = tickMarkStrategy
    }

    fun setScreenSpanValue(screenSpanValue: Long) {
        minScreenSpanValue = screenSpanValue
    }

    fun setMode(@Mode mode: String) {
        setMode(mode, true)
    }

    fun setMode(
        @Mode mode: String,
        setScaleRatio: Boolean,
        isRefreshUnitPixel: Boolean = true
    ) {
        val screeWithDuration: Long
        var index = 0
        when (mode) {
            MODE_ARRAY[0] -> {
                index = 0
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                screeWithDuration = SCREEN_WIDTH_TIME_VALUE_ARRAY[index]
            }

            MODE_ARRAY[1] -> {
                index = 1
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                screeWithDuration = SCREEN_WIDTH_TIME_VALUE_ARRAY[index]
            }

            MODE_ARRAY[2] -> {
                index = 2
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                screeWithDuration = SCREEN_WIDTH_TIME_VALUE_ARRAY[index]
            }

            MODE_ARRAY[3] -> {
                index = 3
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                screeWithDuration = SCREEN_WIDTH_TIME_VALUE_ARRAY[index]
            }

            MODE_ARRAY[4] -> {
                index = 4
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                screeWithDuration = SCREEN_WIDTH_TIME_VALUE_ARRAY[index]
            }

            MODE_ARRAY[5] -> {
                index = 5
                updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
                screeWithDuration = SCREEN_WIDTH_TIME_VALUE_ARRAY[index]
            }

            else -> throw RuntimeException("not support mode: $mode")
        }
        if (isRefreshUnitPixel) {
            //todo  不一定是屏幕宽度
            unitMsPixel = (ScreenUtil.getScreenWidth(context) * 1f / screeWithDuration)
        }
        Log.e(TAG, "unitPixel: $unitMsPixel")
        if (setScaleRatio) {
            setScaleRatio(minScreenSpanValue * 1.0f / screeWithDuration)
        }
        if (mode != mMode) {
            mMode = mode
            scaleChangeListener?.onScaleChange(mMode)
        }
        invalidate()
    }


    private var mColorScale: ColorScale? = null

    fun setColorScale(scale: ColorScale?) {
        mColorScale = scale
    }

    private var drawCursorContent: Boolean = true
    fun setShowCursor(isShowCursorContent: Boolean) {
        drawCursorContent = isShowCursorContent
        invalidate()
    }

    open fun refreshLongPressStartY(startY: Float) {

    }

    open fun refreshCursorValueByComputeScroll(currX: Int) {

    }

    open fun refreshCursorValueByLongPressHandleHorizontalMove(deltaX: Float) {

    }

    open fun refreshCursorValueByOnScroll(distanceX: Float, courseIncrement: Long) {

    }

    open fun refreshOffsetUpTouchX(oriCursorValue: Long) {

    }

    open fun refreshLongPressCurrentTouchY(currentY: Int) {

    }


    /**
     * 裁剪范围向右移动
     *
     * 移动的变量为像素
     */
    fun moveRightByPixel(distance: Float) {
        cursorValue += distance.pixel2Time(unitMsPixel)
        invalidate()
    }


    /**
     * 裁剪范围向右移动
     *
     * 移动的变量为时间
     */
    fun moveRightByTime(time: Long) {
        cursorValue += time
        invalidate()
    }

    interface TickMarkStrategy {
        /**
         * 是否显示刻度值
         *
         * @param scaleValue 刻度值
         * @param keyScale   是否是关键刻度
         * @return
         */
        fun disPlay(scaleValue: Long, keyScale: Boolean): Boolean

        /**
         * 获取显示的刻度值
         *
         * @param scaleValue
         * @param keyScale
         * @return
         */
        fun getScaleValue(scaleValue: Long, keyScale: Boolean): String

        /**
         * 获取当前刻度值显示颜色
         *
         * @param scaleValue
         * @param keyScale
         * @return
         */
        @ColorInt
        fun getColor(scaleValue: Long, keyScale: Boolean): Int

        /**
         * 获取当前刻度值显示大小
         *
         * @param scaleValue
         * @param keyScale
         * @param maxScaleValueSize
         * @return
         */
        @Dimension
        fun getSize(scaleValue: Long, keyScale: Boolean, maxScaleValueSize: Float): Float
    }


}