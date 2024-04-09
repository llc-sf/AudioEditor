package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import dev.audio.timeruler.bean.Ref


/**
 * 波形片段
 *
 * 带裁剪模式
 */
class AudioFragmentWithCut(audioEditorView: AudioCutEditorView) : AudioFragment(audioEditorView) {


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

    private var cutMode: Int = CutPieceFragment.CUT_MODE_SELECT
        get() {
            cutPieceFragments?.forEach {
                return it.cutMode
            }
            return CutPieceFragment.CUT_MODE_SELECT
        }


    val currentPlayingTimeInAudio by Ref { audioEditorView.currentPlayingTimeInAudio }

    private var cutPieceFragments = mutableListOf<CutPieceFragment>()

    val onCutLineChangeListener by Ref { audioEditorView.onCutLineChangeListener }
    val onTrimAnchorChangeListener by Ref { audioEditorView.onTrimAnchorChangeListener }

    fun getContext(): Context? {
        return audioEditorView.context
    }

    override fun initCutFragment() {
        super.initCutFragment()
        cutPieceFragments.add(CutPieceFragment(this, index = cutPieceFragments.size).apply {
            this.initCutFragment(1 / 3f, 2 / 3f)
        })
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
        val saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG) //绘制完整音波
        super.onDraw(canvas) //绘制选中片段
        cutPieceFragments.forEach {
            it.drawCutFragment(canvas)
        } //恢复图层
        canvas.restoreToCount(saved)
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
        return isTarget
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

    fun startCutMinus() {
        cutPieceFragments.forEach {
            it.startCutMinus()
        }
    }

    fun invalidate() {
        audioEditorView.invalidate()
    }

    fun startCutPlus() {
        cutPieceFragments.forEach {
            it.startCutPlus()
        }
    }

    fun startEndMinus() {
        cutPieceFragments.forEach {
            it.startEndMinus()
        }
    }

    fun startEndPlus() {
        cutPieceFragments.forEach {
            it.startEndPlus()
        }
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
            it.freshTrimAnchor()
        }
    }


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


    fun switchCutMode(mode: Int) {
        if (cutPieceFragments.isEmpty()) {
            initCutFragment()
            audioEditorView.invalidate()
            return
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
            this.initCutFragment(audio.currentPlayingTimeInAudio, if (audio.currentPlayingTimeInAudio + 1000L * 6 > audio.duration) audio.duration else audio.currentPlayingTimeInAudio + 1000L * 6)
        })
        audioEditorView.invalidate()
    }

    /**
     * 移除裁剪片段
     */
    fun cutRemove() {
        if(currentCutPieceFragment == null){
            return
        }
        cutPieceFragments.remove(currentCutPieceFragment)
        if (cutPieceFragments.isNotEmpty()) {
            cutPieceFragments[0].isSelected = true
        }
        audioEditorView.invalidate()
    }

}