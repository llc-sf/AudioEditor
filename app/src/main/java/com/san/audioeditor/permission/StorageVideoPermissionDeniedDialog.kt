package com.san.audioeditor.permission

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.san.audioeditor.R
import com.san.audioeditor.databinding.DialogStoragePermissionDeniedBinding
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.TrackerMultiple
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager

/**
 * 存储权限拒绝——video&img
 */
class StorageVideoPermissionDeniedDialog : BaseBottomTranslucentDialog() {


    companion object {

        @JvmStatic
        fun show(manager: FragmentManager?) {
            if (manager != null) {
                val dialog = StorageVideoPermissionDeniedDialog()
                BottomDialogManager.show(manager, dialog)
            }
        }
    }

    private var _binding: DialogStoragePermissionDeniedBinding? = null
    private val binding get() = _binding!!


    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        isCancelable = false
        _binding = DialogStoragePermissionDeniedBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.message.text = getString(R.string.music3_grant_media_files_access_gpt, getString(R.string.app_name_place_holder))
        binding.msgSub.text = getString(R.string.music4_allow_photo_video)
        binding.msgSub.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        //重新请求权限
        binding.btnOpenSetting.text = getString(R.string.btn_allow)
        binding.btnOpenSetting.setOnClickListener {
            TrackerMultiple.onEvent("Storage_Videos", "Permission1_Allow")
            (parentFragment as? InternalFragment)?.onRequestPermissionReal(isFirst = false, AndroidUtil.getVideoPermissionPermissionStringAdapter33())
            dismiss()
        }
        TrackerMultiple.onEvent("Storage_Videos", "Permission1_PV")
    }


    override fun isDragClose(): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnReQuestPermissionListener {
        fun onReQuestPermissionResult(isAllow: Boolean)
        fun onReQuestPermission()
    }
}