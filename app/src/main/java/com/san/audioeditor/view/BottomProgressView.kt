package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.audioeditor.databinding.ViewBottomProgressBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.audio.recorder.utils.Log
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback

/**
 * AudioItem
 */
class BottomProgressView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseProgressView<ViewBottomProgressBinding>(context, attrs) {

    init {
        mBinding = ViewBottomProgressBinding.inflate(LayoutInflater.from(getContext()), this, true)
        setupSeekBar(mBinding.progress)
    }

    override fun getProgressBar(): android.widget.SeekBar {
        return mBinding.progress
    }

    override fun getCurrentTimeTextView(): TextView {
        return mBinding.currentTime
    }

    override fun getDurationTimeTextView(): TextView {
        return mBinding.durationTime
    }
}