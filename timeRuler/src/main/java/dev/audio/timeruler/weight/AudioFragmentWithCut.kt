package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import dev.audio.ffmpeglib.tool.ScreenUtil
import dev.audio.timeruler.bean.AudioFragmentBean
import dev.audio.timeruler.bean.Ref
import dev.audio.timeruler.player.PlayerManager


/**
 * 波形片段
 *
 * 带裁剪模式
 */
class AudioFragmentWithCut(audioEditorView: AudioCutEditorView,
                           var cutMode: Int = CutPieceFragment.CUT_MODE_SELECT) :
    AudioFragment(audioEditorView) {

    var audioFragmentBean: AudioFragmentBean = AudioFragmentBean()
        get() {
            field.cutPieces.clear()
            cutPieceFragments.forEach {
                field.cutPieces.add(it.cutPieceBean)
            }
            field.cursorValue = cursorValue
            field.playingLine = currentPlayingTimeInAudio
            field.path = path
            return field
        }

    var currentCutPieceFragment: CutPieceFragment? = null
        get() {
            try {
                cutPieceFragments.forEach {
                    if (it.isSelected) {
                        return it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }


    var selectedTime: Long = 0L
        get() {
            var time = 0L
            cutPieceFragments?.forEachIndexed { index, cutPieceFragment ->
                if (!cutPieceFragment.isFake) {
                    time += (cutPieceFragment.endTimestampTimeInSelf - cutPieceFragment.startTimestampTimeInSelf)
                }
            }
            when (cutMode) {
                CutPieceFragment.CUT_MODE_SELECT, CutPieceFragment.CUT_MODE_JUMP -> {
                }

                CutPieceFragment.CUT_MODE_DELETE -> {
                    time = (duration ?: 0) - time
                }
            }
            return time
        }

    val currentPlayingTimeInAudio by Ref { audioEditorView.currentPlayingTimeInAudio }

    val isWholeScreen by Ref { (audioEditorView as? AudioCutEditorView)?.isWholeScreen }

    var cutPieceFragments = mutableListOf<CutPieceFragment>()

    val onCutLineChangeListener by Ref { audioEditorView.onCutLineChangeListener }
    val onTrimAnchorChangeListener by Ref { audioEditorView.onTrimAnchorChangeListener }
    val cutModeChangeButtonEnableListener by Ref { audioEditorView.cutModeChangeButtonEnableListener }
    val cutLineFineTuningButtonChangeListener by Ref { audioEditorView.cutLineFineTuningButtonChangeListener }

    var isCutLineStartVisible: Boolean = true
        get() {
            var isStartVisible = false
            cutPieceFragments.forEach {
                isStartVisible = isStartVisible || it.isCutLineStartVisible()
            }
            return isStartVisible
        }

    var isCutLineEndVisible: Boolean = true
        get() {
            var isEndVisible = false
            cutPieceFragments.forEach {
                isEndVisible = isEndVisible || it.isCutLineEndVisible()
            }
            return isEndVisible
        }

    fun getContext(): Context? {
        return audioEditorView.context
    }

    override fun initCutFragment() {
        super.initCutFragment()
        cutPieceFragments.add(CutPieceFragment(audio = this, index = cutPieceFragments.size, mode = cutMode).apply {
            this.initCutFragment(1 / 3f, 2 / 3f)
        })
        updateMediaSource(getCutLineStartTime(), getCutLineEndTime())
    }

    /**
     * 波形中心Y坐标
     */
    override fun getTrackYPosition(): Float {
        return waveStartYInParent + maxHalfWaveHeight
    }


    /**
     * 是否选中  裁剪
     */
    override fun isSelected(x: Float): Boolean {
        var isSelected = false
        cutPieceFragments.forEach {
            isSelected = isSelected || it.isSelected(x)
        }
        return isSelected
    }

    override fun onDraw(canvas: Canvas) { // 开启图层
        cutPieceFragments.forEach {
            it.drawCutBg(canvas)
        } //恢复图层
        val saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG) //绘制完整音波
        super.onDraw(canvas) //绘制选中片段
        cutPieceFragments.forEach {
            it.drawCut(canvas)
        } //恢复图层
        canvas.restoreToCount(saved)
        //前后裁剪条定位
        if(cutPieceFragments.isNullOrEmpty()){
            refreshCutLineAnchor(start = false, end = false)
        }else{
            cutPieceFragments.forEach {
                it.drawCutLines(canvas)
            } //绘制剪切条
        }
    }


    fun onTouchEvent(context: Context, view: View, event: MotionEvent?): Boolean {
        Log.i(BaseAudioEditorView.cut_tag, "onTouchEvent: ")
        return currentCutPieceFragment?.onTouchEvent(context, view, event) ?: false
    }

    fun isCutLineTarget(event: MotionEvent): Boolean {
        var isTarget = false
        cutPieceFragments.forEachIndexed { index, cutPieceFragment ->
            if (cutPieceFragment.isTarget(event)) {
                isTarget = true
                return@forEachIndexed
            }
        }
        return isTarget.apply {
            Log.i(BaseAudioEditorView.cut_tag, "isCutLineTarget() isTarget= $this")
        }
    }

    fun refreshCutLineAnchor(start: Boolean, end: Boolean) {
        Log.i(BaseAudioEditorView.cut_tag, "refreshCutLineAnchor: start=$start  end=$end")
        (audioEditorView as AudioCutEditorView)?.refreshCutLineAnchor(start, end)
    }

    fun anchor2CutEndLine() {
        cutPieceFragments.forEach {
            it.anchor2CutEndLine()
        }
    }

    fun anchor2CutStartLine() {
        cutPieceFragments.forEach {
            it.anchor2CutStartLine()
        }
    }

    fun startCutMinus(): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.startCutMinus()
        }
        return result
    }

    fun invalidate() {
        audioEditorView.invalidate()
    }

    //开始裁剪条是否能做移动
    fun canStartCutMinus(): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.canStartCutMinus()
        }
        return result
    }


    //开始裁剪条是否能做右移动
    fun canStartCutPlus(): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.canStartCutPlus()
        }
        return result
    }


    //结束裁剪条是否能做左移动
    fun canEndCutMinus(): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.canEndCutMinus()
        }
        return result
    }

    //结束裁剪条是否能做右移动
    fun canEndCutPlus(): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.canEndCutPlus()
        }
        return result
    }


    fun startCutPlus(): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.startCutPlus()
        }
        return result
    }

    fun endCutMinus(): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.endCutMinus()
        }
        return result
    }

    fun endCutPlus(): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.endCutPlus()
        }
        return result
    }

    fun trimStart(currentPlayingTimeInAudio: Long) {
        cutPieceFragments.forEach {
            it.trimStart(currentPlayingTimeInAudio)
        }
    }

    fun trimEnd(currentPlayingTimeInAudio: Long) {
        cutPieceFragments.forEach {
            it.trimEnd(currentPlayingTimeInAudio)
        }
    }

    /**
     * 播放条移动
     */
    fun freshTrimAnchor() {
        cutPieceFragments.forEach {
            it.linesChangeNotify()
        }
    }


    /**
     * 单击事件
     */
    fun onSingleTapUp(event: MotionEvent): Boolean {
        if (cutMode == CutPieceFragment.CUT_MODE_JUMP) {
            if (cutPieceFragments.isEmpty()) {
                (audioEditorView as? AudioCutEditorView)?.fineTuningEnable(false)
                return true
            }
            var allUnSelected = true
            cutPieceFragments.forEachIndexed { _, cutPieceFragment ->
                cutPieceFragment.onSingleTapUp(event)
                allUnSelected = (!cutPieceFragment.isSelected || cutPieceFragment.isFake) && allUnSelected
            }
            (audioEditorView as? AudioCutEditorView)?.fineTuningEnable(!allUnSelected)
            return true
        } else {
            (audioEditorView as? AudioCutEditorView)?.fineTuningEnable(true)
            return false
        }

    }


    /**
     * 切换裁剪模式
     */
    fun switchCutMode(mode: Int) {
        cutMode = mode

        cutPieceFragments = cutPieceFragments.filter { !it.isFake }
            .sortedBy { it.startTimestampTimeInSelf }.toMutableList()
        if (cutPieceFragments.isNotEmpty()) { // 只保留第一个元素
            cutPieceFragments = mutableListOf(cutPieceFragments.first())
        }
        cutPieceFragments.forEach {
            it.switchCutMode(mode)
            it.isSelected = true
        }
        if (cutPieceFragments.isEmpty()) {
            initCutFragment()
            audioEditorView.invalidate()
            return
        }
    }

    /**
     * 增加裁剪片段
     */
    fun cutAdd() {
        removeFake()
        cutPieceFragments.forEach {
            it.isSelected = false
        }
        cutPieceFragments.add(CutPieceFragment(this, true, index = cutPieceFragments.size, mode = CutPieceFragment.CUT_MODE_JUMP).apply {
            var endTemp = audio.currentPlayingTimeInAudio.apply {} + 1000L * 6 //不能超范围
            if (endTemp > audio.duration) {
                endTemp = audio.duration
            } //不能重叠
            cutPieceFragments.forEachIndexed { index, cutPieceFragment ->
                if (cutPieceFragment.isInFragment(endTemp)) {
                    endTemp = cutPieceFragment.startTimestampTimeInSelf
                    return@forEachIndexed
                }
            }
            this.initCutFragment(audio.currentPlayingTimeInAudio, endTemp) //开始 结束裁剪条微调控件更新
            audio.cutLineFineTuningButtonChangeListener?.onCutLineFineTuningEnable(true)
            onCutLineChangeListener?.onCutLineChange(startTimestampTimeInSelf, endTimestampTimeInSelf)
        })
        audioEditorView.invalidate()
        freshTrimAnchor()
        PlayerManager.updateMediaSourceDeleteJump(cutPieceFragments)
        var index = playingLineIndexInFragments(currentPlayingTimeInAudio)
        cutLineFineTuningButtonChangeListener?.onCutLineFineTuningEnable(true)
        PlayerManager.seekTo(0, index)
    }

    /**
     * 移除裁剪片段
     */
    fun cutRemove() {
        if (currentCutPieceFragment == null) {
            return
        }
        var isInFragment = currentCutPieceFragment!!.isInFragment(currentPlayingTimeInAudio)
        cutPieceFragments.remove(currentCutPieceFragment)
        if (cutPieceFragments.isEmpty()) {
            var isResumePlaying = PlayerManager.isPlaying
            PlayerManager.pause()
            cutModeChangeButtonEnableListener?.onCutModeChange(true, false)
            cutLineFineTuningButtonChangeListener?.onCutLineFineTuningEnable(false)
            audioEditorView.invalidate()
            if(isResumePlaying){
                (audioEditorView as? AudioCutEditorView)?.play()
            }
            return
        }
        var isPlaying = PlayerManager.isPlaying
        PlayerManager.pause()
        if (isInFragment) { //播放条在删除的片段内   重新定位 1、后一个（如果有）  2、第一个
            var index = -1
            cutPieceFragmentsOrder.forEachIndexed { i, cutPieceFragment ->
                if (cutPieceFragment.startTimestampTimeInSelf > currentPlayingTimeInAudio) {
                    index = i
                    return@forEachIndexed
                }
            }
            PlayerManager.updateMediaSourceDeleteJump(cutPieceFragments)
            (audioEditorView as? AudioCutEditorView)?.freshPlayingLinePosition(cutPieceFragmentsOrder[0].startTimestampTimeInSelf)
            if (index == -1) {
                PlayerManager.seekTo(0, 0)
            } else {
                PlayerManager.seekTo(0, index)
            }
        } else { //播放条不在删除的片段内  直接删除，继续播放
            var index = playingLineIndexInFragments(currentPlayingTimeInAudio)
            PlayerManager.updateMediaSourceDeleteJump(cutPieceFragments)
            PlayerManager.seekTo(currentPlayingTimeInAudio - cutPieceFragments[index].startTimestampTimeInSelf, index)
        }
        if (isPlaying) {
            PlayerManager.play()
        }
        if (cutPieceFragments.isNotEmpty()) {
            cutPieceFragments[0].isSelected = true
        }
        audioEditorView.invalidate()
        freshTrimAnchor()
        cutLineFineTuningButtonChangeListener?.onCutLineFineTuningEnable(false)
    }

    fun getCutLineStartTime(): Long {
        return currentCutPieceFragment?.startTimestampTimeInSelf ?: 0L
    }

    fun getCutLineEndTime(): Long {
        return currentCutPieceFragment?.endTimestampTimeInSelf ?: 0L
    }

    fun setCutLineStartTime(time: Long) {
        currentCutPieceFragment?.startTimestampTimeInSelf = (time)
        audioEditorView.invalidate()

    }

    fun setCutLineEndTime(time: Long) {
        currentCutPieceFragment?.endTimestampTimeInSelf = (time)
        audioEditorView.invalidate()
    }


    fun getNextCutPieceFragment(): CutPieceFragment? {
        if (cutPieceFragments.size <= 1) {
            return null
        }
        val sortedList = cutPieceFragments.sortedBy { it.startTimestampTimeInSelf }
        val index = sortedList.indexOf(currentCutPieceFragment)
        return if (index == cutPieceFragments.size - 1) {
            null
        } else {
            sortedList[index + 1]
        }
    }

    fun getNextCutPieceFragmentWithReal(): CutPieceFragment? {
        if (cutPieceFragments.size <= 1) {
            return null
        }
        val sortedList = cutPieceFragments.filter { !it.isFake }.sortedBy { it.startTimestampTimeInSelf }
        val index = sortedList.indexOf(currentCutPieceFragment)
        return if (index == cutPieceFragments.size - 1) {
            null
        } else {
            sortedList[index + 1]
        }
    }

    fun getPreCutPieceFragment(): CutPieceFragment? {
        if (cutPieceFragments.size <= 1) {
            return null
        }
        val sortedList = cutPieceFragments.sortedBy { it.startTimestampTimeInSelf }
        val index = sortedList.indexOf(currentCutPieceFragment)
        return if (index == 0) {
            null
        } else {
            sortedList[index - 1]
        }
    }

    fun getPreCutPieceFragmentWithReal(): CutPieceFragment? {
        if (cutPieceFragments.size <= 1) {
            return null
        }
        val sortedList = cutPieceFragments.filter { !it.isFake }.sortedBy { it.startTimestampTimeInSelf }
        val index = sortedList.indexOf(currentCutPieceFragment)
        return if (index == 0) {
            null
        } else {
            sortedList[index - 1]
        }
    }

    /**
     * 更新媒体源
     * by：裁剪条滑动抬起
     */
    fun updateMediaSource(startTimestampTimeInSelf: Long, endTimestampTimeInSelf: Long) {
        (audioEditorView as? AudioCutEditorView)?.updateMediaSource(startTimestampTimeInSelf, endTimestampTimeInSelf)

    }

    /**
     * 播放条是否在裁剪片段内
     */
    fun isPlayingLineInAnyCutPiece(currentPlayingTimeInAudio: Long): Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.isInFragment(currentPlayingTimeInAudio)
            if (result) {
                return true
            }
        }
        return false
    }

    /**
     * 播放条在第几个裁剪片段  按顺序
     */
    fun playingLineIndexInFragments(currentPlayingTimeInAudio: Long): Int {
        var result = -1
        cutPieceFragmentsOrder.forEachIndexed { index, cutPieceFragment ->
            if (cutPieceFragment.isInFragment(currentPlayingTimeInAudio)) {
                result = index
                return result
            }
        }
        return result
    }

    fun removeFake() {
        if (hasFakeCut()) { //过滤掉 isFake = true 的
            cutPieceFragments = cutPieceFragments.filter { !it.isFake }.toMutableList()
            PlayerManager.updateMediaSourceDeleteJump(cutPieceFragments)
        }
    }

    private fun hasFakeCut(): Boolean {
        return cutPieceFragments.any { it.isFake }
    }

    var cutPieceFragmentsOrder: MutableList<CutPieceFragment> = mutableListOf()
        get() {
            return cutPieceFragments.sortedBy { it.startTimestampTimeInSelf }.toMutableList()
        }


    fun canLoadMoreWaveDataToStart(): Boolean {
        return when (cutMode) {
            CutPieceFragment.CUT_MODE_SELECT, CutPieceFragment.CUT_MODE_DELETE -> {
                startTimestamp < cursorValue
            }

            CutPieceFragment.CUT_MODE_JUMP -> {
                startTimestamp < cursorValue
            }

            else -> false
        }
    }

    fun canLoadMoreWaveDataToEnd(): Boolean {
        return cursorValue + screenWithDuration < endTimestamp
    }

    fun updatePlayingPosition(positionTime: Long) {
        (audioEditorView as? AudioCutEditorView)?.updatePlayingPosition(positionTime)
    }

    fun resetCurrentFragment() {
        cutPieceFragments = cutPieceFragments.filter { it.index == 0 }.toMutableList()
    }

}