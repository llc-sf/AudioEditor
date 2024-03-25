package dev.android.player.framework.utils;

import android.view.View;

public class DebounceClickListener implements View.OnClickListener {
    private static final long DEBOUNCE_TIME = 500;
    private long lastClickTime = 0;
    private final View.OnClickListener mListener;

    public DebounceClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (System.currentTimeMillis() - lastClickTime > DEBOUNCE_TIME) {
            lastClickTime = System.currentTimeMillis();
            mListener.onClick(v);
        }
    }
}
