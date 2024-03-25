package dev.android.player.framework.utils;

import android.view.View;

/**
 * 防止重复点击
 */
public abstract class OnSingleClickListener implements View.OnClickListener {

    private static final long MIN_CLICK_INTERVAL = 600; // 设置防止点击的时间间隔
    private long mLastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - mLastClickTime) >= MIN_CLICK_INTERVAL) {
            mLastClickTime = currentClickTime;
            onSingleClick(v);
        }
    }
}

