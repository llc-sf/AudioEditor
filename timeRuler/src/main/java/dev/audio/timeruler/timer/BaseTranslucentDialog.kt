package dev.audio.timeruler.timer

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.annotation.IntDef
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.android.player.framework.base.BaseActivity
import dev.audio.timeruler.R

/**
 * 底部透明弹窗
 */
abstract class BaseTranslucentDialog : BottomSheetDialogFragment() {

    private var mRootView: View? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.mRootView = view
        try {
            val sheet = (dialog as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(sheet!!)
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            behavior.isHideable = isDragClose()
            view.addOnAttachStateChangeListener(OnViewAttachWindowListener(behavior))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (e: Exception) {
            dismissAllowingStateLoss()
            e.printStackTrace()
        }
    }

    override fun dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        BottomDialogManager.setShowingDialogDismiss(this)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        BottomDialogManager.setShowingDialogDismiss(this)
        activity?.let {
            if (it.isFinishing || it.isDestroyed) {
                return@let
            }
            if (it is BaseActivity) {
                it.adapterNavigationBar()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(getSoftInputMode())
    }

    /**
     * 有编辑框的重写这个选项
     */
    protected open fun getSoftInputMode(): Int {
        return WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
    }

    protected open fun isDragClose(): Boolean {
        return true
    }


    /**
     * 返回弹窗优先级，越大优先级越高
     */
    open fun getLevel(): Int {
        return 0
    }

    /**
     * 相同等级之间弹出策略
     */
    open fun getLevelStrategy(): Int {
        return Strategy.STACK
    }

    override fun getTheme(): Int {
        return R.style.TranslucentBottomSheetDialog
    }


    @IntDef(value = [Strategy.REPLACE, Strategy.STACK, Strategy.NONE, Strategy.QUEUE])
    annotation class Strategy {
        companion object {
            const val NONE = 1//不做任何处理
            const val REPLACE = 2//关闭之前的弹窗，打开新的弹窗
            const val STACK = 3//堆叠弹窗
            const val QUEUE = 4//队列弹窗
        }
    }

    private class OnViewAttachWindowListener(val behavior: BottomSheetBehavior<View>) : View.OnAttachStateChangeListener {

        private var mGlobalMeasuredObserver: ViewTreeObserver.OnGlobalLayoutListener? = null

        override fun onViewAttachedToWindow(v: View) {
            if (v == null) return
            if (mGlobalMeasuredObserver == null) {
                mGlobalMeasuredObserver = OnGlobalMeasured(v, behavior)
            }
            v.viewTreeObserver.addOnGlobalLayoutListener(mGlobalMeasuredObserver!!)
        }

        override fun onViewDetachedFromWindow(v: View) {
            if (v == null) return
            v.viewTreeObserver.removeOnGlobalLayoutListener(mGlobalMeasuredObserver!!)
            mGlobalMeasuredObserver = null
        }
    }


    private class OnGlobalMeasured(val view: View, val behavior: BottomSheetBehavior<View>) : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            behavior.peekHeight = view.measuredHeight
            view.requestLayout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}



