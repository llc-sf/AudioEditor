package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.FragmentManager
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.timeruler.R
import dev.audio.timeruler.weight.BaseAudioEditorView.TickMarkStrategy
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.timer.DialogTimerSetting
import dev.audio.timeruler.utils.SizeUtils
import dev.audio.timeruler.utils.format2DurationSimple
import dev.audio.timeruler.utils.getTextHeight
import dev.audio.timeruler.utils.getTopY
import kotlin.reflect.KProperty

open class AudioCutEditorView @JvmOverloads constructor(context: Context,
                                                        attrs: AttributeSet? = null) :
    BaseAudioEditorView(context, attrs), TickMarkStrategy {

    /**
     * 波形数据
     */
    private var audioFragment: AudioFragmentWithCut? = null

    /**
     * 是否触摸到裁剪线
     */
    private var touchCutLine = false

    private var touchPlayingLine = false

    //播放条刻度字体大小
    private var playingTextSize = 20f
    private var triangleSideLength = 20f

    var cutMode: Int = CutPieceFragment.CUT_MODE_SELECT
        get() {
            return audioFragment?.cutMode ?: 0
        }

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseScaleBar)
        playingTextSize = typedArray.getDimension(R.styleable.BaseScaleBar_playingTextSize, SizeUtils.sp2px(context, 8f)
            .toFloat())
        triangleSideLength = typedArray.getDimension(R.styleable.BaseScaleBar_triangleSideLength, SizeUtils.sp2px(context, 20f)
            .toFloat())
        typedArray.recycle()

        setTickMarkStrategy(this)
        mCursorPositionProportion = 0.0f
    }


    // 设置波形数据的方法
    fun setWaveform(waveform: Waveform, duration: Long) {
        audioFragment = AudioFragmentWithCut(this).apply {
            index = 0
            this.duration = duration
            maxWaveHeight = waveHeight
            startTimestamp = startValue
            this.waveform = waveform
        }
        currentPlayingTimeInAudio = (audioFragment!!.duration / 3f).toLong()
        currentPlayingPosition = (audioFragment!!.duration / 3f) * unitMsPixel
        currentPlayingTimeInTimeLine = cursorValue + currentPlayingTimeInAudio
        invalidate() // 触发重新绘制
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (audioFragment?.onSingleTapUp(e) == true) {
            return true
        }
        return super.onSingleTapUp(e)
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
                Log.i(cut_tag, "onTouchEvent: ACTION_DOWN touchCutLine=$touchCutLine")
                var isTargetCut = audioFragment?.isCutLineTarget(event) ?: false
                if (isTargetCut) {
                    audioFragment?.onTouchEvent(context, this@AudioCutEditorView, event)
                    touchCutLine = true
                    return true
                }
                var isTargetPlayLine = isPlayingLineTarget(event)
                if (isTargetPlayLine) {
                    currentPlayingPosition = event.x
                    cursorValue + (currentPlayingPosition / unitMsPixel).toLong()
                    touchPlayingLine = true
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.i(cut_tag, "onTouchEvent: ACTION_UP touchCutLine=$touchCutLine")
                if (touchCutLine) {
                    audioFragment?.onTouchEvent(context, this@AudioCutEditorView, event)
                }
                if (touchPlayingLine) {
                    manuallyUpdatePlayingLine(event)
                    invalidate()
                }
                touchCutLine = false
                touchPlayingLine = false
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(cut_tag, "onTouchEvent: ACTION_MOVE touchCutLine=$touchCutLine")
                if (touchCutLine) {
                    audioFragment?.onTouchEvent(context, this@AudioCutEditorView, event)
                    return true
                }
                if (touchPlayingLine) {
                    manuallyUpdatePlayingLine(event)
                    Log.i(playline_tag, "onTouchEvent: ACTION_MOVE currentPlayingPosition=$currentPlayingPosition") //                    cursorValue + (currentPlayingPosition / unitMsPixel).toLong()
                    invalidate()
                    return true
                }
            }
        }
        Log.i(cut_tag, "super.onTouchEvent(event)")
        return super.onTouchEvent(event)
    }

    override fun cursorValueChange(prop: KProperty<*>, old: Long, new: Long) {
        super.cursorValueChange(prop, old, new)
        refreshPlayingLine() //todo cursor改变引发的一些列变化 总结
    }

    /**
     * 主动更新播放条
     */
    private fun manuallyUpdatePlayingLine(event: MotionEvent) {
        currentPlayingPosition = event.x
        currentPlayingTimeInTimeLine = cursorValue + (currentPlayingPosition / unitMsPixel).toLong()
        currentPlayingTimeInAudio = currentPlayingTimeInTimeLine - startValue
    }

    /**
     * 被动刷新播放条位置
     */
    private fun refreshPlayingLine() {
        currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
    }

    override fun drawWaveformSeekBar(canvas: Canvas) {
        super.drawWaveformSeekBar(canvas)
        audioFragment?.onDraw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPlayingLine(canvas)
    }

    fun onDrawCutLinePosition(canvas: Canvas) {

    }


    private val playingLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = CutPieceFragment.strokeWidth_cut
    }


    private fun isPlayingLineTarget(event: MotionEvent): Boolean {
        return event.x <= currentPlayingPosition + 20 && event.x >= currentPlayingPosition - 20
    }


    //    private var currentPlayingPosition: Int = 0
    //        get() {
    //            this.cursorValue = startValue + currentPosition
    //        }


    private fun drawPlayingLine(canvas: Canvas) {
        canvas.drawLine(currentPlayingPosition, baselinePosition, currentPlayingPosition, audioFragment?.rect?.bottom?.toFloat()
            ?: 0f, playingLinePaint)
        var y = drawTriangle(canvas, audioFragment?.rect?.bottom?.toFloat() ?: 0f)
        drawText(canvas, y, currentPlayingTimeInAudio.format2DurationSimple())
    }

    //播放条 播放条与三角的间距
    private var margin1 = 20f
    private fun drawTriangle(canvas: Canvas, y: Float): Float {
        val path = Path()
        path.moveTo(currentPlayingPosition, y + margin1)
        path.lineTo(currentPlayingPosition - triangleSideLength / 2, y + margin1 + triangleSideLength / 2)
        path.lineTo(currentPlayingPosition + triangleSideLength / 2, y + margin1 + triangleSideLength / 2)
        path.close()
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
        return y + margin1 + triangleSideLength / 2
    }

    private var textPaint = Paint().apply {
        color = Color.WHITE
        textSize = playingTextSize
    }

    //播放条三角与文字的间距
    private var margin2 = 10f
    private fun drawText(canvas: Canvas, y: Float, text: String) {
        val textWidth = textPaint.measureText(text)
        canvas.drawText(text, currentPlayingPosition - textWidth / 2, y + textPaint.getTopY() + margin2, textPaint)
    }

    override fun calcContentHeight(): Int {
        return super.calcContentHeight() + (textPaint.getTextHeight() + triangleSideLength + margin1).toInt()
    }

    //分三份
    override fun drawRightKeyTick(canvas: Canvas,
                                  index: Int,
                                  onDrawTickValue: Long,
                                  onDrawTickPosition: Float,
                                  paint: Paint,
                                  rightCount: Int) {

        if (index == 0) {
            var content = (cursorValue - startValue).format2DurationSimple()
            drawTickValue(canvas, content, 20f + paint.measureText(content) / 2, topPadding + paint!!.getTopY())
        } else if (index == rightCount / 3) {
            var content = ((cursorValue - startValue) + screenWithDuration / 3).format2DurationSimple()
            drawTickValue(canvas, content, ScreenUtil.getScreenWidth(context) / 3f, topPadding + paint!!.getTopY())
        } else if (index == rightCount / 3 * 2) {
            var content = ((cursorValue - startValue) + screenWithDuration / 3 * 2).format2DurationSimple()
            drawTickValue(canvas, content, ScreenUtil.getScreenWidth(context) / 3f * 2, topPadding + paint!!.getTopY())
        } else if (index == rightCount - 1) {
            var content = ((cursorValue - startValue) + screenWithDuration).format2DurationSimple()
            drawTickValue(canvas, content, ScreenUtil.getScreenWidth(context)
                .toFloat() - paint!!.measureText(content) / 2 - 20f, topPadding + paint!!.getTopY())
        }
    }


    /**
     * 播放条对应的屏幕位置 x坐标
     */
    private var currentPlayingPosition: Float = 0.0f

    /**
     * 播放条对应的时间戳  在时间轴上
     */
    private var currentPlayingTimeInTimeLine: Long = 0L

    /**
     * 歌曲当前播放位置  对于歌曲时长来说
     */
    var currentPlayingTimeInAudio: Long = 0L
        set(value) {
            onPlayingLineChangeListener?.onPlayingLineChange(value)
            field = value
            audioFragment?.freshTrimAnchor()
        }

    /**
     * 设置 cursor 位置
     */
    fun setPlayerProgress(currentPosition: Long, duration: Long) {
        if (currentPlayingPosition > ScreenUtil.getScreenWidth(context) || currentPlayingPosition < 0) { //播放条移动到屏幕外  需要移动波形到屏幕中间
            cursorValue += (((ScreenUtil.getScreenWidth(context) / 2).toFloat() - currentPlayingPosition) / unitMsPixel).toLong()
            currentPlayingPosition = (ScreenUtil.getScreenWidth(context) / 2).toFloat()
            invalidate()
        }
        if (currentPosition >= duration) { //播放结束
            cursorValue = startValue
            currentPlayingPosition = 0f
            currentPlayingTimeInTimeLine = startValue
            currentPlayingTimeInAudio = 0
            invalidate()
            return
        }
        currentPlayingTimeInAudio = currentPosition
        currentPlayingTimeInTimeLine = startValue + currentPlayingTimeInAudio
        var tempCursorValue = (currentPlayingTimeInTimeLine - (currentPlayingPosition / unitMsPixel).toLong())
        if (tempCursorValue + screenWithDuration >= endValue) { //播放条移动
            this.cursorValue = endValue - screenWithDuration
            currentPlayingPosition = (currentPlayingTimeInTimeLine - this.cursorValue) * unitMsPixel
        } else { //音波移动
            cursorValue = tempCursorValue

        }
        invalidate()
    }

    override fun waveScrollNotify() {
        super.waveScrollNotify()
        notifyPlayLineImp()
    }

    private fun notifyPlayLineImp() { //播放条在屏幕上的位置不动
        //        currentPlayingTimeInTimeLine = cursorValue + (currentPlayingPosition / unitMsPixel).toLong()
        //        currentPlayingTimeInAudio = currentPlayingTimeInTimeLine - startValue

        //播放条在时间轴上的位置不动
        Log.i(playline_tag, "notifyPlayLineImp ")
        currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
    }

    /**
     * 更新播放条位置  因为档位改变
     */
    override fun updatePlayingLineByModeChange(v: Int) {
        super.updatePlayingLineByModeChange(v)
        cursorValue = currentPlayingTimeInTimeLine - (currentPlayingPosition / unitMsPixel).toLong()
        if (cursorValue < startValue) { //左边有空白
            Log.i(playline_tag, "左边有空白")
            cursorValue = startValue
            currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel //避免播放条太靠左
        } else if (cursorValue + screenWithDuration > endValue) { //右边有空白
            Log.i(playline_tag, "右边有空白")
            cursorValue = endValue - screenWithDuration
            currentPlayingPosition = ScreenUtil.getScreenWidth(context)
                .toFloat() - (endValue - currentPlayingTimeInTimeLine) * unitMsPixel
        }

        //避免播放条太靠左
        if (time2PositionInTimeline(startValue) < 0) { //右移动波形不会导致播放条向右超出屏幕
            if (currentPlayingPosition + (cursorValue - startValue).time2Pixel(unitMsPixel) < ScreenUtil.getScreenWidth(context)) {
                cursorValue = startValue
                currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
            }
        } //避免播放条太靠右
        if (time2PositionInTimeline(endValue) > ScreenUtil.getScreenWidth(context)) {
            if (currentPlayingPosition - (time2PositionInTimeline(endValue) - ScreenUtil.getScreenWidth(context)) > 0) { //左移动波形不会导致播放条向左超出屏幕
                cursorValue = endValue - screenWithDuration
                currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
            }
        }
    }

    /**
     * cursor对应的歌曲位置
     */
    fun getCurrentPosition(): Long {
        return currentPlayingTimeInAudio
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


    //*************************************剪切条起点终点时间戳变化监听 start *************************************//
    interface CutModeChangeButtonEnableListener {
        fun onCutModeChange(addEnable: Boolean, removeEnable: Boolean)
    }

    var cutModeChangeButtonEnableListener: CutModeChangeButtonEnableListener? = null

    fun addCutModeChangeButtonEnableListener(listener: CutModeChangeButtonEnableListener) {
        cutModeChangeButtonEnableListener = listener
    } //*************************************剪切条起点终点时间戳变化监听 end *************************************//


    //*************************************剪切条起点终点时间戳变化监听 start *************************************//
    /**
     * 剪切条起点终点时间戳变化监听
     */
    interface OnCutLineChangeListener {
        fun onCutLineChange(startTimeStep: Long, endTimeStep: Long)
    }

    var onCutLineChangeListener: OnCutLineChangeListener? = null

    fun addOnCutLineChangeListener(listener: OnCutLineChangeListener) {
        onCutLineChangeListener = listener
    } //*************************************剪切条起点终点时间戳变化监听 end *************************************//


    //*************************************剪切条锚点定位是否显示的监听 start *************************************//
    /**
     * 剪切条锚点定位是否显示的监听
     */
    interface OnCutLineAnchorChangeListener {
        fun onCutLineChange(start: Boolean, end: Boolean)
    }


    private var onCutLineAnchorChangeListener: OnCutLineAnchorChangeListener? = null

    fun addOnCutLineAnchorChangeListener(listener: OnCutLineAnchorChangeListener) {
        onCutLineAnchorChangeListener = listener
    } //*************************************剪切条锚点定位是否显示的监听 end *************************************//


    //*************************************剪切条锚点定位是否显示的监听 start *************************************//
    interface OnTrimAnchorChangeListener {
        fun onTrimChange(start: Boolean, end: Boolean)
    }

    var onTrimAnchorChangeListener: OnTrimAnchorChangeListener? = null

    fun addOnTrimAnchorChangeListener(listener: OnTrimAnchorChangeListener) {
        onTrimAnchorChangeListener = listener
    } //*************************************剪切条锚点定位是否显示的监听 end *************************************//


    //*************************************播放条位置变化监听 start *************************************//
    /**
     * 播放条位置变化监听
     */
    interface OnPlayingLineChangeListener {
        fun onPlayingLineChange(value: Long)
    }

    private var onPlayingLineChangeListener: OnPlayingLineChangeListener? = null

    fun addOnPlayingLineChangeListener(listener: OnPlayingLineChangeListener) {
        onPlayingLineChangeListener = listener
    } //*************************************播放条位置变化监听 end *************************************//

    fun refreshCutLineAnchor(start: Boolean, end: Boolean) {
        onCutLineAnchorChangeListener?.onCutLineChange(start, end)
    }

    fun anchor2CutEndLine() {
        audioFragment?.anchor2CutEndLine()
    }

    fun anchor2CutStartLine() {
        audioFragment?.anchor2CutStartLine()
    }

    fun startCutMinus() {
        audioFragment?.startCutMinus()
    }

    fun startCutPlus() {
        audioFragment?.startCutPlus()
    }

    fun startEndMinus() {
        audioFragment?.startEndMinus()
    }

    fun startEndPlus() {
        audioFragment?.startEndPlus()
    }

    fun trimStart() {
        audioFragment?.trimStart(currentPlayingTimeInAudio)
    }

    fun trimEnd() {
        audioFragment?.trimEnd(currentPlayingTimeInAudio)
    }

    fun cutAdd() {
        audioFragment?.cutAdd()
    }

    fun switchCutMode(mode: Int) {
        audioFragment?.switchCutMode(mode)
    }

    fun cutRemove() {
        audioFragment?.cutRemove()
    }

    fun getCutLineStartTime(): Long {
        return audioFragment?.getCutLineStartTime() ?: 0
    }

    fun setCutLineStartTime(time: Long) {
        audioFragment?.setCutLineStartTime(time)
    }

    fun getCutLineEndTime(): Long {
        return audioFragment?.getCutLineEndTime() ?: 0
    }

    fun setCutLineEndTime(time: Long) {
        audioFragment?.setCutLineEndTime(time)
    }

    fun editTrimStart(parentFragmentManager: FragmentManager) {
        var min = 0L
        var max = 0L
        when (cutMode){
            CutPieceFragment.CUT_MODE_SELECT,CutPieceFragment.CUT_MODE_DELETE -> {
                min = 0L
                max = getCutLineEndTime() - 100
            }
            CutPieceFragment.CUT_MODE_JUMP -> {
                var preCutPieceFragment = audioFragment?.getPreCutPieceFragment()
                if (preCutPieceFragment != null) {
                    min = preCutPieceFragment.endTimestampTimeInSelf
                } else {
                    min = 0L
                }
                max = getCutLineEndTime() - 100
            }
        }
        DialogTimerSetting.show(parentFragmentManager, getCutLineStartTime(), -1, min, max)?.let {
            it.setTimeSelectionListener(object : DialogTimerSetting.OnTimeSelectionListener {
                override fun onSelection(time: DialogTimerSetting.Time) {
                    setCutLineStartTime(time.time)
                }
            })
        }
    }

    fun editTrimEnd(parentFragmentManager: FragmentManager) {
        var min: Long
        var max: Long
        min = getCutLineStartTime() + 100
        max = audioFragment?.duration ?: 0
        when(cutMode){
            CutPieceFragment.CUT_MODE_SELECT,CutPieceFragment.CUT_MODE_DELETE -> {
                min = getCutLineStartTime() + 100
                max = audioFragment?.duration ?: 0
            }
            CutPieceFragment.CUT_MODE_JUMP -> {
                min = getCutLineStartTime() + 100
                var nextCutPieceFragment = audioFragment?.getNextCutPieceFragment()
                if (nextCutPieceFragment != null) {
                    max = nextCutPieceFragment.startTimestampTimeInSelf
                } else {
                    max = audioFragment?.duration ?: 0
                }
            }
        }
        DialogTimerSetting.show(parentFragmentManager, -1, getCutLineEndTime(), min, max)?.let {
            it.setTimeSelectionListener(object : DialogTimerSetting.OnTimeSelectionListener {
                override fun onSelection(time: DialogTimerSetting.Time) {
                    setCutLineEndTime(time.time)
                }
            })
        }
    }

}