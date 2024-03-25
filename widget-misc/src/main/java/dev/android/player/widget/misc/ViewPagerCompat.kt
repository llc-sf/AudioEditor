package dev.android.player.widget.misc

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


/**
 * 解决ViewPager由于多点触控导致崩溃的问题
 */
class ViewPagerCompat @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {


    private var isCanScroll = true

    /**
     * 设置是否可以滑动
     */
    fun setCanScroll(canScroll: Boolean) {
        this.isCanScroll = canScroll
    }


    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return try {
            super.onTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return isCanScroll && super.canScrollHorizontally(direction)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}