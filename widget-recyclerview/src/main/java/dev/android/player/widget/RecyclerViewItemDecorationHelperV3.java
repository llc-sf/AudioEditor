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
public class RecyclerViewItemDecorationHelperV3 {
    /**
     * 布局边界
     */
    private static class SpaceDividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int mHorSpace;
        private final int mVerSpace;

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
                GridLayoutManager.SpanSizeLookup lookup = manager.getSpanSizeLookup();
                boolean isFullSpan = lookup.getSpanSize(position) == spanCount;//是否是占满一行
                //如果是纵向布局
                int total = 0;
                for (int i = 0; i < itemCount; i++) {
                    total += lookup.getSpanSize(i);
                }
                int count = total % spanCount == 0 ? total / spanCount : total / spanCount + 1;
                if (manager.getOrientation() == GridLayoutManager.VERTICAL) {
                    column = lookup.getSpanIndex(position, spanCount);
                    row = lookup.getSpanGroupIndex(position, spanCount);
                    columnCount = spanCount;
                    rowCount = count;
                } else {
                    column = lookup.getSpanGroupIndex(position, spanCount);
                    row = lookup.getSpanIndex(position, spanCount);
                    columnCount = count;
                    rowCount = spanCount;
                }

                int start = (int) (column * mHorSpace * 1f / columnCount);// column * (列间距 * (1f / 列数))
                int end = (int) (mHorSpace - (column + 1) * mHorSpace * 1f / columnCount);// 列间距 - (column + 1) * (列间距 * (1f /列数))
                int top = (int) (row * mVerSpace * 1f / rowCount); // row * (行间距 * (1f / 行数))
                int bottom = (int) (mVerSpace - (row + 1) * mVerSpace * 1f / rowCount); // 行间距 - (row + 1) * (行间距 * (1f / 行数))
                if (isFullSpan) {
                    if (manager.getOrientation() == GridLayoutManager.VERTICAL) {
                        outRect.top = top;
                        outRect.bottom = bottom;
                        outRect.left = 0;
                        outRect.right = 0;
                    } else {
                        outRect.top = 0;
                        outRect.left = start;
                        outRect.right = end;
                        outRect.bottom = 0;
                    }
                } else {
                    if (isRtl) {
                        outRect.right = start;
                        outRect.left = end;
                    } else {
                        outRect.left = start;
                        outRect.right = end;
                    }
                    outRect.top = top;
                    outRect.bottom = bottom;
                }

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

    private static class BorderItemDecoration extends RecyclerView.ItemDecoration {
        private final int mLeft;
        private final int mTop;
        private final int mRight;
        private final int mBottom;

