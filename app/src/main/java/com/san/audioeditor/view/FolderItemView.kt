package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.san.audioeditor.databinding.ItemFolderViewBinding
import dev.android.player.framework.data.model.Directory

/**
 * AudioItem
 */
class FolderItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {

    private val mBinding = ItemFolderViewBinding.inflate(LayoutInflater.from(getContext()), this, true)

    init {

    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }


    fun setData(directory: Directory) {
        mBinding.title.text = directory.name
        mBinding.audioCount.text = directory.songCount.toString()
    }


}