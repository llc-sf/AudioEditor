package com.san.audioeditor.viewmodel

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.dialog.RenameAudioDialog
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioSaveViewModel : BaseViewModel<AudioSaveViewModel.AudioSavePageData>(),
    RenameAudioDialog.OnRenameResultListener {

    companion object {

    }

    // 定义构造 ViewModel 方法
    class AudioSaveFactory() : ViewModelProvider.Factory {
        // 构造 数据访问接口实例
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioSaveViewModel() as T
        }
    }

    data class AudioSavePageData(var song: Song? = null, var renameResult: Boolean? = null)

    var song: Song? = null
    fun initData(context: Context, arguments: Bundle?) {
        viewModelScope.launch(Dispatchers.IO) {
            launchOnUI {
                song = arguments?.getSerializable(AudioCutActivity.PARAM_SONG) as Song
                song?.let {
                    refresh(UiState(isSuccess = AudioSavePageData().apply {
                        song = it
                    }))
                }
            }
        }
    }


    fun rename(supportFragmentManager: FragmentManager?) {
        song?.let {
            RenameAudioDialog.show(supportFragmentManager, it).apply {
                setOnRenameResultListener(this@AudioSaveViewModel)
            }

        }
    }

    override fun onResult(success: Boolean, resultSong: Song) {
        if (success) {
            song?.let {
                refresh(UiState(isSuccess = AudioSavePageData().apply {
                    renameResult = success
                    song = resultSong
                }))
            }
        }
    }

}