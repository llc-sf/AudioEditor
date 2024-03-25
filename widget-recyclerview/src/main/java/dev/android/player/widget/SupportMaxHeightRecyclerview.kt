package dev.android.player.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class SupportMaxHeightRecyclerview @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {


    private var mMaxHeight: Int = 0


    fun setMaxHeight(maxHeight: Int) {
        this.mMaxHeight = maxHeight
        requestLayout()
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SupportMaxHeightRecyclerview)
        mMaxHeight = a.getDimensionPixelSize(R.styleable.SupportMaxHeightRecyclerview_SMR_MaxHeight, -1)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec = if (mMaxHeight > 0) {
            MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

}