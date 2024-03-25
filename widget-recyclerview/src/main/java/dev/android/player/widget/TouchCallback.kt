package dev.android.player.widget

import android.graphics.Canvas
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 *
 */

class TouchCallback @JvmOverloads constructor(
        val adapter: ITouchAdapter,
        val ItemViewSwipeEnabled: Boolean = false,/*是否支持滑动删除*/
        val ItemViewDragEnabled: Boolean = true,/*是否支持拖拽*/
) : ItemTouchHelper.Callback() {


    private val TAG: String = "TouchCallback"

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val onSwipeFlag = if (ItemViewSwipeEnabled) {
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        } else {
            0
        }
        val onDragFlag = if (ItemViewDragEnabled) {
            ItemTouchHelper.UP or ItemTouchHelper.DOWN
        } else {
            0
        }
        return makeMovementFlags(onDragFlag, onSwipeFlag)
    }

    private var mFromPosition = -1

    private var mTargetPosition = -1

    override fun onMove(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        Log.d(TAG, "onMove() called with: recyclerView = $recyclerView, holder = $holder, target = $target")
        mTargetPosition = target.bindingAdapterPosition
        return adapter.onMove(holder.bindingAdapterPosition, target.bindingAdapterPosition)
    }


    override fun isItemViewSwipeEnabled(): Boolean {
        return ItemViewSwipeEnabled
    }

    override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return current.itemViewType == target.itemViewType
    }

    override fun onSelectedChanged(holder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(holder, actionState)
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                mFromPosition = holder?.bindingAdapterPosition ?: -1
            }
            ItemTouchHelper.ACTION_STATE_IDLE -> {
                if (mTargetPosition != mFromPosition && mTargetPosition >= 0 && mFromPosition >= 0) {
                    adapter.onDrag(mFromPosition, mTargetPosition)
                    mFromPosition = -1
                    mTargetPosition = -1
                }
            }
        }
        Log.d(TAG, "onSelectedChanged() called with: viewHolder = $holder, actionState = $actionState")
    }

    override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onSwiped(holder.bindingAdapterPosition)
    }

    override fun clearView(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder) {
        (holder as? ITouchHolder)?.onInitPreview()
        super.clearView(recyclerView, holder)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, holder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (holder is ITouchHolder && actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            holder.onSwipedPreviewDraw(c, dX, dY, isCurrentlyActive)
        } else {
            super.onChildDraw(c, recyclerView, holder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}


interface ITouchAdapter {

    fun onDrag(from: Int, target: Int)

    fun onMove(from: Int, target: Int): Boolean

    fun onSwiped(position: Int) {}
}

interface ITouchHolder {

    fun onInitPreview()

    fun onSwipedPreviewDraw(c: Canvas, dX: Float, dY: Float, isCurrentlyActive: Boolean)
}