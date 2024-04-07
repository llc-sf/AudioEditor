package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.timeruler.bean.Ref
import dev.audio.timeruler.weight.CutPieceFragment.MoveHandler.Companion.MSG_MOVE_TO_OFFSET
import java.lang.ref.WeakReference


/**
 * 音波上的裁剪片段
 */
class CutPieceFragment(var audio: AudioFragmentWithCut, var isMajor: Boolean = false) {

    companion object {
        //裁剪竖线的宽度
        const val strokeWidth_cut = 5f

        /**
         * 选择剪辑
         */
        const val CUT_MODE_SELECT = 0

        /**
         * 删除剪辑
         */
        const val CUT_MODE_DELETE = 1

        /**
         * 跳剪
         */
        const val CUT_MODE_JUMP = 2
    }

    @IntDef(
        CUT_MODE_SELECT,
        CUT_MODE_DELETE,
        CUT_MODE_JUMP,
    )
    annotation class CutMode

    private var cutMode = CUT_MODE_SELECT


    private val timestampHandlerRadius = 20f
    private val timestampHandlerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = strokeWidth_cut
        style = Paint.Style.STROKE
    }
    private val timestampLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = strokeWidth_cut
    }

    private val startTimestamp by Ref { audio.startTimestamp }
    private val cursorOffsetTime by Ref { audio.cursorOffsetTime }
    private val duration by Ref { audio.duration }
    private val strokeWidth by Ref { audio.strokeWidth }
    private val rect by Ref { audio.rect }
    private val unitMsPixel by Ref { audio.unitMsPixel }


    private val onCutLineChangeListener by Ref { audio.onCutLineChangeListener }

    /**
     * 裁剪选中的起始时间  ms
     * 相对于自己来说
     * 例：歌曲200*1000ms   起始时间80*1000ms
     */
    private var startTimestampTimeInSelf = 0L
        set(value) {
            field = value
            if(isMajor){
                onCutLineChangeListener?.onCutLineChange(startTimestampTimeInSelf, endTimestampTimeInSelf)
            }
        }

    /**
     * 裁剪选中的结束时间点  ms
     * 相对于自己来说
     * 例：歌曲200*1000ms   起始时间120*1000ms
     */
    private var endTimestampTimeInSelf = 0L
        set(value) {
            field = value
            if(isMajor){
                onCutLineChangeListener?.onCutLineChange(startTimestampTimeInSelf, endTimestampTimeInSelf)
            }
        }


    /**
     * 裁剪选中的起始时间点  日历时间  ms时间戳
     */
    private var startTimestampTimeInTimeline = 0L
        get() {
            return startTimestamp + cursorOffsetTime + startTimestampTimeInSelf
        }

    /**
     * 裁剪选中的结束时间  日历时间  ms时间戳
     */
    private var endTimestampTimeInTimeline = 0L
        get() {
            return startTimestamp + cursorOffsetTime + endTimestampTimeInSelf
        }


    /**
     * 裁剪片段开始位置 屏幕上的x坐标
     *
     * 根据时间戳转换为屏幕上的位置，所以无需设置值 关注TimeInSelf
     */
    private var startTimestampPosition: Float = 0f
        get() {
            return audio.time2PositionInTimeline(startTimestampTimeInTimeline)
        }

    /**
     * 裁剪片段的结束位置 屏幕上的x坐标
     *
     * 根据时间戳转换为屏幕上的位置，所以无需设置值
     */
    private var endTimestampPosition: Float = 0f
        get() {
            return audio.time2PositionInTimeline(endTimestampTimeInTimeline)
        }

    /**
     * 歌曲结束时间在屏幕上的位置
     */
    private var endPositionOfAudio: Float = 0f
        get() {
            return audio.time2PositionInTimeline(endTimestampOfAudio)
        }

    /**
     * 歌曲结束时间在时间轴上的位置
     */
    private var endTimestampOfAudio: Long = 0
        get() {
            return startTimestamp + cursorOffsetTime + duration
        }

    /**
     * 是否选中  裁剪
     */
    fun isSelected(x: Float): Boolean {
        return x in startTimestampPosition..endTimestampPosition
    }

    fun initCutFragment(start: Float, end: Float) {
        startTimestampTimeInSelf = (duration * start).toLong()
        endTimestampTimeInSelf = (duration * end).toLong()
    }

    fun drawCutFragment(canvas: Canvas) {
        if (isMajor || cutMode == CUT_MODE_JUMP) {
            drawCutLines(canvas)
        }
        drawCut(canvas)
    }

    private fun drawCutLines(canvas: Canvas) { // 绘制代表开始和结束时间戳的线，线的终止位置应在圆圈的下缘
        canvas.drawLine(startTimestampPosition, timestampHandlerRadius * 2, startTimestampPosition, //            height.toFloat(),
                        rect?.bottom?.toFloat() ?: 0f, timestampLinePaint)
        canvas.drawLine(endTimestampPosition, timestampHandlerRadius * 2, endTimestampPosition, //            height.toFloat(),
                        rect?.bottom?.toFloat()
                            ?: 0f, timestampLinePaint) //        if(startTimestampPosition<0){
        //
        //        }
        //        if(endTimestampPosition>ScreenUtil.getScreenWidth(audio.getContext())){
        //
        //        }
        audio.refreshCutLineAnchor(isMajor && (startTimestampPosition < 0 || startTimestampPosition > ScreenUtil.getScreenWidth(audio.getContext())), isMajor && (endTimestampPosition > ScreenUtil.getScreenWidth(audio.getContext()) || endTimestampPosition < 0)) // 绘制圆圈标记在直线的顶端
        canvas.drawCircle(startTimestampPosition, timestampHandlerRadius, timestampHandlerRadius, timestampHandlerPaint)
        canvas.drawCircle(endTimestampPosition, timestampHandlerRadius, timestampHandlerRadius, timestampHandlerPaint)
    }


    private fun drawCut(canvas: Canvas) { // 假设已经有了一个Bitmap和Canvas，并且波形已经绘制完成
        val paint = Paint()
        paint.color = Color.YELLOW
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        when (cutMode) {
            CUT_MODE_SELECT -> {
                if (!isMajor) {
                    return
                } // 创建覆盖两条竖线中间区域的矩形
                val rect = Rect(startTimestampPosition.toInt() + strokeWidth_cut.toInt(), (rect?.top
                    ?: 0) + strokeWidth.toInt(), endTimestampPosition.toInt() - strokeWidth_cut.toInt(), ((rect?.bottom
                    ?: 0) - strokeWidth.toInt()))

                // 在波形图上绘制这个矩形
                canvas.drawRect(rect, paint)
            }

            CUT_MODE_DELETE -> {
                if (!isMajor) {
                    return
                }
                val rectLeft = Rect(0, (rect?.top
                    ?: 0) + strokeWidth.toInt(), startTimestampPosition.toInt() - strokeWidth_cut.toInt(), ((rect?.bottom
                    ?: 0) - strokeWidth.toInt()))
                canvas.drawRect(rectLeft, paint)


                val rectRight = Rect(endTimestampPosition.toInt() + strokeWidth_cut.toInt(), (rect?.top
                    ?: 0) + strokeWidth.toInt(), endPositionOfAudio.toInt(), ((rect?.bottom
                    ?: 0) - strokeWidth.toInt()))
                canvas.drawRect(rectRight, paint)

            }

            CUT_MODE_JUMP -> { // 创建覆盖两条竖线中间区域的矩形
                val rect = Rect(startTimestampPosition.toInt() + strokeWidth_cut.toInt(), (rect?.top
                    ?: 0) + strokeWidth.toInt(), endTimestampPosition.toInt() - strokeWidth_cut.toInt(), ((rect?.bottom
                    ?: 0) - strokeWidth.toInt()))

                // 在波形图上绘制这个矩形
                canvas.drawRect(rect, paint)
            }
        }

        // 移除Xfermode
        paint.xfermode = null
    }


    private var isMovingStart: Boolean = false
    private var isMovingEnd: Boolean = false
    private var lastTouchXProcess: Float = 0f

    private val moveHandler = MoveHandler(audio = WeakReference(audio), cutPiece = WeakReference(this))

    class MoveHandler(private var audio: WeakReference<AudioFragmentWithCut>? = null,
                      private var cutPiece: WeakReference<CutPieceFragment>? = null) :
        Handler(Looper.getMainLooper()) {

        companion object {
            const val MSG_MOVE = 1
            const val MSG_MOVE_TO_OFFSET = 2

            //靠边缘的移动速度有以下两个变量控制
            //移动波形图的时间间隔
            const val MOVE_INTERVAL_TIME = 30L

            //移动波形图的像素间隔
            const val MOVE_INTERVAL_SPACE = 5f

            //移动波形图的距离
            const val MOVE_STEP_TIME = 500L
            const val MOVE_STEP_PIXEL = 20f

            //移动波形图的时间间隔
            const val MOVE_INTERVAL_TIME_DELAY = 6L

        }

        // 新增一个成员变量来存储剩余的偏移量
        private var remainingOffsetValue = 0L

        //移动波形图的距离
        private var moveStepTime = MOVE_STEP_TIME

        /**
         * 是否固定像素移动
         * true:固定像素移动   各个档位移动速率一样
         * false:固定时间移动  放大档位移动速率快
         */
        private var isMoveByFixedPixel = true
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_MOVE -> { // 实现移动波形图的逻辑
                    audio?.get()?.apply { //波形移动
                        this.moveRightByPixel(MOVE_INTERVAL_SPACE) //剪切范围也扩大
                        cutPiece?.get()?.expendRightByPixel(MOVE_INTERVAL_SPACE)
                        sendMessageDelayed(obtainMessage(MSG_MOVE), MOVE_INTERVAL_TIME)
                    }
                }

                MSG_MOVE_TO_OFFSET -> {
                    if (msg.obj is Long) {
                        remainingOffsetValue = msg.obj as Long
                    }

                    audio?.get()?.apply {
                        if (isMoveByFixedPixel) {
                            moveStepTime = (MOVE_STEP_PIXEL / this.unitMsPixel).toLong()
                        }
                        val stepTimeValue = if (Math.abs(remainingOffsetValue) >= moveStepTime) {
                            moveStepTime
                        } else {
                            Math.abs(remainingOffsetValue)
                        }

                        if (remainingOffsetValue > 0) {
                            cutPiece?.get()?.waveMove(-stepTimeValue)
                            remainingOffsetValue -= stepTimeValue
                        } else {
                            cutPiece?.get()?.waveMove(stepTimeValue)
                            remainingOffsetValue += stepTimeValue
                        }

                        if (Math.abs(remainingOffsetValue) > 0) {
                            sendMessageDelayed(obtainMessage(MSG_MOVE_TO_OFFSET), MOVE_INTERVAL_TIME_DELAY)
                        }
                    }
                }
            }
        }
    }


    /**
     * 移动波形图
     *
     * @param stepTimeValue 移动的时间
     */
    private fun waveMove(stepTimeValue: Long) {
        var tempCursorValue = audio.cursorValue - stepTimeValue
        if (tempCursorValue <= audio.startTimestamp) {
            moveHandler.removeMessages(MSG_MOVE_TO_OFFSET)
            audio.audioEditorView.cursorValue = audio.startTimestamp
            return
        }
        if (tempCursorValue + audio.screenWithDuration >= audio.endTimestamp) {
            moveHandler.removeMessages(MSG_MOVE_TO_OFFSET)
            audio.audioEditorView.cursorValue = audio.endTimestamp - audio.screenWithDuration
            return
        }
        audio.audioEditorView.cursorValue = tempCursorValue
    }

    /**
     * 裁剪范围向右扩展
     *
     * 扩展的变量为像素
     */
    private fun expendRightByPixel(moveIntervalSpace: Float) {
        endTimestampTimeInSelf += (moveIntervalSpace.pixel2Time(unitMsPixel))
    }

    /**
     * 裁剪范围向右扩展
     *
     * 扩展的变量为时间
     */
    private fun expendRightByTime(time: Long) {
        endTimestampTimeInSelf += time
    }


    fun onTouchEvent(context: Context, view: View, event: MotionEvent?): Boolean {
        Log.i(BaseAudioEditorView.cut_tag, "onTouchEvent: ")
        if (event == null) return false
        var width = ScreenUtil.getScreenWidth(context)
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMovingStart = (event.x <= startRect.right + 20 && event.x >= startRect.left - 20)
                isMovingEnd = (event.x <= endRect.right + 20 && event.x >= endRect.left - 20)
                lastTouchXProcess = x
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(BaseAudioEditorView.cut_tag, "cut onTouchEvent: ACTION_DOWN") // 检查控件是否已经初始化宽度
                if (width > 0) {
                    val dx = x - lastTouchXProcess
                    if (isMovingStart) {
                        startTimestampPosition += dx
                        if (startTimestampPosition <= (rect?.left ?: 0)) { //开始小于本身了
                            startTimestampTimeInSelf = 0
                        } else if (startTimestampPosition >= endTimestampPosition - strokeWidth_cut) { //开始大于结束了
                            startTimestampTimeInSelf = endTimestampTimeInSelf - (strokeWidth_cut).pixel2Time(unitMsPixel)
                        } else {
                            startTimestampTimeInSelf += dx.pixel2Time(unitMsPixel)
                        }

                    } else if (isMovingEnd) {
                        if (dx < -10 || event.x <= ScreenUtil.getScreenWidth(context) - 50) {
                            stopMoveRight()
                        }
                        endTimestampPosition += dx
                        if (endTimestampPosition >= (rect?.right ?: 0)) { //结束大于本身了
                            endTimestampTimeInSelf = duration
                        } else if (endTimestampPosition <= startTimestampPosition + strokeWidth_cut) { //结束小于开始了
                            endTimestampTimeInSelf = startTimestampTimeInSelf + (strokeWidth_cut).pixel2Time(unitMsPixel)
                        } else {
                            val newEndTimestampPosition = endTimestampPosition + dx
                            val screenWidth = ScreenUtil.getScreenWidth(context)
                            val maxEndPosition = screenWidth - strokeWidth_cut // 检查是否到达屏幕边界
                            if (newEndTimestampPosition >= maxEndPosition) { // 检查是否有更多波形数据可以加载
                                if (canLoadMoreWaveData(context)) {
                                    moveRight() // 加载更多波形数据
                                    loadMoreWaveData(newEndTimestampPosition) // 更新duration和unitMsPixel
                                    updateDurationAndUnitPixel()
                                } else { // 到达最大范围，不再移动
                                    endTimestampPosition = maxEndPosition
                                }
                            } else {
                                endTimestampPosition = newEndTimestampPosition
                            }

                            endTimestampTimeInSelf += dx.pixel2Time(unitMsPixel)
                        }
                    }
                    lastTouchXProcess = x
                    startTimestampPosition.apply {
                        Log.i(BaseAudioEditorView.cut_tag, "startTimestampPosition: $this")
                    }
                    endTimestampPosition.apply {
                        Log.i(BaseAudioEditorView.cut_tag, "endTimestampPosition: $this")
                    }
                    view.invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isMovingEnd || isMovingStart) {
                    cutLineMove2Middle(event)
                }
                isMovingStart = false
                isMovingEnd = false
                stopMoveRight()

            }
        }
        return true
    }


    /**
     * 移动裁剪线到中间
     */
    private fun cutLineMove2Middle(event: MotionEvent) {
        var offsetTimeValue = ((event.x - ScreenUtil.getScreenWidth(audio.getContext()) / 2) / unitMsPixel).toLong()
        moveHandler.removeMessages(MSG_MOVE_TO_OFFSET)
        moveHandler.sendMessage(moveHandler.obtainMessage(MSG_MOVE_TO_OFFSET, offsetTimeValue))
    }

    private fun moveRight() {
        moveHandler.sendMessage(moveHandler.obtainMessage(MoveHandler.MSG_MOVE))
    }

    private fun stopMoveRight() {
        moveHandler.removeMessages(MoveHandler.MSG_MOVE)
    }

    private fun canLoadMoreWaveData(context: Context): Boolean { // 实现检查是否有更多波形数据可以加载的逻辑
        // 返回true表示可以加载，返回false表示没有更多数据
        return rect?.right ?: 0 > ScreenUtil.getScreenWidth(context)
    }

    private fun loadMoreWaveData(newEndTimestampPosition: Float) { // 实现加载更多波形数据的逻辑
        // 这可能涉及到异步操作，需要确保数据加载完成后再更新UI
    }

    private fun updateDurationAndUnitPixel() { // 更新duration和unitMsPixel的值
        // 这将影响波形图的显示和时间戳的计算
    }

    // 为开始时间戳的圆球创建一个矩形
    private var startRect: Rect = Rect()
        get() {
            return Rect(startTimestampPosition.toInt(), timestampHandlerRadius.toInt(), startTimestampPosition.toInt() + (timestampHandlerRadius * 2).toInt(), timestampHandlerRadius.toInt() + 200)
        }


    // 为结束时间戳的圆球创建一个矩形
    private var endRect: Rect = Rect()
        get() {
            return Rect(endTimestampPosition.toInt(), timestampHandlerRadius.toInt(), endTimestampPosition.toInt() + (timestampHandlerRadius * 2).toInt(), timestampHandlerRadius.toInt() + 200)
        }

    fun isTarget(event: MotionEvent?): Boolean {
        if (event == null) return false
        return (event.x <= startRect.right + 20 && event.x >= startRect.left - 20).apply {

        } || (event.x <= endRect.right + 20 && event.x >= endRect.left - 20).apply {

        }
    }

    fun setCutMode(cutMode: Int) {
        this.cutMode = cutMode
    }

    fun anchor2CutEndLine() {
        if (isMajor) {
            var offsetTimeValue = ((ScreenUtil.getScreenWidth(audio.getContext())
                .toFloat() / 2 - endTimestampPosition) / unitMsPixel).toLong()
            var tempCursor = audio.cursorValue - offsetTimeValue
            if (tempCursor + audio.screenWithDuration > audio.endTimestamp) { //不足了
                tempCursor = audio.endTimestamp - audio.screenWithDuration
            }
            audio.audioEditorView.cursorValue = tempCursor
        }
    }

    fun anchor2CutStartLine() {
        if (isMajor) {
            var offsetTimeValue = ((ScreenUtil.getScreenWidth(audio.getContext())
                .toFloat() / 2 - startTimestampPosition) / unitMsPixel).toLong()
            var tempCursor = audio.cursorValue - offsetTimeValue
            if (tempCursor < audio.startTimestamp) { //不足了
                tempCursor = audio.startTimestamp
            }
            audio.audioEditorView.cursorValue = tempCursor
        }
    }
}