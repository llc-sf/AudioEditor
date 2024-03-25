package dev.android.player.widget.index;

/**
 * Created by MyInnos on 31-01-2017.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.android.player.widget.R;


public class IndexFastScrollRecyclerView extends RecyclerView {

    private IndexFastScrollRecyclerSection mScroller = null;
    private GestureDetector mGestureDetector = null;

    public int setIndexTextSize = 12;
    public int mIndexBarInterval = 4;
    public float mIndexBarWidth = 20;
    public float mIndexBarMargin = 0;
    public int mPreviewPadding = 5;
    public int mIndexBarCornerRadius = 5;
    public float mIndexBarTransparentValue = 0.6f;

    public int mIndexBarBackgroundColor = Color.BLACK;
    public int mIndexBarTextColor = Color.WHITE;


    public int mPreviewTextSize = 50;
    @ColorInt
    public int mPreviewTextColor = Color.WHITE;
    @ColorInt
    public int mPreviewBackgroundColor = Color.BLACK;

    public float mPreviewTransparentValue = 0.6f;
    public int mPreviewRadius = 6;

    public int mIndexBarSelectedTextColor = Color.WHITE;

    //预览框的位置
    public int mPreviewPosition = PreviewPosition.POS_CENTER;

    //预览框的Margin
    public int mPreviewMargin = 0;


    public IndexFastScrollRecyclerView(Context context) {
        this(context, null);
    }

    public IndexFastScrollRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.IndexFastRecyclerViewStyle);
    }

    public IndexFastScrollRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndexFastScrollRecyclerView, defStyleAttr, R.style.DefaultIndexFastRecyclerviewStyle);
            if (typedArray != null) {
                try {
                    setIndexTextSize = typedArray.getInt(R.styleable.IndexFastScrollRecyclerView_setIndexTextSize, setIndexTextSize);
                    mIndexBarWidth = typedArray.getFloat(R.styleable.IndexFastScrollRecyclerView_setIndexBarWidth, mIndexBarWidth);
                    mIndexBarMargin = typedArray.getFloat(R.styleable.IndexFastScrollRecyclerView_setIndexBarMargin, mIndexBarMargin);
                    mPreviewPadding = typedArray.getInt(R.styleable.IndexFastScrollRecyclerView_setPreviewPadding, mPreviewPadding);
                    mIndexBarCornerRadius = typedArray.getInt(R.styleable.IndexFastScrollRecyclerView_setIndexBarCornerRadius, mIndexBarCornerRadius);
                    mIndexBarSelectedTextColor = typedArray.getColor(R.styleable.IndexFastScrollRecyclerView_setIndexBarSelectedTextColor, mIndexBarSelectedTextColor);
                    mIndexBarTransparentValue = typedArray.getFloat(R.styleable.IndexFastScrollRecyclerView_setIndexBarTransparentValue, mIndexBarTransparentValue);

                    if (typedArray.getString(R.styleable.IndexFastScrollRecyclerView_setIndexBarColor) != null) {
                        mIndexBarBackgroundColor = typedArray.getColor(R.styleable.IndexFastScrollRecyclerView_setIndexBarColor, Color.BLACK);
                    }

                    if (typedArray.getString(R.styleable.IndexFastScrollRecyclerView_setIndexBarTextColor) != null) {
                        mIndexBarTextColor = typedArray.getColor(R.styleable.IndexFastScrollRecyclerView_setIndexBarTextColor, Color.WHITE);
                    }
                    mPreviewTextSize = typedArray.getInt(R.styleable.IndexFastScrollRecyclerView_setPreviewTextSize, mPreviewTextSize);
                    mPreviewTextColor = typedArray.getInt(R.styleable.IndexFastScrollRecyclerView_setPreviewTextColor, Color.WHITE);
                    mPreviewBackgroundColor = typedArray.getColor(R.styleable.IndexFastScrollRecyclerView_setPreviewColor, mPreviewBackgroundColor);
                    mPreviewRadius = typedArray.getInt(R.styleable.IndexFastScrollRecyclerView_setPreviewRadius, mPreviewRadius);
                    mPreviewTransparentValue = typedArray.getFloat(R.styleable.IndexFastScrollRecyclerView_setPreviewTransparentValue, mPreviewTransparentValue);

                    mPreviewPosition = typedArray.getInt(R.styleable.IndexFastScrollRecyclerView_setPreviewPosition, mPreviewPosition);
                    mPreviewMargin = typedArray.getInt(R.styleable.IndexFastScrollRecyclerView_setPreviewMargin, mPreviewMargin);
                } finally {
                    typedArray.recycle();
                }
            }
        }
        mScroller = new IndexFastScrollRecyclerSection(context, this);
    }


    @Override
    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
        final LayoutManager layoutManager = getLayoutManager();
        if (getAdapter() != null && layoutManager instanceof GridLayoutManager) {

            GridLayoutAnimationController.AnimationParameters animationParams =
                    (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;

            if (animationParams == null) {
                // If there are no animation parameters, create new once and attach them to
                // the LayoutParams.
                animationParams = new GridLayoutAnimationController.AnimationParameters();
                params.layoutAnimationParameters = animationParams;
            }

            // Next we are updating the parameters

            // Set the number of items in the RecyclerView and the index of this item
            animationParams.count = count;
            animationParams.index = index;

            // Calculate the number of columns and rows in the grid
            final int columns = ((GridLayoutManager) layoutManager).getSpanCount();
            animationParams.columnsCount = columns;
            animationParams.rowsCount = count / columns;

            // Calculate the column/row position in the grid
            final int invertedIndex = count - 1 - index;
            animationParams.column = columns - 1 - (invertedIndex % columns);
            animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns;

        } else {
            // Proceed as normal if using another type of LayoutManager
            super.attachLayoutAnimationParameters(child, params, index, count);
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // Overlay index bar
        if (mScroller != null)
            mScroller.draw(canvas);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (mScroller.contains(ev.getX(), ev.getY())) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            }
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Intercept ListView's touch event
        if (mScroller != null && mScroller.onTouchEvent(ev)) {
            mScroller.setShow(true);
            return true;
        }

        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2,
                                       float velocityX, float velocityY) {
                    mScroller.setShow(true);
                    return super.onFling(e1, e2, velocityX, velocityY);
                }

            });
        }
        mGestureDetector.onTouchEvent(ev);
        mScroller.setShow(true);
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mScroller != null && mScroller.contains(ev.getX(), ev.getY())) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (mScroller != null)
            mScroller.setAdapter(adapter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mScroller != null)
            mScroller.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * @param value int to set the text size of the index bar
     */
    public void setIndexTextSize(int value) {
        mScroller.setIndexTextSize(value);
    }

    public void setIndexBarInterval(int value) {
        mScroller.setIndexBarInterval(value);
    }

    /**
     * @param value float to set the width of the index bar
     */
    public void setIndexBarWidth(float value) {
        mScroller.setIndexBarWidth(value);
    }

    /**
     * @param value float to set the margin of the index bar
     */
    public void setIndexBarMargin(float value) {
        mScroller.setIndexBarMargin(value);
    }

    /**
     * @param value int to set the preview padding
     */
    public void setPreviewPadding(int value) {
        mScroller.setPreviewPadding(value);
    }

    /**
     * @param value int to set the corner radius of the index bar
     */
    public void setIndexBarCornerRadius(int value) {
        mScroller.setIndexBarCornerRadius(value);
    }

    /**
     * @param value float to set the transparency value of the index bar
     */
    public void setIndexBarTransparentValue(int value) {
        mScroller.setIndexBarTransparentValue(value);
    }

    /**
     * @param typeface Typeface to set the typeface of the preview & the index bar
     */
    public void setTypeface(Typeface typeface) {
        mScroller.setTypeface(typeface);
    }

    /**
     * @param color The color for the index bar
     */
    public void setIndexBarColor(int color) {
        mScroller.setIndexBarColor(color);
    }

    /**
     * @param color The text color for the index bar
     */
    public void setIndexBarTextColor(int color) {
        mScroller.setIndexBarTextColor(color);
    }


    @IntDef({
            PreviewPosition.POS_CENTER,
            PreviewPosition.POS_END
    })
    @interface PreviewPosition {
        int POS_CENTER = 1;
        int POS_END = 2;
    }


    public void setIsHideIndexFastScrollView(boolean isHide) {
        mScroller.setHide(isHide);
    }

}