package dev.audio.timeruler;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import android.util.AttributeSet;
import android.util.Log;


import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;


import java.text.SimpleDateFormat;

import dev.audio.timeruler.utils.SizeUtils;

public class TimeRulerBar extends BaseScaleBar implements BaseScaleBar.TickMarkStrategy {


    private Paint mTickPaint;
    private Paint mColorCursorPaint;
    private float mTriangleHeight = 10;
    private int tickValueColor;
    private float tickValueSize;
    private int cursorBackgroundColor;
    private float cursorValueSize;
    private final int colorScaleBackground;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    float tickValueBoundOffsetH = 20;
    private float videoAreaHeight;
    private float videoAreaOffset;
    private boolean drawCursorContent;

    public TimeRulerBar(Context context) {
        this(context, null);
    }

    public TimeRulerBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeRulerBar);
        videoAreaHeight = typedArray.getDimension(R.styleable.TimeRulerBar_videoAreaHeight, SizeUtils.sp2px(getContext(), 20));
        videoAreaOffset = typedArray.getDimension(R.styleable.TimeRulerBar_videoAreaOffset, SizeUtils.sp2px(getContext(), 0));
        tickValueColor = typedArray.getColor(R.styleable.TimeRulerBar_tickValueColor, Color.BLACK);
        tickValueSize = typedArray.getDimension(R.styleable.TimeRulerBar_tickValueSize, SizeUtils.sp2px(getContext(), 8));
        cursorBackgroundColor = typedArray.getColor(R.styleable.TimeRulerBar_cursorBackgroundColor, Color.RED);
        cursorValueSize = typedArray.getDimension(R.styleable.TimeRulerBar_cursorValueSize, SizeUtils.sp2px(getContext(), 10));
        colorScaleBackground = typedArray.getColor(R.styleable.TimeRulerBar_colorScaleBackground, Color.WHITE);
        drawCursorContent = typedArray.getBoolean(R.styleable.TimeRulerBar_drawCursorContent, true);
        typedArray.recycle();
        init();
    }

    void init() {
        tickValueBoundOffsetH = SizeUtils.dp2px(getContext(), 6);

        mTickPaint = new Paint();
        mTickPaint.setColor(tickValueColor);
        mTickPaint.setAntiAlias(true);
        mTickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTickPaint.setTextAlign(Paint.Align.CENTER);
        mTickPaint.setTextSize(tickValueSize);
        mTickPaint.setStrokeWidth(1);
        mTickPaint.setDither(true);

        mColorCursorPaint = new Paint();
        mColorCursorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mColorCursorPaint.setDither(true);

        setTickMarkStrategy(this);
    }

    public void setMode(@Mode String mode) {
        setMode(mode, true);
    }

    @Override
    public void setRange(long start, long end) {
        super.setRange(start, end);
        setMode(mMode, true);
    }

    public void setScreenSpanValue(long screenSpanValue) {
        this.minScreenSpanValue = screenSpanValue;
    }

    public void setMode(@Mode String m, boolean setScaleRatio) {
        long spanValue;
        switch (m) {
            case MODE_UINT_100_MS:
                this.mMode = m;
                updateScaleInfo(VALUE_100_MS * 5, VALUE_100_MS);
                spanValue = MODE_UINT_100_MS_VALUE;
                break;
            case MODE_UINT_500_MS:
                this.mMode = m;
                updateScaleInfo(VALUE_500_MS * 5, VALUE_500_MS);
                spanValue = MODE_UINT_500_MS_VALUE;
                break;
            case MODE_UINT_1000_MS:
                this.mMode = m;
                updateScaleInfo(VALUE_1000_MS * 5, VALUE_1000_MS);
                spanValue = MODE_UINT_1000_MS_VALUE;
                break;
            case MODE_UINT_2000_MS:
                this.mMode = m;
                updateScaleInfo(VALUE_2000_MS * 5, VALUE_2000_MS);
                spanValue = MODE_UINT_2000_MS_VALUE;
                break;
            case MODE_UINT_3000_MS:
                this.mMode = m;
                updateScaleInfo(VALUE_3000_MS * 5, VALUE_3000_MS);
                spanValue = MODE_UINT_3000_MS_VALUE;
                break;
            case MODE_UINT_6000_MS:
                this.mMode = m;
                updateScaleInfo(VALUE_6000_MS * 5, VALUE_6000_MS);
                spanValue = MODE_UINT_6000_MS_VALUE;
                break;
            default:
                throw new RuntimeException("not support mode: " + m);
        }
//        unitPixel = getWidth() * 1f / spanValue;
        Log.e("TAG", "unitPixel: " + unitPixel);
        if (setScaleRatio) {
            setScaleRatio(getMinScreenSpanValue() * 1.0f / spanValue);
        }
        invalidate();
    }

    @Override
    public boolean disPlay(long scaleValue, boolean keyScale) {
        return keyScale;
    }

    @NonNull
    @Override
    public String getScaleValue(long scaleValue, boolean keyScale) {
        String formattedTime = simpleDateFormat.format(scaleValue);
        // 解析天、小时、分钟和秒
        String[] parts = formattedTime.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        // 转换为秒
        return String.valueOf((hours * 3600) + (minutes * 60) + seconds) + "s";
    }

    @Override
    public int getColor(long scaleValue, boolean keyScale) {
        return tickValueColor;
    }

    @Override
    public float getSize(long scaleValue, boolean keyScale, float maxScaleValueSize) {
        return tickValueSize;
    }

    @Override
    protected void onEndTickDraw(Canvas canvas) {
        int startLimit = getScrollX();
        int endLimit = getScrollX() + getWidth();
        float startY = videoAreaOffset;
        float endY = videoAreaHeight + videoAreaOffset;
        // ① 绘制背景
        mColorCursorPaint.setColor(colorScaleBackground);
        canvas.drawRect(startLimit, startY, endLimit, endY, mColorCursorPaint);
        //  绘制颜色刻度尺
        if (null != mColorScale) {
            float cursorPosition = getCursorPosition();
            long cursorValue = getCursorValue();
            float unitPixel = getUnitPixel();
            int size = mColorScale.getSize();
            RectF rect = new RectF();
            rect.top = startY;
            rect.bottom = endY;
            long startValue;
            long endValue;
            float startPiexl;
            float endPiexl;
            // ② 绘制颜色刻度
            for (int i = 0; i < size; i++) {
                startValue = mColorScale.getStart(i);
                endValue = mColorScale.getEnd(i);
                startPiexl = cursorPosition + (startValue - cursorValue) * unitPixel;
                endPiexl = cursorPosition + (endValue - cursorValue) * unitPixel;
                if (endPiexl < startLimit) {
                    continue;
                }
                if (startPiexl > endLimit) {
                    continue;
                }

                rect.left = startPiexl;
                rect.right = endPiexl;

                mColorCursorPaint.setColor(mColorScale.getColor(i));
                canvas.drawRect(rect, mColorCursorPaint);
            }
        }
    }

    @Override
    protected int calcContentHeight(float baselinePositionProportion) {
        int contentHeight = super.calcContentHeight(baselinePositionProportion);

        mColorCursorPaint.setTextSize(cursorValueSize);
        Paint.FontMetrics fontMetrics = mColorCursorPaint.getFontMetrics();
        double ceil = Math.ceil(fontMetrics.bottom - fontMetrics.top);
        int cursorValueHeight = (int) (ceil + mTriangleHeight + tickValueBoundOffsetH) + 5;
        int cursorContentHeight = (int) ((getKeyTickHeight() + cursorValueHeight) / baselinePositionProportion + 0.5f);
        return Math.max(contentHeight, cursorContentHeight);
    }

    @Override
    protected void onScale(ScaleMode info, float unitPixel) {
        int width = getWidth();
        // 计算一屏刻度值跨度
        float screenSpanValue = width / unitPixel;
        updateMode(screenSpanValue);
    }

    public void setShowCursor(boolean isShowCursorContent) {
        drawCursorContent = isShowCursorContent;
        invalidate();
    }

    public void setVideoAreaOffset(int progress) {
        videoAreaOffset = progress;
        invalidate();
    }

    protected void updateMode(float screenSpanValue) {
        Log.i("TAG", "updateMode: " + screenSpanValue);
        if (screenSpanValue >= MODE_UINT_6000_MS_VALUE) {
            setMode(MODE_UINT_6000_MS, false);
        } else if (screenSpanValue >= MODE_UINT_3000_MS_VALUE) {
            setMode(MODE_UINT_3000_MS, false);
        } else if (screenSpanValue >= MODE_UINT_2000_MS_VALUE) {
            setMode(MODE_UINT_2000_MS, false);
        } else if (screenSpanValue >= MODE_UINT_1000_MS_VALUE) {
            setMode(MODE_UINT_1000_MS, false);
        } else if (screenSpanValue >= MODE_UINT_500_MS_VALUE) {
            setMode(MODE_UINT_500_MS, false);
        } else {
            setMode(MODE_UINT_100_MS, false);
        }
    }

    SimpleDateFormat cursorDateFormat = new SimpleDateFormat("HH:mm:ss");

    /*可自行绘制浮标*/
    @Override
    protected void drawCursor(Canvas canvas, float cursorPosition, long cursorValue) {
        super.drawCursor(canvas, cursorPosition, cursorValue);
        if (!drawCursorContent) return;
        float keyTickHeight = getKeyTickHeight();
        float baselinePosition = getBaselinePosition();
        // ①绘制倒三角
        Path path = new Path();
        float startX = cursorPosition;
        float statY = baselinePosition - keyTickHeight;
        // 倒三角形顶边的 y
        float topSidePosition = statY - mTriangleHeight;
        path.moveTo(startX, statY);
        path.lineTo(startX - 3.5f, topSidePosition);
        path.lineTo(startX + 3.5f, topSidePosition);
        path.close();
        mTickPaint.setColor(cursorBackgroundColor);
        canvas.drawPath(path, mTickPaint);

        String content = cursorDateFormat.format(cursorValue);
        Rect textBound = new Rect();
        mTickPaint.setTextSize(cursorValueSize);
        // 测量内容大小
        mTickPaint.getTextBounds(content, 0, content.length(), textBound);

        // ②绘制内容背景
        // 创建包裹内容的背景大小
        RectF rectF = new RectF(0, 0, textBound.width() + 20, textBound.height() + tickValueBoundOffsetH);
        // 背景位置
        // x方向： 关于游标居中  y方向:在倒三角形上边
        rectF.offset(cursorPosition - rectF.width() * 0.5f, topSidePosition + 0.5f - rectF.height());
        float rx = rectF.width() * 0.5f;
        float ry = rx;
        mTickPaint.setColor(cursorBackgroundColor);
        canvas.drawRoundRect(rectF, rx, ry, mTickPaint);

        mTickPaint.setColor(tickValueColor);
        // ③ 绘制内容
        // 使内容绘制在背景内,达到包裹效果
        float textY = rectF.centerY() + textBound.height() * 0.5f;
        canvas.drawText(content, cursorPosition, textY, mTickPaint);
    }

    private ColorScale mColorScale;

    public void setColorScale(ColorScale scale) {
        this.mColorScale = scale;
    }

    public interface ColorScale {
        /*需要绘制的颜色数量*/
        int getSize();

        /*开始时间*/
        long getStart(int index);

        /*结束时间*/
        long getEnd(int index);

        /*绘制的颜色*/
        @ColorInt
        int getColor(int index);
    }


}