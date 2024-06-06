package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback

abstract class BaseProgressView<T : ViewBinding> @JvmOverloads constructor(context: Context,
                                                                           attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs), PlayerProgressCallback {

    protected lateinit var mBinding: T

    init {
        clipChildren = false
        clipToPadding = false
    }

    protected fun setupSeekBar(seekBar: SeekBar) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    PlayerManager.seekTo((progress * PlayerManager.player.duration / 100).toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun addProgressListener() {
        PlayerManager.addProgressListener(this)
    }

    override fun onProgressChanged(currentWindowIndex: Int, position: Long, duration: Long) {
        getProgressBar().progress = (position * 100 / duration.toFloat()).toInt()
        getCurrentTimeTextView().text = DateUtil.formatTime(position)
        getDurationTimeTextView().text = DateUtil.formatTime(duration)
    }

    fun setData(song: Song) {
        val position = PlayerManager.getCurrentPosition()
        getCurrentTimeTextView().text = DateUtil.formatTime(position)
        getDurationTimeTextView().text = DateUtil.formatTime(song.duration.toLong())
        getProgressBar().progress = (position * 100 / song.duration.toFloat()).toInt()
    }

    abstract fun getProgressBar(): SeekBar
    abstract fun getCurrentTimeTextView(): TextView
    abstract fun getDurationTimeTextView(): TextView
}