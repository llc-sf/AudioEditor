package dev.audio.timeruler.timer

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import dev.audio.timeruler.databinding.ViewSleepTimerPickBinding


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
        binding.hour.minValue = 0
        binding.hour.maxValue = cutTime.maxTime.hours
        binding.hour.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.hour.setOnValueChangedListener(this)
        binding.hour.setOnValueChangeListenerInScrolling(this)

        binding.minute.setDisplayedValues(getMinuteDisplayValue())
        binding.minute.minValue = 0
        binding.minute.maxValue = 59
        binding.minute.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.minute.setOnValueChangedListener(this)
        binding.minute.setOnValueChangeListenerInScrolling(this)

        binding.second.setDisplayedValues(getMinuteDisplayValue())
        binding.second.minValue = 0
        binding.second.maxValue = 59
        binding.second.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.second.setOnValueChangedListener(this)
        binding.second.setOnValueChangeListenerInScrolling(this)


        binding.msecond.setDisplayedValues(getMsSecondDisplayValue())
        binding.msecond.minValue = 0
        binding.msecond.maxValue = 9
        binding.msecond.setContentTextTypeface(Typeface.DEFAULT_BOLD)
        binding.msecond.setOnValueChangedListener(this)
        binding.msecond.setOnValueChangeListenerInScrolling(this)
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
        mListener?.onSelection(getTime())
        performHapticFeedback(picker)
    }

    /**
     * 返回选择时间的毫秒数
     */
    fun getTime(): Long {
        return (binding.hour.value * 3600L + binding.minute.value * 60L + binding.second.value) * 1000
    }

    fun setTimeSelectionListener(listener: OnTimeSelectionListener?) {
        this.mListener = listener
    }

    private lateinit var cutTime: DialogTimerSetting.CutTime


    /**
     * 设置固定的时间
     * @param time 毫秒数
     */
    fun setTime(time: Long) {

        val _hour = (time / 1000) / 3600
        val _minute = ((time / 1000) % 3600) / 60
        val _second = (time / 1000) % 60
        val _msecond = (time % 1000) / 100
        binding.hour.value = _hour.toInt()
        binding.minute.value = _minute.toInt()
        binding.second.value = _second.toInt()
        binding.msecond.value = _msecond.toInt()
    }

    fun setTime(cutTime: DialogTimerSetting.CutTime) {
        this.cutTime = cutTime
        initView()

        binding.hour.value = this.cutTime.time.hours
        binding.minute.value = this.cutTime.time.minutes
        binding.second.value = this.cutTime.time.second
        binding.msecond.value = this.cutTime.time.secondDecimal
    }


    /**
     * 时间选择监听
     */
    interface OnTimeSelectionListener {
        fun onSelection(time: Long)
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
}