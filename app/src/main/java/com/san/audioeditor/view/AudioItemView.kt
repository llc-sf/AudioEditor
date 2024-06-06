package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.san.audioeditor.R
import com.san.audioeditor.databinding.ItemSongViewBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.android.player.framework.utils.FileUtils
import dev.android.player.framework.utils.bindHighLightColorRes
import dev.audio.recorder.utils.Log
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback
import dev.audio.timeruler.utils.AudioFileUtils
import musicplayer.playmusic.audioplayer.base.loader.load

/**
 * AudioItem
 */
class AudioItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs), PlayerProgressCallback {

    private val mBinding = ItemSongViewBinding.inflate(LayoutInflater.from(getContext()), this, true)

    private var mCurrentSong: Song? = null

    // 定义一个枚举类来表示跳转来源
    enum class Source {
        SOURCE_OUTPUT, SOURCE_SEARCH, SOURCE_MUTIMANAGER, SOURCE_PICK
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private var song: Song? = null


    @JvmOverloads
    fun setData(song: Song, adapterPosition: Int, onItemListener: OnItemListener?) {
        try {
            if (onItemListener == null) {
                return
            }
            this.song = song
            this.mCurrentSong = song
            when (onItemListener.getSource()) {
                Source.SOURCE_OUTPUT, Source.SOURCE_SEARCH, Source.SOURCE_PICK -> {
                    mBinding.more.setImageResource(R.drawable.ic_more)
                }

                Source.SOURCE_MUTIMANAGER -> {
                    mBinding.more.setImageResource(R.drawable.song_muti_selected_bg_selector)
                }
            }
            mBinding.title.text = song.title
            onItemListener.getKeyWord()?.let {
                if (it.isEmpty()) return@let //设置高亮显示的标题文字
                mBinding.title.bindHighLightColorRes(it, R.color.colorAccent)
            }
            mBinding.description.text = "${DateUtil.formatTime((song!!.duration).toLong())} | ${FileUtils.getFileSize(song!!.size)} | ${
                AudioFileUtils.getExtension(song!!.path).uppercase()
            }"
            mBinding.cover.isVisible = true
            mBinding.cover.load(song)
            mBinding.root.setOnClickListener {
                onItemListener.onItemClick(adapterPosition, song)
            }
            mBinding.progressContainer.setData(song)
            freshPlayStateView(mCurrentSong?.path == onItemListener.getPlayingSong()?.path, onItemListener.getSource())
            mBinding.more.setOnClickListener {
                onItemListener.onMoreClick(adapterPosition, song)
            }
            mBinding.more.isSelected = song.isSelected
            mBinding.cover.setOnClickListener {
                onItemListener.onCoverClick(adapterPosition, song)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun freshPlayStateView(isCurrentSongPlaying: Boolean, source: Source) {
        if (source == Source.SOURCE_MUTIMANAGER) {
            mBinding.pause.isVisible = false
            mBinding.mask.isVisible = false
            mBinding.progressContainer.isVisible = false
            return
        }
        if (isCurrentSongPlaying) {
            mBinding.progressContainer.isVisible = true
            mBinding.progressContainer.addProgressListener()
            mBinding.pause.setImageResource(if (PlayerManager.isPlaying) R.drawable.ic_item_pause else R.drawable.ic_item_play)
        } else {
            mBinding.progressContainer.isVisible = false
            mBinding.pause.setImageResource(R.drawable.ic_item_play)
        }
    }

    override fun onProgressChanged(currentWindowIndex: Int, position: Long, duration: Long) {
        mBinding.progressContainer.onProgressChanged(currentWindowIndex, position, duration)
    }


    interface OnItemListener {
        fun onItemClick(position: Int, song: Song)
        fun onCoverClick(position: Int, song: Song)
        fun onMoreClick(position: Int, song: Song)
        fun getPlayingSong(): Song?
        fun getKeyWord(): String?
        fun getSource(): Source
    }


}