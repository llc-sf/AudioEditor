package com.san.audioeditor.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Context.RECEIVER_NOT_EXPORTED
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
import com.san.audioeditor.storage.AudioSyncService
import com.san.audioeditor.viewmodel.pagedata.AudioPickPageData
import com.san.audioeditor.storage.AudioSyncUtil
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import dev.audio.recorder.utils.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioPickViewModel : BaseViewModel<AudioPickPageData>() {

    companion object {

    }

    // 定义构造 ViewModel 方法
    class ThemeListViewFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioPickViewModel() as T
        }
    }

    private val _mediaViewState = MutableLiveData<MediaPickPageState>()
    var mediaPickState: LiveData<MediaPickPageState> = _mediaViewState


    data class MediaPickPageState(
        var songs: List<Song>? = null,
    )


    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {
//                if (AudioSyncUtil.songs.isEmpty()) {
//                    Log.i(AudioSyncService.TAG, "registerReceiver ACTION_SYNC_COMPLETED")
//                    registerSy(context)
//                } else {
//                    Log.i(AudioSyncService.TAG, "initData songs not empty")
//                    refresh(MediaPickPageState(songs = AudioSyncUtil.songs))
//                }
                registerSy(context)
                refresh(MediaPickPageState(songs = AudioSyncUtil.songs))
                AudioSyncService.sync(AppProvider.context)
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
            refresh(MediaPickPageState(songs = AudioSyncUtil.songs))
        }
    }


    private fun refresh(pageState: MediaPickPageState) {
        _mediaViewState.value = pageState
    }

    fun onRefresh(context: Context) {
        registerSy(context)
        AudioSyncService.sync(context)
    }

}