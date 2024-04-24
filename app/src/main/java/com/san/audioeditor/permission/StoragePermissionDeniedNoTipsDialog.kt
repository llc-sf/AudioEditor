package com.san.audioeditor.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.san.audioeditor.databinding.DialogStoragePermissionDeniedNoTipsBinding
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.TrackerMultiple
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager

/**
 * 存储权限拒绝并不再提示弹窗
 */
class StoragePermissionDeniedNoTipsDialog : BaseBottomTranslucentDialog() {

    companion object {
        private const val TAG = "StoragePermissionDeniedNoTipsDialog"

        @JvmStatic
        fun show(manager: FragmentManager?) {
            if (manager == null) {
                return
            }
            val dialog = StoragePermissionDeniedNoTipsDialog()
            BottomDialogManager.show(manager, dialog)
        }
    }

    private var _binding: DialogStoragePermissionDeniedNoTipsBinding? = null
    private val binding get() = _binding!!


    private var isRequestSetting = false//是否已经请求设置界面

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        isCancelable = false
        _binding = DialogStoragePermissionDeniedNoTipsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            isRequestSetting = savedInstanceState.getBoolean("isRequestSetting")
        }
        //重新请求权限
        binding.btnOpenSetting.setOnClickListener {
            startSettings(it.context)
        }
        TrackerMultiple.onEvent("Storage_Audio", "Permission2_PV")
    }


    private fun startSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
        TrackerMultiple.onEvent("Storage_Audio", "Permission2_OpenSetting")
        isRequestSetting = true
    }


    override fun onResume() {
        super.onResume()
        if (isRequestSetting) {
            if (StoragePermissionManager.hasStoragePermission(context, AndroidUtil.getStoragePermissionPermissionString())) {
                TrackerMultiple.onEvent("Storage_Audio", "Permission2_Success")
                (parentFragment as? InternalFragment)?.onStoragePermissionGrant()
                dismiss()
            } else {
                TrackerMultiple.onEvent("Storage_Audio", "Permission2_Failed")
            }
        }
    }

    override fun isDragClose(): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isRequestSetting", isRequestSetting)
    }
}