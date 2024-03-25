package dev.android.player.widget.misc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ChatGPT on 2023/3/22
 *
 * @author ChatGPT
 */
public class CircleProgressBar extends View {

    private final SweepGradient mSweepGradient;
    /**
     * 用于绘制进度条的画笔
     */
    private Paint mProgressPaint;

    /**
     * 用于绘制进度条背景的画笔
     */
    private Paint mBackgroundPaint;

    /**
     * 进度条所在矩形区域
     */
    private RectF mProgressRect;

    /**
     * 进度条背景所在矩形区域
     */
    private RectF mBackgroundRect;

    /**
     * 进度条所在矩形区域的半径
     */
    private float mProgressBarWidth;

    /**
     * 进度条背景所在矩形区域的半径
     */
    private float mProgressStokeWidth;

    /**
     * 进度条颜色
     */
    private int mProgressColor;

    /**
     * 进度条渐变色开始颜色
     */
    private int mProgressStartColor;

    /**
     * 进度条渐变色结束颜色
     */
    private int mProgressEndColor;

    /**
     * 进度条背景颜色
     */
    private int mProgressBackgroundColor;

    /**
     * 进度条背景宽度
     */
    private float mProgressBackgroundWidth;

    /**
     * 进度条最大进度值
     */
    private float mMaxProgress;

    /**
     * 进度条当前进度值
     */
    private float mCurrentProgress;

    /**
     * 进度条两端是否带圆角
     */
    private boolean mIsCapRound;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 从属性集合中读取自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyleAttr, 0);
        mProgressColor = a.getColor(R.styleable.CircleProgressBar_circle_progress_color, Color.BLACK);
        mProgressStartColor = a.getColor(R.styleable.CircleProgressBar_circle_progress_start_color, Color.BLACK);
        mProgressEndColor = a.getColor(R.styleable.CircleProgressBar_circle_progress_end_color, Color.BLACK);
        mProgressStokeWidth = a.getDimension(R.styleable.CircleProgressBar_circle_progress_stoke_width, 10);
        mProgressBarWidth = a.getDimension(R.styleable.CircleProgressBar_circle_progress_bar_width, mProgressStokeWidth);
        mProgressBackgroundColor = a.getColor(R.styleable.CircleProgressBar_circle_progress_background_color, Color.GRAY);
        mProgressBackgroundWidth = a.getDimension(R.styleable.CircleProgressBar_circle_progress_background_width, mProgressBarWidth);
        mMaxProgress = a.getFloat(R.styleable.CircleProgressBar_circle_progress_bar_max_progress, 100);
        mCurrentProgress = a.getFloat(R.styleable.CircleProgressBar_circle_progress_bar_progress, 0);
        mIsCapRound = a.getBoolean(R.styleable.CircleProgressBar_circle_progress_bar_cap, false);
        a.recycle();

        // 初始化画笔
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStrokeCap(mIsCapRound ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        // 初始化画笔
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressBarWidth);


        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mProgressBackgroundWidth);
        mBackgroundPaint.setColor(mProgressBackgroundColor);

        // 初始化所在矩形区域
        mProgressRect = new RectF();
        mBackgroundRect = new RectF();

        // 初始化颜色渐变
        int[] colors = {mProgressStartColor, mProgressEndColor};
        float[] positions = {0f, 1f};
        mSweepGradient = new SweepGradient(getWidth() / 2f, getHeight() / 2f, colors, positions);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算进度条所在矩形区域
        float half = Math.max(mProgressBarWidth, mProgressBackgroundWidth) / 2f;
        mProgressRect.set(half, half, getWidth() - half, getHeight() - half);

        // 计算进度条背景所在矩形区域
        mBackgroundRect.set(half, half, getWidth() - half, getHeight() - half);

        // 绘制进度条背景
        mBackgroundPaint.setColor(mProgressBackgroundColor);
        mBackgroundPaint.setStrokeWidth(mProgressBackgroundWidth);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(mBackgroundRect, 0, 360, false, mBackgroundPaint);

        // 绘制进度条
        mProgressPaint.setStrokeWidth(mProgressBarWidth);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        // 如果渐变色开始颜色和结束颜色相同，则使用单色
        if (mProgressStartColor == mProgressEndColor) {
            mProgressPaint.setColor(mProgressColor);
            mProgressPaint.setShader(null);
        } else {
            // 绘制进度条
            mProgressPaint.setShader(mSweepGradient);
        }
        // 绘制进度条
        float sweepAngle = mCurrentProgress / mMaxProgress * 360f;
        mProgressPaint.setStrokeCap(mIsCapRound ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        canvas.drawArc(mProgressRect, -90, sweepAngle, false, mProgressPaint);
    }

    /**
     * 设置进度条最大进度值
     *
     * @param maxProgress 最大进度值
     */
    public void setMaxProgress(float maxProgress) {
        /**
         * 如果最大进度值小于等于0，则不设置
         */
        if (maxProgress <= 0) {
            return;
        }
        mMaxProgress = maxProgress;
        invalidate();
    }

    /**
     * 设置进度条当前进度值
     *
     * @param progress 当前进度值
     */
    public void setProgress(float progress) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > mMaxProgress) {
            progress = mMaxProgress;
        }
        mCurrentProgress = progress;
        invalidate();
    }
}