        public BorderItemDecoration(int left, int top, int right, int bottom) {
            this.mLeft = left;
            this.mTop = top;
            this.mRight = right;
            this.mBottom = bottom;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            RecyclerView.LayoutManager LayoutManager = parent.getLayoutManager();
            int itemCount = state.getItemCount();
            if (LayoutManager instanceof GridLayoutManager) {
                GridLayoutManager manager = (GridLayoutManager) LayoutManager;
                //设置网格间距
                int spanCount = ((GridLayoutManager) manager).getSpanCount();
                int columnCount;//总共有多少列
                int rowCount;//总共有多少行
                int column;// view 所在的列
                int row; // view 所在的行

                //计算总的行数
                GridLayoutManager.SpanSizeLookup lookup = manager.getSpanSizeLookup();
                int total = 0;
                for (int i = 0; i < itemCount; i++) {
                    total += lookup.getSpanSize(i);
                }
                int count = total % spanCount == 0 ? total / spanCount : total / spanCount + 1;
                boolean isFullSpan = lookup.getSpanSize(position) == spanCount;//是否是占满一行
                //计算行列
                if (manager.getOrientation() == GridLayoutManager.VERTICAL) {
                    column = lookup.getSpanIndex(position, spanCount);
                    row = lookup.getSpanGroupIndex(position, spanCount);
                    columnCount = spanCount;
                    rowCount = count;
                } else {
                    column = lookup.getSpanGroupIndex(position, spanCount);
                    row = lookup.getSpanIndex(position, spanCount);
                    columnCount = count;
                    rowCount = spanCount;
                }

                boolean isTop = row == 0;//是否是第一行
                boolean isBottom = row == rowCount - 1;//是否是最后一行
                boolean isStart = column == 0;//是否是第一列
                boolean isEnd = column == columnCount - 1;//是否是最后一列
                boolean isRtl = parent.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;//是否是从右向左排列

                if (isFullSpan) {
                    if (manager.getOrientation() == GridLayoutManager.VERTICAL) {
                        outRect.set(0, isTop ? mTop : 0, 0, isBottom ? mBottom : 0);
                    } else {
                        if (isRtl) {
                            outRect.set(isStart ? mRight : 0, 0, isEnd ? mLeft : 0, 0);
                        } else {
                            outRect.set(isStart ? mLeft : 0, 0, isEnd ? mRight : 0, 0);
                        }
                    }
                } else {
                    if (row == 0) {
                        outRect.top = mTop;
                    }
                    if (row == rowCount - 1) {
                        outRect.bottom = mBottom;
                    }
                    if (column == 0) {
                        if (isRtl) {
                            outRect.right = mRight;
                        } else {
                            outRect.left = mLeft;
                        }
                    }
                    if (column == columnCount - 1) {
                        if (isRtl) {
                            outRect.left = mLeft;
                        } else {
                            outRect.right = mRight;
                        }
                    }
                }

            } else if (LayoutManager instanceof LinearLayoutManager) {
                // Handle LinearLayoutManager
                LinearLayoutManager manager = (LinearLayoutManager) LayoutManager;
                int orientation = manager.getOrientation();

                if (orientation == LinearLayoutManager.VERTICAL) {
                    // Set the top offset for the first item in vertical layout
                    if (position == 0) {
                        outRect.top = mTop;
                    }
                    // Set the bottom offset for the last item in vertical layout
                    if (position == manager.getItemCount() - 1) {
                        outRect.bottom = mBottom;
                    }
                } else {
                    // Set the left offset for the first item in horizontal layout
                    if (position == 0) {
                        outRect.left = mLeft;
                    }
                    // Set the right offset for the last item in horizontal layout
                    if (position == manager.getItemCount() - 1) {
                        outRect.right = mRight;
                    }
                }
            }
        }
    }


    public static final class SpaceDividerBuilder {
        private int mHorSpace;//横向间距
        private int mVerSpace;//纵向间距

        private int mLeft;//左边距
        private int mTop;//上边距
        private int mRight;//右边距
        private int mBottom;//下边距

        public static SpaceDividerBuilder create() {
            return new SpaceDividerBuilder();
        }

        /**
         * 设置间距
         *
         * @param space
         * @return
         */
        public SpaceDividerBuilder setSpace(int space) {
            this.mHorSpace = space;
            this.mVerSpace = space;
            return this;
        }

        /**
         * 设置间距
         *
         * @param hor
         * @param ver
         * @return
         */
        public SpaceDividerBuilder setSpace(int hor, int ver) {
            this.mHorSpace = hor;
            this.mVerSpace = ver;
            return this;
        }

        public SpaceDividerBuilder setBorder(int left, int top, int right, int bottom) {
            this.mLeft = left;
            this.mTop = top;
            this.mRight = right;
            this.mBottom = bottom;
            return this;
        }

        public SpaceDividerBuilder setBorder(int border) {
            this.mLeft = border;
            this.mTop = border;
            this.mRight = border;
            this.mBottom = border;
            return this;
        }


        public void add(RecyclerView recyclerView) {
            recyclerView.addItemDecoration(new BorderItemDecoration(mLeft, mTop, mRight, mBottom));
            recyclerView.addItemDecoration(new SpaceDividerItemDecoration(mHorSpace, mVerSpace));
        }

    }


    public static final class BorderDividerBuilder {
        private int mLeft;//左边距
        private int mTop;//上边距
        private int mRight;//右边距
        private int mBottom;//下边距

        public static BorderDividerBuilder create() {
            return new BorderDividerBuilder();
        }

        public BorderDividerBuilder setBorder(int left, int top, int right, int bottom) {
            this.mLeft = left;
            this.mTop = top;
            this.mRight = right;
            this.mBottom = bottom;
            return this;
        }

        public BorderDividerBuilder setBorder(int border) {
            this.mLeft = border;
            this.mTop = border;
            this.mRight = border;
            this.mBottom = border;
            return this;
        }

        public RecyclerView.ItemDecoration build() {
            return new BorderItemDecoration(mLeft, mTop, mRight, mBottom);
        }
    }

}
