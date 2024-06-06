package com.san.audioeditor.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.app.AppProvider
import com.san.audioeditor.R
import com.san.audioeditor.storage.AudioSyncService
import com.san.audioeditor.storage.AudioSyncUtil
import com.san.audioeditor.viewmodel.pagedata.AudioListPageState
import dev.android.player.app.business.SortBusiness
import dev.android.player.framework.data.model.Directory
import dev.android.player.framework.data.model.Song
import dev.audio.recorder.utils.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.stream.Collectors

class AudioPickViewModel : BaseAudioListViewModel() {

    companion object {

        const val ALL_AUDIO = "all/audio"
    }


    // 定义构造 ViewModel 方法
    class AudioPickViewFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioPickViewModel() as T
        }
    }

    init {
        AudioSyncUtil.songs.observeForever { songs ->
            if (songs.isNotEmpty()) {
                refresh(UiState(isSuccess = AudioListPageState(songs = songs.filter {
                    currentDir == null || ALL_AUDIO == currentDir?.path || File(it.path).parent.equals(currentDir?.path)
                })))
                getDirectoriesBySongs(true)
            }
        }
    }

    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            mCurrentSort = SortBusiness.getAllSongsSortStatus()
            launchOnUI {
                registerSy(context)
                AudioSyncService.sync(AppProvider.context)
                if (AudioSyncUtil.songs.value?.isNotEmpty() == true) {
                    refresh(UiState(isSuccess = AudioListPageState(songs = AudioSyncUtil.songs.value)))
                }
            }
            dirList = getDirectoriesBySongs()
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
            if (AudioSyncUtil.songs.value?.isNullOrEmpty() == true) {
                refresh(UiState(isSuccess = AudioListPageState(songs = emptyList())))
            }
        }
    }


    private var dirList: List<Directory>? = null
    var currentDir: Directory? = Directory().apply {
        name = AppProvider.context.getString(R.string.all_audio)
        path = ALL_AUDIO
    }

    fun getDirectoriesBySongs(force: Boolean = false): List<Directory>? {
        if (!force) {
            if (dirList != null && dirList!!.isNotEmpty()) {
                return dirList!!
            }
        }
        var songs = AudioSyncUtil.songs.value ?: return emptyList()
        val mapping = songs.stream().collect(Collectors.groupingBy { song: Song ->
            File(song.path).parent
        }).entries
        var result = mapping.stream().map { (path, value): Map.Entry<String, List<Song>> ->
            val file = File(path)
            val directory = Directory()
            directory.name = file.name
            directory.path = path
            directory.updateTime = value.maxByOrNull { it.dateAdded }?.dateAdded ?: 0
            directory.songCount = value.size
            directory
        }.filter { it: Directory -> it.songCount > 0 }.collect(Collectors.toList())
        if (result.isNotEmpty()) {
            result.add(0, Directory().apply {
                name = AppProvider.context.getString(R.string.all_audio)
                songCount = songs.size
                path = ALL_AUDIO
                updateTime = songs.maxByOrNull { it.dateAdded }?.dateAdded ?: 0
            })
        }
        if (currentDir == null) {
            currentDir = result[0]
        }
        dirList = result
        return dirList
    }

    fun onRefresh(context: Context) {
        registerSy(context)
        AudioSyncService.sync(context)
    }

    fun onFolderSelected(it: Directory) {
        currentDir = it
        refresh(UiState(isSuccess = AudioListPageState(songs = AudioSyncUtil.songs.value?.filter {
            File(it.path).parent.equals(currentDir?.path) || ALL_AUDIO == currentDir?.path
        })))
    }


}