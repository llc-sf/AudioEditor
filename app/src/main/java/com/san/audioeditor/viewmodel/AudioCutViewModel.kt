package com.san.audioeditor.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import android.widget.toast.ToastCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.app.AppProvider
import com.masoudss.lib.utils.WaveformOptions
import com.san.audioeditor.R
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.activity.AudioSaveActivity
import com.san.audioeditor.handler.FFmpegHandler
import com.san.audioeditor.storage.AudioSyncService
import com.san.audioeditor.storage.convertSong
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import dev.audio.ffmpeglib.tool.FFmpegUtil
import dev.audio.ffmpeglib.tool.FileUtil
import dev.audio.timeruler.bean.AudioFragmentBean
import dev.audio.timeruler.utils.AudioFileUtils
import dev.audio.timeruler.utils.toInverseSegmentsArray
import dev.audio.timeruler.utils.toSegmentsArray
import dev.audio.timeruler.weight.BaseAudioEditorView
import dev.audio.timeruler.weight.CutPieceFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference


class AudioCutViewModel(var song: Song) : BaseViewModel<AudioCutViewModel.AudioCutPageData>() {

    companion object {

    }

    //裁剪中取消
    var isCancel = false
    var isConformed = false
    var isCutLineMoved = false
    var isSave = false

    //设置封面
    var isCover = false

    //confirm 临时文件
    var tempConfirmAudios: MutableList<AudioFragmentBean> = mutableListOf()

