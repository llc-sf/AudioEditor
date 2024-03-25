package dev.android.player.widget

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView


fun RecyclerView.addTouchListener(OnClick: RecyclerViewItemClickListener? = null,//Item点击事件
                                  OnLongPress: RecyclerViewItemLongPressedListener? = null//Item长按事件
) {
    this.addOnItemTouchListener(RecyclerViewItemTouchListener(this, OnClick, OnLongPress))
}


/**
 * RecyclerView Item Long Press Listener
 */
interface RecyclerViewItemLongPressedListener {
    fun onItemLongPressed(parent: RecyclerView, position: Int)
}

/**
 * RecyclerView Item Click Listener
 */
interface RecyclerViewItemClickListener {
    fun onItemClick(parent: RecyclerView, position: Int)
}


private class RecyclerViewItemTouchListener(view: RecyclerView,
                                            OnClick: RecyclerViewItemClickListener?,//Click
                                            OnLongPress: RecyclerViewItemLongPressedListener?//Long Press
) : RecyclerView.SimpleOnItemTouchListener() {


    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val childView = view.findChildViewUnder(e.x, e.y)
            if (childView != null) {
                val position = view.getChildAdapterPosition(childView)
                if (position != RecyclerView.NO_POSITION) {
                    OnClick?.onItemClick(view, position)
                }
            }
            return false
        }

        override fun onLongPress(e: MotionEvent) {
            val child = view.findChildViewUnder(e.x, e.y)
            if (child != null) {
                val position = view.getChildAdapterPosition(child)
                OnLongPress?.onItemLongPressed(view, position)
            }
        }

    }

    private val mGesture = GestureDetector(view.context, mGestureListener)


    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return mGesture.onTouchEvent(e)
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        mGesture.onTouchEvent(e)
    }
}