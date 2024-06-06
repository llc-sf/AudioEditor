package com.san.audioeditor.delete.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.san.audioeditor.R
import com.san.audioeditor.databinding.DialogDeleteSongsNoPermissionBinding
import dev.android.player.framework.utils.TrackerMultiple
import dev.audio.timeruler.timer.BottomDialogManager
import dev.audio.timeruler.timer.TopIconTranslucentDialog

/**
 * 删除歌曲失败
 */
class DeleteFailedDialog : TopIconTranslucentDialog() {


    private var _binding: DialogDeleteSongsNoPermissionBinding? = null
    private val binding get() = _binding!!


    companion object {

        private const val TAG = "SDCardPermissionDialog"


        @JvmStatic
        fun show(activity: AppCompatActivity, callback: ISAFPermissionRequest?) {
            val dialog = DeleteFailedDialog()
            dialog.setCallback(callback)
            BottomDialogManager.show(activity, dialog)
        }
    }

    private fun setCallback(callback: ISAFPermissionRequest?) {
        this.callback = callback
    }

    private var callback: ISAFPermissionRequest? = null

    override fun onContentCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDeleteSongsNoPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //无权限提示文字

        var colorString = "#EE294D"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.messageDetail.text = Html.fromHtml(getString(R.string.music4_delete_permission_access).replace("#7D92FF", colorString), Html.FROM_HTML_MODE_LEGACY)
        } else {
            binding.messageDetail.text = Html.fromHtml(getString(R.string.music4_delete_permission_access).replace("#7D92FF", colorString))
        }

        binding.btnCancel.setOnClickListener {
            callback?.onDismissCancel()
            TrackerMultiple.onEvent("Delete", "DeleteFailed_Cancel")
            dismiss()
        }
        binding.btnAccess.setOnClickListener {
            TrackerMultiple.onEvent("Delete", "DeleteFailed_Allow")
            callback?.onAccessPermissionsRequest()
            dismiss()
        }

        TrackerMultiple.onEvent("Delete", "DeleteFailed_PV")
    }

    override fun onCancel(dialog: DialogInterface) {
        callback?.onDismissCancel()
        super.onCancel(dialog)
    }

    override fun getIcon(): Int {
        return R.drawable.ic_pup_warning
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        callback = null
    }

}