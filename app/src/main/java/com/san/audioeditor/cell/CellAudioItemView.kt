package com.san.audioeditor.cell

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.view.AudioItemView
import dev.android.player.framework.data.model.Song
import dev.android.player.widget.cell.ItemViewBinder

class CellAudioItemView(
    private var itemClickNotify: (Int) -> Unit,
    private var playingPosition: () -> Int,
    private var selectCallBack: (Song,Boolean) -> Unit = { _, _ -> },
    private var source: AudioItemView.Source = AudioItemView.Source.SOURCE_OUTPUT,
) : ItemViewBinder<Song, CellAudioItemView.CellMediaItemViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater,
                                    parent: ViewGroup): CellMediaItemViewHolder {
        return CellMediaItemViewHolder(AudioItemView(source = source, parent.context))
    }

    override fun onBindViewHolder(holder: CellMediaItemViewHolder, item: Song) {
        (holder.itemView as? AudioItemView)?.setData(item, holder.adapterPosition, itemClickNotify, playingPosition,selectCallBack)
    }

    class CellMediaItemViewHolder(itemView: AudioItemView?) : RecyclerView.ViewHolder(itemView!!)
}