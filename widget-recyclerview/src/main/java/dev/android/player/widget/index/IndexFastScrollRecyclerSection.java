package dev.android.player.widget.index;

/**
 * Created by MyInnos on 31-01-2017.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SectionIndexer;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class IndexFastScrollRecyclerSection extends RecyclerView.AdapterDataObserver {

    ExecutorService mExecutor;
    private float mIndexBarWidth;
    private float mIndexBarMargin;
    private final float mPreviewPadding;
    private final float mDensity;
    private final float mScaledDensity;
    private int mListViewWidth;
    private int mListViewHeight;
    private int mCurrentSection = -1;
    private boolean mIsIndexing = false;
    private WeakReference<IndexFastScrollRecyclerView> mRecyclerViewRef = null;
    private SectionIndexer mIndexer = null;
    private String[] mSections = null;
    private RectF mIndexBarRect;

    private Typeface setTypeface = null;

    private int setIndexTextSize;
    private int mIndexBarInterval;
    private float setIndexBarWidth;
    private float setIndexBarMargin;
    private int setPreviewPadding;
    private int setIndexBarCornerRadius;
    private int indexBarBackgroundColor;
    private int indexBarTextColor;
    private int indexBarBackgroundAlpha;
    private int indexBarSelectedTextColor;


    public int previewTextSize;
    private int previewBackgroundColor;
    private int previewTransparentValue;
    private int previewRadius;
    private int previewTextColor;

    private final int mPreviewPosition;
    private final int mPreviewMargin;


    private boolean touchable;
    private boolean mShow = false;

    //是否隐藏 优先于mShow
    private boolean mIsHide = false;
    //上次定位的位置
    private int mLastPosition = -1;

    public IndexFastScrollRecyclerSection(Context context, IndexFastScrollRecyclerView rv) {
        mDensity = context.getResources().getDisplayMetrics().density;
        mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        mRecyclerViewRef = new WeakReference<>(rv);
        setAdapter(rv.getAdapter());


        setIndexTextSize = rv.setIndexTextSize;
        mIndexBarInterval = rv.mIndexBarInterval;
        setIndexBarWidth = rv.mIndexBarWidth;
        setIndexBarMargin = rv.mIndexBarMargin;
        setPreviewPadding = rv.mPreviewPadding;
        setIndexBarCornerRadius = rv.mIndexBarCornerRadius;
        indexBarBackgroundColor = rv.mIndexBarBackgroundColor;
        indexBarTextColor = rv.mIndexBarTextColor;
        indexBarBackgroundAlpha = convertTransparentValueToBackgroundAlpha(rv.mIndexBarTransparentValue);

        previewTextSize = rv.mPreviewTextSize;
        previewBackgroundColor = rv.mPreviewBackgroundColor;
        previewTransparentValue = convertTransparentValueToBackgroundAlpha(rv.mPreviewTransparentValue);
        previewRadius = rv.mPreviewRadius;
        previewTextColor = rv.mPreviewTextColor;

        mPreviewPosition = rv.mPreviewPosition;
        mPreviewMargin = rv.mPreviewMargin;

        indexBarSelectedTextColor = rv.mIndexBarSelectedTextColor;


        mIndexBarWidth = setIndexBarWidth * mDensity;
        mIndexBarMargin = setIndexBarMargin * mDensity;
        mPreviewPadding = setPreviewPadding * mDensity;

        mExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("IndexFastScrollRecyclerSection");
            return thread;
        });
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    touchable = true;
                } else {
                    touchable = false;
                }
            }
        });
    }

    public void draw(Canvas canvas) {
        if (!mShow||mIsHide) {
            return;
        } else {
            delayHide();
        }

        if (mSections != null && mSections.length > 0) {
            Paint indexbarPaint = new Paint();
            indexbarPaint.setColor(indexBarBackgroundColor);
            indexbarPaint.setAlpha(indexBarBackgroundAlpha);
            indexbarPaint.setAntiAlias(true);
            canvas.drawRoundRect(mIndexBarRect, setIndexBarCornerRadius * mDensity, setIndexBarCornerRadius * mDensity, indexbarPaint);


            Paint indexPaint = new Paint();
            indexPaint.setColor(indexBarTextColor);
            indexPaint.setAntiAlias(true);
            indexPaint.setTextSize(setIndexTextSize * mScaledDensity);
            indexPaint.setTypeface(setTypeface);

            //记录选中的文字位置
            float SectionTextY = -1f;

            float interval = mIndexBarInterval * mScaledDensity;
            int intervalCount = mSections.length > 0 ? mSections.length + 1 : 0;
            float sectionHeight = (mIndexBarRect.height() - 2 * mIndexBarMargin - interval * intervalCount) / mSections.length;
            float paddingTop = (sectionHeight - (indexPaint.descent() - indexPaint.ascent())) / 2 + interval;
            for (int i = 0; i < mSections.length; i++) {
                float paddingLeft = (mIndexBarWidth - indexPaint.measureText(mSections[i])) / 2;
                float y = mIndexBarRect.top + mIndexBarMargin + sectionHeight * i + paddingTop + i * interval - indexPaint.ascent();

                if (mCurrentSection >= 0 && i == mCurrentSection) {
                    indexPaint.setColor(indexBarSelectedTextColor);
                    canvas.drawText(mSections[i], mIndexBarRect.left + paddingLeft, y, indexPaint);
                    SectionTextY = y;
                } else {
                    indexPaint.setColor(indexBarTextColor);
                    canvas.drawText(mSections[i], mIndexBarRect.left + paddingLeft, y, indexPaint);
                }
            }


            // Preview is shown when mCurrentSection is set
            if (mCurrentSection >= 0 && SectionTextY > 0) {
                //预览部分
                Paint previewPaint = new Paint();
                previewPaint.setColor(previewBackgroundColor);
                previewPaint.setAlpha(previewTransparentValue);
                previewPaint.setAntiAlias(true);
                previewPaint.setShadowLayer(3, 0, 0, Color.argb(64, 0, 0, 0));

                Paint previewTextPaint = new Paint();
                previewTextPaint.setColor(previewTextColor);
                previewTextPaint.setAntiAlias(true);
                previewTextPaint.setTextSize(previewTextSize * mScaledDensity);
                previewTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

                float previewTextWidth = previewTextPaint.measureText(mSections[mCurrentSection]);
                float previewSize = 2 * mPreviewPadding + previewTextPaint.descent() - previewTextPaint.ascent();
                RectF previewRect;
                if (mPreviewPosition == IndexFastScrollRecyclerView.PreviewPosition.POS_CENTER) {
                    float x = mListViewWidth / 2f;
                    float y = mListViewHeight / 2f;
                    float left = x - previewSize / 2;
                    float top = y - previewSize / 2;
                    float right = x + previewSize / 2;
                    float bottom = y + previewSize / 2;
                    previewRect = new RectF(left, top, right, bottom);
                } else {
                    boolean isRtl = mRecyclerViewRef.get().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                    float x;
                    if (isRtl) {
                        x = mIndexBarRect.right + mPreviewMargin * mDensity;
                    } else {
                        x = mIndexBarRect.left - mIndexBarMargin - mPreviewMargin * mDensity - previewSize;
                    }
                    float top = SectionTextY - previewSize / 2;
                    float right = x + previewSize;
                    float bottom = SectionTextY + previewSize / 2;
                    previewRect = new RectF(x, top, right, bottom);
                }
                canvas.drawRoundRect(previewRect, previewRadius * mDensity, previewRadius * mDensity, previewPaint);
                canvas.drawText(mSections[mCurrentSection], previewRect.left + (previewSize - previewTextWidth) / 2 - 1
                        , previewRect.top + mPreviewPadding - previewTextPaint.ascent() + 1, previewTextPaint);
                fade(300);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!mShow||mIsHide) {
            return false;
        }
        if (!touchable) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // If down event occurs inside index bar region, start indexing
                if (contains(ev.getX(), ev.getY())) {

                    // It demonstrates that the motion event started from index bar
                    mIsIndexing = true;
                    // Determine which section the point is in, and move the list to that section
                    mCurrentSection = getSectionByPoint(ev.getY());
                    scrollToPosition();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsIndexing) {
                    // If this event moves inside index bar
                    // Determine which section the point is in, and move the list to that section
                    mCurrentSection = getSectionByPoint(ev.getY());
                    scrollToPosition();
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsIndexing) {
                    mIsIndexing = false;
                    mCurrentSection = -1;
                }
                break;
        }
        return false;
    }

    private void scrollToPosition() {

        int position = mIndexer.getPositionForSection(mCurrentSection);
        if (position < 0) {
            return;
        }
        if (mLastPosition == position) {
            return;
        }
        RecyclerView rv = mRecyclerViewRef.get();
        if (rv == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position, 0);
        } else {
            layoutManager.scrollToPosition(position);
        }
        rv.postInvalidate();
        mLastPosition = position;

    }

    private void updateRect() {
        if (mListViewHeight == 0) {
            return;
        }
        int height = 0;
        if (mSections != null) {
            //重新测算的时候变量重置
            setIndexTextSize = mRecyclerViewRef.get().setIndexTextSize;
            mIndexBarInterval = mRecyclerViewRef.get().mIndexBarInterval;

            float size = setIndexTextSize * mScaledDensity;
            float interval = mIndexBarInterval * mScaledDensity;
            int intervalCounts = mSections.length > 0 ? mSections.length + 1 : 0;
            height = (int) (size * mSections.length + 2 * mIndexBarMargin + intervalCounts * interval);

            while (mListViewHeight - height < 0) {
                if (mIndexBarInterval > 0) {
                    mIndexBarInterval -= 2;
                } else if (setIndexTextSize > 8) {
                    setIndexTextSize -= 2;
                } else {
                    break;
                }
                size = setIndexTextSize * mScaledDensity;
                interval = mIndexBarInterval * mScaledDensity;
                intervalCounts = mSections.length > 0 ? mSections.length + 1 : 0;
                height = (int) (size * mSections.length + 2 * mIndexBarMargin + intervalCounts * interval);
            }

        }

        boolean isRtl = mRecyclerViewRef.get().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        if (isRtl) {
            mIndexBarRect = new RectF(mIndexBarMargin,
                    (mListViewHeight - height) / 2,
                    mIndexBarWidth + mIndexBarMargin,
                    (mListViewHeight + height) / 2);
        } else {
            mIndexBarRect = new RectF(mListViewWidth - mIndexBarMargin - mIndexBarWidth,
                    (mListViewHeight - height) / 2,
                    mListViewWidth - mIndexBarMargin,
                    (mListViewHeight + height) / 2);
        }

    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mListViewWidth = w;
        mListViewHeight = h;
        updateRect();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (mIndexer != null) {
            if (mIndexer instanceof RecyclerView.Adapter) {
                ((RecyclerView.Adapter) mIndexer).unregisterAdapterDataObserver(this);
            }
            mIndexer = null;
        }
        if (adapter instanceof SectionIndexer) {
            try {
                adapter.registerAdapterDataObserver(this);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mIndexer = (SectionIndexer) adapter;
            updateSections();
        }
    }

    private void updateSections() {
        mExecutor.execute(() -> {
            if (mIndexer == null) {
                return;
            }
            try {
                mSections = (String[]) mIndexer.getSections();
                updateRect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onChanged() {
        super.onChanged();
        updateSections();
        Log.e("RecyclerSession", "onChanged ");
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        updateSections();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        updateSections();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        updateSections();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        updateSections();
    }

    public boolean contains(float x, float y) {
        if (!mShow||mIsHide) {
            return false;
        }
        // Determine if the point is in index bar region, which includes the right margin of the bar
        return mIndexBarRect.contains(x, y);
    }

    private int getSectionByPoint(float y) {
        if (mSections == null || mSections.length == 0)
            return 0;
        if (y < mIndexBarRect.top + mIndexBarMargin)
            return 0;
        if (y >= mIndexBarRect.top + mIndexBarRect.height() - mIndexBarMargin)
            return mSections.length - 1;
        float oneInterval = mIndexBarInterval * mScaledDensity;
        float pos = y - mIndexBarRect.top - mIndexBarMargin + oneInterval / 2;
        float oneHeight = (mIndexBarRect.height() - 2 * mIndexBarMargin + oneInterval) / mSections.length;
        return (int) (pos / oneHeight);
    }


    private final Runnable WHAT_FADE_PREVIEW_RUNNABLE = () -> {
        RecyclerView rv = mRecyclerViewRef.get();
        if (rv != null) {
            rv.invalidate();
        }
    };

    private void fade(long delay) {
        if (mRecyclerViewRef.get() != null) {
            mRecyclerViewRef.get().removeCallbacks(WHAT_FADE_PREVIEW_RUNNABLE);
            mRecyclerViewRef.get().postDelayed(WHAT_FADE_PREVIEW_RUNNABLE, delay);
        }
    }

    private final Runnable WHAT_HIDE_RUNNABLE = () -> setShow(false);

    private void delayHide() {
        if (mRecyclerViewRef.get() != null) {
            mRecyclerViewRef.get().removeCallbacks(WHAT_HIDE_RUNNABLE);
            mRecyclerViewRef.get().postDelayed(WHAT_HIDE_RUNNABLE, 3000);
        }
    }

    public void setShow(boolean show) {
        mShow = show;
        RecyclerView rv = mRecyclerViewRef.get();
        if (rv != null) {
            rv.postInvalidate();
        }
    }

    public void setHide(boolean hide) {
        mIsHide = hide;
        RecyclerView rv = mRecyclerViewRef.get();
        if (rv != null) {
            rv.postInvalidate();
        }
    }

    private int convertTransparentValueToBackgroundAlpha(float value) {
        return (int) (255 * value);
    }

    /**
     * @param value int to set the text size of the index bar
     */
    public void setIndexTextSize(int value) {
        setIndexTextSize = value;
        updateRect();
    }

    /**
     * @param value float to set the width of the index bar
     */
    public void setIndexBarWidth(float value) {
        mIndexBarWidth = value;
    }

    /**
     * @param value float to set the margin of the index bar
     */
    public void setIndexBarMargin(float value) {
        mIndexBarMargin = value;
        updateRect();
    }

    /**
     * @param value int to set preview padding
     */
    public void setPreviewPadding(int value) {
        setPreviewPadding = value;
    }

    /**
     * @param value int to set the radius of the index bar
     */
    public void setIndexBarCornerRadius(int value) {
        setIndexBarCornerRadius = value;
    }

    /**
     * @param value float to set the transparency of the color for index bar
     */
    public void setIndexBarTransparentValue(float value) {
        indexBarBackgroundAlpha = convertTransparentValueToBackgroundAlpha(value);
    }

    /**
     * @param typeface Typeface to set the typeface of the preview & the index bar
     */
    public void setTypeface(Typeface typeface) {
        setTypeface = typeface;
    }

    /**
     * @param color The color for the scroll track
     */
    public void setIndexBarColor(int color) {
        indexBarBackgroundColor = color;
    }

    /**
     * @param color The text color for the index bar
     */
    public void setIndexBarTextColor(int color) {
        indexBarTextColor = color;
    }

    public void setIndexBarInterval(int value) {
        mIndexBarInterval = value;
        updateRect();
    }
}