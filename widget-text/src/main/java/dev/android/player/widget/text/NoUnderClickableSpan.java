package dev.android.player.widget.text;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

public class NoUnderClickableSpan extends ClickableSpan {

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
       super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(@NonNull View widget) {

    }
}
