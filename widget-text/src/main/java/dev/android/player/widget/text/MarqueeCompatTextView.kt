package dev.android.player.widget.text

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * 跑马灯效果的TextView
 * 默认开启跑马灯效果,焦点一直开启
 */
class MarqueeCompatTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    override fun isFocused(): Boolean {
        return true
    }
}