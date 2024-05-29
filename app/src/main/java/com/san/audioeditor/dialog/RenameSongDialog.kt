package com.san.audioeditor.dialog

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.toast.ToastCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.san.audioeditor.R
import com.san.audioeditor.databinding.DialogRenameSongBinding
import dev.android.player.framework.data.model.MediaFileInfo
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.KeyboardUtil
import dev.android.player.framework.utils.TrackerMultiple
import dev.android.player.framework.utils.dp
import dev.android.player.framework.utils.fileName2ExtensionWithPoint
import dev.android.player.framework.utils.fileName2Name
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * 重名播放列表
 */
class RenameSongDialog : BaseBottomTranslucentDialog() {



    private var origLength = 0

    companion object {
        private const val TAG = "RenameVideoDialog"

        private const val EXTRA_SONG = "extra_song"

        const val DEFAULT_LENGTH = 100

        @JvmOverloads
        @JvmStatic
        fun show(manager: FragmentManager?, song: Song) {
            val fragment = RenameSongDialog()
            val args = bundleOf(EXTRA_SONG to song)
            fragment.arguments = args
            manager?.apply { BottomDialogManager.show(this, fragment) }
        }
    }


    //已经存在的PlayList名称
    private val mExistsNames = mutableListOf<String>()


    private val mSong by lazy {
        arguments?.getParcelable<Song>(EXTRA_SONG)
    }

    private var _binding: DialogRenameSongBinding? = null
    private val binding get() = _binding!!

    private val showKeyBoardRunnable = Runnable {
        if (_binding != null) {
            KeyboardUtil.showKeyBoard(binding.edit)
        }
    }


    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogRenameSongBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.setText(R.string.rename)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnCreate.setOnClickListener { onRenameNewPlayList() }
        binding.btnCreate.setText(R.string.rename)
        onCheckInput(null)
        binding.edit.text = mSong?.path?.fileName2Name()?.let {
            origLength = it.length
            var max = if (origLength > DEFAULT_LENGTH) origLength else DEFAULT_LENGTH
            binding.edit.filters = arrayOf(InputFilter.LengthFilter(max))
            SpannableStringBuilder(it)
        }
        binding.edit.setSelection(binding.edit.text.length)
        setMaxLength(binding.edit.text)

        binding.edit.doAfterTextChanged(this::onCheckInput)

        binding.edit.postDelayed(showKeyBoardRunnable, 200)

        binding.clear.setOnClickListener {
            binding.edit.setText("")
        }
    }


    /**
     * 检查输入数据
     */
    private fun onCheckInput(name: Editable?) {
        if (origLength > DEFAULT_LENGTH) {
            var max = if ((name?.toString()?.length ?: 0) <= DEFAULT_LENGTH) {
                DEFAULT_LENGTH
            } else {
                origLength
            }
            binding.edit.filters = arrayOf(InputFilter.LengthFilter(max))
        }
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(name.toString().trim())) {
            binding.btnCreate.isEnabled = false
            binding.btnCreate.alpha = 0.2f
        } else {
            if (TextUtils.equals(name.toString().trim(), mSong?.path?.fileName2Name())) {
                //和原来的名字一样
                binding.btnCreate.isEnabled = false
                binding.btnCreate.alpha = 0.2f
            } else {
                binding.btnCreate.isEnabled = true
                binding.btnCreate.alpha = 1f
            }
        }
        setMaxLength(name)
    }

    private fun setMaxLength(name: Editable?) {
        val length = name?.length ?: 0
        binding.editCount.text = String.format("%d/%d", length, DEFAULT_LENGTH)
        context?.let {
            binding.editCount.setTextColor(if (length >= DEFAULT_LENGTH)
                                               ContextCompat.getColor(it, R.color.colorAccent)
                                           else
                                               ContextCompat.getColor(it, R.color.txt_color_secondary))
        }
        binding.existTips.visibility = View.INVISIBLE
        binding.clear.isVisible = !TextUtils.isEmpty(name.toString())
    }

    /**
     * 创建新的播放列表
     */
    private fun onRenameNewPlayList() {
        if (mSong == null) return
        val newName: String = binding.edit.text?.trim()?.toString() ?: ""

        //判断是否重名
        if (mExistsNames.contains(newName.lowercase(Locale.getDefault()) + mSong!!.path!!.lowercase(Locale.getDefault()).fileName2ExtensionWithPoint())) {
            if (binding.existTips.isVisible) {
                binding.existTips.startAnimation(getShakeAnim())
            } else {
                binding.existTips.visibility = View.VISIBLE
            }
            return
        }
        val activity = activity ?: return
        val exceptionHandler = CoroutineExceptionHandler { _, _ ->
            ToastCompat.makeText(activity, false, R.string.rename_failed).show()
        }
        lifecycleScope.launch(exceptionHandler) {
            var result = withContext(Dispatchers.IO) {
                true
            }
            if (result) {
                ToastCompat.makeText(activity, true, R.string.rename_success).show()
                TrackerMultiple.onEvent("More_Videos", "Rename_Success")
            } else {
                ToastCompat.makeText(activity, false, R.string.rename_failed).show()
            }
            dismiss()
        }


    }

    private fun rename(mVideo: MediaFileInfo, newName: String) {
        // 全部涉及到的文件路径改一遍：
        doRename(mVideo.filePath, newName + mVideo.fileName.fileName2ExtensionWithPoint())
    }

    private fun doRename(filePath: String, newName: String) {

    }


    private fun getShakeAnim(): Animation {
        val anim = TranslateAnimation(0f, 4f.dp, 0f, 0f)
        anim.interpolator = CycleInterpolator(3f)
        anim.duration = 500
        return anim
    }

    override fun getSoftInputMode(): Int {
        return WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.edit.removeCallbacks(showKeyBoardRunnable)
        _binding = null
    }


}