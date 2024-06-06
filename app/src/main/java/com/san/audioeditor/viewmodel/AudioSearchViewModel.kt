package com.san.audioeditor.viewmodel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.Player
import com.san.audioeditor.storage.AudioSyncUtil
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.StringsUtils
import dev.android.player.framework.utils.debounce
import dev.audio.timeruler.player.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioSearchViewModel : BaseViewModel<List<Song>>(), Player.EventListener {

    private val _keyword = MutableLiveData<String>()
    var playingSong: Song? = null
    var keyword: LiveData<String> = _keyword.debounce(300L, viewModelScope)
    private var key: String? = null
    private var directoryPath: String? = null

    init {
        AudioSyncUtil.songs.observeForever {
            if (!TextUtils.isEmpty(key)) {
                directoryPath?.let { path ->
                    refresh(UiState(isSuccess = searchFromSongs(key!!, AudioSyncUtil.getSongsByPath(it, path))))
                }
            }
        }
        PlayerManager.addEventListener(this)
    }

    fun sendKeyword(word: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _keyword.value = word
        }
    }

    fun searchFromTargetPath(key: String, directoryPath: String) {
        this.key = key
        this.directoryPath = directoryPath
        searchFromTargetSongs(key, AudioSyncUtil.getSongsByPath(AudioSyncUtil.songs.value, directoryPath))
    }

    fun searchFromTargetSongs(key: String, targetSongs: List<Song>) {
        if (key.isEmpty()) {
            refresh(UiState(isSuccess = targetSongs))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val songs = searchFromSongs(key, targetSongs)
            launchOnUI {
                refresh(UiState(isSuccess = songs))
            }
        }
    }

    private fun searchFromSongs(key: String, songs: List<Song>) =
        songs.filter { StringsUtils.getPattern(key).matcher(it.title).find() }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)

    }
}