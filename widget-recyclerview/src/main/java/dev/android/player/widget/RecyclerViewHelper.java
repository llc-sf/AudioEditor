package dev.android.player.widget;

import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView 移动到指定Position
 */
public class RecyclerViewHelper {

    public static void moveToPosition(int position, RecyclerView recyclerView) {
        Log.d("RecyclerViewHelper", "moveToPosition() called with: position = [" + position + "], recyclerView = [" + recyclerView + "]");
        if (recyclerView == null) return;
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (manager == null) return;
        //先从RecyclerView的LayoutManager中获取第一项和最后一项的Position
        int firstItem = manager.findFirstVisibleItemPosition();
        int lastItem = manager.findLastVisibleItemPosition();
        //然后区分情况
        if (position <= firstItem) {
            //当要置顶的项在当前显示的第一个项的前面时
            recyclerView.scrollToPosition(position);
        } else if (position <= lastItem) {
            //当要置顶的项已经在屏幕上显示时
            if (manager.getOrientation() == RecyclerView.VERTICAL) {
                int top = recyclerView.getChildAt(position - firstItem).getTop();
                recyclerView.scrollBy(0, top);
            } else {
                int left = recyclerView.getChildAt(position - firstItem).getLeft();
                recyclerView.scrollBy(left, 0);
            }
        } else {
            //当要置顶的项在当前显示的最后一项的后面时
            manager.scrollToPositionWithOffset(position, 0);
//            recyclerView.clearOnScrollListeners();
//            recyclerView.addOnScrollListener(new RecyclerScrollListener(position, manager));
//            recyclerView.scrollToPosition(position);
        }
    }

    /**
     * 平滑滑动到 RecycleView 指定位置
     *
     * @param position
     * @param recyclerView
     */
    public static void smoothToPosition(int position, RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerScrollListener(position, layoutManager));
        recyclerView.smoothScrollToPosition(position);
    }

    private static class RecyclerScrollListener extends RecyclerView.OnScrollListener {
        private int position;
        private LinearLayoutManager layoutManager;

        RecyclerScrollListener(int position, LinearLayoutManager layoutManager) {
            this.position = position;
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int n = position - layoutManager.findFirstVisibleItemPosition();
            if (0 <= n && n < recyclerView.getChildCount()) {
                if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                    //获取要置顶的项顶部离RecyclerView顶部的距离
                    int top = recyclerView.getChildAt(n).getTop();
                    //最后的移动
                    recyclerView.scrollBy(0, top);
                } else {
                    //获取要置顶的项顶部离RecyclerView顶部的距离
                    int left = recyclerView.getChildAt(n).getLeft();
                    //最后的移动
                    recyclerView.scrollBy(left, 0);
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                recyclerView.removeOnScrollListener(this);
            }
        }
    }
}
