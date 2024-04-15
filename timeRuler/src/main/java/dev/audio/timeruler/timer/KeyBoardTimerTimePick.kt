package dev.audio.timeruler.timer

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
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


    fun setData(time: Long) { // 将时间戳转换为秒
        //        val totalSeconds = time / 1000.0
        //        val hours = (totalSeconds / 3600).toInt()
        //        val minutes = ((totalSeconds % 3600) / 60).toInt()
        //        val seconds = (totalSeconds % 60).toInt()
        //        val decimalPart = ((totalSeconds % 60) * 10).toInt() % 10  // 取第一个小数位
        //
        //        binding.hour.setText(if (hours < 10) "0$hours" else hours.toString())
        //        binding.minute.setText(if (minutes < 10) "0$minutes" else minutes.toString())
        //        binding.second.setText(if (seconds < 10) "0$seconds" else seconds.toString())
        //        binding.secondDecimal.setText(decimalPart.toString())
    }

    fun setTime(time: DialogTimerSetting.CutTime) {
        if (time.time.hours == 0) {
            binding.hour.isVisible = false
            binding.space1.isVisible = false
        } else {
            binding.hour.isVisible = true
            binding.space1.isVisible = true
            binding.hour.setText(time.time.hoursStr)
        }

        if (time.time.minutes == 0) {
            binding.minute.isVisible = false
            binding.space2.isVisible = false
        } else {
            binding.minute.isVisible = true
            binding.space2.isVisible = true
            binding.minute.setText(time.time.minutesStr)
        }

        if(time.time.second == 0) {
            binding.second.isVisible = false
            binding.space3.isVisible = false
        } else {
            binding.second.isVisible = true
            binding.space3.isVisible = true
            binding.second.setText(time.time.secondStr)
        }

        binding.secondDecimal.setText(time.time.secondDecimalStr)

    }
}