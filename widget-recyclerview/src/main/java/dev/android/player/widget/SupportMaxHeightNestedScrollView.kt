package dev.android.player.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class SupportMaxHeightNestedScrollView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : NestedScrollView(context, attrs) {

    private var mMaxHeight: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SupportMaxHeightNestedScrollView)
        mMaxHeight = a.getDimensionPixelSize(R.styleable.SupportMaxHeightNestedScrollView_SMN_MaxHeight, 0)
        a.recycle()
    }


    fun setMaxHeight(maxHeight: Int) {
        mMaxHeight = maxHeight
        requestLayout()
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