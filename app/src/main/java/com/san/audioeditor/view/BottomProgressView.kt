package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.san.audioeditor.databinding.ViewBottomProgressBinding

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