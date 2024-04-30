package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import dev.audio.timeruler.bean.AudioFragmentBean
import dev.audio.timeruler.bean.Ref
import dev.audio.timeruler.player.PlayerManager


/**
 * 波形片段
 *
 * 带裁剪模式
 */
class AudioFragmentWithCut(audioEditorView: AudioCutEditorView) : AudioFragment(audioEditorView) {

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

    var cutMode: Int = CutPieceFragment.CUT_MODE_SELECT
        get() {
            cutPieceFragments?.forEach {
                return it.cutMode
            }
            return CutPieceFragment.CUT_MODE_SELECT
        }


    val currentPlayingTimeInAudio by Ref { audioEditorView.currentPlayingTimeInAudio }

    var cutPieceFragments = mutableListOf<CutPieceFragment>()

    val onCutLineChangeListener by Ref { audioEditorView.onCutLineChangeListener }
    val onTrimAnchorChangeListener by Ref { audioEditorView.onTrimAnchorChangeListener }
    val cutModeChangeButtonEnableListener by Ref { audioEditorView.cutModeChangeButtonEnableListener }

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
        cutPieceFragments.add(CutPieceFragment(this, index = cutPieceFragments.size).apply {
            this.initCutFragment(1 / 3f, 2 / 3f)
        })
        updateMediaSource(getCutLineStartTime(), getCutLineEndTime())
    }

    /**
     * 波形中心Y坐标
     */
    override fun getTrackYPosition(): Float {
        return waveStartYInParent + maxWaveHeight
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
        cutPieceFragments.forEach {
            it.drawCutLines(canvas)
        } //绘制剪切条
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

    fun startCutMinus():Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.startCutMinus()
        }
        return result
    }

    fun invalidate() {
        audioEditorView.invalidate()
    }

    fun startCutPlus():Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.startCutPlus()
        }
        return result
    }

    fun startEndMinus():Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.startEndMinus()
        }
        return result
    }

    fun startEndPlus():Boolean {
        var result = false
        cutPieceFragments.forEach {
            result = result || it.startEndPlus()
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
            cutPieceFragments.forEachIndexed { _, cutPieceFragment ->
                cutPieceFragment.onSingleTapUp(event)
            }
            return true
        } else {
            return false
        }

    }


    /**
     * 切换裁剪模式
     */
    fun switchCutMode(mode: Int) {
        if (cutPieceFragments.isEmpty()) {
            initCutFragment()
            audioEditorView.invalidate()
            return
        }
        if (mode == CutPieceFragment.CUT_MODE_JUMP) { //cutPieceFragments只保留isSelected
            //其他模式转跳剪辑  只保留当前选中的
            cutPieceFragments = cutPieceFragments.filter { it.isSelected }.toMutableList()
        }
        cutPieceFragments.forEach {
            it.switchCutMode(mode)
        }
    }

    /**
     * 增加裁剪片段
     */
    fun cutAdd() {
        cutPieceFragments.forEach {
            it.isSelected = false
        }
        cutPieceFragments.add(CutPieceFragment(this, true, index = cutPieceFragments.size, mode = CutPieceFragment.CUT_MODE_JUMP).apply {
            var endTemp = audio.currentPlayingTimeInAudio + 1000L * 6 //不能超范围
            if (endTemp > audio.duration) {
                endTemp = audio.duration
            } //不能重叠
            cutPieceFragments.forEachIndexed { index, cutPieceFragment ->
                if (cutPieceFragment.isInFragment(endTemp)) {
                    endTemp = cutPieceFragment.startTimestampTimeInSelf
                    return@forEachIndexed
                }
            }
            this.initCutFragment(audio.currentPlayingTimeInAudio, endTemp)
        })
        audioEditorView.invalidate()
        freshTrimAnchor()
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
            PlayerManager.pause()
            audioEditorView.invalidate()
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
            return currentCutPieceFragment
        }
        val sortedList = cutPieceFragments.sortedBy { it.startTimestampTimeInSelf }
        val index = sortedList.indexOf(currentCutPieceFragment)
        return if (index == cutPieceFragments.size - 1) {
            null
        } else {
            sortedList[index + 1]
        }
    }

    fun getPreCutPieceFragment(): CutPieceFragment? {
        if (cutPieceFragments.size <= 1) {
            return currentCutPieceFragment
        }
        val sortedList = cutPieceFragments.sortedBy { it.startTimestampTimeInSelf }
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
                startTimestamp <= cursorValue
            }

            CutPieceFragment.CUT_MODE_JUMP -> {
                startTimestamp <= cursorValue
            }

            else -> false
        }
    }

    fun canLoadMoreWaveDataToEnd(): Boolean {
        return cursorValue + screenWithDuration <= duration + startTimestamp
    }

}