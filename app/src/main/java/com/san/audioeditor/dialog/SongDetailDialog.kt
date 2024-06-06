package com.san.audioeditor.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.android.app.AppProvider
import com.san.audioeditor.databinding.DialogSongDetailBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.android.player.framework.utils.FileUtils
import dev.android.player.framework.utils.fileName2Extension
import dev.audio.timeruler.timer.BaseBottomTranslucentDialog
import dev.audio.timeruler.timer.BottomDialogManager
import dev.audio.timeruler.utils.AudioFileUtils
import java.io.File


/**
 * 歌曲详情
 */
class SongDetailDialog : BaseBottomTranslucentDialog() {

    companion object {

        private const val TAG = "SongDetailDialog"

        private const val EXTRA_SONG = "extra_song"

        @JvmStatic
        fun show(manger: FragmentManager, song: Song?) {
            val dialog = SongDetailDialog()
            dialog.arguments = bundleOf(EXTRA_SONG to song)
            BottomDialogManager.show(manger, dialog)
        }
    }

    private val mSong: Song? by lazy {
        arguments?.getParcelable(EXTRA_SONG)
    }


    private var _binding: DialogSongDetailBinding? = null
    private val binding get() = _binding!!
    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}


    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogSongDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.songTitleValue.text = mSong?.title ?: ""
        binding.songFormValue.text = (mSong?.path ?: "").fileName2Extension()?.uppercase()
        binding.bitRateValue.text = AudioFileUtils.getAudioBitRate(mSong?.path ?: "")
        binding.songSampleRateValue.text = AudioFileUtils.getAudioSampleRate(mSong?.path ?: "")
        binding.songDurationValue.text = DateUtil.formatTime(mSong?.duration?.toLong() ?: 0L)
        binding.songSizeValue.text = FileUtils.getFileSize(mSong?.size ?: 0L)


        // 设置下划线
        val content = SpannableString(mSong?.path ?: "")
        content.setSpan(UnderlineSpan(), 0, content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.songLocationValue.text = content

        // 设置点击事件
        binding.songLocationValue.setOnClickListener {
            File(mSong?.path ?: "").parent?.let { folderPath ->
                AudioFileUtils.openFolder(openDocumentLauncher, folderPath)
            }
        }

        binding.close.setOnClickListener {
            dismiss()
        }

    }

    private fun openFolder2(path: String) {
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(path), "resource/folder")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            // 检查是否有应用可以处理这个Intent
            if (intent.resolveActivity(AppProvider.context.packageManager) != null) {
                startActivity(intent)
            } else {
                // 如果没有应用可以处理这个Intent，尝试使用其他方法
                openFolderWithFileProvider(path)
            }
        } else {
            println("文件夹不存在")
        }
    }

    private fun openFolderWithFileProvider(path: String) {
        val file = File(path)
        val uri = FileProvider.getUriForFile(
            AppProvider.context,
            "${AppProvider.context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            println("无法打开文件夹")
        }
    }

    private fun openFolder1(path: String) {
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val uri = FileProvider.getUriForFile(
                AppProvider.context,
                "${AppProvider.context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "resource/folder")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                // 处理无法打开文件夹的情况
                println("无法打开文件夹")
            }
        } else {
            // 处理文件夹不存在的情况
            println("文件夹不存在")
        }
    }

    private fun openFolder(path: String) {
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            val uri = FileProvider.getUriForFile(AppProvider.context, "${AppProvider.context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        } else { // 处理文件夹不存在的情况
            println("文件夹不存在")
        }
    }

    private fun openMusic(path: String) {
        val uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:$path")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("*/*") //想要展示的文件类型

        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        startActivityForResult(intent, 0)

    }


}