package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import dev.audio.timeruler.R
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.utils.SizeUtils
import dev.audio.timeruler.weight.BaseAudioEditorView.TickMarkStrategy
import java.text.SimpleDateFormat

open class MultiTrackAudioEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseAudioEditorView(context, attrs), TickMarkStrategy {
    private var mTickPaint: Paint? = null
    private var mColorCursorPaint: Paint? = null
    private val mTriangleHeight = 10f
    private val cursorBackgroundColor: Int
    private val cursorValueSize: Float
    private val colorScaleBackground: Int
    private var tickValueBoundOffsetH = 20f
    private var drawCursorContent: Boolean


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseScaleBar)
        cursorBackgroundColor =
            typedArray.getColor(R.styleable.BaseScaleBar_cursorBackgroundColor, Color.RED)
        cursorValueSize = typedArray.getDimension(
            R.styleable.BaseScaleBar_cursorValueSize, SizeUtils.sp2px(getContext(), 10f).toFloat()
        )
        colorScaleBackground =
            typedArray.getColor(R.styleable.BaseScaleBar_colorScaleBackground, Color.WHITE)
        drawCursorContent = typedArray.getBoolean(R.styleable.BaseScaleBar_drawCursorContent, true)
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

    override fun calcContentHeight(): Int {
        return super.calcContentHeight() + 500//todo   加上所有波形图的高度+间隙
    }

    // 设置波形数据的方法
    fun setWaveform(waveform: Waveform) {
        audioFragments.add(AudioFragment(this).apply {
            index = 0
            duration = 1000 * 60 * 2
            maxWaveHeight = 50f
            waveVerticalPosition = 200f
            color = Color.RED
            startTimestamp = startValue ?: 0
            this.waveform = waveform
        })
        audioFragments.add(AudioFragment(this).apply {
            index = 1
            duration = 1000 * 60 * 2
            maxWaveHeight = 50f
            waveVerticalPosition = 400f
            color = Color.RED
            startTimestamp = startValue ?: 0
            this.waveform = waveform
        })
        audioFragments.add(AudioFragment(this).apply {
            index = 2
            duration = 1000 * 60 * 2
            maxWaveHeight = 50f
            waveVerticalPosition = 600f
            color = Color.RED
            startTimestamp = startValue
            this.waveform = waveform
        })
        invalidate() // 触发重新绘制
    }


    private var audioFragments = mutableListOf<AudioFragment>()

    /**
     * 绘制游标
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCursor(canvas, cursorPosition, cursorValue)
    }


    //**************************** 长按处理 ****************************
    //是否是长按
    var onLongPress = false

    // 长按水平方向上的初始位置
    private var longPressStartTouchX = 0f

    //长按命中的是哪一个轨道
    var longTouchIndex = 0


    override fun onLongPress(e: MotionEvent) {
        // do nothing
        Log.i(long_press_tag, "onLongPress")
        onLongPress = true
        //确定长按命中的轨道
        longTouchIndex = onLongPressTrackIndex(e)
        // 记录长按横坐标的初始位置
        longPressStartTouchX = e.x
        // 记录长按竖坐标的初始位置
        refreshLongPressStartY(e.y)

    }

    private fun handleLongPressHorizontalMovement(deltaX: Float) {
        Log.i(touch_tag, "unitPixel=$unitMsPixel")
        try {
            if (unitMsPixel != 0f) {
//                mCursorValue1 -= (deltaX / unitPixel).toLong()
                refreshCursorValueByLongPressHandleHorizontalMove(deltaX)
                invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 裁剪拨片的触摸事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
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
                    refreshOffsetLongPressUpTouchX(cursorValue)
                }                 // 结束长按状态

            }
        }

        return true
    }

    //**************************** 长按处理 ****************************
    override fun drawWaveformSeekBar(canvas: Canvas) {
        super.drawWaveformSeekBar(canvas)
        audioFragments.forEach { audioFragment ->
            audioFragment.onDraw(canvas)
        }
    }

    override fun disPlay(scaleValue: Long, keyScale: Boolean): Boolean {
        return keyScale
    }

    override fun getCursorEndOffset(): Long {
        return 0
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
            0f, 0f, (textBound.width() + 20).toFloat(), textBound.height() + tickValueBoundOffsetH
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

    /**
     * 长按命中的轨道 index
     */
    private fun onLongPressTrackIndex(e: MotionEvent): Int {
        //view 在屏幕上的y坐标
        audioFragments.forEachIndexed { index, audioFragment ->
            if (e.y > (audioFragment.rect?.top ?: 0) && e.y < (audioFragment.rect?.bottom
                    ?: 0) && e.x > (audioFragment.rect?.left
                    ?: 0) && e.x < (audioFragment.rect?.right ?: 0)
            ) {
                return index.apply {
                    Log.i(
                        long_press_tag,
                        "TimeRulerBar onLongPressTrackIndex touchy=$y,index=$this,rect=${audioFragment.rect}"
                    )
                }
            }
        }
        return 0.apply {
            Log.i(long_press_tag, "TimeRulerBar onLongPressTrackIndex touchy=$y,index=$this")
        }
    }

    /**
     * 长按起始的Y坐标
     */
    private fun refreshLongPressStartY(startY: Float) {
        Log.i(
            long_press_tag,
            "TimeRulerBar refreshLongPressStartY startY=$startY,longTouchIndex=$longTouchIndex"
        )
        audioFragments[longTouchIndex]?.let {
            it.refreshLongPressStartY(startY)
        }
    }

    /**
     * 长按移动的Y坐标
     */
    fun refreshLongPressCurrentTouchY(currentY: Int) {
        audioFragments[longTouchIndex]?.let {
            it.refreshLongPressCurrentTouchY(currentY)
        }
    }

    /**
     * 惯性滑动 tag时间戳更新
     */
    override fun refreshCursorValueByComputeScroll(currX: Int) {
        audioFragments.forEach {
            it.refreshCursorValueByComputeScroll(currX)
        }
    }

    /**
     * 长按波形图 水平方向移动
     */
    private fun refreshCursorValueByLongPressHandleHorizontalMove(deltaX: Float) {
        Log.i(
            long_press_tag,
            "TimeRulerBar refreshCursorValueByLongPressHandleHorizontalMove: $deltaX"
        )
        audioFragments[longTouchIndex]?.let {
            it.refreshCursorValueByLongPressHandleHorizontalMove(deltaX)
        }
    }

    /**
     * 滑动时间轴，tag时间戳更新
     */
    override fun refreshCursorValueByOnScroll(distanceX: Float, courseIncrement: Long) {
        audioFragments.forEach {
            it.refreshCursorValueByOnScroll(distanceX, courseIncrement)
        }
    }

    /**
     * 长按结束 抬起手指 手指x轴的坐标
     *
     * 长按移动，手指抬起时  与原来起点的cursorValue差值，惯性滑动用
     */
    private fun refreshOffsetLongPressUpTouchX(oriCursorValue: Long) {
        audioFragments[longTouchIndex]?.let {
            it.refreshOffsetUpTouchX(oriCursorValue)
        }
    }


    /**
     * 手指拿起
     * y轴上的坐标清除(上下拖拽波形图功能)
     */
    private fun onLongPressTouchUpEvent() {
        audioFragments[longTouchIndex]?.let {
            it.onLongPressTouchUpEvent()
        }
    }

}