package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.san.audioeditor.cell.CellFolderView
import dev.android.player.framework.data.model.Directory
import dev.android.player.framework.utils.dp
import dev.android.player.widget.cell.MultiTypeAdapter


class FolderSelectedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val mAdapter by lazy {
        MultiTypeAdapter()
    }


    private var mRecyclerView: RecyclerView
    private var currentDirectory: Directory? = null


    init {
        setPadding(8.dp, 8.dp, 8.dp, 8.dp)
        mRecyclerView = RecyclerView(context)
        mRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        addView(mRecyclerView, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        mAdapter.register(CellFolderView({
                                             currentDirectory
                                         }, {
                                             this.onClick?.invoke(it)
                                         }))
    }


    private var onClick: (Directory) -> Unit = {}
    fun setData(directoryList: List<Directory>,
                currentDirectory: Directory?,
                onClick: (Directory) -> Unit) {
        mAdapter.items = mutableListOf<Directory>().apply {
            addAll(directoryList)
        }
        this.currentDirectory = currentDirectory
        this.onClick = onClick
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val maxHeightPx = 360.dp
        if (measuredHeight > maxHeightPx) {
            setMeasuredDimension(measuredWidth, maxHeightPx)
            mRecyclerView.updateLayoutParams<LinearLayout.LayoutParams> {
                height = maxHeightPx
            }
        }
    }


}