package com.san.audioeditor.cell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.view.SettingsCategoryItemView
import dev.android.player.framework.data.model.SettingsCategoryItemData
import dev.android.player.widget.cell.ItemViewBinder

class CellSettingsCategoryItemView :
    ItemViewBinder<SettingsCategoryItemData, CellSettingsCategoryItemView.CellSettingsItemViewHolder>() {
    var itemClick: ((view: View, data: SettingsCategoryItemData?) -> Unit)? = null

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): CellSettingsItemViewHolder {
        return CellSettingsItemViewHolder(SettingsCategoryItemView(parent.context).apply {
            itemClick = this@CellSettingsCategoryItemView.itemClick
        })
    }

    override fun onBindViewHolder(holder: CellSettingsItemViewHolder, item: SettingsCategoryItemData) {
        (holder.itemView as? SettingsCategoryItemView)?.setData(item)
    }

    class CellSettingsItemViewHolder(itemView: SettingsCategoryItemView) :
        RecyclerView.ViewHolder(itemView)
}