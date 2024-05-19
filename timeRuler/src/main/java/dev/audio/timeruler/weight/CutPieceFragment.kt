package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.content.ContextCompat
import com.android.app.AppProvider
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.timeruler.BuildConfig
import dev.audio.timeruler.R
import dev.audio.timeruler.bean.CutPieceBean
import dev.audio.timeruler.bean.Ref
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.utils.isTouch
import dev.audio.timeruler.weight.CutPieceFragment.MoveHandler.Companion.MSG_MOVE_TO_OFFSET
import java.lang.ref.WeakReference


/**
 * 音波上的裁剪片段
 * @param audio 音波
 * @param isSelected 是否选中
 * @param index 第几个裁剪片段
 */
class CutPieceFragment(var audio: AudioFragmentWithCut,
                       var isSelected: Boolean = true,
                       var index: Int,
                       mode: Int = CUT_MODE_SELECT,
                       var isFake: Boolean = false,
                       var addTime: Long = System.currentTimeMillis()) {

    companion object {

        //裁剪片段的最小宽度
        const val MIN_CUT_GAP = 100

        //相邻裁剪片段的最小间隔
        const val MIN_CUT_ADJACENT = 100

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

        const val TIME_STEP = 100L
    }

    @IntDef(
        CUT_MODE_SELECT,
        CUT_MODE_DELETE,
        CUT_MODE_JUMP,
    )
    annotation class CutMode

    /**
     * 区别 switchCutMode
     */
    var cutMode = mode
        set(value) {
            field = value
            linesChangeNotify()
        }

    /**
     * 剪切模式切换专用
     */
    fun switchCutMode(@CutMode mode: Int) {
        this.cutMode = mode //重点
        if (audio.currentCutPieceFragment == null) {
            isSelected = index == 0
        } //todo  非跳裁剪转跳剪 可能存在重合的情况
        initEndHandleBitmap(true)
        initStartHandleBitmap(true)
        audio.invalidate()
    }


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
    private val baselinePosition by Ref { audio.baselinePosition }
    private val isWholeScreen by Ref { audio.isWholeScreen }

    private val onCutLineChangeListener by Ref { audio.onCutLineChangeListener }
    private val onTrimAnchorChangeListener by Ref { audio.onTrimAnchorChangeListener }
    private val cutModeChangeButtonEnableListener by Ref { audio.cutModeChangeButtonEnableListener }

    var cutPieceBean = CutPieceBean()
        get() {
            field.startTimestampTimeInSelf = startTimestampTimeInTimeline
            field.endTimestampTimeInSelf = endTimestampTimeInTimeline
            return field
        }


    /**
     * 裁剪选中的起始时间  ms
     * 相对于自己来说
     * 例：歌曲200*1000ms   起始时间80*1000ms
     */
    var startTimestampTimeInSelf = 0L
        set(value) {
            field = value
            if (isSelected) {
                onCutLineChangeListener?.onCutLineChange(startTimestampTimeInSelf, endTimestampTimeInSelf)
                linesChangeNotify()
            }
        }

    /**
     * 裁剪选中的结束时间点  ms
     * 相对于自己来说
     * 例：歌曲200*1000ms   起始时间120*1000ms
     */
    var endTimestampTimeInSelf = 0L
        set(value) {
            field = value
            if (isSelected) {
                onCutLineChangeListener?.onCutLineChange(startTimestampTimeInSelf, endTimestampTimeInSelf)
                linesChangeNotify()
            }
        }

    /**
     * 裁剪条或者播放条变化
     * 1、播放条移动时
     * 2、裁剪模式变化
     * 3、裁剪条移动
     */
    fun linesChangeNotify() {
        if (isSelected) {
            if (cutMode == CUT_MODE_DELETE) {
                if (audio.currentPlayingTimeInAudio < this.endTimestampTimeInSelf && audio.currentPlayingTimeInAudio > this.startTimestampTimeInSelf) {
                    onTrimAnchorChangeListener?.onTrimChange(start = true, end = true)
                } else {
                    onTrimAnchorChangeListener?.onTrimChange(audio.currentPlayingTimeInAudio < this.startTimestampTimeInSelf, audio.currentPlayingTimeInAudio > this.endTimestampTimeInSelf)
                }
            } else if (cutMode == CUT_MODE_SELECT) {
                onTrimAnchorChangeListener?.onTrimChange(start = true, end = true)
            }
        }

        when (cutMode) {
            CUT_MODE_SELECT, CUT_MODE_DELETE -> {
                cutModeChangeButtonEnableListener?.onCutModeChange(addEnable = false, removeEnable = false)
            }

            CUT_MODE_JUMP -> {
                var addEnable = true
                var removeEnable = false
                audio.cutPieceFragments.filter { !it.isFake }
                    .forEachIndexed { _, cutPieceFragment ->
                        addEnable = addEnable && (audio.currentPlayingTimeInAudio < cutPieceFragment.startTimestampTimeInSelf || audio.currentPlayingTimeInAudio > cutPieceFragment.endTimestampTimeInSelf)
                        removeEnable = removeEnable || (audio.currentPlayingTimeInAudio >= cutPieceFragment.startTimestampTimeInSelf && audio.currentPlayingTimeInAudio <= cutPieceFragment.endTimestampTimeInSelf)
                    }
                cutModeChangeButtonEnableListener?.onCutModeChange(addEnable, removeEnable) //播放条在裁剪范围，变为选中态，且取消其他范围的选中态
                //动态更新选中态
                if (isSelected && !isMovingStart && !isMovingEnd) {
                    audio.cutPieceFragments.forEach {
                        if (audio.currentPlayingTimeInAudio in it.startTimestampTimeInSelf..it.endTimestampTimeInSelf) {
                            if (!it.isSelected) {
                                it.isSelected = true
                                audio.cutLineFineTuningButtonChangeListener?.onCutLineFineTuningEnable(true)
                                onCutLineChangeListener?.onCutLineChange(it.startTimestampTimeInSelf, it.endTimestampTimeInSelf)
                                audio.invalidate()
                            }
                        } else {
                            if (it.isSelected) {
                                it.isSelected = false
                                audio.invalidate()
                            }
                        }
                    }
                }
            }
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

    fun initCutFragment(start: Long, end: Long) {
        startTimestampTimeInSelf = if (start < 0) 0 else start
        endTimestampTimeInSelf = if (end > duration) duration else end
    }


    fun drawCutBg(canvas: Canvas) {
        if (isFake) {
            return
        }
        when (cutMode) {
            CUT_MODE_SELECT, CUT_MODE_JUMP -> {
                var bitmap = BitmapFactory.decodeResource(audio.getContext()?.resources, R.drawable.cut_piece_bg)
                var rect = Rect(startTimestampPosition.toInt(), baselinePosition.toInt(), endTimestampPosition.toInt(), rect?.bottom
                    ?: 0)
                canvas.drawBitmap(bitmap, null, rect, null)
            }

            CUT_MODE_DELETE -> {
                var bitmap = BitmapFactory.decodeResource(audio.getContext()?.resources, R.drawable.cut_piece_bg)

                var rectStart = Rect(0, baselinePosition.toInt(), startTimestampPosition.toInt(), rect?.bottom
                    ?: 0)
                canvas.drawBitmap(bitmap, null, rectStart, null)

                var rectEnd = Rect(endTimestampPosition.toInt(), baselinePosition.toInt(), ScreenUtil.getScreenWidth(audio.getContext()), rect?.bottom
                    ?: 0)
                canvas.drawBitmap(bitmap, null, rectEnd, null)
            }

        }

    }


    fun drawCutLines(canvas: Canvas) { // 绘制代表开始和结束时间戳的线，线的终止位置应在圆圈的下缘
        if (isFake) {
            return
        }
        if (isSelected || cutMode == CUT_MODE_JUMP) {
            canvas.drawLine(startTimestampPosition, baselinePosition, startTimestampPosition, //            height.toFloat(),
                            rect?.bottom?.toFloat() ?: 0f, timestampLinePaint)
            canvas.drawLine(endTimestampPosition, baselinePosition, endTimestampPosition, //            height.toFloat(),
                            rect?.bottom?.toFloat() ?: 0f, timestampLinePaint)
            audio.refreshCutLineAnchor(isSelected && (startTimestampPosition < 0 || startTimestampPosition > ScreenUtil.getScreenWidth(audio.getContext())), isSelected && (endTimestampPosition > ScreenUtil.getScreenWidth(audio.getContext()) || endTimestampPosition < 0)) // 绘制圆圈标记在直线的顶端
            drawStartHandle(canvas)
            drawEndHandle(canvas)
        }
    }

    fun isCutLineStartVisible(): Boolean {
        return isSelected && (startTimestampPosition < 0 || startTimestampPosition > ScreenUtil.getScreenWidth(audio.getContext()))
    }

    fun isCutLineEndVisible(): Boolean {
        return isSelected && (endTimestampPosition > ScreenUtil.getScreenWidth(audio.getContext()) || endTimestampPosition < 0)
    }

    private var startHandleBitmap: Bitmap? = null
    private var startHandleTouchRect = Rect()
    private var startHandleRealRect = Rect()
    private var startMargin = ScreenUtil.dp2px(audio.getContext(), 12 + 10) //10为keyTickHeight高度

    // 绘制把手的函数，假定它是在你提供的代码的类中
    private fun drawStartHandle(canvas: Canvas) {
        initStartHandleBitmap()
        if (isSelected) {
            val handleXStart = startTimestampPosition - startHandleBitmap!!.width / 2
            val handleYStart = baselinePosition + startMargin
            var clickPadding = ScreenUtil.dp2px(audio.getContext(), 5)
            startHandleTouchRect = Rect(handleXStart.toInt() - clickPadding, handleYStart.toInt() - clickPadding, (handleXStart + startHandleBitmap!!.width).toInt() + clickPadding, (handleYStart + startHandleBitmap!!.height).toInt() + clickPadding)
            startHandleRealRect = Rect(handleXStart.toInt(), handleYStart.toInt(), (handleXStart + startHandleBitmap!!.width).toInt(), (handleYStart + startHandleBitmap!!.height).toInt())
            canvas.drawBitmap(startHandleBitmap!!, handleXStart, handleYStart, null)
            if (BuildConfig.DEBUG) { //画空心rect
                val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.GREEN
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                }
                canvas.drawRect(startHandleTouchRect, rectPaint)
                rectPaint.color = Color.BLUE
                canvas.drawRect(startHandleRealRect, rectPaint)
            }
        }
    }

    private fun initStartHandleBitmap(forceInit: Boolean = false) {
        if (startHandleBitmap == null || forceInit) {
            startHandleBitmap = BitmapFactory.decodeResource(audio.getContext()?.resources, if (cutMode == CUT_MODE_DELETE) R.mipmap.ic_bar_right else R.mipmap.ic_bar_left)
            startHandleBitmap = Bitmap.createScaledBitmap(startHandleBitmap!!, startHandleBitmap!!.width * 2, startHandleBitmap!!.height * 2, true)
        }
    }


    private var endHandleBitmap: Bitmap? = null
    private var endHandleTouchRect = Rect()
    private var endHandleRealRect = Rect()
    private var endMargin = ScreenUtil.dp2px(audio.getContext(), 12)

    /**
     * 剪切条把手
     */
    private fun drawEndHandle(canvas: Canvas) {
        initEndHandleBitmap()
        if (isSelected) {
            val handleXEnd = endTimestampPosition - endHandleBitmap!!.width / 2
            val handleYEnd = ((rect?.bottom ?: 0) - endHandleBitmap!!.height - endMargin).toFloat()
            var clickPadding = ScreenUtil.dp2px(audio.getContext(), 5)
            endHandleTouchRect = Rect(handleXEnd.toInt() - clickPadding, handleYEnd.toInt() - clickPadding, (handleXEnd + endHandleBitmap!!.width).toInt() + clickPadding, (handleYEnd + endHandleBitmap!!.height).toInt() + clickPadding) // 绘制把手Bitmap在开始位置
            endHandleRealRect = Rect(handleXEnd.toInt(), handleYEnd.toInt(), (handleXEnd + endHandleBitmap!!.width).toInt(), (handleYEnd + endHandleBitmap!!.height).toInt()) // 绘制把手Bitmap在开始位置
            canvas.drawBitmap(endHandleBitmap!!, handleXEnd, handleYEnd, null)
            if (BuildConfig.DEBUG) { //画空心rect
                val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = Color.GREEN
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = strokeWidth
                }
                canvas.drawRect(endHandleTouchRect, rectPaint)
                rectPaint.color = Color.BLUE
                canvas.drawRect(endHandleRealRect, rectPaint)
            }
        }
    }

    /**
     * 剪切条把手
     */
    private fun initEndHandleBitmap(forceInit: Boolean = false) {
        if (endHandleBitmap == null || forceInit) {
            endHandleBitmap = BitmapFactory.decodeResource(audio.getContext()?.resources, if (cutMode == CUT_MODE_DELETE) R.mipmap.ic_bar_left else R.mipmap.ic_bar_right)
            endHandleBitmap = Bitmap.createScaledBitmap(endHandleBitmap!!, endHandleBitmap!!.width * 2, endHandleBitmap!!.height * 2, true)
        }
    }


    fun drawCut(canvas: Canvas) { // 假设已经有了一个Bitmap和Canvas，并且波形已经绘制完成
        if (isFake) {
            return
        }
        val paint = Paint()
        paint.color = ContextCompat.getColor(AppProvider.context, R.color.color_fe2b54)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        when (cutMode) {
            CUT_MODE_SELECT -> {
                if (!isSelected) {
                    return
                } // 创建覆盖两条竖线中间区域的矩形
                val rect = Rect(startTimestampPosition.toInt() + strokeWidth_cut.toInt(), (rect?.top
                    ?: 0) + strokeWidth.toInt(), endTimestampPosition.toInt() - strokeWidth_cut.toInt(), ((rect?.bottom
                    ?: 0) - strokeWidth.toInt()))

                // 在波形图上绘制这个矩形
                canvas.drawRect(rect, paint)
            }

            CUT_MODE_DELETE -> {
                if (!isSelected) {
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
            const val MSG_MOVE_TO_OFFSET = 100
            const val MSG_MOVE_END_OF_END = 1000 //结束裁剪条向右移动
            const val MSG_MOVE_START_OF_START = 1001 //开始裁剪条向做移动
            const val MSG_MOVE_END_OF_START = 1002 //开始裁剪条向右移动
            const val MSG_MOVE_START_OF_END = 1003 //结束裁剪条向左移动

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
                MSG_MOVE_END_OF_END -> { // 实现移动波形图的逻辑
                    audio?.get()?.apply {
                        if (canLoadMoreWaveDataToEnd()) { //波形移动
                            when (cutMode) {
                                CUT_MODE_SELECT -> {
                                    this.moveRightByPixel(MOVE_INTERVAL_SPACE) //剪切范围也扩大
                                    cutPiece?.get()?.expendRightByPixel(MOVE_INTERVAL_SPACE)
                                    sendMessageDelayed(obtainMessage(MSG_MOVE_END_OF_END), MOVE_INTERVAL_TIME)
                                }

                                CUT_MODE_DELETE -> {
                                    this.moveRightByPixel(MOVE_INTERVAL_SPACE) //剪切范围也扩大
                                    cutPiece?.get()?.expendRightByPixel(MOVE_INTERVAL_SPACE)
                                    sendMessageDelayed(obtainMessage(MSG_MOVE_END_OF_END), MOVE_INTERVAL_TIME)
                                }

                                CUT_MODE_JUMP -> { //防止交叉
                                    if (cutPiece?.get()
                                            ?.expendRightByPixel(MOVE_INTERVAL_SPACE) == true
                                    ) {
                                        this.moveRightByPixel(MOVE_INTERVAL_SPACE) //剪切范围也扩大
                                        sendMessageDelayed(obtainMessage(MSG_MOVE_END_OF_END), MOVE_INTERVAL_TIME)
                                    }
                                }
                            }


                        }
                    }
                }

                MSG_MOVE_END_OF_START -> {
                    audio?.get()?.apply {
                        if (cutPiece?.get()?.startCutLineCanMoveEnd(MOVE_INTERVAL_SPACE) == true) {
                            this.moveRightByPixel(MOVE_INTERVAL_SPACE)
                            sendMessageDelayed(obtainMessage(MSG_MOVE_END_OF_START), MOVE_INTERVAL_TIME)
                        }
                    }
                }

                MSG_MOVE_START_OF_END -> {
                    audio?.get()?.apply {
                        if (cutPiece?.get()?.endCutLineCanMoveStart(MOVE_INTERVAL_SPACE) == true) {
                            this.moveStartByPixel(MOVE_INTERVAL_SPACE)
                            sendMessageDelayed(obtainMessage(MSG_MOVE_START_OF_END), MOVE_INTERVAL_TIME)
                        }
                    }
                }

                MSG_MOVE_START_OF_START -> { // 实现移动波形图的逻辑  开始方向
                    audio?.get()?.apply { //波形移动
                        if (canLoadMoreWaveDataToStart()) {
                            when (cutMode) {
                                CUT_MODE_SELECT -> {
                                    moveStartByPixel(MOVE_INTERVAL_SPACE) //剪切范围也扩大
                                    cutPiece?.get()?.expendStartByPixel(MOVE_INTERVAL_SPACE)
                                    sendMessageDelayed(obtainMessage(MSG_MOVE_START_OF_START), MOVE_INTERVAL_TIME)
                                }

                                CUT_MODE_DELETE -> {
                                    moveStartByPixel(MOVE_INTERVAL_SPACE) //剪切范围也扩大
                                    cutPiece?.get()?.expendStartByPixel(MOVE_INTERVAL_SPACE)
                                    sendMessageDelayed(obtainMessage(MSG_MOVE_START_OF_START), MOVE_INTERVAL_TIME)
                                }

                                CUT_MODE_JUMP -> { //防止交叉
                                    if (cutPiece?.get()
                                            ?.expendStartByPixel(MOVE_INTERVAL_SPACE) == true
                                    ) {
                                        moveStartByPixel(MOVE_INTERVAL_SPACE) //剪切范围也扩大
                                        sendMessageDelayed(obtainMessage(MSG_MOVE_START_OF_START), MOVE_INTERVAL_TIME)
                                    }
                                }


                            }

                        }
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
        audio?.moveWave(stepTimeValue)
    }

    /**
     * 裁剪范围向右扩展
     *
     * 扩展的变量为像素
     */
    private fun expendRightByPixel(moveIntervalSpace: Float): Boolean {
        var newEndTimestampTimeInSelf = endTimestampTimeInSelf + (moveIntervalSpace.pixel2Time(unitMsPixel))
        if (isInOtherFragments(newEndTimestampTimeInSelf)) {
            return false
        }
        endTimestampTimeInSelf += (moveIntervalSpace.pixel2Time(unitMsPixel))
        return true
    }

    /**
     * 裁剪范围向右扩展
     *
     * 扩展的变量为像素
     */
    private fun expendStartByPixel(moveIntervalSpace: Float): Boolean {
        var newStartTimestampTimeInSelf = startTimestampTimeInSelf - (moveIntervalSpace.pixel2Time(unitMsPixel))
        if (isInOtherFragments(newStartTimestampTimeInSelf)) {
            return false
        }
        startTimestampTimeInSelf -= (moveIntervalSpace.pixel2Time(unitMsPixel))
        return true
    }

    /**
     * 裁剪范围向右扩展
     *
     * 扩展的变量为时间
     */
    private fun expendRightByTime(time: Long) {
        endTimestampTimeInSelf += time
    }


    //用于正在播放时候裁剪条拖动后恢复播放
    private var resumePlaying = false
    fun onTouchEvent(context: Context, view: View, event: MotionEvent?): Boolean {
        Log.i(BaseAudioEditorView.cut_tag, "onTouchEvent: ")
        if (event == null) return false
        var width = ScreenUtil.getScreenWidth(context)
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMovingStart = startHandleTouchRect.isTouch(event)
                isMovingEnd = endHandleTouchRect.isTouch(event)
                onCutLineChangeListener?.onCutLineLight(isMovingStart, isMovingEnd)
                if (isMovingStart || isMovingEnd) {
                    if (PlayerManager.isPlaying) {
                        resumePlaying = true
                    } //                    PlayerManager.pause()
                }
                lastTouchXProcess = x
                Log.i(BaseAudioEditorView.cut_tag, "cut onTouchEvent: ACTION_DOWN isMovingStart=$isMovingStart isMovingEnd=$isMovingEnd")
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(BaseAudioEditorView.cut_tag, "cut onTouchEvent: ACTION_MOVE") // 检查控件是否已经初始化宽度
                if (width > 0) {
                    val dx = x - lastTouchXProcess
                    if (isMovingStart) {
                        onCutLineChangeListener?.onCutLineMove()
                        if (dx > 10 || event.x >= 50) {
                            stopMoveStart()
                            stopMove()
                        }
                        startTimestampPosition += dx
                        if (startTimestampPosition <= (rect?.left ?: 0)) { //开始小于本身了
                            startTimestampTimeInSelf = 0
                        } else if (dx > 0 && startTimestampPosition >= endTimestampPosition - MIN_CUT_GAP * unitMsPixel) { //开始大于结束了
                            startTimestampTimeInSelf = endTimestampTimeInSelf - MIN_CUT_GAP
                        } else if (cutMode == CUT_MODE_JUMP && isInOtherFragments(startTimestampTimeInSelf + dx.pixel2Time(unitMsPixel))) {
                            startTimestampTimeInSelf = getFragmentInOtherFragments(endTimestampTimeInSelf + dx.pixel2Time(unitMsPixel))?.endTimestampTimeInSelf
                                ?: startTimestampTimeInSelf
                        } else {
                            startTimestampTimeInSelf += dx.pixel2Time(unitMsPixel)

                            val newStartTimestampPosition = startTimestampPosition
                            val minStartPosition = strokeWidth_cut + endHandleTouchRect.width() // 检查是否到达屏幕边界
                            val maxEndPosition = ScreenUtil.getScreenWidth(context) - strokeWidth_cut - endHandleTouchRect.width() // 检查是否到达屏幕边界
                            if (newStartTimestampPosition < minStartPosition) { //做移动到边缘
                                if (canLoadMoreWaveDataToStart()) {
                                    moveStart()
                                    loadMoreWaveData(newStartTimestampPosition) // 更新duration和unitMsPixel
                                    updateDurationAndUnitPixel()
                                } else {
                                    stopMoveStart()
                                }
                            } else if (newStartTimestampPosition >= maxEndPosition) { //向右移动到边缘
                                if (canLoadMoreWaveDataToEnd(context)) {
                                    moveEndOfStart() // 加载更多波形数据
                                    loadMoreWaveData(newStartTimestampPosition) // 更新duration和unitMsPixel
                                    updateDurationAndUnitPixel()
                                } else {
                                    stopMoveEndOfStart()
                                }
                            } else {
                                startTimestampPosition = newStartTimestampPosition
                            }

                        } //容错 开始时间大于结束时间了
                        if (startTimestampPosition > endTimestampPosition) {
                            startTimestampTimeInSelf = endTimestampTimeInSelf
                        }

                    } else if (isMovingEnd) {
                        onCutLineChangeListener?.onCutLineMove()
                        if (dx < -10 || event.x <= ScreenUtil.getScreenWidth(context) - 50) {
                            stopMoveRight()
                            stopMove()
                        }
                        endTimestampPosition += dx
                        if (endTimestampPosition >= (rect?.right ?: 0)) { //结束大于本身了
                            endTimestampTimeInSelf = duration
                        } else if (dx < 0 && endTimestampPosition <= startTimestampPosition + MIN_CUT_GAP * unitMsPixel) { //结束小于开始了
                            endTimestampTimeInSelf = startTimestampTimeInSelf + MIN_CUT_GAP
                        } else if (cutMode == CUT_MODE_JUMP && isInOtherFragments(endTimestampTimeInSelf + dx.pixel2Time(unitMsPixel))) {
                            endTimestampTimeInSelf = getFragmentInOtherFragments(endTimestampTimeInSelf + dx.pixel2Time(unitMsPixel))?.startTimestampTimeInSelf
                                ?: endTimestampTimeInSelf
                        } else {
                            val newEndTimestampPosition = endTimestampPosition + dx
                            val screenWidth = ScreenUtil.getScreenWidth(context)
                            val maxEndPosition = screenWidth - strokeWidth_cut - endHandleTouchRect.width() // 检查是否到达屏幕边界
                            val minStartPosition = strokeWidth_cut + endHandleTouchRect.width() // 检查是否到达屏幕边界
                            if (newEndTimestampPosition >= maxEndPosition) { // 检查是否有更多波形数据可以加载
                                //向右移动到边缘
                                if (canLoadMoreWaveDataToEnd(context)) {
                                    moveRight() // 加载更多波形数据
                                    loadMoreWaveData(newEndTimestampPosition) // 更新duration和unitMsPixel
                                    updateDurationAndUnitPixel()
                                } else { // 到达最大范围，不再移动
                                    endTimestampPosition = maxEndPosition
                                    stopMoveRight()
                                }
                            } else if (newEndTimestampPosition < minStartPosition) { //向左移动到边缘
                                if (canLoadMoreWaveDataToStart()) {
                                    moveStartOfEnd()
                                } else {
                                    stopMoveStartOfEnd()
                                }
                            } else {
                                endTimestampPosition = newEndTimestampPosition
                            }

                            endTimestampTimeInSelf += dx.pixel2Time(unitMsPixel) //容错 结束时间小于开始时间了
                            if (endTimestampPosition < startTimestampPosition) {
                                endTimestampTimeInSelf = startTimestampTimeInSelf
                            }
                        }
                    }
                    lastTouchXProcess = x
                    startTimestampPosition.apply {
                        Log.i(BaseAudioEditorView.cut_tag, "startTimestampPosition: $this")
                    }
                    endTimestampPosition.apply {
                        Log.i(BaseAudioEditorView.cut_tag, "endTimestampPosition: $this")
                    }
                    checkPlayingLine()
                    view.invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isMovingEnd || isMovingStart) {
                    cutLineMove2Middle(event)
                    checkPlayingLine()
                    if (resumePlaying) {
                        if (isWholeScreen == true) {
                            PlayerManager.play()
                        }
                    }
                    resumePlaying = false
                }
                isMovingStart = false
                isMovingEnd = false
                stopMoveRight()
                stopMoveStart()
                onCutLineChangeListener?.onCutLineLight(isMovingStart, isMovingEnd)
            }
        }
        return true
    }

    /**
     * 裁剪条松手检查
     */
    private fun checkPlayingLine() {
        audio.updateMediaSource(startTimestampTimeInSelf, endTimestampTimeInSelf)
        when (cutMode) {
            CUT_MODE_SELECT -> {
                if (audio.currentPlayingTimeInAudio > this.endTimestampTimeInSelf || audio.currentPlayingTimeInAudio < this.startTimestampTimeInSelf) {
                    audio.updatePlayingPosition(startTimestampTimeInSelf)
                    PlayerManager.seekTo(0)
                }
            }

            CUT_MODE_DELETE -> {
                if (audio.currentPlayingTimeInAudio < this.endTimestampTimeInSelf && audio.currentPlayingTimeInAudio > this.startTimestampTimeInSelf) {
                    PlayerManager.seekTo(0)
                    audio?.updatePlayingPosition(0)
                }
            }

            CUT_MODE_JUMP -> {
                if (!audio.isPlayingLineInAnyCutPiece(audio.currentPlayingTimeInAudio)) {
                    audio.updatePlayingPosition(startTimestampTimeInSelf)
                    var indexOrder = audio.playingLineIndexInFragments(startTimestampTimeInSelf)
                    PlayerManager.seekTo(0, index = indexOrder)
                }
            }
        }
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
        moveHandler.sendMessage(moveHandler.obtainMessage(MoveHandler.MSG_MOVE_END_OF_END))
    }

    private fun moveStart() {
        moveHandler.sendMessage(moveHandler.obtainMessage(MoveHandler.MSG_MOVE_START_OF_START))
    }

    private fun moveEndOfStart() {
        moveHandler.sendMessage(moveHandler.obtainMessage(MoveHandler.MSG_MOVE_END_OF_START))
    }

    private fun moveStartOfEnd() {
        moveHandler.sendMessage(moveHandler.obtainMessage(MoveHandler.MSG_MOVE_START_OF_END))
    }

    private fun stopMoveRight() {
        moveHandler.removeMessages(MoveHandler.MSG_MOVE_END_OF_END)
    }

    private fun stopMoveStartOfEnd() {
        moveHandler.removeMessages(MoveHandler.MSG_MOVE_START_OF_END)
    }

    private fun stopMoveEndOfStart() {
        moveHandler.removeMessages(MoveHandler.MSG_MOVE_END_OF_START)
    }

    private fun stopMoveStart() {
        moveHandler.removeMessages(MoveHandler.MSG_MOVE_START_OF_START)
    }

    private fun stopMove() {
        moveHandler.removeMessages(MoveHandler.MSG_MOVE_END_OF_END)
        moveHandler.removeMessages(MoveHandler.MSG_MOVE_START_OF_START)
        moveHandler.removeMessages(MoveHandler.MSG_MOVE_END_OF_START)
        moveHandler.removeMessages(MoveHandler.MSG_MOVE_START_OF_END)
    }

    private fun canLoadMoreWaveDataToEnd(context: Context): Boolean { // 实现检查是否有更多波形数据可以加载的逻辑
        // 返回true表示可以加载，返回false表示没有更多数据
        return audio.canLoadMoreWaveDataToEnd()
    }

    private fun canLoadMoreWaveDataToStart(): Boolean { // 实现检查是否有更多波形数据可以加载的逻辑
        // 返回true表示可以加载，返回false表示没有更多数据
        return audio.canLoadMoreWaveDataToStart()
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
        if (event == null || !isSelected) return false
        return startHandleTouchRect.isTouch(event) || endHandleTouchRect.isTouch(event)
    }


    fun anchor2CutEndLine() {
        if (isSelected) {
            PlayerManager.pause()
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
        if (isSelected) {
            PlayerManager.pause()
            var offsetTimeValue = ((ScreenUtil.getScreenWidth(audio.getContext())
                .toFloat() / 2 - startTimestampPosition) / unitMsPixel).toLong()
            var tempCursor = audio.cursorValue - offsetTimeValue
            if (tempCursor < audio.startTimestamp) { //不足了
                tempCursor = audio.startTimestamp
            }
            audio.audioEditorView.cursorValue = tempCursor
        }
    }


    //start 剪切条向左移动
    fun startCutMinus(): Boolean {
        if (isSelected) {
            var temp = startTimestampTimeInSelf - TIME_STEP
            if (isInOtherFragments(temp)) {
                return false
            }
            if (temp <= 0) {
                startTimestampTimeInSelf = 0
                audio.invalidate()
                return false
            }
            startTimestampTimeInSelf = temp
            audio.invalidate()
            return true
        }
        return false
    }

    //start 剪切条向右移动
    fun startCutPlus(): Boolean {
        if (isSelected) {
            var temp = startTimestampTimeInSelf + TIME_STEP
            if (endTimestampTimeInSelf - temp <= MIN_CUT_GAP) {
                return false
            }
            if (isInOtherFragments(temp)) {
                return false
            }
            if (temp >= endTimestampTimeInSelf) {
                startTimestampTimeInSelf = endTimestampTimeInSelf - 100
                audio.invalidate()
                return false
            }
            startTimestampTimeInSelf = temp
            audio.invalidate()
            return true
        }
        return false
    }

    //end 剪切条向左移动
    fun endCutMinus(): Boolean {
        if (isSelected) {
            var temp = endTimestampTimeInSelf - TIME_STEP
            if (temp - startTimestampTimeInSelf <= MIN_CUT_GAP) {
                return false
            }
            if (isInOtherFragments(temp)) {
                return false
            }
            if (temp <= startTimestampTimeInSelf) {
                endTimestampTimeInSelf = startTimestampTimeInSelf + MIN_CUT_ADJACENT
                audio.invalidate()
                return false
            }
            endTimestampTimeInSelf = temp
            audio.invalidate()
            return true
        }
        return false
    }

    //end 剪切条向右移动
    fun endCutPlus(): Boolean {
        if (isSelected) {
            var temp = endTimestampTimeInSelf + TIME_STEP
            if (isInOtherFragments(temp)) {
                return false
            }
            if (temp >= duration) {
                endTimestampTimeInSelf = duration
                audio.invalidate()
                return false
            }
            endTimestampTimeInSelf = temp
            audio.invalidate()
            return true
        }
        return false
    }

    //end 剪切条是否能向右移动
    fun canEndCutPlus(): Boolean {
        if (isSelected) {
            var temp = endTimestampTimeInSelf + TIME_STEP
            if (isInOtherFragments(temp)) {
                return false
            }
            return temp < duration
        }
        return false
    }

    //start 剪切条是否能向左移动
    fun canEndCutMinus(): Boolean {
        if (isSelected) {
            var temp = endTimestampTimeInSelf - TIME_STEP
            return temp > startTimestampTimeInSelf + TIME_STEP
        }
        return false
    }

    //start 剪切条是否能向左移动
    fun canStartCutMinus(): Boolean {
        if (isSelected) {
            var temp = startTimestampTimeInSelf - TIME_STEP
            if (isInOtherFragments(temp)) {
                return false
            }
            return temp > 0
        }
        return false
    }

    //start 剪切条是否能向右移动
    fun canStartCutPlus(): Boolean {
        if (isSelected) {
            var temp = startTimestampTimeInSelf + TIME_STEP
            if (isInOtherFragments(temp)) {
                return false
            }
            return temp + TIME_STEP < endTimestampTimeInSelf
        }
        return false
    }

    /**
     * 设定播放位置为裁剪起点
     */
    fun trimStart(currentPlayingTimeInAudio: Long) {
        if (isSelected) {
            when (cutMode) {
                CUT_MODE_SELECT -> {
                    if (currentPlayingTimeInAudio < endTimestampTimeInSelf) {
                        startTimestampTimeInSelf = currentPlayingTimeInAudio
                    } else { //                endTimestampTimeInSelf = currentPlayingTimeInAudio
                        startTimestampTimeInSelf = currentPlayingTimeInAudio
                        endTimestampTimeInSelf = (startTimestampTimeInSelf + 10000L).coerceAtMost(duration)
                    }
                    audio.invalidate()
                }

                CUT_MODE_DELETE -> {
                    startTimestampTimeInSelf = currentPlayingTimeInAudio
                    audio.invalidate()
                }
            }
            audio.updateMediaSource(startTimestampTimeInSelf, endTimestampTimeInSelf)
        }
    }

    /**
     * 设定播放位置为裁剪终点
     */
    fun trimEnd(currentPlayingTimeInAudio: Long) {
        if (isSelected) {
            when (cutMode) {
                CUT_MODE_SELECT -> {
                    if (currentPlayingTimeInAudio > startTimestampTimeInSelf) {
                        endTimestampTimeInSelf = currentPlayingTimeInAudio
                    } else { //                startTimestampTimeInSelf = currentPlayingTimeInAudio
                        endTimestampTimeInSelf = currentPlayingTimeInAudio
                        startTimestampTimeInSelf = (endTimestampTimeInSelf - 10000L).coerceAtLeast(0)
                    }
                    audio.invalidate()
                }

                CUT_MODE_DELETE -> {
                    endTimestampTimeInSelf = currentPlayingTimeInAudio
                    audio.updateMediaSource(startTimestampTimeInSelf, endTimestampTimeInSelf)
                    audio.invalidate()
                }
            }
            audio.updateMediaSource(startTimestampTimeInSelf, endTimestampTimeInSelf)
        }
    }


    fun onSingleTapUp(event: MotionEvent): Boolean {
        isSelected = event.x in startTimestampPosition..endTimestampPosition
        if (isSelected) {
            onCutLineChangeListener?.onCutLineChange(startTimestampTimeInSelf, endTimestampTimeInSelf)
        }
        audio.invalidate()
        return isSelected
    }

    /**
     * 时间点是否在区间内
     */
    fun isInFragment(timeStep: Long): Boolean {
        return timeStep in startTimestampTimeInSelf..endTimestampTimeInSelf
    }


    /**
     * 时间戳是否别的片段内
     */
    private fun isInOtherFragments(timeStep: Long): Boolean {
        var result = false
        audio.cutPieceFragments.forEach {
            if (it != this@CutPieceFragment && it.isInFragment(timeStep)) {
                result = true
                return@forEach
            }
        }
        return result
    }


    /**
     * 时间戳是否别的片段内 并返回所在片段
     */
    private fun getFragmentInOtherFragments(timeStep: Long): CutPieceFragment? {
        var result: CutPieceFragment? = null
        audio.cutPieceFragments.forEach {
            if (it != this@CutPieceFragment && it.isInFragment(timeStep)) {
                result = it
                return@forEach
            }
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        return if (other is CutPieceFragment) {
            other.index == index
        } else {
            false
        }
    }

    fun startCutLineCanMoveEnd(moveIntervalSpace: Float): Boolean {
        var newStartTimestampTimeInSelf = startTimestampTimeInSelf + (moveIntervalSpace.pixel2Time(unitMsPixel))
        var result = newStartTimestampTimeInSelf < endTimestampTimeInSelf
        if (result) {
            startTimestampTimeInSelf = newStartTimestampTimeInSelf
        } else {
            endTimestampTimeInSelf - 0.1
        }
        return result
    }

    fun endCutLineCanMoveStart(moveIntervalSpace: Float): Boolean {
        var newEndTimestampTimeInSelf = endTimestampTimeInSelf - (moveIntervalSpace.pixel2Time(unitMsPixel))
        var result = newEndTimestampTimeInSelf > startTimestampTimeInSelf
        if (result) {
            endTimestampTimeInSelf = newEndTimestampTimeInSelf
        } else {
            startTimestampTimeInSelf + 0.1
        }
        return result
    }


}