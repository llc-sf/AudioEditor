package com.san.audioeditor.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.app.AppProvider
import com.san.audioeditor.R
import com.san.audioeditor.business.ShareBusiness
import com.san.audioeditor.storage.AudioSyncService
import com.san.audioeditor.storage.AudioSyncUtil
import com.san.audioeditor.viewmodel.pagedata.AudioPickPageData
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Directory
import dev.android.player.framework.data.model.Song
import dev.audio.recorder.utils.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.stream.Collectors

class AudioMutiManagerViewModel : BaseViewModel<AudioPickPageData>() {

    companion object {

    }


    // 定义构造 ViewModel 方法
    class AudioMutiManagerViewFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioMutiManagerViewModel() as T
        }
    }

    private val _audioMutiViewState = MutableLiveData<SongMutiManagerPageState>()
    var audioMutiManagerState: LiveData<SongMutiManagerPageState> = _audioMutiViewState


    data class SongMutiManagerPageState(var songs: List<Song>? = null,
                                        var isAllSelected: Boolean? = null,
                                        var isShowBottomActionView: Boolean? = null)


    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {
                registerSy(context)
                AudioSyncService.sync(AppProvider.context)
                refresh(SongMutiManagerPageState(songs = AudioSyncUtil.songs))
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
            refresh(SongMutiManagerPageState(songs = AudioSyncUtil.songs))
        }
    }


    private fun refresh(pageState: SongMutiManagerPageState) {
        if (pageState.songs != null) {
            currentSongs = pageState.songs!!.toMutableList()
        }
        _audioMutiViewState.value = pageState
    }

    fun onRefresh(context: Context) {
        registerSy(context)
        AudioSyncService.sync(context)
    }

    fun selectSong(song: Song, selected: Boolean) {
        song.isSelected = selected

        var isAllSelected = AudioSyncUtil.songs.filter { it.isSelected }.size.apply {
            Log.i("isAllSelected", "isAllSelected: $this")
        } == AudioSyncUtil.songs.size
        var isShowBottomActionView = AudioSyncUtil.songs.any { it.isSelected }
        refresh(SongMutiManagerPageState(isAllSelected = isAllSelected, isShowBottomActionView = isShowBottomActionView))
    }

    private var currentSongs = mutableListOf<Song>()

    fun selectAll(isAllSelected: Boolean) {
        currentSongs.forEach {
            it.isSelected = isAllSelected
        }
        refresh(SongMutiManagerPageState(isAllSelected = isAllSelected, isShowBottomActionView = isAllSelected))
    }

    fun shareSongs(requireContext: Context) { //List<String>
        val shareList = currentSongs.stream().filter { it.isSelected }.map { it.path }
            .collect(Collectors.toList())
        ShareBusiness.shareTrackList(requireContext, shareList)
    }

    fun deleteSongs(requireContext: Context) {

    }


}