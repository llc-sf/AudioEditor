package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import dev.audio.timeruler.weight.BaseAudioEditorView.TickMarkStrategy
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.player.PlayerManager

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

    /**
     * 设置 cursor 位置
     */
    fun setPlayerProgress(currentPosition: Long, duration: Long) {
        this.cursorValue = startValue + currentPosition
        if (cursorValue + screenWithDuration >= endValue) {
            this.cursorValue = endValue - screenWithDuration
            return
        }
        invalidate()
    }

    /**
     * cursor对应的歌曲位置
     */
    fun getCurrentPosition(): Long {
        return cursorValue - startValue
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