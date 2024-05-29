package com.san.audioeditor.cell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.view.SettingsCommonItemView
import dev.android.player.framework.data.model.SettingsCommonItemData
import dev.android.player.widget.cell.ItemViewBinder

class CellSettingsItemView :
    ItemViewBinder<SettingsCommonItemData, CellSettingsItemView.CellSettingsItemViewHolder>() {
    var itemClick: ((view: View, data: SettingsCommonItemData?) -> Unit)? = null

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): CellSettingsItemViewHolder {
        return CellSettingsItemViewHolder(SettingsCommonItemView(parent.context).apply {
            itemClick = this@CellSettingsItemView.itemClick
        })
    }

    override fun onBindViewHolder(holder: CellSettingsItemViewHolder, item: SettingsCommonItemData) {
        (holder.itemView as? SettingsCommonItemView)?.setData(item)
    }

    class CellSettingsItemViewHolder(itemView: SettingsCommonItemView) :
        RecyclerView.ViewHolder(itemView)
}