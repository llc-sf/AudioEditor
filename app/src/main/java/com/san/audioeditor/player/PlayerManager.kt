package com.san.audioeditor.player

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.masoudss.lib.utils.Utils
import com.san.audioeditor.AudioEditorApplication
import dev.audio.recorder.utils.Log
import dev.audio.timeruler.multitrack.MultiTrackRenderersFactory
import dev.audio.timeruler.multitrack.MultiTrackSelector

object PlayerManager {

    const val TAG = "PlayerManager"

    var player: SimpleExoPlayer

    init {
        player = initExoPlayer(AudioEditorApplication.getAppContext())
    }

    private fun initExoPlayer(mAppContext: Context): SimpleExoPlayer {
        val player = SimpleExoPlayer.Builder(
            mAppContext,
            MultiTrackRenderersFactory(
                1,
                mAppContext
            )
        ).setTrackSelector(MultiTrackSelector()).build()
        player.repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
        player.playWhenReady = true
        player.setPlaybackParameters(PlaybackParameters(1f))
        return player
    }

    fun playByMediaSource(mediaSource: MediaSource) {
        player.playWhenReady = true
        player.setMediaSource(mediaSource)
        player.prepare()
    }

    fun playByPath(path: String) {
        Log.i(TAG, "path=$path")
        //path 转 uri
        var uri: Uri? =
            Utils.getAudioUriFromPath(AudioEditorApplication.getAppContext(), path) ?: return
        var dataSourceFactory = DefaultDataSourceFactory(
            AudioEditorApplication.getAppContext(),
            Util.getUserAgent(
                AudioEditorApplication.getAppContext(),
                AudioEditorApplication.getAppContext().packageName
            )
        )
        // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(uri!!.apply {
                Log.i(TAG, "uri=$this")
            })
        )
        playByMediaSource(audioSource)
    }

    fun playByUri(uri: Uri) {
        var dataSourceFactory = DefaultDataSourceFactory(
            AudioEditorApplication.getAppContext(),
            Util.getUserAgent(
                AudioEditorApplication.getAppContext(),
                AudioEditorApplication.getAppContext().packageName
            )
        )
        // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(uri)
        )
        playByMediaSource(audioSource)
    }

}