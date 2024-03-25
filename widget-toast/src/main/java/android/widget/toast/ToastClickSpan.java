package android.widget.toast;

import android.text.TextPaint;

import androidx.annotation.NonNull;

public class ToastClickSpan extends ToastTouchBaseSpan {

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}
