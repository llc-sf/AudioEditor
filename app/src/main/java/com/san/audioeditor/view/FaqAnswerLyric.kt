package com.san.audioeditor.view

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.san.audioeditor.R
import com.san.audioeditor.databinding.ViewFaqAnswerLyricBinding
import dev.android.player.framework.utils.dimen
import dev.android.player.framework.utils.toSpannableString
import dev.audio.timeruler.timer.CenteredImageSpan

/**
 *  description : 歌曲消失、音量不受控制-回答
 */
class FaqAnswerLyric @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : FaqAnswer(context, attributeSet) {

    private var binding: ViewFaqAnswerLyricBinding
    private val tagIcon = "_more_icon_"

    init {
        binding = ViewFaqAnswerLyricBinding.inflate(LayoutInflater.from(context), null, false)
        binding.faqAnswer1.text = context.getString(R.string.lyric_not_scrolling_faq_ans1_gpt).toSpannableString(context, R.color.colorAccent)
        binding.faqAnswer2.text = replaceMoreIcon(context,
                context.getString(R.string.lyric_not_scrolling_faq_ans2_gpt, tagIcon,
                        context.getString(R.string.lyrics_search_online)))
        bindChildViews(binding.root)
    }

    private fun replaceMoreIcon(context: Context, content: String): CharSequence {
        return try {
            val replaceIndex = content.indexOf(tagIcon)
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_faq_more_small)
            val spannableString = SpannableString(content)
            icon?.let {
                it.setBounds(0, 0, dimen(R.dimen.dp_20), dimen(R.dimen.dp_20))
                val imageSpan = CenteredImageSpan(context, it.toBitmap())
                if (replaceIndex != -1) {
                    spannableString.setSpan(imageSpan, replaceIndex, (replaceIndex+tagIcon.length), Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
            spannableString
        } catch (e: Throwable) {
            e.printStackTrace()
            content
        }
    }
}