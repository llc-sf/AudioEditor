package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.san.audioeditor.R
import com.san.audioeditor.activity.AudioCutActivity
import com.san.audioeditor.databinding.ItemSongViewBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.android.player.framework.utils.FileUtils
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback
import dev.audio.timeruler.utils.AudioFileUtils
import musicplayer.playmusic.audioplayer.base.loader.load

/**
 * AudioItem
 */
class AudioItemView @JvmOverloads constructor(var source: Source = Source.SOURCE_OUTPUT,
                                              context: Context,
                                              attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs), PlayerProgressCallback {

    private val mBinding = ItemSongViewBinding.inflate(LayoutInflater.from(getContext()), this, true)

    private var mCurrentSong: Song? = null

    // 定义一个枚举类来表示跳转来源
    enum class Source {
        SOURCE_OUTPUT, SOURCE_SEARCH, SOURCE_MUTIMANAGER, SOURCE_PICK
    }


    init {
        when (source) {
            Source.SOURCE_OUTPUT -> {
                mBinding.more.setImageResource(R.drawable.ic_more)
            }

            Source.SOURCE_SEARCH -> {
                mBinding.more.setImageResource(R.drawable.ic_more)
            }

            Source.SOURCE_MUTIMANAGER -> {
                mBinding.more.setImageResource(R.drawable.song_muti_selected_bg_selector)
            }

            Source.SOURCE_PICK -> {
                mBinding.more.setImageResource(R.drawable.ic_more)

            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        PlayerManager.removeProgressListener()
    }

    private var song: Song? = null


    @JvmOverloads
    fun setData(song: Song,
                adapterPosition: Int,
                itemClickNotify: (Int) -> Unit,
                playingPosition: () -> Int,
                selectCallBack: (Song, Boolean) -> Unit = { _, _ -> }) {
        try {
            this.song = song
            this.mCurrentSong = song
            mBinding.title.text = song.title
            mBinding.description.text = "${DateUtil.formatTime((song!!.duration).toLong())} | ${FileUtils.getFileSize(song!!.size)} | ${
                AudioFileUtils.getExtension(song!!.path).uppercase()
            }"
            mBinding.cover.isVisible = true
            mBinding.cover.load(song)
            mBinding.root.setOnClickListener {
                when (source) {
                    Source.SOURCE_OUTPUT -> {
                    }

                    Source.SOURCE_SEARCH -> {
                    }

                    Source.SOURCE_MUTIMANAGER -> {
                        onMoreClickDeal(selectCallBack, song)
                    }

                    Source.SOURCE_PICK -> {
                        AudioCutActivity.open(context, song)
                    }
                }
            }
            mBinding.progressContainer.setData(song)
            freshPlayStateView(adapterPosition == playingPosition())
            mBinding.more.setOnClickListener {
                when (source) {
                    Source.SOURCE_OUTPUT -> {
                        SongMoreBottomDialog.show(context, mCurrentSong)
                    }

                    Source.SOURCE_SEARCH -> {
                        SongMoreBottomDialog.show(context, mCurrentSong)
                    }

                    Source.SOURCE_MUTIMANAGER -> {
                        onMoreClickDeal(selectCallBack, song)
                    }

                    Source.SOURCE_PICK -> {
                        SongMoreBottomDialog.show(context, mCurrentSong)
                    }
                }
            }
            mBinding.more.isSelected = song.isSelected

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onMoreClickDeal(selectCallBack: (Song, Boolean) -> Unit, song: Song) {
        mBinding.more.isSelected = !mBinding.more.isSelected
        selectCallBack(song, mBinding.more.isSelected)
    }

    private fun freshPlayStateView(isPlaying: Boolean) {
        if (isPlaying) {
            mBinding.progressContainer.isVisible = true
            mBinding.progressContainer.addProgressListener()
        } else {
            mBinding.progressContainer.isVisible = false
        }
    }

    override fun onProgressChanged(currentWindowIndex: Int, position: Long, duration: Long) {
        mBinding.progressContainer.onProgressChanged(currentWindowIndex, position, duration)
    }


}