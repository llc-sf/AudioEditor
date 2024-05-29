package com.san.audioeditor.cell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.view.SettingsVersionItemView
import dev.android.player.framework.data.model.SettingsVersionItemData
import dev.android.player.widget.cell.ItemViewBinder

class CellSettingsVersionItemView :
    ItemViewBinder<SettingsVersionItemData, CellSettingsVersionItemView.CellSettingsItemViewHolder>() {
    var itemClick: ((view: View, data: SettingsVersionItemData?) -> Unit)? = null
    override fun onCreateViewHolder(
        inflater: LayoutInflater, parent: ViewGroup
    ): CellSettingsItemViewHolder {
        return CellSettingsItemViewHolder(SettingsVersionItemView(parent.context).apply {
            itemClick = this@CellSettingsVersionItemView.itemClick
        })
    }

    override fun onBindViewHolder(
        holder: CellSettingsItemViewHolder,
        item: SettingsVersionItemData
    ) {
        (holder.itemView as? SettingsVersionItemView)?.setData(item)
    }

    class CellSettingsItemViewHolder(itemView: SettingsVersionItemView) :
        RecyclerView.ViewHolder(itemView)
}