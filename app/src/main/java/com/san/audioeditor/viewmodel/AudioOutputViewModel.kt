package com.san.audioeditor.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.app.AppProvider
import com.san.audioeditor.R
import com.san.audioeditor.storage.AudioSyncService
import com.san.audioeditor.storage.AudioSyncUtil
import com.san.audioeditor.viewmodel.pagedata.AudioPickPageData
import dev.android.player.app.business.SortBusiness
import dev.android.player.app.business.data.SortStatus
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Directory
import dev.android.player.framework.data.model.Song
import dev.audio.recorder.utils.Log
import dev.audio.timeruler.utils.AudioFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.stream.Collectors

class AudioOutputViewModel : BaseViewModel<AudioPickPageData>() {

    companion object {

        const val ALL_AUDIO = "all/audio"
    }

    var playingPosition: Int = -1

    lateinit var mCurrentSort: SortStatus

    // 定义构造 ViewModel 方法
    class AudioOutputViewFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioOutputViewModel() as T
        }
    }

    private val _outputViewState = MutableLiveData<AudioOutputPageState>()
    var outputState: LiveData<AudioOutputPageState> = _outputViewState


    data class AudioOutputPageState(
        var songs: List<Song>? = null,
    )


    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            mCurrentSort = SortBusiness.getAllSongsSortStatus()
            launchOnUI {
                registerSy(context)
                AudioSyncService.sync(AppProvider.context)
                if (AudioSyncUtil.songs.isNotEmpty()) {
                    refresh(AudioOutputPageState(songs = getOutputSongs()))
                }
            }
        }
    }

    private fun registerSy(context: Context) {
        IntentFilter(AudioSyncService.ACTION_SYNC_COMPLETED).also { filter ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(syncCompletedReceiver, filter, RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(syncCompletedReceiver, filter)
            }
        }
    }

    // 注册广播接收器
    private val syncCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { // 收到广播后更新界面
            Log.i(AudioSyncService.TAG, "syncCompletedReceiver")
            refresh(AudioOutputPageState(songs = getOutputSongs()))
        }
    }


    fun getOutputSongs(): List<Song> {
        return AudioSyncUtil.songs.filter { TextUtils.equals(File(it.path).parent, AudioFileUtils.OUTPUT_FOLDER) }
    }


    private fun refresh(pageState: AudioOutputPageState) {
        _outputViewState.value = pageState
    }

    fun onRefresh(context: Context) {
        registerSy(context)
        AudioSyncService.sync(context)
    }

}