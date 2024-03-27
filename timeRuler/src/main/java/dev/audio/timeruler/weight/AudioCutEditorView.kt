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

open class AudioCutEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseAudioEditorView(context, attrs), TickMarkStrategy {


    init {
        init()
    }

    private fun init() {
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
                    cut_tag, "onTouchEvent: ACTION_DOWN touchCutLine=$touchCutLine"
                )
                var isTargetCut = isTouchCutLine(event)
                if (isTargetCut) {
                    audioFragments?.forEachIndexed { index, audioFragment ->
                        audioFragment.onTouchEvent(context, this@AudioCutEditorView, event)
                    }
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.i(
                    cut_tag, "onTouchEvent: ACTION_UP touchCutLine=$touchCutLine"
                )
                if (touchCutLine) {
                    audioFragments?.forEachIndexed { index, audioFragment ->
                        audioFragment.onTouchEvent(context, this@AudioCutEditorView, event)
                    }
                }
                touchCutLine = false
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(
                    cut_tag, "onTouchEvent: ACTION_MOVE touchCutLine=$touchCutLine"
                )
                if (touchCutLine) {
                    audioFragments?.forEachIndexed { index, audioFragment ->
                        audioFragment.onTouchEvent(context, this@AudioCutEditorView, event)
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

    private val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
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


    private var drawCursorContent: Boolean = true
    fun setShowCursor(isShowCursorContent: Boolean) {
        drawCursorContent = isShowCursorContent
        invalidate()
    }


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
        val topSidePosition = statY
        path.moveTo(cursorPosition, statY)
        path.lineTo(cursorPosition - 3.5f, topSidePosition)
        path.lineTo(cursorPosition + 3.5f, topSidePosition)
        path.close()
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
     * 滑动时间轴，tag时间戳更新
     */
    override fun refreshCursorValueByOnScroll(distanceX: Float, courseIncrement: Long) {
        audioFragments.forEach {
            it.refreshCursorValueByOnScroll(distanceX, courseIncrement)
        }
    }




}