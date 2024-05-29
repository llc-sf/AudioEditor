package com.san.audioeditor.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.san.audioeditor.R
import dev.android.player.framework.utils.sp

class MoreActionItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val mIcon: ImageView

    private val mTitle: TextView

    init {
        inflate(context, R.layout.item_more_action_view, this)
        mIcon = findViewById(R.id.icon)
        mTitle = findViewById(R.id.title)

        val array = context.obtainStyledAttributes(attrs, R.styleable.MoreActionItem, 0, 0)
        mIcon.setImageDrawable(array.getDrawable(R.styleable.MoreActionItem_icon))
        mTitle.text = array.getString(R.styleable.MoreActionItem_title)
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, array.getDimension(R.styleable.MoreActionItem_More_TitleTextSize, 16f.sp))

        array.recycle()
    }

    fun setIcon(icon: Int) {
        mIcon.setImageResource(icon)
    }

    fun setIcon(icon: Drawable) {
        mIcon.setImageDrawable(icon)
    }


    fun setTitle(title: CharSequence) {
        mTitle.text = title
    }

    fun setTitle(title: Int) {
        mTitle.setText(title)
    }
}