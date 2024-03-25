package android.widget.toast;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;


/**
 * 自定义弹出Toast View
 */
public class ToastView extends RelativeLayout {

    private final TextView mTitle;

    private final ImageView mIcon;

    private final int mDrawableSuccess;

    private final int mDrawableError;

    private int mScreenMargin;

    private int maxViewWidth; // 最大宽度


    public ToastView(Context context) {
        this(context, null);
    }

    public ToastView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.CustomToastCompatStyle);
    }

    public ToastView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.toast_view, this);

        int verticalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
        int horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        mScreenMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());


        mTitle = findViewById(R.id.title);
        mTitle.setMovementMethod(ToastTouchableMovementMethod.getInstance());
        mIcon = findViewById(R.id.icon);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ToastView, defStyleAttr, R.style.ToastCompatStyle);
        mDrawableError = array.getResourceId(R.styleable.ToastView_toast_error_drawable, -1);
        mDrawableSuccess = array.getResourceId(R.styleable.ToastView_toast_success_drawable, -1);
        mScreenMargin = array.getDimensionPixelSize(R.styleable.ToastView_toast_margin, mScreenMargin);

        maxViewWidth = context.getResources().getDisplayMetrics().widthPixels - mScreenMargin * 2;

        //获取背景设置
        int color = array.getColor(R.styleable.ToastView_toast_solid_color, Color.BLACK);
        float radius = array.getDimensionPixelSize(R.styleable.ToastView_toast_radius, 0);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        setBackground(drawable);

        //文字设置
        int appearance = array.getResourceId(R.styleable.ToastView_toast_text_appearance, R.style.ToastCompatStyleTexAppearance);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTitle.setTextAppearance(appearance);
        } else {
            mTitle.setTextAppearance(getContext(), appearance);
        }
        int textSize = array.getDimensionPixelSize(R.styleable.ToastView_toast_text_size, 14);
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        int textColor = array.getColor(R.styleable.ToastView_toast_text_color, Color.WHITE);
        mTitle.setTextColor(textColor);

        array.recycle();

    }

    public void setTitle(@StringRes int res) {
        mTitle.setText(res);
    }

    public void setTitle(CharSequence text) {
        mTitle.setText(text);
    }

    public void setSuccess(boolean isSuccess) {
        mIcon.setImageResource(isSuccess ? mDrawableSuccess : mDrawableError);
    }

    public void showIcon(boolean show) {
        if (mIcon != null) {
            mIcon.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void setIcon(int iconRes) {
        mIcon.setImageResource(iconRes);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        // 如果视图的宽度超过最大宽度限制，就将宽度设置为最大宽度
        if (maxViewWidth > 0 && widthSize > maxViewWidth) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxViewWidth, MeasureSpec.getMode(widthMeasureSpec));
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    /**
     * 获取屏幕边距
     *
     * @return
     */
    public float getScreenMargin() {
        return mScreenMargin;
    }


}
