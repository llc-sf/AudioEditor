package com.san.audioeditor.delete.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.san.audioeditor.R
import com.san.audioeditor.databinding.StorageLessRecommenderBinding
import dev.android.player.framework.utils.AndroidUtil
import dev.android.player.framework.utils.FileUtils
import dev.audio.timeruler.timer.BottomDialogManager
import dev.audio.timeruler.timer.TopIconTranslucentDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StorageLessRecommenderDialog : TopIconTranslucentDialog() {

    companion object {
        private const val TAG = "StorageLessRecommenderDialog"
        const val DEFAULT_REMAINING_SIZE = 1024L * 1024L * 80L

        @JvmStatic
        fun show(activity: Activity) {
            BottomDialogManager.show(activity, StorageLessRecommenderDialog())
        }
    }

    private var _binding: StorageLessRecommenderBinding? = null
    private val binding get() = _binding!!
    private var isGoClear = false

    override fun onContentCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = StorageLessRecommenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.free.setOnClickListener {
            isGoClear = true
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
            startActivity(intent)
        }
        binding.required.text = getString(R.string.free_space_needed, FileUtils.getFileSize(DEFAULT_REMAINING_SIZE))
        refreshRemainingSize()
    }

    private fun refreshRemainingSize() {
        lifecycleScope.launch(Dispatchers.Main) {
            var result = withContext(Dispatchers.IO) {
                AndroidUtil.getInternalStorageFreeSpace()
            }
            binding.remaining.text = getString(R.string.remaining_x, FileUtils.getFileSize(result))
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isAdded) {
            return
        }
        if (isGoClear) {
            lifecycleScope.launch(Dispatchers.Main) {
                var enough = withContext(Dispatchers.IO) {
                    AndroidUtil.getInternalStorageFreeSpace() > DEFAULT_REMAINING_SIZE
                }
                if (enough) {
                    dismiss()
                } else {
                    refreshRemainingSize()
                }
            }
        }
        isGoClear = false
    }

    override fun getIcon(): Int {
        return R.drawable.ic_stroage_less_warning
    }
}