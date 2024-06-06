package com.san.audioeditor.config

import android.content.Context
import com.san.audioeditor.R
import com.san.audioeditor.view.FaqAnswer
import com.san.audioeditor.view.FaqAnswerBattery
import com.san.audioeditor.view.FaqAnswerCommon
import com.san.audioeditor.view.FaqAnswerLyric
import com.san.audioeditor.view.FaqAnswerScanMusic
import com.san.audioeditor.view.FaqAnswerSongRepeat
import dev.android.player.framework.data.model.FAQItemData
import dev.android.player.framework.utils.toSpannableString
import dev.android.player.framework.utils.toSpannableStringClickable

class AnswerViewProvider {
    fun getAnswerView(context: Context, faqItemData: FAQItemData?, clickCall: ((faqItemData: FAQItemData?) -> Unit)? = null): FaqAnswer {
        faqItemData ?: return FaqAnswerCommon(context)
        var faqAnswerView: FaqAnswer? = null
        when (faqItemData.iconId) {
            R.drawable.ic_faq_trim_end -> {
                faqAnswerView = FaqAnswerCommon(context).apply {
                    val content = context.getString(
                            R.string.download_music_faq_ans_gpt,
                            context.getString(R.string.faq_text_app_name)
                    )
                    setAnswerContent(content.toSpannableString(context, R.color.colorAccent))
                }
            }

            R.drawable.ic_faq_trim_middle -> {
                faqAnswerView = FaqAnswerBattery(context)
            }

            R.drawable.ic_faq_trim_multiple -> {
                faqAnswerView = FaqAnswerScanMusic(context).apply {
                    setJumpText(context.getString(R.string.scan_library))
                    val content = context.getString(R.string.not_show_all_faq_ans1_gpt)
                            .toSpannableString(context, R.color.colorAccent)
                    setContentText(content)
                    val content2 = context.getString(R.string.not_show_all_faq_ans2_gpt, context.getString(R.string.faq_text_app_name))
                            .toSpannableString(context, R.color.colorAccent)
                    setSecondContentText(content2)
                    setOnClickJumpListener {
//                        context.startActivity(Intent(context, ScanMusicActivity::class.java))
                    }
                }
            }

            R.mipmap.ic_faq_trim_point -> {
                faqAnswerView = FaqAnswerLyric(context)
            }

            R.mipmap.ic_faq_trim_quickly -> {
                faqAnswerView = FaqAnswerScanMusic(context).apply {
                    setJumpText(context.getString(R.string.turn_off_equalizer))
                    val content = context.getString(R.string.volume_change_faq_ans1_gpt)
                            .toSpannableString(context, R.color.colorAccent)
                    val context2 = context.getString(R.string.volume_change_faq_ans2_gpt)
                    setContentText(content)
                    setSecondContentText(context2)
                    setOnClickJumpListener {
//                        context.startActivity(Intent(context, EqualizerActivity::class.java))
                    }
                }
            }

            R.drawable.ic_faq_not_find -> {
                faqAnswerView = FaqAnswerCommon(context).apply {
                    val content = String.format(
                            context.getString(R.string.music_pause_switch_faq_answer_gpt),
                            context.getString(R.string.faq_text_app_name)
                    ).toSpannableString(context, R.color.colorAccent)
                    setAnswerContent(content)
                }
            }

            R.drawable.ic_faq_quickly_find -> {
                faqAnswerView = FaqAnswerCommon(context).apply {
                    val content = String.format(
                            context.getString(R.string.add_lyrics_faq_ans_gpt),
                            context.getString(R.string.lyrics_search_online),
                            context.getString(R.string.lyrics_add_local)
                    ).toSpannableString(context, R.color.colorAccent)
                    setAnswerContent(content)
                }
            }

            R.drawable.ic_faq_output_store -> {
                faqAnswerView = FaqAnswerCommon(context).apply {
                    val content = String.format(
                            context.getString(R.string.ads_after_subscribe_faq_answer_gpt),
                            context.getString(R.string.feedback_or_suggestion)
                    ).toSpannableString(context, R.color.colorAccent)
                    setAnswerContent(content)
                }
            }

            R.drawable.ic_faq_sound_quality -> {
                faqAnswerView = FaqAnswerCommon(context).apply {
                    val content = String.format(context.getString(R.string.song_repeat_faq_anw_gpt),
                            context.getString(R.string.shuffle),
                            context.getString(R.string.duplicate_songs_appear_faq_ask_gpt))
                            .toSpannableStringClickable(context, context.getString(R.string.duplicate_songs_appear_faq_ask_gpt), {
                                clickCall?.invoke(faqItemData)
                            }, R.color.colorAccent)
                    setAnswerContentClickable(content)
                }
            }

            R.mipmap.ic_faq_support_files -> {
                faqAnswerView = FaqAnswerSongRepeat(context)
            }
        }
        return (faqAnswerView ?: FaqAnswerCommon(context)).apply { setData(faqItemData) }
    }
}