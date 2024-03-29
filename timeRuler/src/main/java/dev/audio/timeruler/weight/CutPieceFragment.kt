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
import java.lang.ref.WeakReference


/**
 * 音波上的裁剪片段
 */
class CutPieceFragment(var audio: AudioFragmentWithCut) {

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

    private var cutMode = CUT_MODE_DELETE


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

    /**
     * 裁剪选中的起始时间  ms
     * 相对于自己来说
     * 例：歌曲200*1000ms   起始时间80*1000ms
     */
    private var startTimestampTimeInSelf = 0L

    /**
     * 裁剪选中的结束时间点  ms
     * 相对于自己来说
     * 例：歌曲200*1000ms   起始时间120*1000ms
     */
    private var endTimestampTimeInSelf = 0L


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

    fun initCutFragment() {
        startTimestampTimeInSelf = duration / 3
        endTimestampTimeInSelf = duration / 3 * 2
    }

    fun drawCutFragment(canvas: Canvas) {
        // 绘制代表开始和结束时间戳的线，线的终止位置应在圆圈的下缘
        canvas.drawLine(
            startTimestampPosition, timestampHandlerRadius * 2, startTimestampPosition,
//            height.toFloat(),
            200f, timestampLinePaint
        )
        canvas.drawLine(
            endTimestampPosition, timestampHandlerRadius * 2, endTimestampPosition,
            //            height.toFloat(),
            200f, timestampLinePaint
        )

        // 绘制圆圈标记在直线的顶端
        canvas.drawCircle(
            startTimestampPosition,
            timestampHandlerRadius,
            timestampHandlerRadius,
            timestampHandlerPaint
        )
        canvas.drawCircle(
            endTimestampPosition,
            timestampHandlerRadius,
            timestampHandlerRadius,
            timestampHandlerPaint
        )
        drawCut(canvas)
    }


