package com.san.audioeditor.viewmodel

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.Player
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.viewmodel.pagedata.AudioListPageState
import com.san.audioeditor.viewmodel.pagedata.NotifyItemChangedState
import dev.android.player.app.business.data.SortStatus
import dev.android.player.framework.base.viewmodel.BaseViewModel
import dev.android.player.framework.data.model.Song
import dev.audio.timeruler.player.PlayerManager

abstract class BaseAudioListViewModel : BaseViewModel<AudioListPageState>(), Player.EventListener {

    lateinit var mCurrentSort: SortStatus

    var playingSong: Song? = null
    var playingPosition: Int? = null


    init {
        PlayerManager.addEventListener(this)
    }


    fun onResume() {
        playingSong?.let {
            PlayerManager.playByPathWithProgress(it.path, false)
            PlayerManager.pause()
        }
    }

    //是否播放过
    var isExpend: Boolean = false

    fun onCoverClick(position: Int, song: Song) {
        playingPosition = position
        isExpend = true
        if (PlayerManager.isPlaying && TextUtils.equals(playingSong?.path, song.path)) {
            PlayerManager.pause()
            refresh(UiState(isSuccess = AudioListPageState(notifyItemChangedState = NotifyItemChangedState(position))))
        } else {
            PlayerManager.playByPathWithProgress(song.path, true)
            playingSong = song
            refresh(UiState(isSuccess = AudioListPageState(notifyItemChangedState = NotifyItemChangedState(-1))))
        }
    }

    fun onItemClick(context: Context, position: Int, song: Song) {
        PlayerManager.pause()
        refresh(UiState(isSuccess = (AudioListPageState(notifyItemChangedState = NotifyItemChangedState(position)))))
        AudioCutActivity.open(context, song)
    }

    fun onDestroy() {
        PlayerManager.stop()
        PlayerManager.removeProgressListener()
        PlayerManager.removeListener(this)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        playingPosition?.let { refresh(UiState(isSuccess = AudioListPageState(notifyItemChangedState = NotifyItemChangedState(it)))) }
    }


}