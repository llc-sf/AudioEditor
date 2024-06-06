package com.san.audioeditor.config

import android.content.Context
import com.san.audioeditor.R
import dev.android.player.framework.data.model.FAQItemData

object FAQHelper {
    fun getFaqNumber(context: Context, faqData: FAQItemData?): String {
        faqData?: return "1"
        return when (faqData.iconId) {
             R.drawable.ic_faq_download-> "1"
//             R.drawable.ic_faq_interrupted-> if (SystemBatteryOptimization.isShouldSetting(context)) "2-1" else "2-2"
             R.drawable.ic_faq_displayed-> "3"
             R.drawable.ic_faq_lyricsscroll-> "4"
             R.drawable.ic_faq_avoidsound-> "5"
             R.drawable.ic_faq_paused-> "6"
             R.drawable.ic_faq_addlyrics-> "7"
             R.drawable.ic_faq_ad-> "8"
             R.drawable.ic_faq_repeat-> "9"
             R.drawable.ic_faq_songlist-> "10"
            else -> "1"
        }
    }
}