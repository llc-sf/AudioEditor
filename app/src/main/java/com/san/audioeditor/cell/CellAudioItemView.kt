package com.san.audioeditor.cell

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.view.AudioItemView
import dev.android.player.framework.data.model.Song
import dev.android.player.widget.cell.ItemViewBinder

class CellAudioItemView(private var onItemListener: AudioItemView.OnItemListener?) :
    ItemViewBinder<Song, CellAudioItemView.CellMediaItemViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater,
                                    parent: ViewGroup): CellMediaItemViewHolder {
        return CellMediaItemViewHolder(AudioItemView(parent.context))
    }

    override fun onBindViewHolder(holder: CellMediaItemViewHolder, item: Song) {
        (holder.itemView as? AudioItemView)?.setData(item, holder.adapterPosition, onItemListener)
    }


    fun onDestroy() {
        onItemListener = null
    }

    class CellMediaItemViewHolder(itemView: AudioItemView?) : RecyclerView.ViewHolder(itemView!!)
}