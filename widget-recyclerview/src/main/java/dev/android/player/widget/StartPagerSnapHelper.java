package dev.android.player.widget;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class StartPagerSnapHelper extends PagerSnapHelper {


    private boolean isIncludeDecorated = false;//是否包含装饰


    public StartPagerSnapHelper() {
        this(false);
    }

    public StartPagerSnapHelper(boolean isIncludeDecorated) {
        this.isIncludeDecorated = isIncludeDecorated;
    }


    private static final String TAG = "StartSnapHelper";

    private OrientationHelper mHorizontalHelper, mVerticalHelper;

    private RecyclerView mRecyclerView;

    private boolean isRtl = false;

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(RecyclerView.LayoutManager layoutManager, View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager), layoutManager);
        } else {
            out[0] = 0;
        }
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager), layoutManager);
        } else {
            out[1] = 0;
        }
        Log.d(TAG, "calculateDistanceToFinalSnap() called with: out = [" + out[0] + "]");
        return out;
    }

    private int distanceToStart(View targetView, OrientationHelper helper, RecyclerView.LayoutManager layoutManager) {
        int decoratedStartSize = 0;
        isRtl = mRecyclerView != null && mRecyclerView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        if (!isIncludeDecorated) {
            if (isRtl) {
                decoratedStartSize = layoutManager.getRightDecorationWidth(targetView);
            } else {
                decoratedStartSize = layoutManager.getLeftDecorationWidth(targetView);
            }
        }
        if (isRtl) {
            return helper.getDecoratedEnd(targetView) - helper.getEndAfterPadding() - decoratedStartSize;
        } else {
            return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding() + decoratedStartSize;
        }
    }


    private OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

    private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;

    }
}