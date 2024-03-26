package com.san.audioeditor.viewmodel

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.san.audioeditor.viewmodel.pagedata.AudioCutPageData
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioCutViewModel(var song: Song) : BaseViewModel<AudioCutPageData>() {

    companion object {

    }

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
    )


    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {
                refresh(
                    AudioCutViewModel(
                    )
                )

            }
        }
    }


    private fun refresh(pageState: AudioCutViewModel) {
        _audioCutViewState.value = pageState
    }

}