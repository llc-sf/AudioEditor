package dev.android.player.framework.utils

import android.text.SpannableStringBuilder

/**
 * 添加多个Span
 * @param spans SpannableStringBuilder

 */
fun SpannableStringBuilder.appends(text: CharSequence, vararg spans: Any, flag: Int): SpannableStringBuilder {
    append(text)
    spans.forEach {
        setSpan(it, length - text.length, length, flag)
    }
    return this
}