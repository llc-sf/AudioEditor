package dev.audio.timeruler.timer

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.core.view.isVisible
import dev.android.player.framework.utils.KeyboardUtil
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
        initView(cutTime)
    }

    private fun initView(time: DialogTimerSetting.CutTime) {
        if (time.maxTime.hours == 0) {
            binding.hour.isVisible = false
            binding.space1.isVisible = false
        } else {
            binding.hour.isVisible = true
            binding.space1.isVisible = true
            binding.hour.setText(time.time.hoursStr)
        }

        if (time.maxTime.minutes == 0) {
            binding.minute.isVisible = false
            binding.space2.isVisible = false
        } else {
            binding.minute.isVisible = true
            binding.space2.isVisible = true
            binding.minute.setText(time.time.minutesStr)
        }

        if (time.maxTime.second == 0) {
            binding.second.isVisible = false
            binding.space3.isVisible = false
        } else {
            binding.second.isVisible = true
            binding.space3.isVisible = true
            binding.second.setText(time.time.secondStr)
        }

        binding.secondDecimal.setText(time.time.secondDecimalStr)


        binding.hour.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (binding.hour.text.length == 1) {
                    binding.hour.setText("0" + binding.hour.text)
                }
            }
        }
        binding.minute.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (binding.minute.text.length == 1) {
                    binding.minute.setText("0" + binding.minute.text)
                }
            }
        }
        binding.second.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (binding.second.text.length == 1) {
                    binding.second.setText("0" + binding.second.text)
                }
            }
        }

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

        if (time.hours == 0 && time.minutes == 0) {
            binding.minute.isVisible = false
            binding.space2.isVisible = false
        } else {
            binding.minute.isVisible = true
            binding.space2.isVisible = true
            binding.minute.setText(time.minutesStr)
        }


        binding.second.isVisible = true
        binding.space3.isVisible = true
        binding.second.setText(time.secondStr)

        binding.secondDecimal.setText(time.secondDecimalStr)
    }

    fun getTime(): DialogTimerSetting.Time {
        return DialogTimerSetting.Time(getTimeLong())
    }

    private fun getTimeLong(): Long {
        val hour = if (TextUtils.isEmpty(binding.hour.text.toString())) 0 else binding.hour.text.toString()
            .toInt()
        val minute = if (TextUtils.isEmpty(binding.minute.text.toString())) 0 else binding.minute.text.toString()
            .toInt()
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


    fun showErrorTip(isStart: Boolean = true) { //        var errorTips = if (getTime().time == cutTime.maxTime.time) {
        //            context.resources.getString(R.string.trime_start_end_time_error)
        //        } else {
        //            if (isStart) {
        //                context.resources.getString(R.string.trime_start_time_error)
        //            } else {
        //                context.resources.getString(R.string.trime_end_time_error)
        //            }
        //        }
        var errorTips = if (isStart) {
            if (getTime().time == cutTime.maxTime.time) {
                context.resources.getString(R.string.trime_start_end_time_error)
            } else if (getTime().time > cutTime.maxTime.time) {
                context.resources.getString(R.string.trime_start_time_error)
            } else if (getTime().time < cutTime.minTime.time) {
                "开始时间过小"
            } else {
                ""
            }

        } else {
            if (getTime().time == cutTime.minTime.time) {
                context.resources.getString(R.string.trime_start_end_time_error)
            } else if (getTime().time > cutTime.maxTime.time) {
                "结束时间过大"
            } else if (getTime().time < cutTime.minTime.time) {
                context.resources.getString(R.string.trime_end_time_error)
            } else {
                ""
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

    fun focous() {
        if (binding.hour.isVisible) {
            binding.hour.requestFocus()
            KeyboardUtil.showKeyBoard(binding.hour)
            binding.hour.setSelection(binding.hour.text.length)
        } else if (binding.minute.isVisible) {
            binding.minute.requestFocus()
            KeyboardUtil.showKeyBoard(binding.minute)
            binding.minute.setSelection(binding.minute.text.length)
        } else if (binding.second.isVisible) {
            binding.second.requestFocus()
            KeyboardUtil.showKeyBoard(binding.second)
            binding.second.setSelection(binding.second.text.length)
        } else {
            binding.secondDecimal.requestFocus()
            KeyboardUtil.showKeyBoard(binding.secondDecimal)
            binding.secondDecimal.setSelection(binding.secondDecimal.text.length)
        }
    }


}