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
import androidx.annotation.ColorInt
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.timeruler.R
import dev.audio.timeruler.weight.BaseAudioEditorView.TickMarkStrategy
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.utils.SizeUtils
import java.text.SimpleDateFormat
import kotlin.reflect.KProperty

open class MultiTrackAudioEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseAudioEditorView(context, attrs), TickMarkStrategy {
    private var mTickPaint: Paint? = null
    private var mColorCursorPaint: Paint? = null
    private val mTriangleHeight = 10f
    private val cursorBackgroundColor: Int
    private val cursorValueSize: Float
    private val colorScaleBackground: Int
    private val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
    private var tickValueBoundOffsetH = 20f
    private var drawCursorContent: Boolean


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeRulerBar)
        cursorBackgroundColor =
            typedArray.getColor(R.styleable.TimeRulerBar_cursorBackgroundColor, Color.RED)
        cursorValueSize = typedArray.getDimension(
            R.styleable.TimeRulerBar_cursorValueSize, SizeUtils.sp2px(getContext(), 10f).toFloat()
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

    /**
     * 时间轴上的时间戳对应屏幕上的位置
     *
     * timeStamp时间与当前游标timeStamp的差值 * 单位像素 = 当前时间戳应该位置x-游标位置x
     *
     */
    fun time2PositionInTimeline(timeStamp: Long): Float {
        return (cursorPosition + (timeStamp - cursorValue) * unitMsPixel)
    }


    // 设置波形数据的方法
    fun setWaveform(waveform: Waveform) {
        audioFragments.add(AudioFragmentWithCut(this).apply {
            index = 0
            duration = 1000 * 60 * 2
            maxWaveHeight = 50f
            waveVerticalPosition = 200f
            color = Color.RED
            startTimestamp = mScaleInfo?.startValue ?: 0
            this.waveform = waveform
        })
//        audioFragments.add(CutAudioFragment().apply {
//            index = 1
//            duration = 1000 * 60 * 2
//            maxWaveHeight = 50f
//            waveVerticalPosition = 400f
//            color = Color.RED
//            cursorPosition = mCursorPosition
//            startValue = mScaleInfo?.startValue ?: 0
//            this.unitMsPixel = unitPixel
//            this.waveform = waveform
//            cursorValue = mCursorTimeValue
//        })
//        audioFragments.add(CutAudioFragment().apply {
//            index = 2
//            duration = 1000 * 60 * 2
//            maxWaveHeight = 50f
//            waveVerticalPosition = 600f
//            color = Color.RED
//            cursorPosition = mCursorPosition
//            startValue = mScaleInfo?.startValue ?: 0
//            this.unitMsPixel = unitPixel
//            this.waveform = waveform
//            cursorValue = mCursorTimeValue
//        })
        invalidate() // 触发重新绘制
    }


    private var audioFragments = mutableListOf<AudioFragmentWithCut>()


    private var touchCutLine = false
    private fun isTouchCutLine(event: MotionEvent): Boolean {
        audioFragments?.forEachIndexed { index, audioFragment ->
            if (audioFragment.isTarget(event)) {
                touchCutLine = true
                return touchCutLine
            }
        }
        return touchCutLine
    }

    /**
     * 绘制游标
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCursor(canvas, cursorPosition, cursorValue)
    }


    /**
     * 裁剪拨片的触摸事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(
                    BaseAudioEditorView.cut_tag,
                    "onTouchEvent: ACTION_DOWN touchCutLine=$touchCutLine"
                )
                var isTargetCut = isTouchCutLine(event)
                if (isTargetCut) {
                    audioFragments?.forEachIndexed { index, audioFragment ->
                        audioFragment.onTouchEvent(context, this@MultiTrackAudioEditorView, event)
                    }
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.i(
                    BaseAudioEditorView.cut_tag,
                    "onTouchEvent: ACTION_UP touchCutLine=$touchCutLine"
                )
                if (touchCutLine) {
                    audioFragments?.forEachIndexed { index, audioFragment ->
                        audioFragment.onTouchEvent(context, this@MultiTrackAudioEditorView, event)
                    }
                }
                touchCutLine = false
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(
                    BaseAudioEditorView.cut_tag,
                    "onTouchEvent: ACTION_MOVE touchCutLine=$touchCutLine"
                )
                if (touchCutLine) {
                    audioFragments?.forEachIndexed { index, audioFragment ->
                        audioFragment.onTouchEvent(context, this@MultiTrackAudioEditorView, event)
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun drawWaveformSeekBar(canvas: Canvas) {
        super.drawWaveformSeekBar(canvas)
        audioFragments.forEach { audioFragment ->
            audioFragment.drawWave(canvas)
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


    fun setShowCursor(isShowCursorContent: Boolean) {
        drawCursorContent = isShowCursorContent
        invalidate()
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
    override fun onLongPressTrackIndex(e: MotionEvent): Int {
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
    override fun refreshLongPressStartY(startY: Float) {
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
    override fun refreshLongPressCurrentTouchY(currentY: Int) {
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
    override fun refreshCursorValueByLongPressHandleHorizontalMove(deltaX: Float) {
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
    override fun refreshOffsetUpTouchX(oriCursorValue: Long) {
        audioFragments[longTouchIndex]?.let {
            it.refreshOffsetUpTouchX(oriCursorValue)
        }
    }


    /**
     * 手指拿起
     * y轴上的坐标清除(上下拖拽波形图功能)
     */
    override fun onLongPressTouchUpEvent() {
        super.onLongPressTouchUpEvent()
        audioFragments[longTouchIndex]?.let {
            it.onLongPressTouchUpEvent()
        }
    }

}