package com.san.audioeditor.viewmodel

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.san.audioeditor.viewmodel.pagedata.MediaPickPageData
import com.san.audioeditor.storage.AudioSyncUtil
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioPickViewModel : BaseViewModel<MediaPickPageData>() {

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

    var code: String? = null


    data class MediaPickPageState(
        var songs: List<Song>? = null,
    )


    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {
                refresh(
                    MediaPickPageState(
                        songs = AudioSyncUtil.songs
                    )
                )

            }
        }
    }


    private fun refresh(pageState: MediaPickPageState) {
        _mediaViewState.value = pageState
    }

}