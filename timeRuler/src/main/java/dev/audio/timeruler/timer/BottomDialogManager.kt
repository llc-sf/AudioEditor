package dev.audio.timeruler.timer

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

/**
 * BottomDialog 管理
 */
object BottomDialogManager {

    private const val DIALOG_TAG = "BottomDialogManager"

    private var mShowingDialog: WeakReference<BaseTranslucentDialog?>? = null

    //主动问询弹窗是否显示  因为目前主动问询是不受BottomDialogManager控制的
    var rateUsDialogShowing = false

    /**
     * 是否有弹窗显示
     */
    @JvmStatic
    fun isShowingDialog(): Boolean {
        return (mShowingDialog?.get() != null || rateUsDialogShowing).apply {
            Log.i("MainDialog", "isShowingDialog $this")
        }
    }

    fun setShowingDialog(dialog: BaseTranslucentDialog?) {
        mShowingDialog = WeakReference(dialog)
    }

    fun setShowingDialogDismiss(dialog: BaseTranslucentDialog?) {
        mShowingDialog?.clear()
        mShowingDialog = null
    }


    fun show(activity: Activity, dialog: BaseTranslucentDialog) {
        if (activity is AppCompatActivity) {
            show(activity.supportFragmentManager, dialog)
        }
    }

    fun show(fragment: Fragment, dialog: BaseTranslucentDialog) {
        showImpl(fragment.childFragmentManager, dialog)
    }

    fun show(manager: FragmentManager, dialog: BaseTranslucentDialog) {
        showImpl(manager, dialog)
    }


    private fun showImpl(manager: FragmentManager, dialog: BaseTranslucentDialog) {
        try {
            val fragment = manager.findFragmentByTag(DIALOG_TAG) ?: mShowingDialog?.get()
            if (fragment != null) {
                if (fragment.javaClass == dialog.javaClass) {//两个弹窗是同一个类，替换新的
                    (fragment as? BaseTranslucentDialog)?.dismissAllowingStateLoss()
                    showDialogImpl(manager, dialog)
                } else {//两个弹窗不是同一个类，做判断看
                    if (fragment is BaseTranslucentDialog) {//如果是BaseTranslucentDialog,进行比较
                        if (shouldShowDialog(fragment, dialog)) {
                            showDialogImpl(manager, dialog)
                        }
                    } else {
                        showDialogImpl(manager, dialog)
                    }
                }
            } else {//没有弹窗，直接显示
                showDialogImpl(manager, dialog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showDialogImpl(manager: FragmentManager, dialog: BaseTranslucentDialog) {
        dialog.showNow(manager, DIALOG_TAG)
        setShowingDialog(dialog)
    }


    /**
     * return 是否需要弹出新的dialog
     */
    private fun shouldShowDialog(current: BaseTranslucentDialog, dialog: BaseTranslucentDialog): Boolean {
        val currentLevel = current.getLevel()
        val dialogLevel = dialog.getLevel()
        Log.d(DIALOG_TAG, "shouldShowDialog: current = ${current::class.java}  dialog = ${dialog::class.java}")
        when {
            currentLevel > dialogLevel -> {//如果正在弹出的弹窗优先级大于要弹出的弹窗优先级，则不弹出
                //Todo 看后续需求可以做一个队列，把要弹出的弹窗放到队列中，然后等当前弹出的弹窗关闭后，再弹出队列中的弹窗
                Log.d(DIALOG_TAG, "shouldShowDialog: currentLevel > dialogLevel")
                return false
            }
            currentLevel == dialogLevel -> {//如果正在弹出的弹窗优先级等于要弹出的弹窗优先级 看当前Dialog设置
                when (dialog.getLevelStrategy()) {
                    BaseTranslucentDialog.Strategy.REPLACE -> {//替换
                        current.dismissAllowingStateLoss()
                        Log.d(DIALOG_TAG, "shouldShowDialog: REPLACE")
                        return true
                    }
                    BaseTranslucentDialog.Strategy.NONE -> {//不弹出
                        Log.d(DIALOG_TAG, "shouldShowDialog: NONE")
                        return false
                    }
                    BaseTranslucentDialog.Strategy.QUEUE -> {//队列
                        //Todo 看后续需求可以做一个队列，把要弹出的弹窗放到队列中，然后等当前弹出的弹窗关闭后，再弹出队列中的弹窗
                        Log.d(DIALOG_TAG, "shouldShowDialog: QUEUE")
                        return false
                    }
                    BaseTranslucentDialog.Strategy.STACK -> {//堆叠在一起，默认情况,可以弹出多个弹窗
                        Log.d(DIALOG_TAG, "shouldShowDialog: STACK")
                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }
            else -> {
                //如果正在弹出的弹窗优先级小于要弹出的弹窗优先级，则关闭正在弹出的弹窗
                current.dismissAllowingStateLoss()
                return true
            }
        }
    }


}