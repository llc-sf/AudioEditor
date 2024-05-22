package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Scroller
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.core.view.GestureDetectorCompat
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.ffmpeglib.tool.TimeUtil
import dev.audio.timeruler.BuildConfig
import dev.audio.timeruler.R
import dev.audio.timeruler.listener.OnScaleChangeListener
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.utils.SizeUtils
import dev.audio.timeruler.utils.dp
import dev.audio.timeruler.utils.formatToCursorDateString
import dev.audio.timeruler.utils.getTextHeight
import dev.audio.timeruler.utils.getTopY
import org.jetbrains.anko.collections.forEachReversedWithIndex
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.reflect.KProperty

/**
 * 概念解释
 * 坐标轴：
 * 坐标轴游标：中间的一个竖线
 *
 */
abstract class BaseAudioEditorView @JvmOverloads constructor(context: Context,
                                                             attrs: AttributeSet? = null) :
    View(context, attrs), ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener {

    companion object {

        const val TAG = "BMTAEdV"
        const val long_press_tag = "long_press_tag"
        const val touch_tag = "touch_tag"
        const val time_line_tag = "time_line_tag"
        const val cut_tag = "cut_tag"
        const val deadline_tag = "deadline_tag"
        const val init_tag = "init_tag"
        const val wave_tag = "wave_tag"
        const val playline_tag = "playline_tag"
        const val jni_tag = "jni_tag"

        /**
         * updateScaleInfo(500ms, 100ms);
         */
        const val MODE_UINT_100_MS = 0

        /**
         * updateScaleInfo(2.5s, 500ms);
         */
        const val MODE_UINT_500_MS = 1

        /**
         * updateScaleInfo(5s, 1s);
         */
        const val MODE_UINT_1000_MS = 2

        /**
         * updateScaleInfo(10s, 2s);
         */
        const val MODE_UINT_2000_MS = 3

        /**
         * updateScaleInfo(15s, 3s);
         */
        const val MODE_UINT_3000_MS = 4

        /**
         * updateScaleInfo(30s, 6s);
         */
        const val MODE_UINT_6000_MS = 5

        //数组管理
        val MODE_ARRAY = arrayOf(MODE_UINT_100_MS, MODE_UINT_500_MS, MODE_UINT_1000_MS, MODE_UINT_2000_MS, MODE_UINT_3000_MS, MODE_UINT_6000_MS)


        private const val VALUE_100_MS: Long = 100
        private const val VALUE_500_MS: Long = 500
        private const val VALUE_1000_MS: Long = 1000
        private const val VALUE_2000_MS: Long = 2000
        private const val VALUE_3000_MS: Long = 3000
        private const val VALUE_6000_MS: Long = 6000
        val VALUE_ARRAY = arrayOf(VALUE_100_MS, VALUE_500_MS, VALUE_1000_MS, VALUE_2000_MS, VALUE_3000_MS, VALUE_6000_MS)

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
        val SCREEN_WIDTH_TIME_VALUE_ARRAY = arrayOf(SCREEN_WIDTH_TIME_100_MS_VALUE, SCREEN_WIDTH_TIME_500_MS_VALUE, SCREEN_WIDTH_TIME_1000_MS_VALUE, SCREEN_WIDTH_TIME_2000_MS_VALUE, SCREEN_WIDTH_TIME_3000_MS_VALUE, SCREEN_WIDTH_TIME_6000_MS_VALUE)

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

    @IntDef(MODE_UINT_100_MS, MODE_UINT_500_MS, MODE_UINT_1000_MS, MODE_UINT_2000_MS, MODE_UINT_3000_MS, MODE_UINT_6000_MS)
    annotation class Mode

    @Mode
    var mMode = MODE_UINT_1000_MS

    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var mScalePaint: Paint? = null
    private var tickPaint: Paint? = null

    /* 普通刻度线与关键刻度线的比 */
    private val mNormalTickAndKeyTickRatio: Float

    /* 关键刻度高度 */
    protected val keyTickHeight: Float

    /* 刻度高度 */
    private var mTickHeight = 0f

    /* 刻度间距 像素*/
    /**
     * 设置时机：
     * 1、onSizeChanged
     * 2、onScale
     */
    private var mTickSpacing = 0f
        get() {
            return unitValue * unitMsPixel
        }

    /* 时间刻度开始时间 */
    protected var startValue: Long = 0
        set(value) {
            if (cursorValue == 0L) {
                cursorValue = startValue
            }
            field = value
        }

    /* 时间刻度结束时间 */
    protected var endValue: Long = 0

    /* 间隔多少毫秒是一个刻度（普通） */
    private var unitValue: Long = 0

    /* 间隔多少毫秒是一个关键刻度 */
    private var keyScaleRange: Long = 0

    /* 刻度线颜色 */
    val tickValueColor: Int

    /* 顶部padding */
    val topPadding: Float

    /* 底部padding */
    val bottomPadding: Float

    /* 音波高度 */
    var waveHeight: Float

    /* 刻度字体大小 */
    val tickValueSize: Float

    /*  每毫秒多少像素 */
    var unitMsPixel: Float = 0f

    /*一个屏幕宽度最少显示多少毫秒  8s*/
    private var minScreenSpanValue: Long = 0

    /**
     * 一个屏幕宽度最多显示多少毫秒
     *
     * 注意缩小，手势或者档位变化时候，需要判断边界值
     */
    private var maxScreenSpanValue: Long = 0

    /*一毫秒最多占多少像素*/
    private var maxUnitPixel = 0f

    /*一毫秒最少占多少像素*/
    private var minUnitPixel = 0f


    fun initConfig(config: AudioEditorConfig) {
        mMode = config.mode
        startValue = config.startValue
        endValue = config.endValue
        if (config.maxScreenSpanValue > 0) {
            maxScreenSpanValue = config.maxScreenSpanValue
        }
        setMode(mMode, isWaveFullScreen = true)
    }

    open fun setRange(start: Long, end: Long) {
        if (start >= end) {
            return
        }
        startValue = start
        endValue = end
        cursorValue = start
        invalidate()
    }

    /**
     * @param keyScaleRange 关键刻度值f
     * @param unitValue     每个刻度的值
     */
    private fun updateScaleInfo(keyScaleRange: Long, unitValue: Long) {
        this.keyScaleRange = keyScaleRange
        this.unitValue = unitValue
    }

    open fun cursorPositionPixelChange(prop: KProperty<*>, old: Float, new: Float) {
        invalidate()
    }


    open fun cursorValueChange(prop: KProperty<*>, old: Long, new: Long) {
        invalidate()
    }


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
     * 一屏幕显示多少时间
     */
    var screenWithDuration: Long = 0
        get() {
            return (width / unitMsPixel).toLong()
        }

    /**
     *
     * 游标相对view width的位置  0～ 1
     */
    protected var mCursorPositionProportion = 0.5f


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

    /*文字偏移量*/
    private val tickValueOffset: Float

    private var mGestureDetectorCompat: GestureDetectorCompat? = null
    private var scrollHappened = false

    // 刻度尺横线位置 像素
    val baselinePosition: Float
        get() = (mScalePaint?.getTextHeight() ?: 0f) + topPadding

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

    /**
     * 波形绘制开始  y坐标开始
     */
    var waveStartYInParent = 0f
        get() {
            return baselinePosition + keyTickHeight
        }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseScaleBar)
        tickValueColor = typedArray.getColor(R.styleable.BaseScaleBar_tickValueColor, Color.WHITE)
        tickValueSize = typedArray.getDimension(R.styleable.BaseScaleBar_tickValueSize, SizeUtils.sp2px(getContext(), 8f)
            .toFloat())
        topPadding = typedArray.getDimension(R.styleable.BaseScaleBar_topPadding, SizeUtils.sp2px(getContext(), 0f)
            .toFloat())
        bottomPadding = typedArray.getDimension(R.styleable.BaseScaleBar_bottomPadding, SizeUtils.sp2px(getContext(), 0f)
            .toFloat())
        waveHeight = typedArray.getDimension(R.styleable.BaseScaleBar_waveHeight, SizeUtils.sp2px(getContext(), 60.dp.toFloat())
            .toFloat())
        keyTickHeight = typedArray.getDimension(R.styleable.BaseScaleBar_keyTickHeight, SizeUtils.dp2px(getContext(), 10f)
            .toFloat())
        tickValueOffset = typedArray.getDimension(R.styleable.BaseScaleBar_tickValueOffset, SizeUtils.dp2px(getContext(), -30f)
            .toFloat())
        tickDirectionUp = typedArray.getBoolean(R.styleable.BaseScaleBar_tickDirectionUp, true)
        mNormalTickAndKeyTickRatio = typedArray.getFloat(R.styleable.BaseScaleBar_normalTickRatio, 0.67f)
        tickColor = typedArray.getColor(R.styleable.BaseScaleBar_tickColor, Color.BLACK)
        cursorLineColor = typedArray.getColor(R.styleable.BaseScaleBar_cursorLineColor, Color.YELLOW)
        showCursorLine = typedArray.getBoolean(R.styleable.BaseScaleBar_showCursorLine, true)
        showTickValue = typedArray.getBoolean(R.styleable.BaseScaleBar_showCursorLine, true)
        showTickLine = typedArray.getBoolean(R.styleable.BaseScaleBar_showTickLine, true)
        var position = typedArray.getFloat(R.styleable.BaseScaleBar_cursorPosition, 0.5f)
        mCursorPositionProportion = position
        typedArray.recycle()
        init()
    }


    private fun init() {
        mScalePaint = Paint()
        mScalePaint!!.isAntiAlias = true
        mScalePaint!!.strokeWidth = 1f
        mScalePaint!!.isDither = true
        mScalePaint!!.style = Paint.Style.FILL_AND_STROKE

        tickPaint = Paint()
        tickPaint!!.isAntiAlias = true
        tickPaint!!.strokeWidth = 1f.dp
        tickPaint!!.isDither = true
        tickPaint!!.style = Paint.Style.FILL_AND_STROKE

        minScreenSpanValue = SCREEN_WIDTH_TIME_VALUE_ARRAY[0]
        maxScreenSpanValue = SCREEN_WIDTH_TIME_VALUE_ARRAY[SCREEN_WIDTH_TIME_VALUE_ARRAY.size - 1]

        unitValue = 60000
        keyScaleRange = (5 * 60 * 1000).toLong()
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        startValue = calendar.timeInMillis

        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        endValue = calendar.timeInMillis

        cursorValue = startValue

        mScalePaint!!.textSize = tickValueSize
    }


    class ObservableProperty<T>(var value: T,
                                val onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

        operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
            val oldValue = value
            value = newValue
            onChange(property, oldValue, newValue)
        }
    }


    /**
     * 时间轴上的时间戳对应屏幕上的位置
     *
     * timeStamp时间与当前游标timeStamp的差值 * 单位像素 = 当前时间戳应该位置x-游标位置x
     *
     */
    fun time2PositionInTimeline(timeStamp: Long): Float {
        return (cursorPosition + (timeStamp - cursorValue) * unitMsPixel)
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
        Log.i(init_tag, "onSizeChanged()")
        mTickHeight = keyTickHeight * mNormalTickAndKeyTickRatio
        cursorPosition = w * mCursorPositionProportion
        maxUnitPixel = w * 1.0f / minScreenSpanValue
        minUnitPixel = w * 1.0f / maxScreenSpanValue
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), getHeightSize(suggestedMinimumHeight, heightMeasureSpec))
    }

    private fun getHeightSize(size: Int, heightMeasureSpec: Int): Int {
        return calcContentHeight()
    }

    /**
     * 计算内容高度
     * todo
     *
     * @param baselinePositionProportion
     * @return
     */
    open fun calcContentHeight(): Int {
        return (topPadding + mScalePaint!!.getTextHeight() + keyTickHeight + bottomPadding + waveHeight * 2).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        Log.i(long_press_tag, "onDraw cursorValue=${TimeUtil.getDetailTime(cursorValue)}")
        tickPaint!!.color = tickColor
        drawKeyValueRange(canvas)
        if (showTickLine) {
            canvas.drawLine(scrollX.toFloat(), baselinePosition, (scrollX + width).toFloat(), baselinePosition, mScalePaint!!)
        } //cursorValue 与 startValue 的距离 time
        val leftRange = cursorValue - startValue //cursorValue 左边几格
        val leftNeighborOffset = leftRange % unitValue //
        val leftNeighborTickValue = cursorValue - leftNeighborOffset
        val leftNeighborPosition = cursorPosition - leftNeighborOffset * unitMsPixel
        val leftCount = (cursorPosition / mTickSpacing + 0.5f).toInt()
        var onDrawTickPosition: Float
        var onDrawTickValue: Long
        for (i in 0 until leftCount) {
            onDrawTickValue = leftNeighborTickValue - unitValue * i
            if (onDrawTickValue < startValue) {
                break
            }
            onDrawTickPosition = leftNeighborPosition - mTickSpacing * i
            if (tickDirectionUp) {
                if ((onDrawTickValue - startValue) % keyScaleRange == 0L) {
                    canvas.drawLine(onDrawTickPosition, baselinePosition, onDrawTickPosition, baselinePosition - keyTickHeight, tickPaint!!)
                    drawTickValue(canvas, onDrawTickPosition, baselinePosition - keyTickHeight, onDrawTickValue, true)
                } else {
                    canvas.drawLine(onDrawTickPosition, baselinePosition - mTickHeight, onDrawTickPosition, baselinePosition, tickPaint!!)
                    drawTickValue(canvas, onDrawTickPosition, baselinePosition - mTickHeight, onDrawTickValue, false)
                }
            } else {
                if ((onDrawTickValue - startValue) % keyScaleRange == 0L) {
                    canvas.drawLine(onDrawTickPosition, baselinePosition, onDrawTickPosition, baselinePosition + keyTickHeight, tickPaint!!)
                    drawLeftKeyTick(canvas, i, onDrawTickValue, onDrawTickPosition, mScalePaint!!, leftCount)
                } else {
                    canvas.drawLine(onDrawTickPosition, baselinePosition, onDrawTickPosition, baselinePosition + mTickHeight, tickPaint!!)
                    drawTickValue(canvas, onDrawTickPosition, baselinePosition - mTickHeight, onDrawTickValue, false)
                }
            }
        }
        val rightNeighborTickValue = leftNeighborTickValue + unitValue.apply {
            Log.i(wave_tag, "unitValue=$unitValue;unitMsPixel=$unitMsPixel,mTickSpacing=$mTickSpacing")
        }
        val rightNeighborPosition = leftNeighborPosition + mTickSpacing
        val rightCount = ((width - cursorPosition) / mTickSpacing + 0.5f).toInt()
        for (i in 0 until rightCount) {
            onDrawTickValue = rightNeighborTickValue + unitValue * i
            if (onDrawTickValue > endValue) {
                break
            }
            onDrawTickPosition = rightNeighborPosition + mTickSpacing * i
            if (tickDirectionUp) {
                if ((onDrawTickValue - startValue) % keyScaleRange == 0L) {
                    canvas.drawLine(onDrawTickPosition, baselinePosition, onDrawTickPosition, baselinePosition - keyTickHeight, tickPaint!!)
                    drawTickValue(canvas, onDrawTickPosition, baselinePosition - keyTickHeight, onDrawTickValue, true)
                } else {
                    canvas.drawLine(onDrawTickPosition, baselinePosition, onDrawTickPosition, baselinePosition - mTickHeight, tickPaint!!)
                    drawTickValue(canvas, onDrawTickPosition, baselinePosition - mTickHeight, onDrawTickValue, false)
                }
            } else {
                drawRightKeyTick(canvas, i, onDrawTickValue, onDrawTickPosition, mScalePaint!!, rightCount, keyScaleRange)
                if ((onDrawTickValue - startValue) % keyScaleRange == 0L) {
                    canvas.drawLine(onDrawTickPosition, baselinePosition, onDrawTickPosition, baselinePosition + keyTickHeight, tickPaint!!)
                } else {
                    canvas.drawLine(onDrawTickPosition, baselinePosition, onDrawTickPosition, baselinePosition + mTickHeight, tickPaint!!)
                    drawTickValue(canvas, onDrawTickPosition, baselinePosition - mTickHeight, onDrawTickValue, false)
                }
            }
        }
        drawWaveformSeekBar(canvas)

        drawRange(canvas)
    }

    /**
     * cursorValue 左边关键帧刻度值绘制
     *  默认关键帧绘制值 如有特殊需求再在子类中绘制
     */
    open fun drawLeftKeyTick(canvas: Canvas,
                             index: Int,
                             onDrawTickValue: Long,
                             onDrawTickPosition: Float,
                             paint: Paint,
                             leftCount: Int) {
        if ((onDrawTickValue - startValue) % (keyScaleRange * 2) == 0L) {
            drawTickValue(canvas, onDrawTickPosition, baselinePosition + keyTickHeight + mScalePaint!!.getTopY(), onDrawTickValue, true)
        }
    }

    /**
     * cursorValue 右边关键帧刻度值绘制 默认关键帧绘制值
     * 默认关键帧绘制值 如有特殊需求再在子类中绘制
     */
    open fun drawRightKeyTick(canvas: Canvas,
                              index: Int,
                              onDrawTickValue: Long,
                              onDrawTickPosition: Float,
                              paint: Paint,
                              rightCount: Int,
                              keyScaleRange: Long) { //关键刻度绘制刻度值
        if ((onDrawTickValue - startValue) % (keyScaleRange * 2) == 0L) {
            drawTickValue(canvas, onDrawTickPosition, baselinePosition + keyTickHeight + mScalePaint!!.getTopY(), onDrawTickValue, true)
        }
    }

    private fun drawKeyValueRange(canvas: Canvas) {
        if (!BuildConfig.DEBUG) {
            return
        }
        var rect = Rect(0, topPadding.toInt(), width, ((mScalePaint?.getTextHeight() ?: 0).toInt()))

        //画空心rect
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.BLUE
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        canvas.drawRect(rect!!, rectPaint)
    }

    private fun drawRange(canvas: Canvas) {
        if (!BuildConfig.DEBUG) {
            return
        }
        var rect = Rect(0, 0, width, height)

        //画空心rect
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.GREEN
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        canvas.drawRect(rect!!, rectPaint)
    }

    protected open fun drawWaveformSeekBar(canvas: Canvas) {}

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
    fun drawTickValue(canvas: Canvas, x: Float, y: Float, scaleValue: Long, keyScale: Boolean) {
        if (showTickValue) {
            if (mTickMarkStrategy?.disPlay(scaleValue, keyScale) == true) {
                mScalePaint!!.color = tickValueColor
                mScalePaint!!.textAlign = Paint.Align.CENTER
                canvas.drawText(getScaleValue(scaleValue, keyScale), x, y, mScalePaint!!)
            }
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
    fun drawTickValue(canvas: Canvas, content: String, x: Float, y: Float) {
        if (showTickValue) {
            mScalePaint!!.color = tickValueColor
            mScalePaint!!.textAlign = Paint.Align.CENTER
            canvas.drawText(content, x, y, mScalePaint!!)
        }
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


    override fun onScale(detector: ScaleGestureDetector): Boolean {
        var scaleFactor = detector.scaleFactor
        Log.d(TAG, "onScalescaleFactor: $scaleFactor")
        status = STATUS_ZOOM
        unitMsPixel *= scaleFactor
        if (unitMsPixel > maxUnitPixel) {
            unitMsPixel = maxUnitPixel
        } else if (unitMsPixel < minUnitPixel) {
            unitMsPixel = minUnitPixel
        }
        onScale()
        return unitMsPixel < maxUnitPixel || unitMsPixel > minUnitPixel
    }

    private var lastScale = 0f

    /*0~1000之间*/
    fun setScale(scale: Float) {
    }


    private fun onScale() { // 计算一屏刻度值跨度
        val screenSpanValue = width / unitMsPixel
        close2Mode(screenSpanValue)
    }


    /**
     * 靠近 而不是设置
     *
     * 这时候属于无级变速 结余6档位中间
     */
    private fun close2Mode(screenSpanValue: Float) {
        Log.i("TAG", "updateMode: $screenSpanValue")
        if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[5]) {
            setMode(MODE_ARRAY[5], isRefreshUnitPixel = false)
        } else if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[4]) {
            setMode(MODE_ARRAY[4], isRefreshUnitPixel = false)
        } else if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[3]) {
            setMode(MODE_ARRAY[3], isRefreshUnitPixel = false)
        } else if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[2]) {
            setMode(MODE_ARRAY[2], isRefreshUnitPixel = false)
        } else if (screenSpanValue >= SCREEN_WIDTH_TIME_VALUE_ARRAY[1]) {
            setMode(MODE_ARRAY[1], isRefreshUnitPixel = false)
        } else {
            setMode(MODE_ARRAY[0], isRefreshUnitPixel = false)
        }
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {

    }

    override fun onDown(e: MotionEvent): Boolean {
        Log.i(touch_tag, "onDown")
        if (status == STATUS_SCROLL_FLING) {
            scroller.forceFinished(true)
        } else {
            scrollHappened = false
        }
        status = STATUS_DOWN // 返回出 拦截事件
        return true
    }

    override fun onShowPress(e: MotionEvent) { // do nothing
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean { // do nothing
        return false
    }

    /**
     * 真正滚动回调
     */
    override fun onScroll(e1: MotionEvent,
                          e2: MotionEvent,
                          distanceX: Float,
                          distanceY: Float): Boolean {
        Log.i(touch_tag, "onScroll")
        if (PlayerManager.isPlaying) {
            return true
        }
        if (e2.pointerCount > 1) {
            return false
        }
        if (scaleGestureDetect.isInProgress) {
            return false
        } // TODO: 处理第一次触发滚动产生的距离过大,呈现滚动突兀不友好体验问题 ------ 待优化
        if (!scrollHappened) {
            scrollHappened = true
            if (null != mOnCursorListener) {
                mOnCursorListener!!.onStartTrackingTouch(cursorValue)
            }
            return true
        }
        status = STATUS_SCROLL // 游标刻度值增量
        val courseIncrement = (distanceX / unitMsPixel).toLong()
        Log.i(TAG, "unitPixel: $unitMsPixel")
        cursorValue += courseIncrement //        mCursorValue1 += courseIncrement
        refreshCursorValueByOnScroll(distanceX, courseIncrement)
        var result = true
        Log.i(deadline_tag, "cursorValue=${cursorValue.formatToCursorDateString()}; mScaleInfo!!.startValue=${startValue.formatToCursorDateString()}; mScaleInfo!!.endValue=${endValue.formatToCursorDateString()}")

        if (cursorValue < startValue) {
            cursorValue = startValue
            result = false
        } else if (cursorValue > endValue - (mTickMarkStrategy?.getCursorEndOffset() ?: 0)) {
            cursorValue = endValue - (mTickMarkStrategy?.getCursorEndOffset() ?: 0)
            result = false
        }
        if (null != mOnCursorListener) {
            mOnCursorListener!!.onProgressChanged(cursorValue, true)
        }
        waveScrollNotify()
        invalidate()
        return result
    }

    /**
     * 波形滚动通知
     */
    open fun waveScrollNotify() {

    }

    /**
     * View 滚动回调
     * 只要触摸 就有回调
     */
    override fun computeScroll() {
        Log.i(touch_tag, "computeScroll")
        if (PlayerManager.isPlaying) {
            return
        }
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            cursorValue = startValue + (currX / unitMsPixel).toLong() //            mCursorValue1 =
            //                mScaleInfo!!.startValue + offsetOriCurrentValue1 + (currX / unitPixel).toLong()
            refreshCursorValueByComputeScroll(currX)
            if (cursorValue < startValue) {
                cursorValue = startValue
            } else if (cursorValue > endValue - (mTickMarkStrategy?.getCursorEndOffset() ?: 0)) {
                cursorValue = endValue - (mTickMarkStrategy?.getCursorEndOffset() ?: 0)
            }
            if (null != mOnCursorListener) {
                mOnCursorListener!!.onProgressChanged(cursorValue, true)
            }
            waveScrollNotify()
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

    override fun onFling(e1: MotionEvent,
                         e2: MotionEvent,
                         velocityX: Float,
                         velocityY: Float): Boolean {
        Log.i(touch_tag, "onFling")
        if (PlayerManager.isPlaying) {
            true
        }
        status = STATUS_SCROLL_FLING
        val startX = ((cursorValue - startValue) * unitMsPixel).toInt()
        val maX = ((endValue - startValue) * unitMsPixel).toInt()
        scroller.fling(startX, 0, -velocityX.toInt(), 0, 0, maX, 0, 0)
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

    private var scaleChangeListener: OnScaleChangeListener? = null

    fun setOnScaleChangeListener(onScaleChangeListenerListener: OnScaleChangeListener?) {
        this.scaleChangeListener = onScaleChangeListenerListener
    }

    private var mTickMarkStrategy: TickMarkStrategy? = null

    fun setTickMarkStrategy(tickMarkStrategy: TickMarkStrategy?) {
        mTickMarkStrategy = tickMarkStrategy
    }


    fun setMode(@Mode mode: Int) {
        setMode(mode, true)
    }


    /**
     * @param isRefreshUnitPixel    是否刷新单位像素
     * 1、isRefreshUnitPixel=false   手势放大缩小 调节unitMsPixel，screeWithDuration由调节unitMsPixel决定
     * 2、isRefreshUnitPixel=true  直接档位调节 调节screeWithDuration，调节unitMsPixel由调节screeWithDuration决定
     *
     * 调用处：
     * 1、初始化 initConfig
     * 2、放大缩小调节档位
     * 3、手势缩放
     *
     * 元素刷新：
     *  1、unitValue      间隔多少毫秒是一个刻度（普通）
     *  2、keyScaleRange  间隔多少毫秒是一个刻度（关键刻度）
     *  3、unitMsPixel    每毫秒多少像素
     *  4、cursorValue    控制波形图的x坐标以免尾部出现空白现象
     */
    private var maxMode = 0
    fun setMode(@Mode mode: Int,
                isRefreshUnitPixel: Boolean = true,
                isWaveFullScreen: Boolean = false) { //计算屏幕显示多少时间
        var screeWithDuration: Long
        var index = mode
        updateScaleInfo(5 * VALUE_ARRAY[index], VALUE_ARRAY[index])
        screeWithDuration = SCREEN_WIDTH_TIME_VALUE_ARRAY[index] //检测是否超出范围
        if (screeWithDuration > maxScreenSpanValue || isWaveFullScreen) {
            screeWithDuration = maxScreenSpanValue
            SCREEN_WIDTH_TIME_VALUE_ARRAY.forEachReversedWithIndex { i, value ->
                if (value > screeWithDuration) {
                    index = (i - 1)
                    return@forEachReversedWithIndex
                }
            }
            if(SCREEN_WIDTH_TIME_VALUE_ARRAY[MODE_UINT_6000_MS]<maxScreenSpanValue){
                index = MODE_UINT_6000_MS
            }
            this.unitValue = maxScreenSpanValue / SCREEN_SECTIONS
            updateScaleInfo(this.unitValue * 5, this.unitValue) //audio显示完全
            cursorValue = startValue.apply {
                Log.i(wave_tag, "startValue:${startValue.formatToCursorDateString()}")
            }
            maxMode = index
        }
        if (!isRefreshUnitPixel) { //手势放大缩小
            screeWithDuration = (width / (unitMsPixel)).toLong()
        } else { //直接档位变化
            //todo  不一定是屏幕宽度
            unitMsPixel = (ScreenUtil.getScreenWidth(context) * 1f / screeWithDuration)
        }
        if (cursorValue + screeWithDuration > endValue) { //判断是否有 尾部空白没有坐标的情况
            cursorValue = endValue - screeWithDuration
        }
        mMode = index
        scaleChangeListener?.onScaleChange(mMode, 0, maxMode)
        if (!isWaveFullScreen) { //刷新游标位置
            updatePlayingLineByModeChange(index - mode)
        }
        invalidate()
    }

    open fun updatePlayingLineByModeChange(v: Int) {
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


    open fun refreshCursorValueByComputeScroll(currX: Int) {

    }

    open fun refreshCursorValueByOnScroll(distanceX: Float, courseIncrement: Long) {

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
     * 移动的变量为像素
     */
    fun moveStartByPixel(distance: Float) {
        cursorValue -= distance.pixel2Time(unitMsPixel)
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

    private val simpleDateFormat = SimpleDateFormat("HH:mm:ss")

    /**
     * xxs  格式
     */
    private fun getScaleValue(scaleValue: Long, keyScale: Boolean): String {
        val formattedTime = simpleDateFormat.format(scaleValue) // 解析天、小时、分钟和秒
        val parts = formattedTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val seconds = parts[2].toInt() // 转换为秒
        return (hours * 3600 + minutes * 60 + seconds).toString() + "s"
    }

    /**
     * 放大
     */
    open fun zoomIn() {
        if (mMode > 0) {
            setMode(MODE_ARRAY[mMode - 1])
        }
    }

    /**
     * 缩小
     */
    open fun zoomOut() {
        if (mMode < MODE_ARRAY.size - 1) {
            setMode(MODE_ARRAY[mMode + 1])
        }
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

        //        /**
        //         * 获取显示的刻度值
        //         *
        //         * @param scaleValue
        //         * @param keyScale
        //         * @return
        //         */
        //        fun getScaleValue(scaleValue: Long, keyScale: Boolean): String

        //        /**
        //         * 获取当前刻度值显示颜色
        //         *
        //         * @param scaleValue
        //         * @param keyScale
        //         * @return
        //         */
        //        @ColorInt
        //        fun getColor(scaleValue: Long, keyScale: Boolean): Int

        //        /**
        //         * 获取当前刻度值显示大小
        //         *
        //         * @param scaleValue
        //         * @param keyScale
        //         * @param maxScaleValueSize
        //         * @return
        //         */
        //        @Dimension
        //        fun getSize(scaleValue: Long, keyScale: Boolean, maxScaleValueSize: Float): Float

        /**
         * cursor 最大值相关
         *
         * 限制波形往左移动的做大距离
         *
         * if(cursorValue > mScaleInfo!!.endValue - getCursorEndOffset()){
         *    cursorValue = mScaleInfo!!.endValue - getCursorEndOffset()
         * }
         */
        fun getCursorEndOffset(): Long
    }


}
