package com.san.audioeditor.view

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import com.san.audioeditor.databinding.ViewFaqAnswerCommonBinding

/**
 *  description :
 */
class FaqAnswerCommon @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : FaqAnswer(context, attributeSet) {

    private var binding: ViewFaqAnswerCommonBinding

    init {
        binding = ViewFaqAnswerCommonBinding.inflate(LayoutInflater.from(context), null, false)
        bindChildViews(binding.root)
    }

    fun setAnswerContent(text: String) {
        binding.faqAnswerTittle.text = text
    }
    
    fun setAnswerContent(text: CharSequence) {
        binding.faqAnswerTittle.text = text
    }

    fun setAnswerContentClickable(text: CharSequence) {
        setAnswerContent(text)
        binding.faqAnswerTittle.movementMethod = LinkMovementMethod.getInstance()
    }
}