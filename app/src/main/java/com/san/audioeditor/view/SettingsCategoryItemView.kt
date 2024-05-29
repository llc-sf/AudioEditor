package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.san.audioeditor.databinding.ItemSettingsCategoryViewBinding
import com.san.audioeditor.databinding.ItemSettingsViewBinding
import dev.android.player.framework.data.model.SettingsCategoryItemData
import dev.android.player.framework.data.model.SettingsCommonItemData

class SettingsCategoryItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ItemSettingsCategoryViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var itemData: SettingsCategoryItemData? = null
    var itemClick: ((view: View, data: SettingsCategoryItemData?) -> Unit)? = null

    init {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.root.setOnClickListener {
            itemClick?.invoke(this, itemData)
        }
    }

    fun setData(data: SettingsCategoryItemData) {
        itemData = data
        binding.tvTitle.text = data.title
    }
}