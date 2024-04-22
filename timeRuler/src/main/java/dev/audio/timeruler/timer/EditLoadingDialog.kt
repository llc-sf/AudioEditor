package dev.audio.timeruler.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import dev.audio.timeruler.databinding.EditLoadingDialogBinding


class EditLoadingDialog : BaseBottomTranslucentDialog() {

    companion object {


        fun show(
            manager: FragmentManager?,
        ): EditLoadingDialog? {
            if (manager == null) {
                return null
            }
            val fragment = EditLoadingDialog()
            BottomDialogManager.show(manager, fragment)
            return fragment
        }
    }

    private var _binding: EditLoadingDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = EditLoadingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            onCancelListener?.onCancel()
            dismiss()
        }
    }

    fun freshProgress(progress: Int) {
        binding.progress.progress = progress
    }

    interface OnCancelListener {
        fun onCancel()
    }

    private var onCancelListener: OnCancelListener? = null

    fun setOnCancelListener(listener: OnCancelListener) {
        onCancelListener = listener
    }


}
