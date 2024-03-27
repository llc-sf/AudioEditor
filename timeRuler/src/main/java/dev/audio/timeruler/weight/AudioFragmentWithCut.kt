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

    override fun drawWave(canvas: Canvas) {
        // 开启图层
        val saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
        //绘制完整音波
        super.drawWave(canvas)
        //绘制选中片段
        cutPieceFragment?.drawCutFragment(canvas)
        //恢复图层
        canvas.restoreToCount(saved)
    }


    fun onTouchEvent(context: Context, view: View, event: MotionEvent?): Boolean {
        Log.i(BaseAudioEditorView.cut_tag, "onTouchEvent: ")
        return cutPieceFragment?.onTouchEvent(context, view, event) ?: false
    }


    fun isTarget(event: MotionEvent): Boolean {
        return cutPieceFragment?.isTarget(event) ?: false
    }

}