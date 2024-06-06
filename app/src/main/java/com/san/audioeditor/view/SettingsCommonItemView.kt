package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.san.audioeditor.databinding.ItemSettingsViewBinding
import dev.android.player.framework.data.model.SettingsCommonItemData

class SettingsCommonItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ItemSettingsViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var itemData: SettingsCommonItemData? = null
    var itemClick: ((view: View, data: SettingsCommonItemData?) -> Unit)? = null

    init {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun setData(data: SettingsCommonItemData) {
        itemData = data
        binding.run {
            tvTitle.text = data.title
            tvSummary.isVisible = data.desc.isNotEmpty()
            tvSummary.text = data.desc
            ivIcon.setImageResource(data.icon)
            if (itemData?.clickable == true) {
                root.setOnClickListener {
                    itemClick?.invoke(this@SettingsCommonItemView, itemData)
                }
            }
        }
    }
}