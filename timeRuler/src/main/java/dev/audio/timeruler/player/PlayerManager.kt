package dev.audio.timeruler.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.app.AppProvider
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.masoudss.lib.utils.Utils
import dev.audio.timeruler.multitrack.MultiTrackRenderersFactory
import dev.audio.timeruler.multitrack.MultiTrackSelector
import dev.audio.timeruler.weight.AudioFragmentWithCut
import dev.audio.timeruler.weight.BaseAudioEditorView.Companion.playline_tag
import dev.audio.timeruler.weight.CutPieceFragment

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
                val currentWindowIndex = player.currentWindowIndex
                progressListener?.onProgressChanged(currentWindowIndex, position, duration)
                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
            }
        }
    }


    private fun initExoPlayer(mAppContext: Context): SimpleExoPlayer {
        val player = SimpleExoPlayer.Builder(mAppContext, MultiTrackRenderersFactory(1, mAppContext))
            .setTrackSelector(MultiTrackSelector()).build()
        player.repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
        player.playWhenReady = true
        player.setPlaybackParameters(PlaybackParameters(1f)) // 设置播放器事件监听器
        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                eventListeners.forEach {
                    it.onPlayerStateChanged(playWhenReady, playbackState)
                }
                if (playbackState == Player.STATE_READY && playWhenReady) { // 播放器准备好并且处于播放状态，开始进度更新
                    handler.post(progressRunnable)
                } else if (playbackState == Player.STATE_ENDED || !playWhenReady) { // 播放结束或播放器不处于播放状态，移除进度更新
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
        handler.removeCallbacks(progressRunnable) // 可能还有其他资源释放操作
    }

    var eventListeners: MutableList<Player.EventListener> = mutableListOf()

    fun addListener(listener: Player.EventListener) {
        eventListeners.add(listener)
    }

    private var progressListener: PlayerProgressCallback? = null
    fun addProgressListener(listener: PlayerProgressCallback) {
        progressListener = listener
    }

    fun playByMediaSource(mediaSource: MediaSource, autoPlay: Boolean = false) {
        player.playWhenReady = autoPlay
        player.setMediaSource(mediaSource)
        player.prepare()
    }

    var uri: Uri? = null
    fun playByPath(path: String, autoPlay: Boolean = false) { //path 转 uri
        uri = Utils.getAudioUriFromPath(AppProvider.context, path) ?: return
        var dataSourceFactory = DefaultDataSourceFactory(AppProvider.context, Util.getUserAgent(AppProvider.context, AppProvider.context.packageName)) // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri!!.apply {}))
        playByMediaSource(audioSource, autoPlay)
    }

    fun playByUri(uri: Uri, autoPlay: Boolean = false) {
        var dataSourceFactory = DefaultDataSourceFactory(AppProvider.context, Util.getUserAgent(AppProvider.context, AppProvider.context.packageName)) // 创建多个 MediaSource，分别对应不同的音频文件
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
        playByMediaSource(audioSource, autoPlay)
    }

    fun pause() {
        player.pause()
    }

    fun play() {
        player.play()
    }

    fun playWithSeek(position: Long, index: Int = 0) {
        player.seekTo(index, position)
        player.play()
    }

    fun seekTo(position: Long, index: Int = 0) {
        player.seekTo(index, position)
    }

    fun playCutPiece(start: Long, end: Long) {
        if (uri == null) {
            return
        }
        var dataSourceFactory = DefaultDataSourceFactory(AppProvider.context, Util.getUserAgent(AppProvider.context, AppProvider.context.packageName))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri!!))
        player.playWhenReady = true
        player.setMediaSource(ClippingMediaSource(audioSource, start * 1000, end * 1000))
        player.play()
    }

    fun updateMediaSource(start: Long, end: Long) {
        if (uri == null) {
            return
        }
        var dataSourceFactory = DefaultDataSourceFactory(AppProvider.context, Util.getUserAgent(AppProvider.context, AppProvider.context.packageName))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri!!))
        player.setMediaSource(ClippingMediaSource(audioSource, start * 1000, end * 1000))
    }

    fun updateMediaSourceDelete(start: Long, end: Long, duration: Long) {
        Log.i(playline_tag, "updateMediaSourceDelete: start=$start end=$end duration=$duration")
        if (uri == null) {
            return
        }
        var dataSourceFactory = DefaultDataSourceFactory(AppProvider.context, Util.getUserAgent(AppProvider.context, AppProvider.context.packageName))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri!!))
        var s1 = ClippingMediaSource(audioSource, 0, start * 1000)
        var s2 = ClippingMediaSource(audioSource, end * 1000, duration * 1000)
        var s = ConcatenatingMediaSource(s1, s2)
        player.setMediaSource(s)
        player.prepare()
        Log.i(playline_tag, "updateMediaSourceDelete duration: ${player.duration}")
    }

    fun updateMediaSourceDeleteJump(cutPieceFragments: List<CutPieceFragment>? = null) {
        if (cutPieceFragments.isNullOrEmpty()) {
            return
        }
        if (uri == null) {
            return
        }
        var dataSourceFactory = DefaultDataSourceFactory(AppProvider.context, Util.getUserAgent(AppProvider.context, AppProvider.context.packageName))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri!!))
        var s = ConcatenatingMediaSource()
        cutPieceFragments.sortedBy { it.startTimestampTimeInSelf }.forEach {
            s.addMediaSource(ClippingMediaSource(audioSource, it.startTimestampTimeInSelf * 1000, it.endTimestampTimeInSelf * 1000))
        }
        player.setMediaSource(s)
        player.prepare()
    }

    fun updateMediaSourceDeleteJumpOut(cutPieceFragments: MutableList<CutPieceFragment>? = null,
                                       start: Long,
                                       duration: Long,
                                       audioFragmentWithCut: AudioFragmentWithCut): Int {
        var resultIndex = -1
        if (cutPieceFragments.isNullOrEmpty()) {
            return resultIndex
        }
        if (uri == null) {
            return resultIndex
        } //多虑掉CutPieceFragment中 startTimestampTimeInSelf 小于 start 的
        val cutPieceFragmentsFilter = cutPieceFragments.filter { it.startTimestampTimeInSelf >= start }
        var end = duration
        if (cutPieceFragmentsFilter.isEmpty()) {
            CutPieceFragment(audioFragmentWithCut, false, 0, CutPieceFragment.CUT_MODE_JUMP, isFake = true).apply {
                startTimestampTimeInSelf = start
                endTimestampTimeInSelf = end
                cutPieceFragments.add(this)
            }
            resultIndex = cutPieceFragments.size - 1
        } else {
            cutPieceFragmentsFilter.forEachIndexed { index, cutPieceFragment ->
                if (cutPieceFragment.startTimestampTimeInSelf > start) {
                    end = cutPieceFragment.startTimestampTimeInSelf
                    resultIndex = index
                    return@forEachIndexed
                }
            }
            CutPieceFragment(audioFragmentWithCut, false, 0, CutPieceFragment.CUT_MODE_JUMP, isFake = true).apply {
                startTimestampTimeInSelf = start
                endTimestampTimeInSelf = end
                cutPieceFragments.add(resultIndex, this)
            }
        }


        val dataSourceFactory = DefaultDataSourceFactory(AppProvider.context, Util.getUserAgent(AppProvider.context, AppProvider.context.packageName))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri!!))
        val s = ConcatenatingMediaSource()
        cutPieceFragments.forEach {
            Log.i("llc_fuck", "start:${it.startTimestampTimeInSelf * 1000},end=${it.endTimestampTimeInSelf * 1000}")
            s.addMediaSource(ClippingMediaSource(audioSource, it.startTimestampTimeInSelf * 1000, it.endTimestampTimeInSelf * 1000))
        }
        player.setMediaSource(s)
        player.prepare()
        return resultIndex
    }

    fun updateMediaSource(mediaSource: MediaSource) {
        player.setMediaSource(mediaSource)
    }

    fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    var isPlaying: Boolean = false
        get() = player.isPlaying

}