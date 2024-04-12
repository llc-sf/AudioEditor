package dev.audio.timeruler.timer

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import dev.audio.timeruler.databinding.ViewKeyboardTimerPickBinding
import dev.audio.timeruler.databinding.ViewSleepTimerPickBinding


/**
 * 定时器 时间选择控件
 */
class KeyBoardTimerTimePick @JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    /**
     * 时间选择后监听
     */


    private var binding: ViewKeyboardTimerPickBinding

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        binding = ViewKeyboardTimerPickBinding.inflate(LayoutInflater.from(context), this)


    }


}