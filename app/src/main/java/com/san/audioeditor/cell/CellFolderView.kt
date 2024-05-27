package com.san.audioeditor.cell

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.view.FolderItemView
import dev.android.player.framework.data.model.Directory
import dev.android.player.widget.cell.ItemViewBinder

class CellFolderView(private var currentDirectory: (() -> Directory?),private var onClick: (Directory) -> Unit) : ItemViewBinder<Directory, CellFolderView.CellFolderItemViewHolder>() {
    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): CellFolderItemViewHolder {
        return CellFolderItemViewHolder(FolderItemView(parent.context))
    }

    override fun onBindViewHolder(holder: CellFolderItemViewHolder, item: Directory) {
        (holder.itemView as? FolderItemView)?.setData(item,currentDirectory,onClick)
    }

    class CellFolderItemViewHolder(itemView: FolderItemView?) : RecyclerView.ViewHolder(itemView!!)
}