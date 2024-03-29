package dev.audio.timeruler.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.android.app.AppProvider
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.masoudss.lib.utils.Utils
import dev.audio.timeruler.multitrack.MultiTrackRenderersFactory
import dev.audio.timeruler.multitrack.MultiTrackSelector

object PlayerManager {

    const val TAG = "PlayerManager"

    var player: SimpleExoPlayer

    init {
        player = initExoPlayer(AppProvider.context)
    }

    private const val PROGRESS_UPDATE_INTERVAL = 10L


    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (player.isPlaying) { // 确保播放器正在播放时才更新进度
                val position = player.currentPosition
                val duration = player.duration
                progressListener?.onProgressChanged(position, duration)
                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
            }
        }
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
        // 设置播放器事件监听器
        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                eventListeners.forEach {
                    it.onPlayerStateChanged(playWhenReady, playbackState)
                }
                if (playbackState == Player.STATE_READY && playWhenReady) {
                    // 播放器准备好并且处于播放状态，开始进度更新
                    handler.post(progressRunnable)
                } else if (playbackState == Player.STATE_ENDED || !playWhenReady) {
                    // 播放结束或播放器不处于播放状态，移除进度更新
                    handler.removeCallbacks(progressRunnable)
                }
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                eventListeners.forEach {
                    it.onPlayerError(error)
                }
            }

            // 添加其他需要监听的事件
        })
        return player
    }

    fun releasePlayer() {
        player.stop(true)
        handler.removeCallbacks(progressRunnable)
        // 可能还有其他资源释放操作
    }

    var eventListeners: MutableList<Player.EventListener> = mutableListOf()

    fun addListener(listener: Player.EventListener) {
        eventListeners.add(listener)
    }

    private var progressListener: PlayerProgressCallback? = null
    fun addProgressListener(listener: PlayerProgressCallback) {
        progressListener = listener
    }

    fun playByMediaSource(mediaSource: MediaSource) {
        player.playWhenReady = true
        player.setMediaSource(mediaSource)
        player.prepare()
    }

    fun playByPath(path: String) {
        //path 转 uri
        var uri: Uri? =
            Utils.getAudioUriFromPath(AppProvider.context, path) ?: return
        var dataSourceFactory = DefaultDataSourceFactory(
            AppProvider.context,
            Util.getUserAgent(
                AppProvider.context,
                AppProvider.context.packageName
            )
        )
        // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(uri!!.apply {
            })
        )
        playByMediaSource(audioSource)
    }

    fun playByUri(uri: Uri) {
        var dataSourceFactory = DefaultDataSourceFactory(
            AppProvider.context,
            Util.getUserAgent(
                AppProvider.context,
                AppProvider.context.packageName
            )
        )
        // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            MediaItem.fromUri(uri)
        )
        playByMediaSource(audioSource)
    }

    fun pause() {
        player.pause()
    }

    fun play() {
        player.play()
    }

    var isPlaying: Boolean = false
        get() = player.isPlaying


}