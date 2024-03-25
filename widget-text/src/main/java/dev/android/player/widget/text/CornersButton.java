package dev.android.player.widget.text;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.ThemeUtils;
import androidx.core.content.res.ResourcesCompat;


/**
 * 圆角Button
 */
public class CornersButton extends AppCompatTextView {

    private int mCornerRadius = 0;

    @ColorInt
    private int mCornerSolidColor = Color.TRANSPARENT;

    @ColorInt
    private int mCornerStrokeColor = Color.TRANSPARENT;

    private int mCornerStrokeWidth = 0;

    @ColorInt
    private int mCornerRippleColor = Color.TRANSPARENT;

    private boolean mIsUseDrawable = false;//是否使用drawable

    private int mConnerDrawableId = -1;//drawable id


    public CornersButton(Context context) {
        this(context, null, 0);
    }

    public CornersButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornersButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    private void initView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CornersButton);
        mIsUseDrawable = typedArray.hasValue(R.styleable.CornersButton_corner_btn_drawable);//是否使用drawable
        mConnerDrawableId = typedArray.getResourceId(R.styleable.CornersButton_corner_btn_drawable, -1);
        mCornerSolidColor = typedArray.getColor(R.styleable.CornersButton_conner_btn_solid_color, Color.TRANSPARENT);
        mCornerStrokeColor = typedArray.getColor(R.styleable.CornersButton_conner_btn_stroke_color, Color.TRANSPARENT);
        mCornerStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CornersButton_conner_btn_stroke_width, 0);
        mCornerRippleColor = typedArray.getColor(R.styleable.CornersButton_conner_btn_ripple_color, 0);
        mCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CornersButton_conner_btn_radius, 0);

        boolean isCenter = typedArray.getBoolean(R.styleable.CornersButton_conner_btn_is_center, true);

        setButtonBackground();
        typedArray.recycle();
        setClickable(true);
        if (isCenter) {
            setGravity(Gravity.CENTER);
        } else {
            setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }
    }


    private void setButtonBackground() {
        if (mIsUseDrawable && mConnerDrawableId != -1) {
            setBackgroundResource(mConnerDrawableId);
        } else {
            Drawable bg = ConnerRippleHelper.getRippleDrawable(mCornerSolidColor, mCornerStrokeColor, mCornerStrokeWidth, mCornerRippleColor, mCornerRadius);
            setBackgroundDrawable(bg);
        }
    }

    public void setCornerRadius(int radius) {
        this.mCornerRadius = radius;
        setButtonBackground();
    }

    public void setCornerSolidColorRes(@ColorRes int color) {
        setCornerSolidColor(getResources().getColor(color));
    }

    public void setCornerSolidColor(@ColorInt int color) {
        mCornerSolidColor = color;
        setButtonBackground();
    }

    public void setCornerStrokeColorRes(@ColorRes int color) {
        setCornerStrokeColor(getResources().getColor(color));
    }

    public void setCornerStrokeColor(@ColorInt int color) {
        mCornerStrokeColor = color;
        setButtonBackground();
    }

    public void setCornerStrokeWidth(int width) {
        this.mCornerStrokeWidth = width;
        setButtonBackground();
    }


    public void setCornerRippleColorRes(@ColorRes int color) {
        setCornerRippleColor(getResources().getColor(color));
    }

    public void setCornerRippleColor(@ColorInt int color) {
        mCornerRippleColor = color;
        setButtonBackground();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }


    private static class ConnerRippleHelper {

        /**
         * @param solid
         * @param border
         * @param width
         * @param mask
         * @param radius
         * @return
         */
        public static final Drawable getRippleDrawable(int solid, int border, int width, int mask, float radius) {
            GradientDrawable normal = new GradientDrawable();
            normal.setColor(solid);
            normal.setStroke(width, border);
            normal.setCornerRadius(radius);

            GradientDrawable ripple = new GradientDrawable();
            ripple.setColor(mask);
            ripple.setCornerRadius(radius);

            ColorStateList colors = ColorStateList.valueOf(mask);

            return new RippleDrawable(colors, normal, ripple);

        }
    }

}
