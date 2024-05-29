package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.san.audioeditor.databinding.ViewBottomProgressBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback

/**
 * AudioItem
 */
class BottomProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs), PlayerProgressCallback {

    private val mBinding = ViewBottomProgressBinding.inflate(LayoutInflater.from(getContext()), this, true)


    init {

        mBinding.progress.setOnSeekBarChangeListener(object :
                                                         android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?,
                                           progress: Int,
                                           fromUser: Boolean) {
                if (fromUser) {
                    PlayerManager.seekTo((progress * PlayerManager.player.duration / 100).toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {

            }
        })
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        PlayerManager.removeProgressListener()
    }

    fun addProgressListener() {
        PlayerManager.addProgressListener(this)
    }

    fun setData(song: Song) {
        mBinding.currentTime.text = DateUtil.formatTime(0)
        mBinding.durationTime.text = DateUtil.formatTime(song.duration.toLong())
    }


    override fun onProgressChanged(currentWindowIndex: Int, position: Long, duration: Long) {
        mBinding.progress.progress = (position * 100 / duration.toFloat()).toInt()
        mBinding.currentTime.text = DateUtil.formatTime(position)
        mBinding.durationTime.text = DateUtil.formatTime(duration)
    }


}