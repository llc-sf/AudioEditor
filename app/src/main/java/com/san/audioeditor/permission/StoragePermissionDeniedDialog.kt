package com.san.audioeditor.permission

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
 * 存储权限拒绝
 */
class StoragePermissionDeniedDialog : BaseBottomTranslucentDialog() {


    companion object {
        private const val TAG = "StoragePermissionDeniedDialog"

        @JvmStatic
        fun show(manager: FragmentManager?) {
            if (manager != null) {
                val dialog = StoragePermissionDeniedDialog()
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
        binding.message.text = getString(R.string.music3_guide_allow_description, getString(R.string.app_name_place_holder))
        //重新请求权限
        binding.btnOpenSetting.setOnClickListener {
            TrackerMultiple.onEvent("Storage_Audio", "Permission1_OpenSetting")
            (parentFragment as? InternalFragment)?.onRequestPermissionReal(isFirst = false, AndroidUtil.getStoragePermissionPermissionString())
            dismiss()
        }
        TrackerMultiple.onEvent("Storage_Audio", "Permission1_PV")
    }

    override fun isDragClose(): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}