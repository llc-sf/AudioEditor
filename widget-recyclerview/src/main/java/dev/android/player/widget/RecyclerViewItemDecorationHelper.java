package dev.android.player.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 添加分割线
 */
public class RecyclerViewItemDecorationHelper {

    /**
     * RecyclerView 添加 ItemDecoration
     */
    public static void addItemDecoration(RecyclerView recyclerView, int space) {
        recyclerView.addItemDecoration(new SpaceDividerItemDecoration(space));
    }

    public static void addItemDecoration(RecyclerView recyclerView, int hor, int ver) {
        recyclerView.addItemDecoration(new SpaceDividerItemDecoration(hor, ver));
    }


    /**
     * 布局边界
     */
    private static class SpaceDividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int mHorSpace;
        private final int mVerSpace;

        public SpaceDividerItemDecoration(int space) {
            this(space, space);
        }

        public SpaceDividerItemDecoration(int hor, int ver) {
            this.mHorSpace = hor;
            this.mVerSpace = ver;
        }


        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            int position = parent.getChildAdapterPosition(view);
            boolean isRtl = parent.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;//是否是从右向左排列

            RecyclerView.LayoutManager LayoutManager = parent.getLayoutManager();
            int itemCount = state.getItemCount();

            if (LayoutManager instanceof GridLayoutManager) {//网格布局
                GridLayoutManager manager = (GridLayoutManager) LayoutManager;
                //设置网格间距
                int spanCount = ((GridLayoutManager) manager).getSpanCount();
                int columnCount;//总共有多少列
                int rowCount;//总共有多少行
                int column;// view 所在的列
                int row; // view 所在的行
                //如果是纵向布局
                int count = itemCount % spanCount == 0 ? itemCount / spanCount : itemCount / spanCount + 1;
                if (manager.getOrientation() == GridLayoutManager.VERTICAL) {
                    column = position % spanCount;
                    row = position / spanCount;
                    columnCount = spanCount;
                    rowCount = count;
                } else {
                    column = position / spanCount;
                    row = position % spanCount;
                    columnCount = count;
                    rowCount = spanCount;
                }
                int start = (int) (column * mHorSpace * 1f / columnCount);// column * (列间距 * (1f / 列数))
                int end = (int) (mHorSpace - (column + 1) * mHorSpace * 1f / columnCount);// 列间距 - (column + 1) * (列间距 * (1f /列数))

                if (isRtl) {
                    outRect.right = start;
                    outRect.left = end;
                } else {
                    outRect.left = start;
                    outRect.right = end;
                }
                outRect.top = (int) (row * mVerSpace * 1f / rowCount); // row * (行间距 * (1f / 行数))
                outRect.bottom = (int) (mVerSpace - (row + 1) * mVerSpace * 1f / rowCount); // 行间距 - (row + 1) * (行间距 * (1f / 行数))

            } else if (LayoutManager instanceof LinearLayoutManager) { //判断RecyclerView的布局方向
                LinearLayoutManager manager = (LinearLayoutManager) LayoutManager;
                if (manager.getOrientation() == LinearLayoutManager.HORIZONTAL) {//横向
                    //第一个item不需要左间距 最后一个不要右间距
                    if (position == 0) {  //第一个
                        if (isRtl) {
                            outRect.left = mHorSpace / 2;
                        } else {
                            outRect.right = mHorSpace / 2;
                        }
                    } else if (position == itemCount - 1) { //最后一个
                        if (isRtl) {
                            outRect.right = mHorSpace / 2;
                        } else {
                            outRect.left = mHorSpace / 2;
                        }
                    } else {
                        outRect.left = mHorSpace / 2;
                        outRect.right = mHorSpace / 2;
                    }
                } else {
                    //第一个item不需要上间距 最后一个不要下间距
                    if (position == 0) {  //第一个
                        outRect.bottom = mVerSpace / 2;
                    } else if (position == itemCount - 1) { //最后一个
                        outRect.top = mVerSpace / 2;
                    } else {
                        outRect.top = mVerSpace / 2;
                        outRect.bottom = mVerSpace / 2;
                    }
                }
            }
        }
    }

}
