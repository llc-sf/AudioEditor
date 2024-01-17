package dev.audio.timeruler

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
import dev.audio.timeruler.utils.SizeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.reflect.KProperty

open class BaseScaleBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs), ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener {

    companion object {

        private const val TAG = "BaseScaleBar"
        const val long_press_tag = "long_press_tag"
        const val touch_tag = "touch_tag"

        /**
         * updateScaleInfo(500ms, 100ms);
         */
        private const val MODE_UINT_100_MS = "unit 100 ms"

        /**
         * updateScaleInfo(2.5s, 500ms);
         */
        private const val MODE_UINT_500_MS = "unit 500 ms"

        /**
         * updateScaleInfo(5s, 1s);
         */
        private const val MODE_UINT_1000_MS = "unit 1000 ms"

        /**
         * updateScaleInfo(10s, 2s);
         */
        private const val MODE_UINT_2000_MS = "unit 2000 ms"

        /**
         * updateScaleInfo(15s, 3s);
         */
        private const val MODE_UINT_3000_MS = "unit 3000 ms"

        /**
         * updateScaleInfo(30s, 6s);
         */
        private const val MODE_UINT_6000_MS = "unit 6000 ms"

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
         * 6个档位
         * 屏幕宽度分成60份
         * 控制刻度尺的密度 一个刻度多少像素 值越大刻度越密集
         *
         * 一屏显示多长时间
         */
        const val SCALE: Long = 60
        private const val MODE_UINT_100_MS_VALUE = VALUE_100_MS * SCALE
        private const val MODE_UINT_500_MS_VALUE = VALUE_500_MS * SCALE
        private const val MODE_UINT_1000_MS_VALUE = VALUE_1000_MS * SCALE
        private const val MODE_UINT_2000_MS_VALUE = VALUE_2000_MS * SCALE
        private const val MODE_UINT_3000_MS_VALUE = VALUE_3000_MS * SCALE
        private const val MODE_UINT_6000_MS_VALUE = VALUE_6000_MS * SCALE
        val MODE_UINT_VALUE_ARRAY = arrayOf(
            MODE_UINT_100_MS_VALUE,
            MODE_UINT_500_MS_VALUE,
            MODE_UINT_1000_MS_VALUE,
            MODE_UINT_2000_MS_VALUE,
            MODE_UINT_3000_MS_VALUE,
            MODE_UINT_6000_MS_VALUE
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

    /*每毫秒多少像素*/
    protected var unitPixel: Float by ObservableProperty(0f) { prop, old, new ->
        println("${prop.name} changed from $old to $new")
        unitPixelChange(prop, old, new)
        // 这里可以添加更多的变化监听逻辑
    }


    open fun unitPixelChange(prop: KProperty<*>, old: Float, new: Float) {

    }

    open fun cursorPositionPixelChange(prop: KProperty<*>, old: Float, new: Float) {

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
    protected var mCursorValue: Long = 0
        set(value) {
            field = value
        }
    private var mGestureDetectorCompat: GestureDetectorCompat? = null
    private var scrollHappened = false

    //    protected var mCursorPosition = 0f
//        set(value) {
//            field = value
//        }
    /*每毫秒多少像素*/
    protected var mCursorPosition: Float by ObservableProperty(0f) { prop, old, new ->
        println("${prop.name} changed from $old to $new")
        cursorPositionPixelChange(prop, old, new)
        // 这里可以添加更多的变化监听逻辑
    }
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
    private fun init() {
        mScalePaint = Paint()
        mScalePaint!!.isAntiAlias = true
        mScalePaint!!.strokeWidth = 1.0f
        mScalePaint!!.isDither = true
        mScalePaint!!.style = Paint.Style.FILL_AND_STROKE
        minScreenSpanValue = MODE_UINT_VALUE_ARRAY[0]
        maxScreenSpanValue = MODE_UINT_VALUE_ARRAY[MODE_UINT_VALUE_ARRAY.size - 1]

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

        mCursorValue = mScaleInfo!!.startValue
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

    var cursorValue: Long
        get() = mCursorValue
        set(cursorValue) {
//        print(cursorValue);
//        print(mScaleInfo.startValue);
//        print(mScaleInfo.endValue);
            if (status != STATUS_NONE) {
                return
            }
            if (cursorValue < mScaleInfo!!.startValue || cursorValue > mScaleInfo!!.endValue) {
                //mScaleInfo.startValue 转 年月日 时分秒
                return
            }
            mCursorValue = cursorValue
            if (mOnCursorListener != null) mOnCursorListener!!.onProgressChanged(
                mCursorValue,
                false
            )
            invalidate()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mTickHeight = keyTickHeight * mNormalTickAndKeyTickRatio
        mCursorPosition = w * mCursorPositionProportion
        mBaselinePosition = h * mBaselinePositionProportion
        maxUnitPixel = w * 1.0f / minScreenSpanValue
        minUnitPixel = w * 1.0f / maxScreenSpanValue
        unitPixel = maxUnitPixel * mScaleRatio
        mTickSpacing = mScaleInfo!!.unitValue * unitPixel
    }

    open fun setRange(start: Long, end: Long) {
        if (start >= end) {
            return
        }
        mScaleInfo!!.startValue = start
        mScaleInfo!!.endValue = end
        mCursorValue = start
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
        val leftRange = mCursorValue - mScaleInfo!!.startValue
        val leftNeighborOffest = leftRange % mScaleInfo!!.unitValue
        val leftNeighborTickValue = mCursorValue - leftNeighborOffest
        val leftNeighborPosition = mCursorPosition - leftNeighborOffest * unitPixel
        val leftCount = (mCursorPosition / mTickSpacing + 0.5f).toInt()
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
        val rightCount = ((width - mCursorPosition) / mTickSpacing + 0.5f).toInt()
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
//        onEndTickDraw(canvas)
        drawCursor(canvas, mCursorPosition, mCursorValue)

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
                        mOnCursorListener!!.onStopTrackingTouch(mCursorValue)
                    }
                }
                scrollHappened = false
                if (status == STATUS_DOWN || status == STATUS_SCROLL || status == STATUS_ZOOM) {
                    status = STATUS_NONE
                }
            }
        }
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (onLongPress) {
                    onLongPressTouchUpEvent()
                }
            }
        }

        if (onLongPress) {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    // 计算偏移量
                    val deltaX = event.x - longPressStartTouchX
                    longPressStartTouchX = event.x
//                    currentY1 = event.y.toInt()
                    refreshLongPressCurrentTouchY(event.y.toInt())
                    // 使用偏移量进行你的操作
                    handleLongPressHorizontalMovement(deltaX)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onLongPress = false
//                    offsetOriCurrentValue1 = mCursorValue1 - mCursorValue
                    refreshOffsetUpTouchX(mCursorValue)
                }                 // 结束长按状态

            }
        }

        return true
    }

    open fun onLongPressTouchUpEvent() {
    }

    private fun handleLongPressHorizontalMovement(deltaX: Float) {
        Log.i(touch_tag, "unitPixel=$unitPixel")
        try {
            if (unitPixel != 0f) {
//                mCursorValue1 -= (deltaX / unitPixel).toLong()
                refreshCursorValueByLongPressHandleHorizontalMove(deltaX)
                invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        var scaleFactor = detector.scaleFactor
        Log.d(TAG, "onScalescaleFactor: $scaleFactor")
        status = STATUS_ZOOM
        unitPixel *= scaleFactor
        if (unitPixel > maxUnitPixel) {
            unitPixel = maxUnitPixel
            scaleFactor = 1.0f
        } else if (unitPixel < minUnitPixel) {
            unitPixel = minUnitPixel
            scaleFactor = 1.0f
        }
        onScale(mScaleInfo, unitPixel)
        mScaleRatio *= scaleFactor
        mTickSpacing = mScaleInfo!!.unitValue * unitPixel
        Log.d(TAG, mScaleRatio.toString() + "onScale:mTickSpacing " + mTickSpacing)
        invalidate()
        return unitPixel < maxUnitPixel || unitPixel > minUnitPixel
    }

    private var lastScale = 0f

    /*0~1000之间*/
    fun setScale(scale: Float) {
        var scaleFactor = 0.9f + scale / 10000
        if (lastScale < scale) {
            scaleFactor = 1 + scale / 10000
        }
        status = STATUS_ZOOM
        unitPixel *= scaleFactor
        if (unitPixel > maxUnitPixel) {
            unitPixel = maxUnitPixel
            scaleFactor = 1.0f
        } else if (unitPixel < minUnitPixel) {
            unitPixel = minUnitPixel
            scaleFactor = 1.0f
        }
        Log.d(TAG, unitPixel.toString() + "onScalescaleFactor: " + scaleFactor)
        if (scale == 1000f) {
            scaleFactor = 1f
            unitPixel = (6.0f * Math.pow(10.0, -4.0)).toFloat()
        }
        if (scale == 0f) {
            scaleFactor = 1f
            unitPixel = (1.25f * Math.pow(10.0, -5.0)).toFloat()
        }
        onScale(mScaleInfo, unitPixel)
        mScaleRatio *= scaleFactor
        mTickSpacing = mScaleInfo!!.unitValue * unitPixel
        Log.d(TAG, mScaleRatio.toString() + "onScale:mTickSpacing " + mTickSpacing)
        invalidate()
        lastScale = scale
    }

    protected open fun onScale(info: ScaleMode?, unitPixel: Float) {}
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
                mOnCursorListener!!.onStartTrackingTouch(mCursorValue)
            }
            return true
        }
        status = STATUS_SCROLL
        // 游标刻度值增量
        val courseIncrement = (distanceX / unitPixel).toLong()
        Log.i(TAG, "unitPixel: $unitPixel")
        mCursorValue += courseIncrement
