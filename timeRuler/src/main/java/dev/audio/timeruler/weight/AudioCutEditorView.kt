package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import dev.audio.timeruler.weight.BaseAudioEditorView.TickMarkStrategy
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.utils.formatToCursorDateString
import kotlin.reflect.KProperty

open class AudioCutEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseAudioEditorView(context, attrs), TickMarkStrategy {

    /**
     * 波形数据
     */
    private var audioFragment: AudioFragmentWithCut? = null

    /**
     * 是否触摸到裁剪线
     */
    private var touchCutLine = false

    init {
        init()
    }

    private fun init() {
        setTickMarkStrategy(this)
        mCursorPositionProportion = 0.0f
    }


    // 设置波形数据的方法
    fun setWaveform(waveform: Waveform, duration: Long) {
        audioFragment = AudioFragmentWithCut(this).apply {
            index = 0
            this.duration = duration
            maxWaveHeight = 200f
            waveVerticalPosition = 500f
            color = Color.RED
            startTimestamp = startValue
            this.waveform = waveform
        }
        currentAudioPlayingTime = (audioFragment!!.duration / 6f).toLong()
        currentPlayingPosition = (audioFragment!!.duration / 6f) * unitMsPixel
        invalidate() // 触发重新绘制
    }


    /**
     * 裁剪拨片的触摸事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (PlayerManager.isPlaying) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(
                    cut_tag, "onTouchEvent: ACTION_DOWN touchCutLine=$touchCutLine"
                )
                var isTargetCut = audioFragment?.isCutLineTarget(event) ?: false
                if (isTargetCut) {
                    audioFragment?.onTouchEvent(context, this@AudioCutEditorView, event)
                    touchCutLine = true
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.i(
                    cut_tag, "onTouchEvent: ACTION_UP touchCutLine=$touchCutLine"
                )
                if (touchCutLine) {
                    audioFragment?.onTouchEvent(context, this@AudioCutEditorView, event)
                }
                touchCutLine = false
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(
                    cut_tag, "onTouchEvent: ACTION_MOVE touchCutLine=$touchCutLine"
                )
                if (touchCutLine) {
                    audioFragment?.onTouchEvent(context, this@AudioCutEditorView, event)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun drawWaveformSeekBar(canvas: Canvas) {
        super.drawWaveformSeekBar(canvas)
        audioFragment?.onDraw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPlayingLine(canvas)
    }


    private val playingLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = CutPieceFragment.strokeWidth_cut
    }

//    private var currentPlayingPosition: Int = 0
//        get() {
//            this.cursorValue = startValue + currentPosition
//        }


    private fun drawPlayingLine(canvas: Canvas) {
        canvas.drawLine(
            currentPlayingPosition, 0.0f,
            currentPlayingPosition, audioFragment?.rect?.bottom?.toFloat() ?: 0f,
            playingLinePaint
        )
    }


    /**
     * 播放条对应的屏幕位置 x坐标
     */
    private var currentPlayingPosition: Float = 0.0f

    /**
     * 播放条对应的时间戳  在时间轴上
     */
    private var currentPlayingTimeStamp: Long = 0L

    /**
     * 歌曲当前播放位置  对于歌曲时长来说
     */
    private var currentAudioPlayingTime: Long = 0L

    /**
     * 设置 cursor 位置
     */
    fun setPlayerProgress(currentPosition: Long, duration: Long) {
        if(currentPosition==duration){
            cursorValue = startValue
            currentAudioPlayingTime = 0
            currentPlayingTimeStamp = startValue
            currentPlayingPosition = 0f
            invalidate()
            return
        }
        this.currentAudioPlayingTime = currentPosition
        currentPlayingTimeStamp = startValue + currentAudioPlayingTime
        var tempCursorValue = (currentPlayingTimeStamp - (currentPlayingPosition / unitMsPixel).toLong())
        if (tempCursorValue + screenWithDuration >= endValue) {
            //播放条移动
            this.cursorValue = endValue - screenWithDuration
            currentPlayingPosition = (currentPlayingTimeStamp - this.cursorValue) * unitMsPixel
            Log.i("llc_fuck","setPlayerProgress currentPlayingPosition=$currentPlayingPosition")
        } else {
            //音波移动
            cursorValue = tempCursorValue

        }
        invalidate()
    }

    override fun cursorValueChange(prop: KProperty<*>, old: Long, new: Long) {
        super.cursorValueChange(prop, old, new)
//        extracted(new)
    }

    override fun notifycu() {
        super.notifycu()
        extracted(cursorValue)
    }

    private fun extracted(new: Long) {
        currentPlayingTimeStamp = new + (currentPlayingPosition / unitMsPixel).toLong()
        currentAudioPlayingTime = currentPlayingTimeStamp - startValue
        Log.i(
            cut_tag,
            "cursorValueChange: currentPlayingTimeStamp=${currentPlayingTimeStamp.formatToCursorDateString()}"
        )
        Log.i("llc_fuck","extracted currentPlayingPosition=$currentPlayingPosition")
    }

    /**
     * cursor对应的歌曲位置
     */
    fun getCurrentPosition(): Long {
        return currentAudioPlayingTime
    }


    override fun disPlay(scaleValue: Long, keyScale: Boolean): Boolean {
        return keyScale
    }

    override fun getCursorEndOffset(): Long {
        return screenWithDuration
    }


    /**
     * 惯性滑动 tag时间戳更新
     */
    override fun refreshCursorValueByComputeScroll(currX: Int) {
        audioFragment?.refreshCursorValueByComputeScroll(currX)
    }


    /**
     * 滑动时间轴，tag时间戳更新
     */
    override fun refreshCursorValueByOnScroll(distanceX: Float, courseIncrement: Long) {
        audioFragment?.refreshCursorValueByOnScroll(distanceX, courseIncrement)
    }

    fun setCutMode(cutMode: Int) {
        audioFragment?.setCutMode(cutMode)
        invalidate()
    }


}