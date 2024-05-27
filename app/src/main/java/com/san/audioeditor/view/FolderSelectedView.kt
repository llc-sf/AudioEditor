package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.R
import com.san.audioeditor.cell.CellFolderView
import dev.android.player.framework.data.model.Directory
import dev.android.player.framework.utils.dp
import dev.android.player.widget.cell.MultiTypeAdapter
import dev.android.player.widget.cell.MultiTypeFastScrollAdapter


class FolderSelectedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {


    private val mAdapter by lazy {
        MultiTypeAdapter()
    }


    private var mRecyclerView: RecyclerView


    init {

        setPadding(8.dp, 8.dp, 8.dp, 8.dp)
        mRecyclerView = RecyclerView(context)
        mRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        addView(mRecyclerView, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        mAdapter.register(CellFolderView())

        mAdapter.items= mutableListOf<Directory>().apply {
            add(Directory())
            add(Directory())
            add(Directory())
        }

    }





}