//        mCursorValue1 += courseIncrement
        refreshCursorValueByOnScroll(courseIncrement)
        var result = true
        if (mCursorValue < mScaleInfo!!.startValue) {
            mCursorValue = mScaleInfo!!.startValue
            result = false
        } else if (mCursorValue > mScaleInfo!!.endValue) {
            mCursorValue = mScaleInfo!!.endValue
            result = false
        }
        if (null != mOnCursorListener) {
            mOnCursorListener!!.onProgressChanged(mCursorValue, true)
        }
        invalidate()
        return result
    }


    //**************************** 长按处理 ****************************
    //是否是长按
    var onLongPress = false

    // 长按水平方向上的初始位置
    private var longPressStartTouchX = 0f

    //长按命中的是哪一个轨道
    var longTouchIndex = 0

    //**************************** 长按处理 ****************************
    override fun onLongPress(e: MotionEvent) {
        // do nothing
        Log.i(long_press_tag, "onLongPress")
        onLongPress = true
        //确定长按命中的轨道
        longTouchIndex = onLongPressTrackIndex(e.y.toInt())
        // 记录长按横坐标的初始位置
        longPressStartTouchX = e.x
        // 记录长按竖坐标的初始位置
        refreshLongPressStartY(e.y)

    }

    open fun onLongPressTrackIndex(y: Int): Int {
        return 0
    }


    override fun computeScroll() {
        Log.i(touch_tag, "computeScroll")
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            mCursorValue = mScaleInfo!!.startValue + (currX / unitPixel).toLong()
//            mCursorValue1 =
//                mScaleInfo!!.startValue + offsetOriCurrentValue1 + (currX / unitPixel).toLong()
            refreshCursorValueByComputeScroll(currX)
            if (mCursorValue < mScaleInfo!!.startValue) {
                mCursorValue = mScaleInfo!!.startValue
            } else if (mCursorValue > mScaleInfo!!.endValue) {
                mCursorValue = mScaleInfo!!.endValue
            }
            invalidate()
        } else {
            if (status == STATUS_SCROLL_FLING) {
                status = STATUS_NONE
                if (null != mOnCursorListener) {
                    mOnCursorListener!!.onStopTrackingTouch(mCursorValue)
                }
            }
        }
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.i(touch_tag, "onFling")
        status = STATUS_SCROLL_FLING
        val startX = ((mCursorValue - mScaleInfo!!.startValue) * unitPixel).toInt()
        val maX = ((mScaleInfo!!.endValue - mScaleInfo!!.startValue) * unitPixel).toInt()
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

    protected inner class ScaleMode {
        var startValue: Long = 0
        var endValue: Long = 0

        /**
         * 间隔多少毫秒是一个刻度（普通）
         */
        var unitValue: Long = 0

        /**
         * 间隔多少毫秒是一个关键刻度
         */
        var keyScaleRange: Long = 0
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

    private var mTickMarkStrategy: TickMarkStrategy? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseScaleBar)
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
        if (position != 0f) {
            mCursorPositionProportion = position
        }
        position = typedArray.getFloat(R.styleable.BaseScaleBar_baselinePosition, 0.67f)
        mBaselinePositionProportion = position
        // 释放
        typedArray.recycle()
        init()
    }

    fun setTickMarkStrategy(tickMarkStrategy: TickMarkStrategy?) {
        mTickMarkStrategy = tickMarkStrategy
    }

    open fun refreshLongPressStartY(startY: Float) {

    }

    open fun refreshCursorValueByComputeScroll(currX: Int) {

    }

    open fun refreshCursorValueByLongPressHandleHorizontalMove(deltaX: Float) {

    }

    open fun refreshCursorValueByOnScroll(courseIncrement: Long) {

    }

    open fun refreshOffsetUpTouchX(oriCursorValue: Long) {

    }

    open fun refreshLongPressCurrentTouchY(currentY: Int) {

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