package com.san.audioeditor.viewmodel

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.san.audioeditor.viewmodel.pagedata.AudioPickPageData
import com.san.audioeditor.storage.AudioSyncUtil
import com.san.audioeditor.viewmodel.pagedata.AudioSavePageData
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioSaveViewModel : BaseViewModel<AudioSavePageData>() {

    companion object {

    }

    // 定义构造 ViewModel 方法
    class AudioSaveFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioSaveViewModel() as T
        }
    }

    private val _audioSaveState = MutableLiveData<AudioSavePageData>()
    var audioSaveState: LiveData<AudioSavePageData> = _audioSaveState


    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {

            }
        }
    }


    private fun refresh(pageState: AudioSavePageData) {
        _audioSaveState.value = pageState
    }

}