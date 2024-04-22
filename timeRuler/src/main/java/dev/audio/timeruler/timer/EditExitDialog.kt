package dev.audio.timeruler.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import dev.audio.timeruler.databinding.EditExitDialogBinding


class EditExitDialog : BaseBottomTranslucentDialog() {

    companion object {


        fun show(
            manager: FragmentManager?,
        ): EditExitDialog? {
            if (manager == null) {
                return null
            }
            val fragment = EditExitDialog()
            BottomDialogManager.show(manager, fragment)
            return fragment
        }
    }

    private var _binding: EditExitDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = EditExitDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDiscard.setOnClickListener {
            dismiss()
            activity?.finish()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }


}
