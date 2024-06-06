package com.san.audioeditor.cell

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.view.OutputEmptyView
import dev.android.player.widget.cell.ItemViewBinder

class CellOutputEmptyView() :
    ItemViewBinder<CellOutputEmptyView.OutputEmptyBean, CellOutputEmptyView.CellOutputEmptyViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater,
                                    parent: ViewGroup): CellOutputEmptyViewHolder {
        return CellOutputEmptyViewHolder(OutputEmptyView(parent.context))
    }

    override fun onBindViewHolder(holder: CellOutputEmptyViewHolder,
                                  item: CellOutputEmptyView.OutputEmptyBean) {

    }


    data class OutputEmptyBean(var id: String = "")

    class CellOutputEmptyViewHolder(itemView: OutputEmptyView?) :
        RecyclerView.ViewHolder(itemView!!)
}