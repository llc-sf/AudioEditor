package com.san.audioeditor.delete.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.san.audioeditor.R
import com.san.audioeditor.databinding.DialogSdcardPermissionBinding
import dev.android.player.framework.utils.TrackerMultiple
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager


/**
 * Sdcard permission dialog
 */
class SDCardPermissionDialog : BaseBottomTranslucentDialog() {

    companion object {


        private const val TAG = "SDCardPermissionDialog"


        @JvmStatic
        fun show(activity: AppCompatActivity, callback: ISAFPermissionRequest?) {
            val dialog = SDCardPermissionDialog()
            dialog.setCallback(callback)
            BottomDialogManager.show(activity, dialog)
        }
    }

    private fun setCallback(callback: ISAFPermissionRequest?) {
        this.callback = callback
    }

    private var callback: ISAFPermissionRequest? = null

    private var _binding: DialogSdcardPermissionBinding? = null
    private val binding get() = _binding!!


    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogSdcardPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var colorString = "#EE294D"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.tips1.text = Html.fromHtml(getString(R.string.music4_sd_card_access_step_1_format).replace("#7D92FF", colorString), Html.FROM_HTML_MODE_LEGACY)
            binding.tips2.text = Html.fromHtml(getString(R.string.music4_sd_card_access_step_2_format).replace("#7D92FF", colorString), Html.FROM_HTML_MODE_LEGACY)
        } else {
            binding.tips1.text = Html.fromHtml(getString(R.string.music4_sd_card_access_step_1_format).replace("#7D92FF", colorString))
            binding.tips2.text = Html.fromHtml(getString(R.string.music4_sd_card_access_step_2_format).replace("#7D92FF", colorString))
        }

        binding.btnAccess.setOnClickListener {
            TrackerMultiple.onEvent("Delete", "NoPermission_Allow")
            callback?.onAccessPermissionsRequest()
            dismiss()
        }
        TrackerMultiple.onEvent("Delete", "NoPermission_PV")
    }

    override fun onCancel(dialog: DialogInterface) {
        callback?.onDismissCancel()
        super.onCancel(dialog)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}