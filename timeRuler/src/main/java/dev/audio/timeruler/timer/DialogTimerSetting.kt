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

        fun show(manager: FragmentManager?) {
            if (manager == null) {
                return
            }
            val fragment = DialogTimerSetting()
            BottomDialogManager.show(manager, fragment)
        }
    }

    private var _binding: DialogSleeptimerSettingBinding? = null
    private val binding get() = _binding!!
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
    }


}
