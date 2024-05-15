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
import dev.audio.timeruler.databinding.ViewSleepTimerPickBinding
import dev.audio.timeruler.utils.dp


/**
 * 定时器 时间选择控件
 */
class TimerTimePick @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs), NumberPickerView.OnValueChangeListener,
    NumberPickerView.OnValueChangeListenerInScrolling {

    /**
     * 时间选择后监听
     */
    private var mListener: OnTimeSelectionListener? = null


    private var binding: ViewSleepTimerPickBinding

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        binding = ViewSleepTimerPickBinding.inflate(LayoutInflater.from(context), this)
    }

    private fun initView() {
        binding.hour.setDisplayedValues(getHourDisplayValue())
        binding.hour.minValue = cutTime.minTime.hours
        binding.hour.maxValue = cutTime.maxTime.hours
        binding.hour.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.hour.setOnValueChangedListener(this)
        binding.hour.setOnValueChangeListenerInScrolling(this)
        binding.hour.setItemVerticalSpacing(22.dp)

        binding.minute.setDisplayedValues(getMinuteDisplayValue())
        binding.minute.minValue = if (cutTime.minTime.hours == cutTime.time.hours) cutTime.minTime.minutes else 0
        binding.minute.maxValue = if (cutTime.maxTime.hours == cutTime.time.hours) cutTime.maxTime.minutes else 59
        binding.minute.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.minute.setOnValueChangedListener(this)
        binding.minute.setOnValueChangeListenerInScrolling(this)
        binding.minute.setItemVerticalSpacing(10.dp)

        binding.second.setDisplayedValues(getMinuteDisplayValue())
        binding.second.minValue = if (cutTime.minTime.hours == cutTime.time.hours && cutTime.minTime.minutes == cutTime.time.minutes) cutTime.minTime.second else 0
        binding.second.maxValue = if (cutTime.maxTime.hours == cutTime.time.hours && cutTime.maxTime.minutes == cutTime.time.minutes) cutTime.maxTime.second else 59
        binding.second.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.second.setOnValueChangedListener(this)
        binding.second.setOnValueChangeListenerInScrolling(this)
        binding.second.setItemVerticalSpacing(10.dp)


        binding.msecond.setDisplayedValues(getMsSecondDisplayValue())
        binding.msecond.minValue = if (cutTime.minTime.hours == cutTime.time.hours && cutTime.minTime.minutes == cutTime.time.minutes && cutTime.minTime.second == cutTime.time.second) cutTime.minTime.secondDecimal else 0
        binding.msecond.maxValue = if (cutTime.maxTime.hours == cutTime.time.hours && cutTime.maxTime.minutes == cutTime.time.minutes && cutTime.maxTime.second == cutTime.time.second) cutTime.maxTime.secondDecimal else 9
        binding.msecond.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.msecond.setOnValueChangedListener(this)
        binding.msecond.setOnValueChangeListenerInScrolling(this)
        binding.msecond.setItemVerticalSpacing(10.dp)
    }


    private fun refresh() {
        var oldHour = binding.hour.value
        binding.hour.setDisplayedValues(getHourDisplayValue())
        binding.hour.minValue = cutTime.minTime.hours
        binding.hour.maxValue = cutTime.maxTime.hours
        binding.hour.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.hour.setOnValueChangedListener(this)
        binding.hour.setOnValueChangeListenerInScrolling(this)
        binding.hour.value = oldHour


        var oldMinute = binding.minute.value
        binding.minute.setDisplayedValues(getMinuteDisplayValue())
        binding.minute.minValue = if (cutTime.minTime.hours == binding.hour.value) cutTime.minTime.minutes else 0
        var tempMinuteMaxValue = if (cutTime.maxTime.hours == binding.hour.value) cutTime.maxTime.minutes else 59
        binding.minute.maxValue = maxOf(tempMinuteMaxValue, binding.minute.minValue)
        binding.minute.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.minute.setOnValueChangedListener(this)
        binding.minute.setOnValueChangeListenerInScrolling(this)
        binding.minute.value = oldMinute


        var oldSecond = binding.second.value
        binding.second.setDisplayedValues(getMinuteDisplayValue())
        binding.second.minValue = if (cutTime.minTime.hours == binding.hour.value && cutTime.minTime.minutes == binding.minute.value) cutTime.minTime.second else 0
        var tempMaxSecondValue = if (cutTime.maxTime.hours == binding.hour.value && cutTime.maxTime.minutes == binding.minute.value) cutTime.maxTime.second else 59
        binding.second.maxValue = maxOf(tempMaxSecondValue, binding.second.minValue)
        if (binding.hour.value == cutTime.maxTime.hours) { //最大值处理
            if (cutTime.maxTime.hours == binding.hour.value && cutTime.maxTime.minutes == binding.minute.value && oldSecond > cutTime.maxTime.second) { //策略1：上一位超过  下一位置0
                oldSecond = binding.second.minValue
            } else if (oldSecond > cutTime.maxTime.second) { //秒大于最大  分数最大范围=最大分数-1
                //策略2：下一位超过  上一位置最大值-1
                //binding.minute.maxValue = cutTime.maxTime.minutes - 1
                //binding.minute.value = oldMinute
            } else if (oldSecond == cutTime.maxTime.second) {
                if (cutTime.maxTime.minutes == binding.minute.value && (binding.msecond.value > cutTime.maxTime.secondDecimal)) { //秒等于最大，分数秒大于最大  分数秒归0
                    binding.msecond.value = 0
                }
            }
        }
        if (binding.hour.value == cutTime.minTime.hours) {
            if (cutTime.minTime.hours == binding.hour.value && cutTime.minTime.minutes == binding.minute.value && oldSecond < cutTime.minTime.second) { //策略1：上一位超过  下一位置0
                oldSecond = binding.second.maxValue
            } else if (oldSecond < cutTime.minTime.second) { //策略2：下一位超过  上一位置最小加一
                //                binding.minute.minValue = cutTime.minTime.minutes + 1
                //                binding.minute.value = oldMinute
            } else if (oldSecond == cutTime.minTime.second) {
                if (cutTime.minTime.minutes == binding.minute.value && (binding.msecond.value < cutTime.minTime.secondDecimal)) { //秒等于最大，分数秒大于最大  分数秒归0
                    binding.msecond.value = cutTime.minTime.secondDecimal
                }
            }
        }
        binding.second.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.second.setOnValueChangedListener(this)
        binding.second.setOnValueChangeListenerInScrolling(this)
        binding.second.value = oldSecond


        var oldMsecond = binding.msecond.value
        binding.msecond.setDisplayedValues(getMsSecondDisplayValue())
        binding.msecond.minValue = if (cutTime.minTime.hours == binding.hour.value && cutTime.minTime.minutes == binding.minute.value && cutTime.minTime.second == binding.second.value) cutTime.minTime.secondDecimal else 0
        var tempMaxValue = if (cutTime.maxTime.hours == binding.hour.value && cutTime.maxTime.minutes == binding.minute.value && cutTime.maxTime.second == binding.second.value) cutTime.maxTime.secondDecimal else 9
        binding.msecond.maxValue = maxOf(binding.msecond.minValue,tempMaxValue)
        binding.msecond.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.msecond.setOnValueChangedListener(this)
        binding.msecond.setOnValueChangeListenerInScrolling(this)
        binding.msecond.value = oldMsecond
    }

    private fun getHourDisplayValue(): Array<String> {
        return mutableListOf<String>().apply {
            for (i in 0 until 24) {
                add(String.format("%02d", i))
            }
        }.toTypedArray()
    }

    private fun getMinuteDisplayValue(): Array<String> {
        return mutableListOf<String>().apply {
            for (i in 0 until 60) {
                add(String.format("%02d", i))
            }
        }.toTypedArray()
    }

    private fun getMsSecondDisplayValue(): Array<String> {
        return mutableListOf<String>().apply {
            for (i in 0 until 10) {
                add(i.toString())
            }
        }.toTypedArray()
    }


    override fun onValueChange(picker: NumberPickerView?, oldVal: Int, newVal: Int) {
        refresh()
        mListener?.onSelection(DialogTimerSetting.Time(getTime()))
        performHapticFeedback(picker)
    }

    /**
     * 返回选择时间的毫秒数
     */
    fun getTime(): Long {
        return (binding.hour.value * 3600L + binding.minute.value * 60L + binding.second.value) * 1000 + binding.msecond.value * 100
    }

    fun getTimeBean(): DialogTimerSetting.Time {
        return DialogTimerSetting.Time((binding.hour.value * 3600L + binding.minute.value * 60L + binding.second.value) * 1000 + binding.msecond.value * 100)
    }

    fun getTime(hour: Int, minute: Int, second: Int, msecond: Int): Long {
        return (hour * 3600L + minute * 60L + second) * 1000 + msecond * 100
    }

    fun setTimeSelectionListener(listener: OnTimeSelectionListener?) {
        this.mListener = listener
    }

    private lateinit var cutTime: DialogTimerSetting.CutTime


    fun setTime(cutTime: DialogTimerSetting.CutTime) {
        this.cutTime = cutTime
        initView()
        freshTime(cutTime.time)
    }

    fun timeAvi(time: DialogTimerSetting.Time): Boolean {
        return time.time >= cutTime.minTime.time && time.time <= cutTime.maxTime.time
    }

    fun freshTime(time: DialogTimerSetting.Time) {
        if (timeAvi(time)) {
            binding.hour.value = time.hours
            binding.minute.value = time.minutes
            binding.second.value = time.second
            binding.msecond.value = time.secondDecimal
            if (cutTime.maxTime.hours == 0) {
                binding.hour.visibility = View.GONE
                binding.space1.visibility = View.GONE
            }
        }
    }


    /**
     * 时间选择监听
     */
    interface OnTimeSelectionListener {
        fun onSelection(time: DialogTimerSetting.Time)
    }

    override fun onValueChangeInScrolling(picker: NumberPickerView?, oldVal: Int, newVal: Int) {
        performHapticFeedback(picker)
    }

    /**
     *
     */
    private fun performHapticFeedback(view: View?) {
        try {
            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setData(time: DialogTimerSetting.Time) {

    }


}