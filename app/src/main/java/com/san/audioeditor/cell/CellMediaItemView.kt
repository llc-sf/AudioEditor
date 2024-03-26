package com.san.audioeditor.cell

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.view.SongItemView
import dev.android.player.framework.data.model.Song
import dev.android.player.widget.cell.ItemViewBinder

class CellMediaItemView : ItemViewBinder<Song, CellMediaItemView.CellMediaItemViewHolder>() {
    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): CellMediaItemViewHolder {
        return CellMediaItemViewHolder(SongItemView(parent.context))
    }

    override fun onBindViewHolder(holder: CellMediaItemViewHolder, item: Song) {
        (holder.itemView as? SongItemView)?.setData(item)
    }

    class CellMediaItemViewHolder(itemView: SongItemView?) : RecyclerView.ViewHolder(itemView!!)
}