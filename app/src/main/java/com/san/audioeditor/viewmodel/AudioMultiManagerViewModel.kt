package com.san.audioeditor.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.app.AppProvider
import com.san.audioeditor.business.ShareBusiness
import com.san.audioeditor.delete.DeleteAction
import com.san.audioeditor.delete.DeleteSongPresenterCompat
import com.san.audioeditor.storage.AudioSyncService
import com.san.audioeditor.storage.AudioSyncUtil
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.rx.ioMain
import dev.android.player.framework.utils.StringsUtils
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class AudioMultiManagerViewModel :
    BaseViewModel<AudioMultiManagerViewModel.SongMultiManagerPageState>() {

    // 定义构造 ViewModel 方法
    class AudioMultiManagerViewFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioMultiManagerViewModel() as T
        }
    }

    var keyword: String? = null
    private var currentSongs = mutableListOf<Song>()
    private val totalSongs = mutableListOf<Song>()
    private var _selectedSongs = MutableLiveData<List<Song>>()
    var selectedSongs: LiveData<List<Song>> = _selectedSongs


    data class SongMultiManagerPageState(var songs: List<Song>? = null,
                                         var isAllSelected: Boolean? = null,
                                         var isShowBottomActionView: Boolean? = null)

    init {
        AudioSyncUtil.songs.observeForever {
            if (isTotalSongsNoChanged(AudioSyncUtil.getOutputSongs(it))) return@observeForever
            initTotalSongs(AudioSyncUtil.getOutputSongs(it))
            refresh(SongMultiManagerPageState(searchFromSongs(keyword, totalSongs)))
        }
    }

    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {
                registerSy(context)
                AudioSyncService.sync(AppProvider.context)
                if (AudioSyncUtil.songs.value?.isNotEmpty() == true) {
                    if (isTotalSongsNoChanged(AudioSyncUtil.getOutputSongs(AudioSyncUtil.songs.value))) return@launchOnUI
                    initTotalSongs(AudioSyncUtil.getOutputSongs(AudioSyncUtil.songs.value))
                    refresh(SongMultiManagerPageState(searchFromSongs(keyword, totalSongs)))
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
            if (isTotalSongsNoChanged(AudioSyncUtil.getOutputSongs(AudioSyncUtil.songs.value))) return
            initTotalSongs(AudioSyncUtil.getOutputSongs(AudioSyncUtil.songs.value))
            refresh(SongMultiManagerPageState(searchFromSongs(keyword, totalSongs)))
        }
    }

    private fun refresh(pageState: SongMultiManagerPageState) {
        currentSongs = pageState.songs?.toMutableList() ?: emptyList<Song>().toMutableList()
        refresh(UiState(isSuccess = pageState.also { state -> // 刷新选中音频集合
            _selectedSongs.value = totalSongs.filter { it.isSelected } // 刷新按钮全选状态
            state.isAllSelected = currentSongs.filter { it.isSelected }.size == currentSongs.size // 刷新底部选择框状态
            state.isShowBottomActionView = selectedSongs.value?.isNotEmpty()
        }))
    }

    fun onRefresh(context: Context) {
        registerSy(context)
        AudioSyncService.sync(context)
    }

    fun onAllSelectRefresh() {
        refresh(SongMultiManagerPageState(searchFromSongs(keyword, currentSongs)))
    }

    fun selectAll(isAllSelected: Boolean) {
        currentSongs.forEach {
            it.isSelected = isAllSelected
        }
        refresh(SongMultiManagerPageState(searchFromSongs(keyword, currentSongs)))
    }

    fun shareSongs(requireContext: Context) { //List<String>
        val shareList = totalSongs.stream().filter { it.isSelected }.map { it.path }
            .collect(Collectors.toList())
        ShareBusiness.shareMultipleTracks(requireContext, shareList)
    }

    fun deleteSongs(activity: AppCompatActivity) {
        Flowable.fromCallable {
            DeleteSongPresenterCompat.onDelete(DeleteAction(activity, totalSongs.filter { it.isSelected }))
        }.flatMap { it ->
            DeleteSongPresenterCompat.getDeleteActionSuccess()
        }.ioMain().subscribe {
            AudioSyncService.sync(AppProvider.context)
        }
    }

    fun search(key: String) {
        this.keyword = key
        searchFromTargetSongs(key, totalSongs)
    }

    private fun searchFromTargetSongs(key: String, targetSongs: List<Song>) {
        if (key.isEmpty()) {
            refresh(SongMultiManagerPageState(targetSongs))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val songs = searchFromSongs(key, targetSongs)
            launchOnUI {
                refresh(SongMultiManagerPageState(songs))
            }
        }
    }

    private fun searchFromSongs(key: String?, songs: List<Song>) = if (key.isNullOrEmpty()) songs
    else songs.filter { StringsUtils.getPattern(key).matcher(it.title).find() }

    private fun isTotalSongsNoChanged(songs: Collection<Song>): Boolean {
        if (songs.size != totalSongs.size) {
            return false
        } else {
            songs.forEach {
                if (!totalSongs.contains(it)) {
                    return false
                }
            }
            return true
        }
    }

    private fun initTotalSongs(songs: Collection<Song>) {
        val selectedSongs = songs.filter { it.isSelected }
        totalSongs.clear()
        songs.forEach {
            totalSongs.add(it.newInstance()!!.apply {
                if (selectedSongs.contains(this)) {
                    this.isSelected = true
                }
            })
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentSongs.clear()
        totalSongs.clear()
        keyword = null
    }

}