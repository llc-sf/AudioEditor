package com.san.audioeditor.dialog

import android.content.ContentValues
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.android.app.AppProvider
import com.san.audioeditor.R
import com.san.audioeditor.databinding.DialogRenameSongBinding
import com.san.audioeditor.storage.AudioSyncService
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.ContentHelper
import dev.android.player.framework.utils.IOUtils
import dev.android.player.framework.utils.KeyboardUtil
import dev.android.player.framework.utils.dp
import dev.android.player.framework.utils.fileName2Name
import dev.android.player.framework.utils.filePath2FileNameWithExtension
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * 重名播放列表
 */
class RenameAudioDialog : BaseBottomTranslucentDialog() {


    private var origLength = 0

    companion object {
        private const val TAG = "RenameVideoDialog"

        private const val EXTRA_SONG = "extra_song"

        const val DEFAULT_LENGTH = 100

        @JvmOverloads
        @JvmStatic
        fun show(manager: FragmentManager?, song: Song): RenameAudioDialog {
            val fragment = RenameAudioDialog()
            val args = bundleOf(EXTRA_SONG to song)
            fragment.arguments = args
            manager?.apply { BottomDialogManager.show(this, fragment) }
            return fragment
        }
    }

    interface OnRenameResultListener {
        fun onResult(success: Boolean, song: Song)
    }

    private var onRenameResultListener: OnRenameResultListener? = null

    fun setOnRenameResultListener(listener: OnRenameResultListener) {
        onRenameResultListener = listener
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
        binding.btnCreate.setOnClickListener { onRename() }
        binding.btnCreate.setText(R.string.rename)
        onCheckInput(null)
        binding.edit.text = mSong?.path?.filePath2FileNameWithExtension()?.fileName2Name()?.let {
            origLength = it.length
            var max = if (origLength > DEFAULT_LENGTH) origLength else DEFAULT_LENGTH
            binding.edit.filters = arrayOf(InputFilter.LengthFilter(max))
            SpannableStringBuilder(it)
        }
        binding.edit.setSelection(binding.edit.text.length)
        binding.edit.selectAll()
        setMaxLength(binding.edit.text)

        binding.edit.doAfterTextChanged(this::onCheckInput)

        binding.edit.postDelayed(showKeyBoardRunnable, 200)

        binding.clear.setOnClickListener {
            binding.edit.setText("")
        }

        // 获取当前文件夹下所有文件名称（不带扩展名）并加入 mExistsNames
        mSong?.path?.let { songPath ->
            val currentFolder = File(songPath).parentFile
            currentFolder?.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val fileNameWithoutExtension = file.name.substringBeforeLast('.')
                    mExistsNames.add(fileNameWithoutExtension)
                }
            }
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
            if (TextUtils.equals(name.toString().trim(), mSong?.path?.fileName2Name())) { //和原来的名字一样
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
            binding.editCount.setTextColor(if (length >= DEFAULT_LENGTH) ContextCompat.getColor(it, R.color.colorAccent)
                                           else ContextCompat.getColor(it, R.color.txt_color_secondary))
        }
        binding.existTips.visibility = View.INVISIBLE
        binding.clear.isVisible = !TextUtils.isEmpty(name.toString())
    }

    /**
     * 创建新的播放列表
     */
    /**
     * 创建新的播放列表
     */
    private fun onRename() {
        if (mSong == null) return
        val name: String = binding.edit.text?.trim()?.toString() ?: ""

        //判断是否重名
        if (mExistsNames.contains(name.lowercase(Locale.getDefault()))) {
            if (binding.existTips.isVisible) {
                binding.existTips.startAnimation(getShakeAnim())
            } else {
                binding.existTips.visibility = View.VISIBLE
            }
            return
        }
        lifecycleScope.launch {
            var result = rename(mSong!!, name)
            if(result){
                mSong?.title = name
                mSong?.path = newPath
            }
            onResult(result, mSong!!)
        }
    }


    var toRenamePath: String = ""
    var newPath: String = ""
    private suspend fun rename(song: Song, newName: String): Boolean =
        withContext(Dispatchers.IO) { // 全部涉及到的文件路径改一遍：
            toRenamePath = song.path
            newPath = toRenamePath.substring(0, toRenamePath.lastIndexOf("/") + 1) + newName + toRenamePath.substring(toRenamePath.lastIndexOf("."))
            doRename()
        }

    private fun doRename(): Boolean {
        val file = File(toRenamePath)
        val newFile = File(newPath)

        var isSuccess = false
        try {
            isSuccess = file.renameTo(newFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isSuccess) {
            successRename()
            return true
        }

        try {
            isSuccess = newRename(file, newFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isSuccess) {
            successRename()
            return true
        }

        try {
            isSuccess = ContentHelper.renameFile(AppProvider.context, file, newFile.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isSuccess) {
            successRename()
        }
        return isSuccess

    }

    private fun onResult(isSuccess: Boolean, song: Song) {
        onRenameResultListener?.onResult(isSuccess, song)
        dismiss()
    }

    private fun newRename(file: File, newFile: File): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) false else try {
            val fileUri = IOUtils.getFileUri(AppProvider.context, file.path)
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, newFile.name)
            val i = AppProvider.context.contentResolver.update(fileUri, contentValues, null, null)
            i > 0 || newFile.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun successRename() {
        MediaScannerConnection.scanFile(AppProvider.context, arrayOf(File(newPath).absolutePath), // 文件路径数组
                                        null) // MIME类型数组，null将自动确定MIME类型
        { path, uri -> //            VideoFileScanner.loadVideoDataAsync(false, false, AtomicBoolean(false))
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    AudioSyncService.sync(AppProvider.context)
                }
            }
        }
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

    override fun dismiss() {
        super.dismiss()
        this.onRenameResultListener = null
    }


}