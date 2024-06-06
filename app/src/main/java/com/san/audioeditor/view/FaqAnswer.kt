package com.san.audioeditor.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.san.audioeditor.config.FAQHelper
import com.san.audioeditor.databinding.ViewFaqAnswerBinding
import dev.android.player.framework.data.model.FAQItemData
import dev.android.player.framework.utils.TrackerMultiple

/**
 *  description :
 */
abstract class FaqAnswer @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    private var binding: ViewFaqAnswerBinding
    private var clickNoListener: (() -> Unit)? = null
    private var clickYesListener: (() -> Unit)? = null
    private var data: FAQItemData? = null
    init {
        binding = ViewFaqAnswerBinding.inflate(LayoutInflater.from(context), this, true)
        binding.btnYes.setOnClickListener {
            clickYesListener?.invoke()
            TrackerMultiple.onEvent("FAQ", "Yes_${FAQHelper.getFaqNumber(context, data)}")
            binding.btnYes.isSelected = !binding.btnYes.isSelected
            if (binding.btnYes.isSelected) {//如果选中了yes,则no取消选中
                binding.btnNo.isSelected = false
            }
        }
        binding.btnNo.setOnClickListener {
            clickNoListener?.invoke()
            TrackerMultiple.onEvent("FAQ", "No_${FAQHelper.getFaqNumber(context, data)}")
            binding.btnNo.isSelected = !binding.btnNo.isSelected
            if (binding.btnNo.isSelected) {//如果选中了no,则yes取消选中
                binding.btnYes.isSelected = false
            }
        }
    }

    fun bindChildViews(view: View) {
        binding.flContent.removeAllViews()
        binding.flContent.addView(view, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    fun setClickNoListener(listener: (() -> Unit)) {
        clickNoListener = listener
    }

    fun setClickYesListener(listener: (() -> Unit)) {
        clickYesListener = listener
    }

    fun setData(faqItemData: FAQItemData) {
        this.data = faqItemData
    }
}