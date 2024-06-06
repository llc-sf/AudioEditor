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
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.storage.AudioSyncService
import com.san.audioeditor.storage.AudioSyncUtil
import com.san.audioeditor.viewmodel.pagedata.AudioListPageState
import com.san.audioeditor.viewmodel.pagedata.NotifyItemChangedState
import dev.android.player.app.business.SortBusiness
import dev.android.player.app.business.data.SortStatus
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import dev.audio.recorder.utils.Log
import dev.audio.timeruler.player.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioOutputViewModel : BaseAudioListViewModel() {

    companion object {

    }


    // 定义构造 ViewModel 方法
    class AudioOutputViewFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioOutputViewModel() as T
        }
    }


    init {
        AudioSyncUtil.songs.observeForever {
            if (it.isNotEmpty()) {
                refresh(UiState(isSuccess = AudioListPageState(songs = AudioSyncUtil.getOutputSongs(it))))
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
                    refresh(UiState(isSuccess = AudioListPageState(songs = AudioSyncUtil.getOutputSongs(AudioSyncUtil.songs.value))))
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
            if (AudioSyncUtil.getOutputSongs(AudioSyncUtil.songs.value)?.isEmpty() == true) {
                refresh(UiState(isSuccess = AudioListPageState(songs = emptyList())))
                return
            }
        }
    }


    fun onRefresh(context: Context) {
        registerSy(context)
        AudioSyncService.sync(context)
    }


}