    // 定义构造 ViewModel 方法
    class AudioCutFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioCutPageData() as T
        }
    }

    private var oriSong: Song = song


    data class AudioCutPageData(
        var song: Song? = null,
        var isShowEditLoading: Boolean? = null,
        var progress: Int? = null,
        var isEnableBack: Boolean? = null,
        var waveform: IntArray? = null,
        var cutMode: Int? = null,
        var isShowEditTips: Boolean? = null,
    )


    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {
                refresh(UiState(isSuccess = AudioCutPageData()))
            }
        }
        ffmpegHandler = FFmpegHandler(mHandler)
    }



    fun reInitSongInfo(context: Context, songPath: String): Song? {
        val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + "=?", arrayOf(songPath), null)
        if (cursor?.moveToNext() == true) {
            return cursor.convertSong().apply {
                song = this
            }
        } else {
            song = getSongFromFilePath(songPath)
            return song
        }
    }


    private fun getSongFromFilePath(filePath: String): Song {
        val retriever = MediaMetadataRetriever()
        val song = Song()

        try {
            retriever.setDataSource(filePath)

            song.path = filePath
            song.title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            song.albumName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            song.artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            song.duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toInt() ?: 0
            song.mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            song.genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)

            // 如果某些信息在元数据中不存在，可以根据需要进行补充或处理
            var fileName = AudioFileUtils.getFileNameWithoutExtension(song.path)
            if (TextUtils.isEmpty(song.title)) {
                song.title = fileName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }

        return song
    }


    fun editorError() {
        Toast.makeText(AppProvider.context, "裁剪失败", Toast.LENGTH_SHORT).show()
        isConformed = false
        isSave = false
        isCover = false
        isCancel = true
        refresh(UiState(isSuccess = AudioCutPageData(isShowEditLoading = false, isEnableBack = true)))
        deleteTempFiles()
    }

    fun onSaveSuccess() {
        isSave = false
        isConformed = false
        isSave = false
        isCover = false
        isCancel = true
        refresh(UiState(isSuccess = AudioCutPageData(isShowEditLoading = false, isEnableBack = true)))
        deleteTempFiles()
    }

    fun onConfirmSuccess() {
        isSave = false
        isConformed = false
        isSave = false
        isCover = false
        isCancel = true
        refresh(UiState(isSuccess = AudioCutPageData(isShowEditLoading = false, isEnableBack = true)))
        deleteTempFiles()
    }

    private fun showEditTips() {
        refresh(UiState(isSuccess = AudioCutPageData(isShowEditTips = true)))
    }

    private fun deleteTempFiles() {
        AudioFileUtils.clearTempFiles()
    }

    fun clearDatas() {
        deleteTempFiles()
    }

    fun getAudioData(context: Context, cutMode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            WaveformOptions.getSampleFrom(context, song.path) {
                launchOnUI {
                    refresh(UiState(isSuccess = AudioCutPageData(waveform = it, cutMode = cutMode)))
                }
            }
        }
    }

    private var ffmpegHandler: FFmpegHandler? = null

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        var context: WeakReference<Context>? = null
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                FFmpegHandler.MSG_BEGIN -> {
                    Log.i(BaseAudioEditorView.jni_tag, "begin")
                    if (!isCover) {
                        refresh(UiState(isSuccess = AudioCutPageData(isShowEditLoading = true)))
                    }

                }

                FFmpegHandler.MSG_FINISH -> {
                    if (isCancel) {
                        return
                    }
                    if (isSave) {
                        if (msg.obj == 0) {
                            if (!isCover) {
                                isCover = true
                                var commandLine = FFmpegUtil.addCoverToAudioStep2(oriSong.path, editorAssetsPathTempWithoutCover, editorAssetsPathWithCover)
                                ffmpegHandler!!.executeFFmpegCmd(commandLine)
                            } else {
                                var file = AudioFileUtils.copyAudioToFileStore(File(editorAssetsPathWithCover), AppProvider.context, saveFileName)
                                if (file != null) { //                                AudioFileUtils.deleteFile(editorAssetsPath)
                                    AudioFileUtils.notifyMediaScanner(AppProvider.context, file.absolutePath) { path: String, uri: Uri ->
                                        context?.get()?.let {
                                            reInitSongInfo(it, path)
                                            if (song != null) {
                                                AudioSaveActivity.open(it, song)
                                                AudioSyncService.sync(AppProvider.context)
                                                (it as? Activity)?.finish()
                                                onSaveSuccess()
                                            } else {
                                                editorError()
                                            }
                                        }
                                    }
                                } else {
                                    editorError()
                                }
                            }
                        } else {
                            editorError()
                        }
                    } else {
                        if (msg.obj == 0) {
                            showEditTips()
                            AudioFileUtils.notifyMediaScanner(AppProvider.context, editorAssetsPathTempWithoutCover) { path: String?, uri: Uri? ->
                                reInitSongInfo(AppProvider.context, editorAssetsPathTempWithoutCover)
                                if (song != null) {
                                    context?.get()?.let {
                                        AudioCutActivity.open(it, song)
                                    }
                                    onConfirmSuccess()
                                } else {
                                    editorError()
                                }
                            }
                        } else {
                            editorError()
                        }
                    }
                }

                FFmpegHandler.MSG_PROGRESS -> {
                    val progress = msg.arg1
                    Log.i(BaseAudioEditorView.jni_tag, "progress=$progress")
                    refresh(UiState(isSuccess = AudioCutPageData(progress = progress)))
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


    fun audioDeal(context: Context, cutMode: Int, realCutPieceFragments: List<CutPieceFragment>?) {
        audioEditorDeal(context, cutMode, realCutPieceFragments, false)
    }

    //裁剪产物文件路径 绝对路径 有封面图
    var editorAssetsPathWithCover = ""

    //第一次裁剪产物文件路径 绝对路径 没有封面图
    var editorAssetsPathTempWithoutCover = ""

    //最终保存文件名
    var saveFileName = ""

    private fun audioEditorDeal(context: Context,
                                cutMode: Int,
                                realCutPieceFragments: List<CutPieceFragment>?,
                                isSave: Boolean) {
        this.isSave = isSave
        if (!isSave) {
            isConformed = true
        }
        isCancel = false
        if (realCutPieceFragments.isNullOrEmpty()) {
            ToastCompat.makeText(context, false, context.getString(R.string.error_save)).show()
            return
        }
        mHandler.context = WeakReference(context)
        viewModelScope.launch(Dispatchers.IO) {
            var realCutPieceFragments = realCutPieceFragments?.filter { !it.isFake }
            if (realCutPieceFragments.isNullOrEmpty()) {
                ToastCompat.makeText(context, context.getString(R.string.error_save)).show()
                refresh(UiState(isSuccess = AudioCutPageData(isEnableBack = true)))
                return@launch
            }
            var commandLine: Array<String>? = null
            if (!FileUtil.checkFileExist(song.path)) {
                refresh(UiState(isSuccess = AudioCutPageData(isEnableBack = true)))
                return@launch
            }
            if (!FileUtil.isAudio(song.path)) {
                refresh(UiState(isSuccess = AudioCutPageData(isEnableBack = true)))
                return@launch
            }

            if (isSave) { //1、裁剪到应用文件夹 文件名为 原名_1  2、复制到到公共文件夹 3、清空AudioEditor338
                var tempResultFilePath = AudioFileUtils.PUBLIC_PATH + AudioFileUtils.getFileName(oriSong.path)
                saveFileName = AudioFileUtils.getFileName(AudioFileUtils.generateNewFilePath(tempResultFilePath))
                editorAssetsPathWithCover = AudioFileUtils.generateNewFilePath(AudioFileUtils.APP_PATH + AudioFileUtils.getFileName(song.path))
                editorAssetsPathTempWithoutCover = AudioFileUtils.generateNewFilePath(AudioFileUtils.APP_PATH + AudioFileUtils.AUDIO_TEMP_UN_COVER + AudioFileUtils.getFileName(song.path))
            } else { //1、裁剪到应用文件夹AudioEditor338中 文件名为 cut_时间戳  2、清空AudioEditor338
                var suffix = FileUtil.getFileSuffix(song.path) ?: ""
                if (suffix.isNullOrEmpty()) {
                    editorError()
                    return@launch
                }
                var cutFileName = AudioFileUtils.AUDIO_CUT + (System.currentTimeMillis())
                var confirmTempFileName = cutFileName + suffix
                editorAssetsPathTempWithoutCover = AudioFileUtils.APP_PATH + confirmTempFileName
            }
            if (!File(AudioFileUtils.APP_PATH).exists()) {
                File(AudioFileUtils.APP_PATH).mkdirs()
            }
            commandLine = FFmpegUtil.cutMultipleAudioSegments(song.path, if (cutMode == CutPieceFragment.CUT_MODE_DELETE) realCutPieceFragments.toInverseSegmentsArray(song.duration.toFloat()) else realCutPieceFragments.toSegmentsArray(), editorAssetsPathTempWithoutCover) //            commandLine = FFmpegUtil.addCoverToAudio1("/storage/emulated/0/Android/data/com.san.audioeditor/files/Music/AudioEditor338/cover_song.mp3","/storage/emulated/0/Android/data/com.san.audioeditor/files/Music/AudioEditor338/cover_song.mp3",  "/storage/emulated/0/Android/data/com.san.audioeditor/files/Music/cover/cover_song1.mp3")
            //            Log.i(BaseAudioEditorView.jni_tag, "editorResultPath=${editorAssetsPath}") //打印 commandLine
            var sb = StringBuilder()
            commandLine?.forEachIndexed { index, s ->
                sb.append("$s ")
                android.util.Log.i(BaseAudioEditorView.jni_tag, "s=$s")
            }
            Log.i(BaseAudioEditorView.jni_tag, "sb=$sb")
            if (ffmpegHandler != null && commandLine != null) {
                ffmpegHandler!!.executeFFmpegCmd(commandLine)
            }
        }
    }

    fun save(context: Context, cutMode: Int, realCutPieceFragments: List<CutPieceFragment>?) {
        audioEditorDeal(context, cutMode, realCutPieceFragments, true)
    }

    fun cancelEditor() {
        ffmpegHandler?.cancelExecute(true)
    }

}