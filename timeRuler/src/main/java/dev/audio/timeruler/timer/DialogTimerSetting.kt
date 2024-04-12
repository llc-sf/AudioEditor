package dev.audio.timeruler.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import dev.audio.timeruler.databinding.DialogSleeptimerSettingBinding


class DialogTimerSetting : BaseBottomTranslucentDialog() {

    companion object {

        const val param_start = "start"
        const val param_end = "end"
        const val param_start_min = "start_min"
        const val param_end_max = "end_max"


        fun show(manager: FragmentManager?,start:Long,end:Long,startMin:Long,endMax:Long) {
            if (manager == null) {
                return
            }
            val fragment = DialogTimerSetting()
            fragment.arguments = Bundle().apply {
                putLong(param_start, start)
                putLong(param_end, end)
                putLong(param_start_min, startMin)
                putLong(param_end_max, endMax)
            }
            BottomDialogManager.show(manager, fragment)
        }
    }

    private var _binding: DialogSleeptimerSettingBinding? = null
    private val binding get() = _binding!!

    private var start = 0L
    private var end = 0L
    private var startMin = 0L
    private var endMax = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        start = arguments?.getLong(param_start) ?: -1
        end = arguments?.getLong(param_end) ?: -1
        startMin = arguments?.getLong(param_start_min) ?: -1
        endMax = arguments?.getLong(param_end_max) ?: -1
    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = DialogSleeptimerSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var keyboardMode = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.keyBoard.setOnClickListener {
            keyboardMode = !keyboardMode
            binding.timePickKb.isVisible = keyboardMode
            binding.timePick.isVisible = !keyboardMode
        }

        binding.timePick.setTime(end-start)
    }


}
