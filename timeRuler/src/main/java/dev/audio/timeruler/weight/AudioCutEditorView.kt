package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.FragmentManager
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.timeruler.R
import dev.audio.timeruler.bean.AudioFragmentBean
import dev.audio.timeruler.weight.BaseAudioEditorView.TickMarkStrategy
import dev.audio.timeruler.bean.Waveform
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.timer.DialogTimerSetting
import dev.audio.timeruler.utils.SizeUtils
import dev.audio.timeruler.utils.dp
import dev.audio.timeruler.utils.format2DurationSimple
import dev.audio.timeruler.utils.format2DurationSimpleInt
import dev.audio.timeruler.utils.getTextHeight
import dev.audio.timeruler.utils.getTopY
import java.lang.ref.WeakReference
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

    private val moveHandler = MoveHandler(WeakReference(this))

    var cutMode: Int = CutPieceFragment.CUT_MODE_SELECT
        get() {
            return audioFragment?.cutMode ?: 0
        }

    var audioFragmentBean: AudioFragmentBean? = null
        get() {
            return audioFragment?.audioFragmentBean
        }

    var canZoomOut = false
        get() {
            if (screenWithDuration == 0L) {
                return false
            }
            Log.i("AudioCutEditorView", "canZoomOut: screenWithDuration=$screenWithDuration audioFragment?.duration=${audioFragment?.duration}")
            return screenWithDuration + 1 < audioFragment?.duration ?: 0
        }

    var canZoomIn = false
        get() {
            if (screenWithDuration == 0L) {
                return true
            }
            Log.i("AudioCutEditorView", "canZoomIn: screenWithDuration=$screenWithDuration SCREEN_WIDTH_TIME_VALUE_ARRAY[0]=${SCREEN_WIDTH_TIME_VALUE_ARRAY[0]}")
            return screenWithDuration > SCREEN_WIDTH_TIME_VALUE_ARRAY[0]
        }

    var isWholeScreen = false
        get() {
            return !canZoomOut
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
    fun setWaveform(waveform: Waveform?, duration: Long, path: String) {
        audioFragment = AudioFragmentWithCut(this).apply {
            index = 0
            this.duration = duration
            maxWaveHeight = waveHeight
            startTimestamp = startValue
            this.waveform = waveform
            this.path = path
        }
        currentPlayingTimeInAudio = (audioFragment!!.duration / 3f).toLong()
        currentPlayingPosition = (audioFragment!!.duration / 3f) * unitMsPixel
        invalidate() // 触发重新绘制
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        manuallyUpdatePlayingLineCheck(e)
        invalidate()
        if (audioFragment?.onSingleTapUp(e) == true) {
            return true
        }
        return super.onSingleTapUp(e)
    }

    private var lastTouchXProcess: Float = 0f

    /**
     * 裁剪拨片的触摸事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(cut_tag, "onTouchEvent: ACTION_DOWN touchCutLine=$touchCutLine")
                lastTouchXProcess = x
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
                    manuallyUpdatePlayingLineCheck(event)
                    invalidate()

                    //移动播放条到屏幕中央
                    var offsetTimeValue = ((event.x - ScreenUtil.getScreenWidth(context) / 2) / unitMsPixel).toLong()
                    moveHandler.removeMessages(MoveHandler.MSG_MOVE_TO_OFFSET_PLAYING_LINE)
                    moveHandler.sendMessage(moveHandler.obtainMessage(MoveHandler.MSG_MOVE_TO_OFFSET_PLAYING_LINE, offsetTimeValue))

                }
                touchCutLine = false
                touchPlayingLine = false
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(cut_tag, "onTouchEvent: ACTION_MOVE touchCutLine=$touchCutLine")
                val dx = x - lastTouchXProcess
                if (touchCutLine) {
                    audioFragment?.onTouchEvent(context, this@AudioCutEditorView, event)
                    return true
                }
                if (touchPlayingLine) {
                    if(!isWholeScreen){
                        PlayerManager.pause()
                    }
                    if (dx > 10 || event.x >= 50) {
                        moveHandler.removeMessages(MoveHandler.MSG_MOVE_START)
                    }
                    if (dx < -10 || event.x <= ScreenUtil.getScreenWidth(context) - 50) {
                        moveHandler.removeMessages(MoveHandler.MSG_MOVE_END)
                    }
                    if (audioFragment?.canLoadMoreWaveDataToStart() == true && event.x < currentPlayingAutoMoveRang) { //靠左边自由移动
                        moveHandler.sendEmptyMessage(MoveHandler.MSG_MOVE_START)
                    } else if (audioFragment?.canLoadMoreWaveDataToEnd() == true && event.x > ScreenUtil.getScreenWidth(context) - currentPlayingAutoMoveRang) { //靠右边自由移动
                        moveHandler.sendEmptyMessage(MoveHandler.MSG_MOVE_END)
                    } else {
                        manuallyUpdatePlayingLine(event)
                        Log.i(playline_tag, "onTouchEvent: ACTION_MOVE currentPlayingPosition=$currentPlayingPosition") //                    cursorValue + (currentPlayingPosition / unitMsPixel).toLong()
                        invalidate()
                    }
                    return true
                }
            }
        }
        Log.i(cut_tag, "super.onTouchEvent(event)")
        return super.onTouchEvent(event)
    }

    /**
     * 播放条非因进度更新
     */
    fun freshPlayingLinePosition(timeInWholeAudio: Long) {
        currentPlayingTimeInAudio = timeInWholeAudio
        currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
    }

    override fun cursorValueChange(prop: KProperty<*>, old: Long, new: Long) {
        super.cursorValueChange(prop, old, new)
        refreshPlayingLine() //todo cursor改变引发的一些列变化 总结
    }

    private fun manuallyUpdatePlayingLineCheck(event: MotionEvent) {
        if (audioFragment == null) {
            return
        }
        var tempCurrentPlayingPosition = event.x
        var tempCurrentPlayingTimeInAudio = cursorValue + (tempCurrentPlayingPosition / unitMsPixel).toLong() - startValue
        when (cutMode) {
            CutPieceFragment.CUT_MODE_SELECT -> {
                if (tempCurrentPlayingTimeInAudio in getCutLineStartTime()..getCutLineEndTime()) { //只需要计算出当前播放条的位置即可，seek 在播放的时候做
                    currentPlayingPosition = tempCurrentPlayingPosition
                    currentPlayingTimeInAudio = tempCurrentPlayingTimeInAudio
                    PlayerManager.seekTo(currentPlayingTimeInAudio - getCutLineStartTime(), 0)
                } else {
                    if (!PlayerManager.isPlaying) {
                        currentPlayingTimeInAudio = PlayerManager.getCurrentPosition() + getCutLineStartTime()
                        currentPlayingPosition = cursorPosition + currentPlayingTimeInAudio * unitMsPixel
                    }
                }
            }

            CutPieceFragment.CUT_MODE_DELETE -> {
                if (tempCurrentPlayingTimeInAudio in getCutLineStartTime()..getCutLineEndTime()) { //                    currentPlayingTimeInAudio = 0
                    //只需要计算出当前播放条的位置即可，seek 在播放的时候做
                    if (!PlayerManager.isPlaying) {
                        if (PlayerManager.player.currentWindowIndex == 0) {
                            currentPlayingTimeInAudio = PlayerManager.getCurrentPosition()

                        } else {
                            currentPlayingTimeInAudio = PlayerManager.getCurrentPosition() + getCutLineEndTime()
                        }
                        currentPlayingPosition = cursorPosition + currentPlayingTimeInAudio * unitMsPixel
                    }
                } else {
                    currentPlayingPosition = tempCurrentPlayingPosition
                    currentPlayingTimeInAudio = tempCurrentPlayingTimeInAudio
                    if (currentPlayingTimeInAudio < getCutLineStartTime()) {
                        PlayerManager.seekTo(currentPlayingTimeInAudio, 0)
                    } else if (currentPlayingTimeInAudio > getCutLineEndTime()) {
                        PlayerManager.seekTo(currentPlayingTimeInAudio - getCutLineEndTime(), 1)
                    }
                }

            }

            CutPieceFragment.CUT_MODE_JUMP -> {
                currentPlayingPosition = tempCurrentPlayingPosition
                currentPlayingTimeInAudio = tempCurrentPlayingTimeInAudio
                playJumpSelected(false)
            }
        }
    }


    /**
     * 主动更新播放条
     */
    private fun manuallyUpdatePlayingLine(event: MotionEvent) {
        if (audioFragment == null) {
            return
        } //只需要计算出当前播放条的位置即可，seek 在播放的时候做 todo 其实这里做也行 一会调整吧
        currentPlayingPosition = event.x
        currentPlayingTimeInAudio = cursorValue + (currentPlayingPosition / unitMsPixel).toLong() - startValue //        var seekPosition = when (cutMode) {
    }

    private fun offsetPlayingLine(offset: Float) {
        currentPlayingPosition += offset
        currentPlayingTimeInAudio = cursorValue + (currentPlayingPosition / unitMsPixel).toLong() - startValue
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


    private val playingLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = CutPieceFragment.strokeWidth_cut
    }


    private fun isPlayingLineTarget(event: MotionEvent): Boolean {
        return event.x <= currentPlayingPosition + 20 && event.x >= currentPlayingPosition - 20
    }


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
            var content = (cursorValue - startValue).format2DurationSimpleInt()
            drawTickValue(canvas, content, 20f + paint.measureText(content) / 2, topPadding + paint!!.getTopY())
        } else if (index == rightCount / 3) {
            var content = ((cursorValue - startValue) + screenWithDuration / 3).format2DurationSimpleInt()
            drawTickValue(canvas, content, ScreenUtil.getScreenWidth(context) / 3f, topPadding + paint!!.getTopY())
        } else if (index == rightCount / 3 * 2) {
            var content = ((cursorValue - startValue) + screenWithDuration / 3 * 2).format2DurationSimpleInt()
            drawTickValue(canvas, content, ScreenUtil.getScreenWidth(context) / 3f * 2, topPadding + paint!!.getTopY())
        } else if (index == rightCount - 1) {
            var content = ((cursorValue - startValue) + screenWithDuration).format2DurationSimpleInt()
            drawTickValue(canvas, content, ScreenUtil.getScreenWidth(context)
                .toFloat() - paint!!.measureText(content) / 2 - 20f, topPadding + paint!!.getTopY())
        }
    }


    /**
     * 播放条对应的屏幕位置 x坐标
     */
    private var currentPlayingPosition: Float = 0.0f

    private var currentPlayingAutoMoveRang = 10.dp

    /**
     * 播放条对应的时间戳  在时间轴上
     */
    private var currentPlayingTimeInTimeLine: Long = 0L
        get() {
            return startValue + currentPlayingTimeInAudio
        }

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
     *
     * @param positionInCutPiece 当前真正播放位置,对于播放片段
     * @param durationCutPiece 当前播放片段的时长
     * @param currentWindowIndex 当前播放片段的索引
     *
     * 定位，以播放线定位 此时前提：播放线位置是正确的   随着播放进度，要么移动波形，要么移动播放条，使播放进度正确
     */
    fun onProgressChange(currentWindowIndex: Int,
                         positionInCutPiece: Long,
                         durationCutPiece: Long) { //在真个坐标轴中的位置
        if (touchPlayingLine) {
            return
        }
        Log.i(playline_tag, "setPlayerProgress currentPositionInPlaying=$positionInCutPiece duration=$durationCutPiece")
        if (cutMode == CutPieceFragment.CUT_MODE_SELECT && positionInCutPiece >= durationCutPiece) { //判断播放结束
            restart()
            return
        }
        if (cutMode == CutPieceFragment.CUT_MODE_DELETE && currentWindowIndex == 1 && positionInCutPiece >= durationCutPiece) { //判断播放结束
            restart()
            return
        }
        if (cutMode == CutPieceFragment.CUT_MODE_JUMP && (currentWindowIndex + 1) == (audioFragment?.cutPieceFragments?.size
                ?: 0) && positionInCutPiece >= durationCutPiece
        ) { //判断播放结束
            restart()
            return
        } //当前播放点在整个音频中的时间戳
        var currentPositionInWholeAudio: Long = 0
        when (cutMode) {
            CutPieceFragment.CUT_MODE_SELECT -> {
                currentPositionInWholeAudio = positionInCutPiece + (audioFragment?.getCutLineStartTime()
                    ?: 0)
            }

            CutPieceFragment.CUT_MODE_DELETE -> {
                if (currentWindowIndex == 0) {
                    currentPositionInWholeAudio = positionInCutPiece
                } else {
                    currentPositionInWholeAudio = (getCutLineEndTime()) + positionInCutPiece
                }
            }

            CutPieceFragment.CUT_MODE_JUMP -> {
                val cutFragment = audioFragment?.cutPieceFragmentsOrder?.get(currentWindowIndex)
                currentPositionInWholeAudio = positionInCutPiece + (cutFragment?.startTimestampTimeInSelf
                    ?: 0)
            }
        }
        currentPlayingTimeInAudio = currentPositionInWholeAudio
        if (currentPlayingPosition > ScreenUtil.getScreenWidth(context) || currentPlayingPosition < 0) { //播放条移动到屏幕外  需要移动波形到屏幕中间
            if (currentPositionInWholeAudio.time2Pixel(unitMsPixel) < ScreenUtil.getScreenWidth(context) / 2) { //播放条太靠start，波形固定，移动播放条
                cursorValue = startValue
                currentPlayingPosition = (currentPlayingTimeInTimeLine - this.cursorValue) * unitMsPixel
            } else if (((audioFragment?.duration
                    ?: 0) - currentPositionInWholeAudio) < ScreenUtil.getScreenWidth(context) / 2
            ) { //播放条太靠end，波形固定，移动播放条
                cursorValue = endValue - screenWithDuration
                currentPlayingPosition = (currentPlayingTimeInTimeLine - this.cursorValue) * unitMsPixel
            } else {
                cursorValue += (((ScreenUtil.getScreenWidth(context) / 2).toFloat() - currentPlayingPosition) / unitMsPixel).toLong()
                currentPlayingPosition = (ScreenUtil.getScreenWidth(context) / 2).toFloat()
            }
            invalidate()
            return
        }

        var tempCursorValue = (currentPlayingTimeInTimeLine - (currentPlayingPosition / unitMsPixel).toLong()) //波形移动优先，如果不能移动，再去移动播放条
        if (tempCursorValue + screenWithDuration >= endValue) { //播放条移动
            this.cursorValue = endValue - screenWithDuration
            currentPlayingPosition = (currentPlayingTimeInTimeLine - this.cursorValue) * unitMsPixel
        } else { //音波移动
            cursorValue = tempCursorValue
        }
        invalidate()
    }

    private fun restart() {
        PlayerManager.pause()
        cursorValue = startValue
        when (cutMode) {
            CutPieceFragment.CUT_MODE_SELECT -> { //定位播放条
                currentPlayingTimeInAudio = getCutLineStartTime()
            }

            CutPieceFragment.CUT_MODE_DELETE -> { //定位播放条
                currentPlayingTimeInAudio = 0
            }

            CutPieceFragment.CUT_MODE_JUMP -> { //定位播放条
                audioFragment?.removeFake()
                currentPlayingTimeInAudio = audioFragment?.cutPieceFragmentsOrder?.get(0)?.startTimestampTimeInSelf
                    ?: 0
            }
        }
        currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
        invalidate()
    }

    override fun waveScrollNotify() {
        super.waveScrollNotify()
        notifyPlayLineImp()
    }

    private fun notifyPlayLineImp() { //播放条在屏幕上的位置不动
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
        fun onCutLineMove()
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


    //*************************************裁剪模式变化 start *************************************//
    interface CutModeChangeListener {
        fun onCutModeChange(mode: Int)
    }

    var onCutModeChangeListener: CutModeChangeListener? = null

    fun addOnCutModeChangeListener(listener: CutModeChangeListener) {
        onCutModeChangeListener = listener
    } //*************************************裁剪模式变化 end *************************************//


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
        audioFragment?.let {
            if (it.startCutMinus()) {
                updateMediaSource(it.getCutLineStartTime(), it.getCutLineEndTime())
            }

        }
    }

    fun startCutPlus() {
        audioFragment?.let {
            if (it.startCutPlus()) {
                updateMediaSource(it.getCutLineStartTime(), it.getCutLineEndTime())
            }
        }
    }

    fun startEndMinus() {
        audioFragment?.let {
            if (it.startEndMinus()) {
                updateMediaSource(it.getCutLineStartTime(), it.getCutLineEndTime())
            }
        }
    }

    fun startEndPlus() {
        audioFragment?.let {
            if (it.startEndPlus()) {
                updateMediaSource(it.getCutLineStartTime(), it.getCutLineEndTime())
            }
        }
    }

    /**
     * 播放位置 为裁剪开始时间
     */
    fun trimStart() {
        audioFragment?.trimStart(currentPlayingTimeInAudio)
    }

    /**
     * 播放位置 为裁剪结束时间
     */
    fun trimEnd() {
        audioFragment?.trimEnd(currentPlayingTimeInAudio)
    }

    fun cutAdd() {
        audioFragment?.cutAdd()
        PlayerManager.updateMediaSourceDeleteJump(audioFragment!!.cutPieceFragments)
    }

    fun switchCutMode(mode: Int) {
        if (cutMode == mode) {
            return
        }
        audioFragment?.switchCutMode(mode)
        var isPlay = PlayerManager.isPlaying
        PlayerManager.pause()
        var seekPosition = 0L
        var windowIndex = 0
        when (mode) {
            CutPieceFragment.CUT_MODE_SELECT -> {
                PlayerManager.updateMediaSource(getCutLineStartTime(), getCutLineEndTime())
                currentPlayingTimeInAudio = getCutLineStartTime()
                currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
            }

            CutPieceFragment.CUT_MODE_DELETE -> {
                PlayerManager.updateMediaSourceDelete(getCutLineStartTime(), getCutLineEndTime(), audioFragment?.duration
                    ?: 0)
                currentPlayingTimeInAudio = 0
                currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
            }

            CutPieceFragment.CUT_MODE_JUMP -> {
                if (audioFragment?.isPlayingLineInAnyCutPiece(currentPlayingTimeInAudio) == true) {
                    PlayerManager.updateMediaSourceDeleteJump(audioFragment!!.cutPieceFragments)
                    currentPlayingTimeInAudio = audioFragment!!.cutPieceFragmentsOrder[0].startTimestampTimeInSelf
                    currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
                    seekPosition = 0
                    windowIndex = 0
                }
            }
        }
        if (isPlay) {
            PlayerManager.playWithSeek(seekPosition, windowIndex)
        } else {
            PlayerManager.seekTo(seekPosition, windowIndex)
        }
        onCutModeChangeListener?.onCutModeChange(mode)
    }

    fun cutRemove() { //todo 增删后，保持之前播放/暂停状态（是播放就继续播放）播放源同步
        audioFragment?.cutRemove()
    }

    private fun getCutLineStartTime(): Long {
        return audioFragment?.getCutLineStartTime() ?: 0
    }

    private fun getCutLineEndTime(): Long {
        return audioFragment?.getCutLineEndTime() ?: 0
    }

    fun setCutLineStartTime(time: Long) {
        audioFragment?.setCutLineStartTime(time)
    }

    fun setCutLineEndTime(time: Long) {
        audioFragment?.setCutLineEndTime(time)
    }

    fun editTrimStart(parentFragmentManager: FragmentManager) {
        var min = 0L
        var max = 0L
        when (cutMode) {
            CutPieceFragment.CUT_MODE_SELECT, CutPieceFragment.CUT_MODE_DELETE -> {
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
        when (cutMode) {
            CutPieceFragment.CUT_MODE_SELECT, CutPieceFragment.CUT_MODE_DELETE -> {
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

    /**
     * 先设置播放源，再seekTo播放
     */
    fun play() {
        if (audioFragment == null) {
            return
        }
        when (cutMode) {
            CutPieceFragment.CUT_MODE_SELECT -> {
                audioFragment?.let {
                    if (currentPlayingTimeInAudio in getCutLineStartTime()..getCutLineEndTime()) {
                        PlayerManager.playWithSeek(currentPlayingTimeInAudio - getCutLineStartTime())
                        PlayerManager.play()
                    } else {
                        currentPlayingTimeInAudio = getCutLineStartTime()
                        currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
                        PlayerManager.playWithSeek(0)
                    }
                }
            }

            CutPieceFragment.CUT_MODE_DELETE -> {
                if (currentPlayingTimeInAudio in getCutLineStartTime()..getCutLineEndTime()) {
                    currentPlayingTimeInAudio = 0
                    currentPlayingPosition = (currentPlayingTimeInTimeLine - cursorValue) * unitMsPixel
                    PlayerManager.playWithSeek(0)
                } else {
                    if (currentPlayingTimeInAudio < getCutLineStartTime()) {
                        PlayerManager.playWithSeek(currentPlayingTimeInAudio)
                    } else {
                        PlayerManager.playWithSeek(currentPlayingTimeInAudio - getCutLineEndTime(), 1)
                    }

                }
            }

            CutPieceFragment.CUT_MODE_JUMP -> {
                if (playJumpSelected()) return
            }
        }
    }

    private fun playJumpSelected(isAutoPlay: Boolean = true): Boolean {
        if (audioFragment?.isPlayingLineInAnyCutPiece(currentPlayingTimeInAudio) == true) {
            PlayerManager.updateMediaSourceDeleteJump(audioFragment!!.cutPieceFragmentsOrder)
            val windowIndex = audioFragment?.playingLineIndexInFragments(currentPlayingTimeInAudio)
                ?: 0
            if (isAutoPlay) {
                PlayerManager.playWithSeek(currentPlayingTimeInAudio - audioFragment!!.cutPieceFragmentsOrder[windowIndex].startTimestampTimeInSelf, windowIndex)
            } else {
                PlayerManager.seekTo(currentPlayingTimeInAudio - audioFragment!!.cutPieceFragmentsOrder[windowIndex].startTimestampTimeInSelf, windowIndex)
            }
        } else {
            if (audioFragment!!.cutPieceFragments.isEmpty()) {
                return true
            }
            if (PlayerManager.uri == null) {
                return true
            } //多虑掉CutPieceFragment中 startTimestampTimeInSelf 小于 start 的
            val cutPieceFragmentsFilter = audioFragment!!.cutPieceFragmentsOrder.filter { it.startTimestampTimeInSelf >= currentPlayingTimeInAudio }
            var end = audioFragment!!.duration
            if (cutPieceFragmentsFilter.isEmpty()) {
                CutPieceFragment(audioFragment!!, false, 0, CutPieceFragment.CUT_MODE_JUMP, isFake = true).apply {
                    startTimestampTimeInSelf = currentPlayingTimeInAudio
                    endTimestampTimeInSelf = end
                    audioFragment!!.cutPieceFragments.add(this)
                }
            } else {
                end = cutPieceFragmentsFilter[0].startTimestampTimeInSelf
                CutPieceFragment(audioFragment!!, false, 0, CutPieceFragment.CUT_MODE_JUMP, isFake = true).apply {
                    startTimestampTimeInSelf = currentPlayingTimeInAudio
                    endTimestampTimeInSelf = end
                    audioFragment!!.cutPieceFragments.add(this)
                }
            }

            var windowIndex = 0
            audioFragment!!.cutPieceFragmentsOrder.forEachIndexed { index, cutPieceFragment ->
                if (cutPieceFragment.startTimestampTimeInSelf == currentPlayingTimeInAudio) {
                    windowIndex = index
                    return@forEachIndexed
                }
            }
            PlayerManager.updateMediaSourceDeleteJump(audioFragment!!.cutPieceFragmentsOrder)
            if (isAutoPlay) {
                PlayerManager.playWithSeek(0, windowIndex)
            } else {
                PlayerManager.seekTo(0, windowIndex)
            }
        }
        return false
    }

    fun playOrPause() {
        if (PlayerManager.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun pause() {
        PlayerManager.pause() //删除掉
        audioFragment?.removeFake()
    }

    fun updateMediaSource(startTimestampTimeInSelf: Long, endTimestampTimeInSelf: Long) {
        when (cutMode) {
            CutPieceFragment.CUT_MODE_SELECT -> {
                PlayerManager.updateMediaSource(startTimestampTimeInSelf, endTimestampTimeInSelf)
                PlayerManager.seekTo(currentPlayingTimeInAudio - getCutLineStartTime())

            }

            CutPieceFragment.CUT_MODE_DELETE -> {
                PlayerManager.updateMediaSourceDelete(startTimestampTimeInSelf, endTimestampTimeInSelf, audioFragment?.duration
                    ?: 0)
                if (currentPlayingTimeInAudio > getCutLineEndTime()) {
                    PlayerManager.seekTo(currentPlayingTimeInAudio - getCutLineEndTime(), 1)
                } else {
                    PlayerManager.seekTo(currentPlayingTimeInAudio)
                }
            }

            CutPieceFragment.CUT_MODE_JUMP -> {
                audioFragment?.let {
                    PlayerManager.updateMediaSourceDeleteJump(it.cutPieceFragments)
                    val index = it.playingLineIndexInFragments(currentPlayingTimeInAudio)
                    if (index == -1) {
                        PlayerManager.seekTo(currentPlayingTimeInAudio)
                    } else {
                        PlayerManager.seekTo(currentPlayingTimeInAudio - it.cutPieceFragments[index].startTimestampTimeInSelf, index)
                    }
                }
            }
        }
    }

    fun setLoadingView(duration: Long, path: String) {
        setWaveform(null, duration, path)
    }

    var isCutLineStartVisible = false
        get() {
            return audioFragment?.isCutLineStartVisible ?: false
        }

    var isCutLineEndVisible = false
        get() {
            return audioFragment?.isCutLineEndVisible ?: false
        }


    var cutPieceFragmentsOrder: List<CutPieceFragment>? = null
        get() {
            return audioFragment?.cutPieceFragmentsOrder
        }


    class MoveHandler(private var audioCutEditor: WeakReference<AudioCutEditorView>? = null) :
        Handler(Looper.myLooper()!!) {
        companion object {
            const val MSG_MOVE_TO_OFFSET_PLAYING_LINE = 100
            const val MSG_MOVE_START = 1000
            const val MSG_MOVE_END = 1001

            //移动波形图的时间间隔
            const val MOVE_INTERVAL_TIME_DELAY = 6L

            //移动波形图的距离
            const val MOVE_STEP_TIME = 500L

            //移动波形图的时间间隔
            const val MOVE_INTERVAL_TIME = 30L
        }

        // 新增一个成员变量来存储剩余的偏移量
        private var remainingOffsetValue = 0L

        //移动波形图的距离
        private var moveStepTime = MOVE_STEP_TIME
        private var isMoveByFixedPixel = true
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_MOVE_TO_OFFSET_PLAYING_LINE -> {
                    if (msg.obj is Long) {
                        remainingOffsetValue = msg.obj as Long
                    }
                    audioCutEditor?.get()?.apply {
                        if (isMoveByFixedPixel) {
                            moveStepTime = (CutPieceFragment.MoveHandler.MOVE_STEP_PIXEL / unitMsPixel).toLong()
                        }
                        val stepTimeValue = if (Math.abs(remainingOffsetValue) >= moveStepTime) {
                            moveStepTime
                        } else {
                            Math.abs(remainingOffsetValue)
                        }

                        if (remainingOffsetValue > 0) {
                            this.audioFragment?.moveWave(-stepTimeValue)
                            remainingOffsetValue -= stepTimeValue
                        } else {
                            this.audioFragment?.moveWave(stepTimeValue)
                            remainingOffsetValue += stepTimeValue
                        }

                        if (Math.abs(remainingOffsetValue) > 0) {
                            sendMessageDelayed(obtainMessage(MSG_MOVE_TO_OFFSET_PLAYING_LINE), MOVE_INTERVAL_TIME_DELAY)
                        }
                    }
                }

                MSG_MOVE_START -> {
                    audioCutEditor?.get()?.let {
                        if (it.audioFragment?.canLoadMoreWaveDataToStart() == true) {
                            it.moveStartByPixel(CutPieceFragment.MoveHandler.MOVE_INTERVAL_SPACE)
                            it.offsetPlayingLine(-CutPieceFragment.MoveHandler.MOVE_INTERVAL_SPACE)
                            sendMessageDelayed(obtainMessage(MSG_MOVE_START), MoveHandler.MOVE_INTERVAL_TIME)
                        }
                    }
                }

                MSG_MOVE_END -> {
                    audioCutEditor?.get()?.let {
                        if (it.audioFragment?.canLoadMoreWaveDataToEnd() == true) {
                            it.moveRightByPixel(CutPieceFragment.MoveHandler.MOVE_INTERVAL_SPACE)
                            it.offsetPlayingLine(CutPieceFragment.MoveHandler.MOVE_INTERVAL_SPACE)
                            sendMessageDelayed(obtainMessage(MSG_MOVE_END), MoveHandler.MOVE_INTERVAL_TIME)
                        }
                    }
                }
            }
        }
    }

}