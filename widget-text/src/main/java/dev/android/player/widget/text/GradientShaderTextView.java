package dev.android.player.widget.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 实现渐变色的TextView
 */
public class GradientShaderTextView extends AppCompatTextView {

    private int[] mShaderColors;//渐变颜色
    @ShaderDirection
    private int mShaderDirection;//渐变方向
    private LinearGradient mLinearGradient;//线性渐变
    private Rect mRect = new Rect();

    public GradientShaderTextView(@NonNull Context context) {
        this(context, null);
    }

    public GradientShaderTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradientShaderTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientShaderTextView);
        mShaderDirection = a.getInt(R.styleable.GradientShaderTextView_gradient_orientation, ShaderDirection.LEFT_RIGHT);
        if (a.hasValue(R.styleable.GradientShaderTextView_gradient_colors)) {
            int resId = a.getResourceId(R.styleable.GradientShaderTextView_gradient_colors, 0);
            if (resId != 0) {
                mShaderColors = getResources().getIntArray(resId);
            }
        } else if (a.hasValue(R.styleable.GradientShaderTextView_gradient_start) && a.hasValue(R.styleable.GradientShaderTextView_gradient_start)) {
            int startColor = a.getColor(R.styleable.GradientShaderTextView_gradient_start, Color.WHITE);
            int endColor = a.getColor(R.styleable.GradientShaderTextView_gradient_end, Color.BLACK);
            mShaderColors = new int[]{startColor, endColor};
        }
        a.recycle();
    }


    /**
     * 设置渐变色
     *
     * @param shaderColors 渐变色
     */
    public void setShaderColors(int[] shaderColors) {
        mShaderColors = shaderColors;
    }

    public void setShaderDirection(int mShaderDirection) {
        this.mShaderDirection = mShaderDirection;
    }

    private void prepareShader() {
        if (mShaderColors == null) {
            return;
        }
        switch (mShaderDirection) {
            case ShaderDirection.LEFT_RIGHT:
                mRect.set(0, 0, getWidth(), 0);
                break;
            case ShaderDirection.TOP_BOTTOM:
                mRect.set(0, 0, 0, getHeight());
                break;
            case ShaderDirection.RIGHT_LEFT:
                mRect.set(getWidth(), 0, 0, 0);
                break;
            case ShaderDirection.BOTTOM_TOP:
                mRect.set(0, getHeight(), 0, 0);
                break;
            case ShaderDirection.LEFT_TOP_RIGHT_BOTTOM:
                mRect.set(0, 0, getWidth(), getHeight());
                break;
            case ShaderDirection.RIGHT_TOP_LEFT_BOTTOM:
                mRect.set(getWidth(), 0, 0, getHeight());
                break;
            case ShaderDirection.LEFT_BOTTOM_RIGHT_TOP:
                mRect.set(0, getHeight(), getWidth(), 0);
                break;
            case ShaderDirection.RIGHT_BOTTOM_LEFT_TOP:
                mRect.set(getWidth(), getHeight(), 0, 0);
                break;
        }
        mLinearGradient = new LinearGradient(mRect.left, mRect.top, mRect.right, mRect.bottom, mShaderColors, null, LinearGradient.TileMode.CLAMP);
        getPaint().setShader(mLinearGradient);
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0 && (w != oldw || h != oldh)) {
            prepareShader();
        }
    }

    @IntDef({ShaderDirection.LEFT_RIGHT,
            ShaderDirection.TOP_BOTTOM,
            ShaderDirection.RIGHT_LEFT,
            ShaderDirection.BOTTOM_TOP,
            ShaderDirection.LEFT_TOP_RIGHT_BOTTOM,
            ShaderDirection.RIGHT_TOP_LEFT_BOTTOM,
            ShaderDirection.LEFT_BOTTOM_RIGHT_TOP,
            ShaderDirection.RIGHT_BOTTOM_LEFT_TOP})
    public @interface ShaderDirection {
        int LEFT_RIGHT = 0;//从左到右
        int TOP_BOTTOM = 1;//从上到下
        int RIGHT_LEFT = 2;//从右到左
        int BOTTOM_TOP = 3;//从下到上
        int LEFT_TOP_RIGHT_BOTTOM = 4;//从左上到右下
        int RIGHT_TOP_LEFT_BOTTOM = 5;//从右上到左下
        int LEFT_BOTTOM_RIGHT_TOP = 6;//从左下到右上
        int RIGHT_BOTTOM_LEFT_TOP = 7;//从右下到左上
    }
}
