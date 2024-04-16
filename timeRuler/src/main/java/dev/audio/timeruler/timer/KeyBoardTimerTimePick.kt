package dev.audio.timeruler.timer

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.core.view.isVisible
import dev.audio.timeruler.R
import dev.audio.timeruler.databinding.ViewKeyboardTimerPickBinding
import dev.audio.timeruler.utils.dp


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
        orientation = VERTICAL
        gravity = Gravity.CENTER
        binding = ViewKeyboardTimerPickBinding.inflate(LayoutInflater.from(context), this)
    }


    fun setData(time: DialogTimerSetting.Time) { // 将时间戳转换为秒
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
        freshView(time)
    }

    lateinit var cutTime: DialogTimerSetting.CutTime
    fun setTime(cutTime: DialogTimerSetting.CutTime) {
        this.cutTime = cutTime
        freshView(cutTime.time)

    }

    private fun freshView(time: DialogTimerSetting.Time) {
        if (time.hours == 0) {
            binding.hour.isVisible = false
            binding.space1.isVisible = false
        } else {
            binding.hour.isVisible = true
            binding.space1.isVisible = true
            binding.hour.setText(time.hoursStr)
        }

        if (time.minutes == 0) {
            binding.minute.isVisible = false
            binding.space2.isVisible = false
        } else {
            binding.minute.isVisible = true
            binding.space2.isVisible = true
            binding.minute.setText(time.minutesStr)
        }

        if (time.second == 0) {
            binding.second.isVisible = false
            binding.space3.isVisible = false
        } else {
            binding.second.isVisible = true
            binding.space3.isVisible = true
            binding.second.setText(time.secondStr)
        }

        binding.secondDecimal.setText(time.secondDecimalStr)
    }

    fun getTime(): DialogTimerSetting.Time {
        return DialogTimerSetting.Time(getTimeLong())
    }

    private fun getTimeLong(): Long {
        val hour = binding.hour.text.toString().toInt()
        val minute = binding.minute.text.toString().toInt()
        val second = binding.second.text.toString().toInt()
        val secondDecimal = binding.secondDecimal.text.toString().toInt()
        return (hour * 3600L + minute * 60L + second) * 1000 + secondDecimal * 100
    }

    /**
     * 时间选择后监听
     */
    private var mListener: TimerTimePick.OnTimeSelectionListener? = null

    fun setTimeSelectionListener(listener: TimerTimePick.OnTimeSelectionListener?) {
        this.mListener = listener
    }


    fun showErrorTip(isStart: Boolean = true) {
        var errorTips = if (getTime().time == cutTime.maxTime.time) {
            context.resources.getString(R.string.trime_start_end_time_error)
        } else {
            if (isStart) {
                context.resources.getString(R.string.trime_start_time_error)
            } else {
                context.resources.getString(R.string.trime_end_time_error)
            }
        }
        binding.errorTips.text = errorTips
        val anim = TranslateAnimation(0f, 4f.dp, 0f, 0f)
        anim.interpolator = CycleInterpolator(3f)
        anim.duration = 500
        if (binding.errorTips.isVisible) {
            binding.errorTips.startAnimation(anim)
        } else {
            binding.errorTips.visibility = View.VISIBLE
        }
    }


}