    private fun drawCut(canvas: Canvas) {
        // 假设已经有了一个Bitmap和Canvas，并且波形已经绘制完成
        val paint = Paint()
        paint.color = Color.YELLOW
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        when (cutMode) {
            CUT_MODE_SELECT -> {
                // 创建覆盖两条竖线中间区域的矩形
                val rect = Rect(
                    startTimestampPosition.toInt() + strokeWidth_cut.toInt(),
                    (rect?.top ?: 0) + strokeWidth.toInt(),
                    endTimestampPosition.toInt() - strokeWidth_cut.toInt(),
                    ((rect?.bottom ?: 0) - strokeWidth.toInt())
                )

                // 在波形图上绘制这个矩形
                canvas.drawRect(rect, paint)
            }

            CUT_MODE_DELETE -> {

                val rectLeft = Rect(
                    0,
                    (rect?.top ?: 0) + strokeWidth.toInt(),
                    startTimestampPosition.toInt() - strokeWidth_cut.toInt(),
                    ((rect?.bottom ?: 0) - strokeWidth.toInt())
                )
                canvas.drawRect(rectLeft, paint)


                val rectRight = Rect(
                    endTimestampPosition.toInt() + strokeWidth_cut.toInt(),
                    (rect?.top ?: 0) + strokeWidth.toInt(),
                    endPositionOfAudio.toInt(),
                    ((rect?.bottom ?: 0) - strokeWidth.toInt())
                )
                canvas.drawRect(rectRight, paint)


//                val rectLeft =
//                    Rect(
//                        startTimestampTimeInTimeline.toInt(),
//                        (rect?.top ?: 0) + strokeWidth.toInt(),
//                        startTimestampPosition.toInt() - strokeWidth_cut.toInt(),
//                        ((rect?.bottom ?: 0) - strokeWidth.toInt())
//                    )
//                canvas.drawRect(rectLeft, paint)


//                val rectRight =
//                    Rect(
//                        endTimestampPosition.toInt() + strokeWidth_cut.toInt(),
//                        (rect?.top ?: 0) + strokeWidth.toInt(),
//                        endTimestampTimeInTimeline.toInt() - strokeWidth_cut.toInt(),
//                        ((rect?.bottom ?: 0) - strokeWidth.toInt())
//                    )
//                canvas.drawRect(rectRight, paint)
            }

            CUT_MODE_JUMP -> {
                // 创建覆盖两条竖线中间区域的矩形
                val rect = Rect(
                    startTimestampPosition.toInt() + strokeWidth_cut.toInt(),
                    (rect?.top ?: 0) + strokeWidth.toInt(),
                    endTimestampPosition.toInt() - strokeWidth_cut.toInt(),
                    ((rect?.bottom ?: 0) - strokeWidth.toInt())
                )

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

    private val moveHandler =
        MoveHandler(audio = WeakReference(audio), cutPiece = WeakReference(this))

    class MoveHandler(
        private var audio: WeakReference<AudioFragmentWithCut>? = null,
        private var cutPiece: WeakReference<CutPieceFragment>? = null
    ) : Handler(Looper.getMainLooper()) {

        companion object {
            const val MSG_MOVE = 1


            //靠边缘的移动速度有以下两个变量控制
            //移动波形图的时间间隔
            const val MOVE_INTERVAL_TIME = 30L

            //移动波形图的像素间隔
            const val MOVE_INTERVAL_SPACE = 5f
        }

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_MOVE -> {
                    // 实现移动波形图的逻辑
                    audio?.get()?.apply {
                        //波形移动
                        this.moveRightByPixel(MOVE_INTERVAL_SPACE)
                        //剪切范围也扩大
                        cutPiece?.get()?.expendRightByPixel(MOVE_INTERVAL_SPACE)
                        sendMessageDelayed(obtainMessage(MSG_MOVE), MOVE_INTERVAL_TIME)
                    }
                }
            }
        }
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
                isMovingStart = Math.abs(x - startTimestampPosition) <= strokeWidth_cut
                isMovingEnd = Math.abs(x - endTimestampPosition) <= strokeWidth_cut
                lastTouchXProcess = x
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(BaseAudioEditorView.cut_tag, "cut onTouchEvent: ACTION_DOWN")
                // 检查控件是否已经初始化宽度
                if (width > 0) {
                    val dx = x - lastTouchXProcess
                    if (isMovingStart) {
                        startTimestampPosition += dx
                        if (startTimestampPosition <= (rect?.left ?: 0)) {
                            //开始小于本身了
                            startTimestampTimeInSelf = 0
                        } else if (startTimestampPosition >= endTimestampPosition - strokeWidth_cut) {
                            //开始大于结束了
                            startTimestampTimeInSelf =
                                endTimestampTimeInSelf - (strokeWidth_cut).pixel2Time(unitMsPixel)
                        } else {
                            startTimestampTimeInSelf += dx.pixel2Time(unitMsPixel)
                        }

                    } else if (isMovingEnd) {
                        if (dx < -10 || event.x <= ScreenUtil.getScreenWidth(context) - 50) {
                            stopMoveRight()
                        }
                        endTimestampPosition += dx
                        if (endTimestampPosition >= (rect?.right ?: 0)) {
                            //结束大于本身了
                            endTimestampTimeInSelf = duration
                        } else if (endTimestampPosition <= startTimestampPosition + strokeWidth_cut) {
                            //结束小于开始了
                            endTimestampTimeInSelf =
                                startTimestampTimeInSelf + (strokeWidth_cut).pixel2Time(unitMsPixel)
                        } else {
                            val newEndTimestampPosition = endTimestampPosition + dx
                            val screenWidth = ScreenUtil.getScreenWidth(context)
                            val maxEndPosition = screenWidth - strokeWidth_cut
                            // 检查是否到达屏幕边界
                            if (newEndTimestampPosition >= maxEndPosition) {
                                // 检查是否有更多波形数据可以加载
                                if (canLoadMoreWaveData(context)) {
                                    moveRight()
                                    // 加载更多波形数据
                                    loadMoreWaveData(newEndTimestampPosition)
                                    // 更新duration和unitMsPixel
                                    updateDurationAndUnitPixel()
                                } else {
                                    // 到达最大范围，不再移动
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
                        Log.i(
                            BaseAudioEditorView.cut_tag, "startTimestampPosition: $this"
                        )
                    }
                    endTimestampPosition.apply {
                        Log.i(BaseAudioEditorView.cut_tag, "endTimestampPosition: $this")
                    }
                    view.invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isMovingStart = false
                isMovingEnd = false
                stopMoveRight()

            }
        }
        return true
    }

    private fun moveRight() {
        moveHandler.sendMessage(moveHandler.obtainMessage(MoveHandler.MSG_MOVE))
    }

    private fun stopMoveRight() {
        moveHandler.removeMessages(MoveHandler.MSG_MOVE)
    }

    private fun canLoadMoreWaveData(context: Context): Boolean {
        // 实现检查是否有更多波形数据可以加载的逻辑
        // 返回true表示可以加载，返回false表示没有更多数据
        return rect?.right ?: 0 > ScreenUtil.getScreenWidth(context)
    }

    private fun loadMoreWaveData(newEndTimestampPosition: Float) {
        // 实现加载更多波形数据的逻辑
        // 这可能涉及到异步操作，需要确保数据加载完成后再更新UI
    }

    private fun updateDurationAndUnitPixel() {
        // 更新duration和unitMsPixel的值
        // 这将影响波形图的显示和时间戳的计算
    }


    fun isTarget(event: MotionEvent): Boolean {
        // 为开始时间戳的圆球创建一个矩形
        val startRect = Rect(
            startTimestampPosition.toInt(),
            timestampHandlerRadius.toInt(),
            startTimestampPosition.toInt() + (timestampHandlerRadius * 2).toInt(),
            timestampHandlerRadius.toInt() + 200
        )

        // 为结束时间戳的圆球创建一个矩形
        val endRect = Rect(
            endTimestampPosition.toInt(),
            timestampHandlerRadius.toInt(),
            endTimestampPosition.toInt() + (timestampHandlerRadius * 2).toInt(),
            timestampHandlerRadius.toInt() + 200
        )

        // 检查触摸点是否落在开始或结束的矩形内
        if (startRect.contains(
                event.x.toInt(),
                event.y.toInt()
            ) || endRect.contains(event.x.toInt(), event.y.toInt())
        ) {
            return true.apply {
                Log.i(BaseAudioEditorView.cut_tag, "isTarget: true")
            }
        }

        return false.apply {
            Log.i(BaseAudioEditorView.cut_tag, "isTarget: false")
        }
    }

    fun setCutMode(cutMode: Int) {
        this.cutMode = cutMode
    }


}