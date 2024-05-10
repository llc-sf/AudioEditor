package com.san.audioeditor.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.app.AppProvider
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.activity.AudioSaveActivity
import com.san.audioeditor.handler.FFmpegHandler
import com.san.audioeditor.storage.AudioSyncService
import com.san.audioeditor.storage.convertSong
import com.san.audioeditor.viewmodel.pagedata.AudioCutPageData
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import dev.audio.ffmpeglib.FFmpegApplication
import dev.audio.ffmpeglib.tool.FFmpegUtil
import dev.audio.ffmpeglib.tool.FileUtil
import dev.audio.recorder.utils.Log
import dev.audio.timeruler.bean.AudioFragmentBean
import dev.audio.timeruler.utils.AudioFileUtils
import dev.audio.timeruler.utils.toSegmentsArray
import dev.audio.timeruler.weight.BaseAudioEditorView
import dev.audio.timeruler.weight.CutPieceFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference

class AudioCutViewModel(var song: Song) : BaseViewModel<AudioCutPageData>() {

    companion object {

    }

    var isCancel = false
    var isConformed = false
    var isCutLineMoved = false

    var datas: MutableList<AudioFragmentBean> = mutableListOf()

    // 定义构造 ViewModel 方法
    class ThemeListViewFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioCutViewModel() as T
        }
    }

    private val _audioCutViewState = MutableLiveData<AudioCutViewModel>()
    var audioCutState: LiveData<AudioCutViewModel> = _audioCutViewState


    data class AudioCutViewModel(
        var song: Song? = null,
        var isShowEditLoading: Boolean? = null,
        var progress: Int? = null,
    )


    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {
                refresh(AudioCutViewModel())
            }
        }
        ffmpegHandler = FFmpegHandler(mHandler)
    }


    private fun refresh(pageState: AudioCutViewModel) {
        _audioCutViewState.value = pageState
    }

    var outputPath = ""
    fun save(context: Context,
             realCutPieceFragments: List<CutPieceFragment>?,
             datas: MutableList<AudioFragmentBean>) {
        mHandler.datas = datas
        mHandler.context = WeakReference(context)
        viewModelScope.launch(Dispatchers.IO) {
            val PATH = FFmpegApplication.instance?.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath

            var realCutPieceFragments = realCutPieceFragments?.filter { !it.isFake }
            if (realCutPieceFragments.isNullOrEmpty()) {
                Toast.makeText(context, "请先选择片段", Toast.LENGTH_SHORT).show()
                return@launch
            }
            var commandLine: Array<String>? = null
            if (!FileUtil.checkFileExist(song.path)) {
                return@launch
            }
            if (!FileUtil.isAudio(song.path)) {
                return@launch
            }
            outputPath = AudioFileUtils.generateNewFilePath(PATH + File.separator + AudioFileUtils.getFileName(song.path))

            commandLine = FFmpegUtil.cutMultipleAudioSegments(song.path, realCutPieceFragments.toSegmentsArray(), outputPath)
            android.util.Log.i(BaseAudioEditorView.jni_tag, "outputPath=${outputPath}") //打印 commandLine
            var sb = StringBuilder()
            commandLine?.forEachIndexed { index, s ->
                sb.append("$s ")
                android.util.Log.i(BaseAudioEditorView.jni_tag, "s=$s")
            }
            android.util.Log.i(BaseAudioEditorView.jni_tag, "sb=$sb")
            if (ffmpegHandler != null && commandLine != null) {
                ffmpegHandler!!.executeFFmpegCmd(commandLine)
            }
        }
    }

    fun getSongInfo(context: Context, songPath: String): Song? {
        val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "=?", arrayOf(songPath), null)
        if (cursor?.moveToNext() == true) {
            return cursor.convertSong()
        }
        return null
    }

    private var ffmpegHandler: FFmpegHandler? = null

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        var context: WeakReference<Context>? = null
        var datas: MutableList<AudioFragmentBean>? = null
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                FFmpegHandler.MSG_BEGIN -> {
                    Log.i(BaseAudioEditorView.jni_tag, "begin")
                    refresh(AudioCutViewModel(isShowEditLoading = true))
                }

                FFmpegHandler.MSG_FINISH -> {
                    Log.i(BaseAudioEditorView.jni_tag, "finish resultCode=${msg.obj}")
                    if (msg.obj == 0) {
                        refresh(AudioCutViewModel(isShowEditLoading = false))
                        deleteTempFiles()
                        var cutFileName = AudioFileUtils.getFileName(outputPath)
                        var file = AudioFileUtils.copyAudioToFileStore(File(outputPath), AppProvider.context, cutFileName)
                        if (file != null) {
                            AudioFileUtils.notifyMediaScanner(AppProvider.context, file.absolutePath) { path: String, uri: Uri ->
                                context?.get()?.let {
                                    val song = getSongInfo(it, path)
                                    if (song != null) {
                                        AudioSaveActivity.open(it, song)
                                    }
                                    (it as? Activity)?.finish()
                                    AudioSyncService.sync(AppProvider.context)
                                }
                            }
                        } else {
                            Toast.makeText(AppProvider.context, "裁剪失败", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                FFmpegHandler.MSG_PROGRESS -> {
                    val progress = msg.arg1
                    Log.i(BaseAudioEditorView.jni_tag, "progress=$progress")
                    refresh(AudioCutViewModel(progress = progress))
                }

                FFmpegHandler.MSG_INFO -> {
                    Log.i(BaseAudioEditorView.jni_tag, "${msg.obj}")
                }

                FFmpegHandler.MSG_CONTINUE -> {
                    Log.i(BaseAudioEditorView.jni_tag, "continue")
                }

                else -> {
                }
            }
        }
    }

    private fun deleteTempFiles() {
        GlobalScope.launch(Dispatchers.IO) {
            datas?.forEachIndexed() { index, audioFragmentBean ->
                audioFragmentBean.path?.let {
                    FileUtil.deleteFile(it)
                }
            }
        }
    }

    fun clearDatas() {
        deleteTempFiles()
    }

}