package com.san.audioeditor.view
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.san.audioeditor.R
import com.san.audioeditor.databinding.ViewFaqAnswerSongRepeatBinding
import dev.android.player.framework.utils.dimen
import dev.android.player.framework.utils.toSpannableString

/**
 *  description : 歌曲消失、音量不受控制-回答
 */
class FaqAnswerSongRepeat @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : FaqAnswer(context, attributeSet) {

    private var binding: ViewFaqAnswerSongRepeatBinding

    init {
        binding = ViewFaqAnswerSongRepeatBinding.inflate(LayoutInflater.from(context), null, false)

        val content = formatContent(context, context.getString(R.string.duplicate_songs_appear_faq_ans2_gpt))
        binding.faqStep1.text = content
        binding.faqAnswer2.text = String.format(context.getString(R.string.duplicate_songs_appear_faq_ans3_gpt),
                context.getString(R.string.delete_from_device)).toSpannableString(context, R.color.colorAccent)
        binding.faqAnswer3.text = context.getString(R.string.duplicate_songs_appear_faq_ans4_gpt)
        bindChildViews(binding.root)
    }

    private fun formatContent(context: Context, content: String): CharSequence {
        var rootText = content
        return try {
            val replaceIndex = rootText.indexOf("%s")
            val spannableString = SpannableString(rootText)
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_faq_more_small)
            icon?.let {
                icon.setBounds(0, 0, dimen(R.dimen.dp_20), dimen(R.dimen.dp_20))
                val imageSpan = ImageSpan(it, DynamicDrawableSpan.ALIGN_BOTTOM)
                if (replaceIndex != -1) {
                    spannableString.setSpan(imageSpan, replaceIndex, replaceIndex + 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
            spannableString
        } catch (e: Throwable) {
            e.printStackTrace()
            rootText
        }
    }
}