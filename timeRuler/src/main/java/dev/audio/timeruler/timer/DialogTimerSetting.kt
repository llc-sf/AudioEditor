package dev.audio.timeruler.timer

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import dev.android.player.framework.utils.KeyboardUtil
import dev.audio.timeruler.R
import dev.audio.timeruler.databinding.DialogSleeptimerSettingBinding


class DialogTimerSetting : BaseBottomTranslucentDialog() {

    companion object {

        const val param_start = "start"
        const val param_end = "end"
        const val param_start_min = "start_min"
        const val param_end_max = "end_max"


        fun show(manager: FragmentManager?,
                 start: Long,
                 end: Long,
                 startMin: Long,
                 endMax: Long): DialogTimerSetting? {
            if (manager == null) {
                return null
            }
            val fragment = DialogTimerSetting()
            fragment.arguments = Bundle().apply {
                putLong(param_start, start)
                putLong(param_end, end)
                putLong(param_start_min, startMin)
                putLong(param_end_max, endMax)
            }
            BottomDialogManager.show(manager, fragment)
            return fragment
        }
    }

    private var _binding: DialogSleeptimerSettingBinding? = null
    private val binding get() = _binding!!

    private var start = 0L
    private var end = 0L
    private var min = 0L
    private var max = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        start = arguments?.getLong(param_start) ?: -1
        end = arguments?.getLong(param_end) ?: -1
        min = arguments?.getLong(param_start_min) ?: -1
        max = arguments?.getLong(param_end_max) ?: -1

    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogSleeptimerSettingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog { // 设置对话框的软键盘模式
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return super.onCreateDialog(savedInstanceState)
    }


    private var keyboardMode = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.keyBoard.setOnClickListener {
            keyboardMode = !keyboardMode
            binding.timePickKb.isVisible = keyboardMode
            binding.timePick.isVisible = !keyboardMode
            binding.timePickKb.focous()
            if (!keyboardMode) {
                binding.keyBoard.setImageResource(R.drawable.ic_keyboard)
                if (binding.timePickKb.getTime().time >= min || binding.timePickKb.getTime().time <= max) {
                    binding.timePick.freshTime(Time(binding.timePickKb.getTime().time))
                }
                KeyboardUtil.hideKeyboard(activity)
            } else {
                binding.keyBoard.setImageResource(R.drawable.ic_roll)
            }
        }
        var time = 0L
        if (start != -1L) {
            time = (start)
        } else if (end != -1L) {
            time = (end)
        }
        var cutTime = CutTime(Time(time), Time(min), Time(max))
        binding.timePick.setTime(cutTime)
        binding.timePickKb.setTime(cutTime)

        binding.timePick.setTimeSelectionListener(object : TimerTimePick.OnTimeSelectionListener {
            override fun onSelection(time: Time) {
                binding.timePickKb.setData(time)
            }
        })
        binding.timePickKb.setTimeSelectionListener(object : TimerTimePick.OnTimeSelectionListener {
            override fun onSelection(time: Time) {
                binding.timePick.freshTime(time)
            }
        })
        binding.btnOk.setOnClickListener {
            if (keyboardMode && (binding.timePickKb.getTime().time < min || binding.timePickKb.getTime().time > max)) {
                binding.timePickKb.showErrorTip(start != -1L)
                return@setOnClickListener
            }
            mListener?.onSelection(if (keyboardMode) binding.timePickKb.getTime() else binding.timePick.getTimeBean())
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mListener = null
    }

    interface OnTimeSelectionListener {
        fun onSelection(time: Time)
    }

    var mListener: OnTimeSelectionListener? = null

    fun setTimeSelectionListener(listener: OnTimeSelectionListener?) {
        this.mListener = listener
    }


    class CutTime(var time: Time, var minTime: Time, var maxTime: Time) : Parcelable {
        constructor(parcel: Parcel) : this(parcel.readParcelable(Time::class.java.classLoader)!!, parcel.readParcelable(Time::class.java.classLoader)!!, parcel.readParcelable(Time::class.java.classLoader)!!) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeParcelable(time, flags)
            parcel.writeParcelable(minTime, flags)
            parcel.writeParcelable(maxTime, flags)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<CutTime> {
            override fun createFromParcel(parcel: Parcel): CutTime {
                return CutTime(parcel)
            }

            override fun newArray(size: Int): Array<CutTime?> {
                return arrayOfNulls(size)
            }
        }


    }

    class Time(var time: Long) : Parcelable {

        //时间戳
        val totalSeconds = time / 1000.0
        var hours: Int = 0
            get() {
                return (totalSeconds / 3600).toInt()
            }
        var hoursStr: String = ""
            get() {
                return if (hours < 10) "0$hours" else hours.toString()
            }
        var minutes: Int = 0
            get() {
                return ((totalSeconds % 3600) / 60).toInt()
            }
        var minutesStr: String = ""
            get() {
                return if (minutes < 10) "0$minutes" else minutes.toString()
            }
        var second: Int = 0
            get() {
                return (totalSeconds % 60).toInt()
            }

        var secondStr: String = ""
            get() {
                return if (second < 10) "0$second" else second.toString()
            }

        var secondDecimal: Int = 0
            get() {
                return ((totalSeconds % 60) * 10).toInt() % 10  // 取第一个小数位
            }
        var secondDecimalStr: String = ""
            get() {
                return secondDecimal.toString()
            }

        var timeString: String = ""
            get() {
                return if (hours == 0) {
                    String.format("%02d:%02d.%d", minutes, second, secondDecimal)
                } else {
                    String.format("%02d:%02d:%02d.%d", hours, minutes, second, secondDecimal)
                }
            }

        constructor(parcel: Parcel) : this(parcel.readLong()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(time)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Time> {
            override fun createFromParcel(parcel: Parcel): Time {
                return Time(parcel)
            }

            override fun newArray(size: Int): Array<Time?> {
                return arrayOfNulls(size)
            }
        }

    }


    override fun getSoftInputMode(): Int {
        return WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
    }
}
