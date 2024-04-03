package dev.audio.timeruler.weight

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View


/**
 * 波形片段
 *
 * 带裁剪模式
 */
class AudioFragmentWithCut(audioEditorView: BaseAudioEditorView) :
    AudioFragment(audioEditorView) {


    private var cutPieceFragments = mutableListOf<CutPieceFragment>()

    fun getContext(): Context? {
        return audioEditorView.context
    }

    override fun initCutFragment() {
        super.initCutFragment()
        cutPieceFragments.add(CutPieceFragment(this,true).apply {
            this.initCutFragment(1/6f, 2/6f)
        })
        cutPieceFragments.add(CutPieceFragment(this).apply {
            this.initCutFragment(4/6f, 5/6f)
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

    override fun onDraw(canvas: Canvas) {
        // 开启图层
        val saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
        //绘制完整音波
        super.onDraw(canvas)
        //绘制选中片段
        cutPieceFragments.forEach {
            it.drawCutFragment(canvas)
        }
        //恢复图层
        canvas.restoreToCount(saved)
    }


    fun onTouchEvent(context: Context, view: View, event: MotionEvent?): Boolean {
        Log.i(BaseAudioEditorView.cut_tag, "onTouchEvent: ")
        return cutPieceFragments[currentTouchIndex].onTouchEvent(context, view, event)
    }


    private var currentTouchIndex = -1
    fun isCutLineTarget(event: MotionEvent): Boolean {
        var isTarget = false
        cutPieceFragments.forEachIndexed { index, cutPieceFragment ->
            if (cutPieceFragment.isTarget(event)) {
                currentTouchIndex = index
                isTarget = true
                return@forEachIndexed
            }
        }
        return isTarget.apply {
            if(!this){
                currentTouchIndex = -1
            }
            Log.i(BaseAudioEditorView.cut_tag, "isCutLineTarget: $this   index=$currentTouchIndex")
        }
    }

    fun setCutMode(cutMode: Int) {
        cutPieceFragments.forEach {
            it.setCutMode(cutMode)
        }
    }

}