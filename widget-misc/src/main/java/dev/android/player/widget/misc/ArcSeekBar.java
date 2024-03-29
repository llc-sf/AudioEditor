package dev.android.player.widget.misc;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class ArcSeekBar extends View {


    /**
     * 画笔
     */
    private Paint mPaint;


    /**
     * 笔画描边的宽度
     */
    private float mStrokeWidth;
    private float mNormalStrokeWidth;
    private float mProgressStrokeWidth;

    /**
     *
     */
    private Paint.Cap mStrokeCap = Paint.Cap.ROUND;

    /**
     * 开始角度(默认从12点钟方向开始)
     */
    private int mStartAngle = 270;
    /**
     * 扫描角度(一个圆)
     */
    private int mSweepAngle = 360;

    /**
     * 圆心坐标x
     */
    private float mCircleCenterX;
    /**
     * 圆心坐标y
     */
    private float mCircleCenterY;

    /**
     * 弧形 正常颜色
     */
    private int mNormalColor = 0xFFC8C8C8;
    /**
     * 进度颜色
     */
    private int mProgressColor = 0xFF4FEAAC;

    /**
     * 是否使用着色器
     */
    private boolean isShader = true;

    /**
     * 着色器
     */
    private Shader mShader;

    /**
     * 着色器颜色
     */
    private int[] mShaderColors = new int[]{0xFF4FEAAC, 0xFFA8DD51, 0xFFE8D30F, 0xFFA8DD51, 0xFF4FEAAC};

    /**
     * 半径
     */
    private float mRadius;

    /**
     * 最大进度
     */
    private int mMax = 100;

    /**
     * 当前进度
     */
    private int mProgress = 0;

    /**
     * 动画持续的时间
     */
    private int mDuration = 500;

    /**
     * 进度百分比
     */
    private int mProgressPercent;

    /**
     * 拖动按钮的画笔宽度
     */
    private float mThumbStrokeWidth;
    /**
     * 拖动按钮的颜色
     */
    private int mThumbColor = 0xFFE8D30F;
    /**
     * 拖动按钮的半径
     */
    private float mThumbRadius;
    /**
     * 拖动按钮的中心点X坐标
     */
    private float mThumbCenterX;
    /**
     * 拖动按钮的中心点Y坐标
     */
    private float mThumbCenterY;
    /**
     * 触摸时可偏移距离
     */
    private float mAllowableOffsets;
    /**
     * 触摸时按钮半径放大量
     */
    private float mThumbRadiusEnlarges;

    /**
     * 是否显示拖动按钮
     */
    private boolean isShowThumb = true;

    /**
     * 手势，用来处理点击事件
     */
    private GestureDetector mDetector;

    /**
     * 是否可以拖拽
     */
    private boolean isCanDrag = false;

    /**
     * 是否启用拖拽改变进度
     */
    private boolean isEnabledDrag = true;

    /**
     * 是否启用点击改变进度
     */
    private boolean isEnabledSingle = true;

    private boolean isMeasureCircle = false;

    /**
     * 滑块边框宽度
     */
    private float mThumbBorderWidth = 0;

    /**
     * 滑块边框颜色
     */
    private int mThumbBorderColor = Color.WHITE;

    private OnChangeListener mOnChangeListener;


    public ArcSeekBar(Context context) {
        this(context, null);
    }

    public ArcSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcSeekBar);

        DisplayMetrics displayMetrics = getDisplayMetrics();
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, displayMetrics);
        mNormalStrokeWidth = mStrokeWidth;
        mProgressStrokeWidth = mStrokeWidth;

        mThumbRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, displayMetrics);

        mThumbBorderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, displayMetrics);

        mThumbStrokeWidth = mThumbRadius;


        mAllowableOffsets = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);

        mThumbRadiusEnlarges = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, displayMetrics);

        int size = a.getIndexCount();
        for (int i = 0; i < size; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.ArcSeekBar_arcStrokeWidth) {
                mStrokeWidth = a.getDimension(attr, mStrokeWidth);
                mNormalStrokeWidth = mStrokeWidth;
                mProgressStrokeWidth = mStrokeWidth;
            } else if (attr == R.styleable.ArcSeekBar_arcNormalStrokeWidth) {
                mNormalStrokeWidth = a.getDimension(attr, mNormalStrokeWidth);
            } else if (attr == R.styleable.ArcSeekBar_arcProgressStrokeWidth) {
                mProgressStrokeWidth = a.getDimension(attr, mProgressStrokeWidth);
            } else if (attr == R.styleable.ArcSeekBar_arcStrokeCap) {
                mStrokeCap = getStrokeCap(a.getInt(attr, 3));
            } else if (attr == R.styleable.ArcSeekBar_arcNormalColor) {
                mNormalColor = a.getColor(attr, mNormalColor);
            } else if (attr == R.styleable.ArcSeekBar_arcProgressColor) {
                mProgressColor = a.getColor(attr, mProgressColor);
                isShader = false;
            } else if (attr == R.styleable.ArcSeekBar_arcStartAngle) {
                mStartAngle = a.getInt(attr, mStartAngle);
            } else if (attr == R.styleable.ArcSeekBar_arcSweepAngle) {
                mSweepAngle = a.getInt(attr, mSweepAngle);
            } else if (attr == R.styleable.ArcSeekBar_arcMax) {
                int max = a.getInt(attr, mMax);
                if (max > 0) {
                    mMax = max;
                }
            } else if (attr == R.styleable.ArcSeekBar_arcProgress) {
                mProgress = a.getInt(attr, mProgress);
            } else if (attr == R.styleable.ArcSeekBar_arcDuration) {
                mDuration = a.getInt(attr, mDuration);
            } else if (attr == R.styleable.ArcSeekBar_arcThumbStrokeWidth) {
                mThumbStrokeWidth = a.getDimension(attr, mThumbStrokeWidth);
            } else if (attr == R.styleable.ArcSeekBar_arcThumbColor) {
                mThumbColor = a.getColor(attr, mThumbColor);
            } else if (attr == R.styleable.ArcSeekBar_arcThumbRadius) {
                mThumbRadius = a.getDimension(attr, mThumbRadius);
            } else if (attr == R.styleable.ArcSeekBar_arcThumbRadiusEnlarges) {
                mThumbRadiusEnlarges = a.getDimension(attr, mThumbRadiusEnlarges);
            } else if (attr == R.styleable.ArcSeekBar_arcShowThumb) {
                isShowThumb = a.getBoolean(attr, isShowThumb);
            } else if (attr == R.styleable.ArcSeekBar_arcAllowableOffsets) {
                mAllowableOffsets = a.getDimension(attr, mAllowableOffsets);
            } else if (attr == R.styleable.ArcSeekBar_arcEnabledDrag) {
                isEnabledDrag = a.getBoolean(attr, true);
            } else if (attr == R.styleable.ArcSeekBar_arcEnabledSingle) {
                isEnabledSingle = a.getBoolean(attr, true);
            } else if (attr == R.styleable.ArcSeekBar_arcThumbBorderColor) {
                mThumbBorderColor = a.getColor(attr, mThumbBorderColor);
            } else if (attr == R.styleable.ArcSeekBar_arcThumbBorderWidth) {
                mThumbBorderWidth = a.getDimension(attr, mThumbBorderWidth);
            }
        }


        a.recycle();
        mProgressPercent = (int) (mProgress * 100.0f / mMax);
        mPaint = new Paint();


        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent event) {

                if (isInArc(event.getX(), event.getY())) {
                    updateDragThumb(event.getX(), event.getY(), true);
                    if (mOnChangeListener != null) {
                        mOnChangeListener.onSingleTapUp();
                    }
                    return true;
                }

                return super.onSingleTapUp(event);
            }
        });

    }

    private Paint.Cap getStrokeCap(int value) {
        switch (value) {
            case 1:
                return Paint.Cap.BUTT;
            case 2:
                return Paint.Cap.SQUARE;
            default:
                return Paint.Cap.ROUND;
        }
    }


    private DisplayMetrics getDisplayMetrics() {
        return getResources().getDisplayMetrics();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int defaultValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getDisplayMetrics());

        int width = measureHandler(widthMeasureSpec, defaultValue);
        int height = measureHandler(heightMeasureSpec, defaultValue);

        //圆心坐标
        mCircleCenterX = (width + getPaddingLeft() - getPaddingRight()) / 2.0f;
        mCircleCenterY = (height + getPaddingTop() - getPaddingBottom()) / 2.0f;
        //计算间距
        int padding = Math.max(getPaddingLeft() + getPaddingRight(), getPaddingTop() + getPaddingBottom());
        //半径=视图宽度-横向或纵向内间距值 - 画笔宽度

        float StrokeWidth = Math.max(mStrokeWidth, Math.max(mNormalStrokeWidth, mProgressStrokeWidth));

        mRadius = (width - padding - Math.max(StrokeWidth, mThumbStrokeWidth)) / 2.0f - mThumbRadius - mThumbRadiusEnlarges;

        //默认着色器
        mShader = new SweepGradient(mCircleCenterX, mCircleCenterX, mShaderColors, null);
        isMeasureCircle = true;

        setMeasuredDimension(width, height);

    }

    /**
     * 测量
     *
     * @param measureSpec
     * @param defaultSize
     * @return
     */
    private int measureHandler(int measureSpec, int defaultSize) {

        int result = defaultSize;
        int measureMode = MeasureSpec.getMode(measureSpec);
        int measureSize = MeasureSpec.getSize(measureSpec);
        if (measureMode == MeasureSpec.EXACTLY) {
            result = measureSize;
        } else if (measureMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, measureSize);
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas);
        drawThumb(canvas);
    }


    /**
     * 绘制弧形(默认为一个圆)
     *
     * @param canvas
     */
    private void drawArc(Canvas canvas) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);


        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setShader(null);
        mPaint.setStrokeCap(mStrokeCap);

        //进度圆半径
        float diameter = mRadius * 2;
        float startX = mCircleCenterX - mRadius;
        float startY = mCircleCenterY - mRadius;
        RectF rectF1 = new RectF(startX, startY, startX + diameter, startY + diameter);

        if (mNormalColor != 0) {
            mPaint.setStrokeWidth(mNormalStrokeWidth);
            mPaint.setColor(mNormalColor);
            //绘制底层弧形
            canvas.drawArc(rectF1, mStartAngle, mSweepAngle, false, mPaint);
        }

        //着色器不为空则设置着色器，反之用纯色
        if (isShader && mShader != null) {
            mPaint.setShader(mShader);
        } else {
            mPaint.setColor(mProgressColor);
        }

        float ratio = getRatio();
        if (ratio != 0) {
            mPaint.setStrokeWidth(mProgressStrokeWidth);
            //绘制当前进度弧形
            canvas.drawArc(rectF1, mStartAngle, mSweepAngle * ratio, false, mPaint);
        }

    }

    /**
     * 拖动按钮在最左端时，最上端的X坐标
     *
     * @return
     */
    public float getThumbLeftX() {
        float thumbAngle = mStartAngle;
        mThumbCenterX = (float) (mCircleCenterX + mRadius * Math.cos(Math.toRadians(thumbAngle)));
        return mThumbCenterX;
    }

    /**
     * 拖动按钮在最左端时，最上端的Y坐标
     *
     * @return
     */
    public float getThumbLeftY() {
        float thumbAngle = mStartAngle;
        mThumbCenterY = (float) (mCircleCenterY + mRadius * Math.sin(Math.toRadians(thumbAngle)));
        float offset = (mThumbStrokeWidth - mThumbBorderWidth) / 2;
        return mThumbCenterY - (mThumbRadius + offset + mThumbRadiusEnlarges);
    }

    /**
     * 画滑块
     *
     * @param canvas
     */
    private void drawThumb(Canvas canvas) {
        if (isShowThumb) {


            mPaint.reset();
            mPaint.setAntiAlias(true);

            float thumbAngle = mStartAngle + mSweepAngle * getRatio();
            //已知圆心，半径，角度，求圆上的点坐标
            mThumbCenterX = (float) (mCircleCenterX + mRadius * Math.cos(Math.toRadians(thumbAngle)));
            mThumbCenterY = (float) (mCircleCenterY + mRadius * Math.sin(Math.toRadians(thumbAngle)));


            //绘制滑块
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeWidth(mThumbStrokeWidth);
            mPaint.setColor(mThumbColor);
            if (isCanDrag) {
                canvas.drawCircle(mThumbCenterX, mThumbCenterY, mThumbRadius + mThumbRadiusEnlarges, mPaint);
            } else {
                canvas.drawCircle(mThumbCenterX, mThumbCenterY, mThumbRadius, mPaint);
            }

            //绘制边框
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mThumbBorderWidth);
            mPaint.setColor(mThumbBorderColor);
            float offset = (mThumbStrokeWidth - mThumbBorderWidth) / 2;
            if (isCanDrag) {
                canvas.drawCircle(mThumbCenterX, mThumbCenterY, mThumbRadius + offset + mThumbRadiusEnlarges, mPaint);
            } else {
                canvas.drawCircle(mThumbCenterX, mThumbCenterY, mThumbRadius + offset, mPaint);
            }
        }

    }

    private boolean isTouch;//进度条是否消费事件
    private boolean isEndTouch;//处于一直切换下一曲状态
    private float currentX, currentY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isInArc(event.getX(), event.getY())) {
            return super.onTouchEvent(event);
        }
        currentX = event.getX();
        currentY = event.getY();
        if (isEnabledDrag) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    checkCanDrag(event.getX(), event.getY());
                    if (isCanDrag) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        updateDragThumb(event.getX(), event.getY(), false);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isCanDrag) {
                        updateDragThumb(event.getX(), event.getY(), false);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    if (mOnChangeListener != null) {
                        mOnChangeListener.onStopTrackingTouch(isCanDrag);
                    }
                    isCanDrag = false;
                    invalidate();
                    isTouch = false;
                    break;
            }
        }

        if (isEnabledSingle) {
            mDetector.onTouchEvent(event);
        }
        boolean result = isEnabledSingle || isEnabledDrag || super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN && result) {
            isTouch = true;
        }
        return result;
    }

    /**
     * 判断坐标点是否在弧形上
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInArc(float x, float y) {
        float distance = getDistance(mCircleCenterX, mCircleCenterY, x, y);
        if (Math.abs(distance - mRadius) <= mStrokeWidth / 2f + mAllowableOffsets) {//在圆弧上
            //判断与开始的角度小于可转动的角度即可
            return Math.abs(getTouchDegrees(x, y)) <= Math.abs(mSweepAngle);
        } else {
            return false;
        }
    }

    /**
     * 获取两点间距离
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    @Override
    public void invalidate() {
        if (isEndTouch) {
            return;
        }
        super.invalidate();
    }

    /**
     * 更新多拽进度
     *
     * @param x
     * @param y
     * @param isSingle
     */
    private void updateDragThumb(float x, float y, boolean isSingle) {
        int progress = getProgressForAngle(getTouchDegrees(x, y));
        if (!isSingle) {
            int tempProgressPercent = (int) (progress * 100.0f / mMax);
            //当滑动至至边界值时，增加进度校准机制
            if (mProgressPercent < 10 && tempProgressPercent > 90) {
                progress = 0;
            } else if (mProgressPercent > 90 && tempProgressPercent < 10) {
                progress = mMax;
            }
        }
        setProgress(progress, true);
    }

    /**
     * 通过弧度换算得到当前进度绝对值
     *
     * @param angle
     * @return
     */
    private int getProgressForAngle(float angle) {
        return Math.abs(Math.round(1.0f * mMax / mSweepAngle * angle));
    }

    /**
     * 获取触摸坐标的夹角度数
     *
     * @param x
     * @param y
     * @return
     */
    private float getTouchDegrees(float x, float y) {
        float x1 = x - mCircleCenterX;
        float y1 = y - mCircleCenterY;
        //求触摸点弧形的夹角度数
        float angle = (float) (Math.atan2(y1, x1) * 180 / Math.PI);
        angle -= mStartAngle;
        if (mSweepAngle >= 0) {//顺时针方向
            angle += 360;
        } else {//逆时针方向
            angle += -360;
        }
        angle %= 360;
        Log.d(getClass().getSimpleName(), "getTouchDegrees: " + angle);
        return angle;
    }

    /**
     * 检测是否可拖拽
     *
     * @param x
     * @param y
     */
    private void checkCanDrag(float x, float y) {
        isCanDrag = isInArc(x, y);
        if (mOnChangeListener != null) {
            mOnChangeListener.onStartTrackingTouch(isCanDrag);
        }
        invalidate();
    }

    /**
     * 显示进度动画效果（根据当前已有进度开始）
     *
     * @param progress
     */
    public void showAppendAnimation(int progress) {
        showAnimation(mProgress, progress, mDuration);
    }

    /**
     * 显示进度动画效果
     *
     * @param progress
     */
    public void showAnimation(int progress) {
        showAnimation(progress, mDuration);
    }

    /**
     * 显示进度动画效果
     *
     * @param progress
     * @param duration 动画时长
     */
    public void showAnimation(int progress, int duration) {
        showAnimation(0, progress, duration);
    }

    /**
     * 显示进度动画效果，从from到to变化
     *
     * @param from
     * @param to
     * @param duration 动画时长
     */
    public void showAnimation(int from, int to, int duration) {
        showAnimation(from, to, duration, null);
    }

    /**
     * 显示进度动画效果，从from到to变化
     *
     * @param from
     * @param to
     * @param duration 动画时长
     * @param listener
     */
    public void showAnimation(int from, int to, int duration, Animator.AnimatorListener listener) {
        this.mDuration = duration;
        this.mProgress = from;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.setDuration(duration);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress((int) animation.getAnimatedValue());
            }
        });

        if (listener != null) {
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.addListener(listener);
        }

        valueAnimator.start();
    }

    /**
     * 进度比例
     *
     * @return
     */
    private float getRatio() {
        //最大值为1 最小值为0 如果大于1 则取1
        return Math.min(1.0f, 1.0f * mProgress / mMax);
    }

    /**
     * 设置最大进度
     *
     * @param max
     */
    public void setMax(int max) {
        if (max > 0) {
            this.mMax = max;
            invalidate();
        }
    }

    /**
     * 设置当前进度
     *
     * @param progress
     */
    public void setProgress(int progress) {
        if (isTouch && getTouchDegrees(currentX, currentY) <= mSweepAngle) {
            if (mOnChangeListener != null) {
                mOnChangeListener.onProgressChanged(mMax, mMax, true);
            }
            isEndTouch = true;
            return;
        }
        isEndTouch = false;
        setProgress(progress, false);
    }

    private void setProgress(int progress, boolean fromUser) {
        if (progress < 0) {
            progress = 0;
        } else if (progress > mMax) {
            progress = mMax;
        }
        this.mProgress = progress;
        mProgressPercent = (int) (mProgress * 100.0f / mMax);
        invalidate();

        if (mOnChangeListener != null) {
            mOnChangeListener.onProgressChanged(mProgress, mMax, fromUser);
        }
    }

    /**
     * 设置正常颜色
     *
     * @param color
     */
    public void setNormalColor(int color) {
        this.mNormalColor = color;
        invalidate();
    }


    /**
     * 设置着色器
     *
     * @param shader
     */
    public void setShader(Shader shader) {
        isShader = true;
        this.mShader = shader;
        invalidate();
    }

    /**
     * 设置进度颜色（通过着色器实现渐变色）
     *
     * @param colors
     */
    public void setProgressColor(int... colors) {
        if (isMeasureCircle) {
            Shader shader = new SweepGradient(mCircleCenterX, mCircleCenterX, colors, null);
            setShader(shader);
        } else {
            mShaderColors = colors;
            isShader = true;
        }
    }

    /**
     * 设置进度颜色（纯色）
     *
     * @param color
     */
    public void setProgressColor(int color) {
        isShader = false;
        this.mProgressColor = color;
        invalidate();
    }

    /**
     * 设置进度颜色
     *
     * @param resId
     */
    public void setProgressColorResource(int resId) {
        int color = getResources().getColor(resId);
        setProgressColor(color);
    }


    public int getStartAngle() {
        return mStartAngle;
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    public float getCircleCenterX() {
        return mCircleCenterX;
    }

    public float getCircleCenterY() {
        return mCircleCenterY;
    }

    public float getRadius() {
        return mRadius;
    }

    public int getMax() {
        return mMax;
    }

    public int getProgress() {
        return mProgress;
    }


    public float getThumbRadius() {
        return mThumbRadius;
    }

    public float getThumbCenterX() {
        return mThumbCenterX;
    }

    public float getThumbCenterY() {
        return mThumbCenterY;
    }

    public float getAllowableOffsets() {
        return mAllowableOffsets;
    }

    public boolean isEnabledDrag() {
        return isEnabledDrag;
    }

    public boolean isEnabledSingle() {
        return isEnabledSingle;
    }


    public boolean isShowThumb() {
        return isShowThumb;
    }


    public float getThumbRadiusEnlarges() {
        return mThumbRadiusEnlarges;
    }

    /**
     * 触摸时按钮半径放大量
     *
     * @param thumbRadiusEnlarges
     */
    public void setThumbRadiusEnlarges(float thumbRadiusEnlarges) {
        this.mThumbRadiusEnlarges = thumbRadiusEnlarges;
    }


    /**
     * 是否显示拖动按钮
     *
     * @param showThumb
     */
    public void setShowThumb(boolean showThumb) {
        isShowThumb = showThumb;
        invalidate();
    }

    /**
     * 触摸时可偏移距离：偏移量越大，触摸精度越小
     *
     * @param allowableOffsets
     */
    public void setAllowableOffsets(float allowableOffsets) {
        this.mAllowableOffsets = allowableOffsets;
    }

    /**
     * 是否启用拖拽
     *
     * @param enabledDrag 默认为 true，为 false 时 相当于{@link android.widget.ProgressBar}
     */
    public void setEnabledDrag(boolean enabledDrag) {
        isEnabledDrag = enabledDrag;
    }


    /**
     * 设置是否启用点击改变进度
     *
     * @param enabledSingle
     */
    public void setEnabledSingle(boolean enabledSingle) {
        isEnabledSingle = enabledSingle;
    }


    /**
     * 进度百分比
     *
     * @return
     */
    public int getProgressPercent() {
        return mProgressPercent;
    }


    /**
     * 设置进度改变监听
     *
     * @param onChangeListener
     */
    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.mOnChangeListener = onChangeListener;
    }


    public interface OnChangeListener {
        /**
         * 跟踪触摸事件开始时回调此方法 {@link MotionEvent#ACTION_DOWN}
         *
         * @param isCanDrag
         */
        void onStartTrackingTouch(boolean isCanDrag);

        /**
         * 进度改变时回调此方法
         *
         * @param progress
         * @param max
         * @param fromUser
         */
        void onProgressChanged(float progress, float max, boolean fromUser);

        /**
         * 跟踪触摸事件停止时回调此方法 {@link MotionEvent#ACTION_UP}
         */
        void onStopTrackingTouch(boolean isCanDrag);

        /**
         * 通过点击事件改变进度后回调此方法 {@link GestureDetector#GestureDetector#onSingleTapUp()}
         */
        void onSingleTapUp();
    }

    public abstract static class OnSimpleChangeListener implements OnChangeListener {
        @Override
        public void onStartTrackingTouch(boolean isCanDrag) {

        }

        @Override
        public void onStopTrackingTouch(boolean isCanDrag) {

        }

        @Override
        public void onSingleTapUp() {

        }
    }
}

