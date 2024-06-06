package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.audioeditor.databinding.ViewBottomProgressBinding
import com.san.audioeditor.databinding.ViewVProgressBinding
import dev.android.player.framework.data.model.Song
import dev.android.player.framework.utils.DateUtil
import dev.audio.timeruler.player.PlayerManager
import dev.audio.timeruler.player.PlayerProgressCallback

/**
 * AudioItem
 */
class ProgressVView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseProgressView<ViewVProgressBinding>(context, attrs) {

    init {
        mBinding = ViewVProgressBinding.inflate(LayoutInflater.from(getContext()), this, true)
        setupSeekBar(mBinding.progress)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        PlayerManager.removeProgressListener()
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
