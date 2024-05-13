package dev.audio.timeruler.timer

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import dev.audio.timeruler.R
import dev.audio.timeruler.databinding.EditExitDialogBinding
import dev.audio.timeruler.databinding.UndoConfirmDialogBinding

/**
 * 撤销确认按钮
 */
class UndoConfirmDialog : BaseBottomTranslucentDialog() {

    companion object {


        fun show(
            manager: FragmentManager?,
        ): UndoConfirmDialog? {
            if (manager == null) {
                return null
            }
            val fragment = UndoConfirmDialog()
            BottomDialogManager.show(manager, fragment)
            return fragment
        }
    }

    private var _binding: UndoConfirmDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = UndoConfirmDialogBinding.inflate(inflater, container, false)
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

        try { // 假设你已经有一个 TextView 实例叫 textView
            val text = context?.resources?.getString(R.string.undo_confrim)
                ?: "" // 创建一个 SpannableString
            var keyText = "‘Confirm’"
            val spannable = SpannableString(text) // 查找 "Confirm" 单词在字符串中的位置
            val start = text.indexOf(keyText)
            val end = start + keyText.length // 创建一个颜色 span 来改变文本颜色
            val colorSpan = ForegroundColorSpan(context!!.resources!!.getColor(R.color.colorAccent)) // 应用这个 span
            spannable.setSpan(colorSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE) // 设置 TextView 的文本
            binding.tvTitle.text = spannable
        } catch (e: Exception) {
            e.printStackTrace()
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
