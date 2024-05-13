package dev.audio.timeruler.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import dev.audio.timeruler.databinding.EditExitDialogBinding
import dev.audio.timeruler.databinding.RedoConfirmDialogBinding
import dev.audio.timeruler.databinding.UndoConfirmDialogBinding

/**
 * 重做确认按钮
 */
class RedoConfirmDialog : BaseBottomTranslucentDialog() {

    companion object {


        fun show(
            manager: FragmentManager?,
        ): RedoConfirmDialog? {
            if (manager == null) {
                return null
            }
            val fragment = RedoConfirmDialog()
            BottomDialogManager.show(manager, fragment)
            return fragment
        }
    }

    private var _binding: RedoConfirmDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = RedoConfirmDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirmListener?.onConfirm()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
            onConfirmListener?.onCancel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onConfirmListener = null
    }

    interface OnConfirmListener {
        fun onConfirm()
        fun onCancel()
    }

    private var onConfirmListener: OnConfirmListener? = null

    fun setOnConfirmListener(onConfirmListener: OnConfirmListener) {
        this.onConfirmListener = onConfirmListener
    }


}