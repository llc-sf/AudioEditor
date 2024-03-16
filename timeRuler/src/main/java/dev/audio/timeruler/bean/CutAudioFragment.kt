package dev.audio.timeruler.bean

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.View
import dev.audio.timeruler.BaseMultiTrackAudioEditorView
import dev.audio.timeruler.MultiTrackAudioEditorView


/**
 * 波形片段
 *
 * 带裁剪模式
 */
class CutAudioFragment(multiTrackAudioEditorView: MultiTrackAudioEditorView) :
    AudioFragment(multiTrackAudioEditorView) {


    var cutPieceFragment: CutPieceFragment? = null

    override fun initCutFragment() {
        super.initCutFragment()
        cutPieceFragment = CutPieceFragment(this)
        cutPieceFragment?.initCutFragment()
    }


    /**
     * 是否选中  裁剪
     */
    override fun isSelected(x: Float): Boolean {
        return cutPieceFragment?.isSelected(x) ?: false
    }

    override fun drawWave(canvas: Canvas): Boolean {
        // 开启图层
        val saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
        super.drawWave(canvas)
        cutPieceFragment?.drawCutFragment(canvas)
        //恢复图层
        canvas.restoreToCount(saved)
        return true

    }


    fun onTouchEvent(context: Context, view: View, event: MotionEvent?): Boolean {
        Log.i(BaseMultiTrackAudioEditorView.cut_tag, "onTouchEvent: ")
        return cutPieceFragment?.onTouchEvent(context, view, event) ?: false
    }


    fun isTarget(event: MotionEvent): Boolean {
        return cutPieceFragment?.isTarget(event) ?: false
    }

}