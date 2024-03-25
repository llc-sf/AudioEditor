package dev.android.player.widget.text;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class EditText extends androidx.appcompat.widget.AppCompatEditText {

    private BackKeyListener mBackKeyListener;

    public EditText(Context context) {
        super(context);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBackKeyListener(BackKeyListener backKeyListener) {
        this.mBackKeyListener = backKeyListener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

            if (mBackKeyListener != null) {
                return mBackKeyListener.onBackPressed();
            }
        }

        return super.onKeyPreIme(keyCode, event);
    }

    public interface BackKeyListener {
        boolean onBackPressed();
    }
}
