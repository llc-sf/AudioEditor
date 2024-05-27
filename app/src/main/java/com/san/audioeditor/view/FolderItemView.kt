package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.san.audioeditor.databinding.ItemFolderViewBinding
import dev.android.player.framework.data.model.Directory
import dev.audio.timeruler.utils.toFormattedDateString

/**
 * AudioItem
 */
class FolderItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {

    private val mBinding = ItemFolderViewBinding.inflate(LayoutInflater.from(getContext()), this, true)

    private var onClick: (Directory) -> Unit = {}

    private var directory: Directory? = null

    init {

        setOnClickListener {
            directory?.let {
                onClick.invoke(it)
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }


    fun setData(directory: Directory,
                currentDirectory: (() -> Directory?),
                onClick: (Directory) -> Unit) {
        this.directory = directory
        mBinding.title.text = directory.name
        mBinding.audioCount.text = directory.songCount.toString()
        mBinding.updateTime.text = directory.updateTime.toFormattedDateString()
        mBinding.icSelected.isVisible = directory == currentDirectory.invoke()
        this.onClick = onClick
    }


}