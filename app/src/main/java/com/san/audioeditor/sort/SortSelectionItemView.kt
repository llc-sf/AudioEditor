package com.san.audioeditor.sort

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.san.audioeditor.R

/**
 * 排序选项Item
 */
class SortSelectionItemView @JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs) {
    init {
        View.inflate(context, R.layout.view_sort_select_item, this)
        setOnClickListener { isSelected = true }
    }

    /**
     * 选中状态监听
     */
    private var onItemSelected: (() -> Unit)? = null

    fun setTitle(sequence: CharSequence?) {
        findViewById<TextView>(R.id.item_name).setText(sequence)
    }

    fun setSelectedListener(action: (() -> Unit)?) {
        this.onItemSelected = action
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            findViewById<ImageView>(R.id.item_icon).setImageResource(R.drawable.ic_round_checked)
            findViewById<TextView>(R.id.item_name).setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            onItemSelected?.invoke()
        } else {
            findViewById<ImageView>(R.id.item_icon).setImageResource(R.drawable.ic_round_check)
            findViewById<TextView>(R.id.item_name).setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }
}