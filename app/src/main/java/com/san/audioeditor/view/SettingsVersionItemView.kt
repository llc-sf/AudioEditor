package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.san.audioeditor.databinding.ItemSettingsVersionViewBinding
import dev.android.player.framework.data.model.SettingsVersionItemData

class SettingsVersionItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding =
        ItemSettingsVersionViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var itemData: SettingsVersionItemData? = null
    var itemClick: ((view: View, data: SettingsVersionItemData?) -> Unit)? = null

    init {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.root.setOnClickListener {
            itemClick?.invoke(this, itemData)
        }
    }

    fun setData(data: SettingsVersionItemData) {
        itemData = data
        binding.tvVersion.text = data.version
    }
}