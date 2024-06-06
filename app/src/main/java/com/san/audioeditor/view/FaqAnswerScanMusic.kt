package com.san.audioeditor.view
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.san.audioeditor.R
import com.san.audioeditor.databinding.ViewFaqAnswerScanMusicBinding
import dev.android.player.framework.utils.createGradientRoundedBorderDrawable
import dev.android.player.framework.utils.dimenF
/**
 *  description : 歌曲消失、音量不受控制-回答
 */
class FaqAnswerScanMusic @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : FaqAnswer(context, attributeSet) {

    private var binding: ViewFaqAnswerScanMusicBinding

    init {
        binding = ViewFaqAnswerScanMusicBinding.inflate(LayoutInflater.from(context), null, false)
        binding.faqJump.createGradientRoundedBorderDrawable(
                ContextCompat.getColor(context, R.color.widget_text_start_color),
                ContextCompat.getColor(context, R.color.widget_text_end_color),
                context.dimenF(R.dimen.dp_32), context.dimenF(R.dimen.dp_1))
        bindChildViews(binding.root)
    }

    fun setJumpText(text: String) {
        binding.faqJump.text = text
    }

    fun setContentText(content: CharSequence) {
        binding.faqAnswer1.text = content
    }

    fun setSecondContentText(content: CharSequence) {
        binding.faqAnswer2.text = content
        binding.faqAnswer2.isVisible = true
    }

    fun setOnClickJumpListener(listener: OnClickListener) {
        binding.faqJump.setOnClickListener(listener)
    }
}