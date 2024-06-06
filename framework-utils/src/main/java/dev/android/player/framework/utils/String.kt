package dev.android.player.framework.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat


/**
 * a.mp3 -> mp3
 */
fun String.fileName2Extension(): String {
    val lastIndexOf = lastIndexOf(".")
    return if (lastIndexOf == -1) "" else substring(lastIndexOf + 1)
}

/**
 * a.mp3 -> .mp3
 */
fun String.fileName2ExtensionWithPoint(): String {
    val lastIndexOf = lastIndexOf(".")
    return "." + (if (lastIndexOf == -1) "" else substring(lastIndexOf + 1))
}


/**
 * a.mp3 -> a
 */
fun String.fileName2Name(): String {
    val lastIndexOf = lastIndexOf(".")
    return if (lastIndexOf == -1) "" else substring(0, lastIndexOf)
}


/**
 * /storage/emulated/0/DCIM/Camera/zzzzz7.mp4 -> zzzzz7.mp4
 */
fun String.filePath2FileNameWithExtension(): String {
    val lastIndexOf = lastIndexOf("/")
    return if (lastIndexOf == -1) "" else substring(lastIndexOf + 1)
}


/**
 * /storage/emulated/0/DCIM/Camera/zzzzz7.mp4 -> zzzzz7
 */
fun String.filePath2FileNameWithoutExtension(): String {
    val lastIndexOf = lastIndexOf("/")
    val lastIndexOf2 = lastIndexOf(".")
    return if (lastIndexOf == -1) "" else substring(lastIndexOf + 1, lastIndexOf2)
}


fun String.maxLength(maxLength: Int): String {
    return if (length > maxLength) {
        substring(0, maxLength)
    } else {
        this
    }
}

fun String.toSpannableString(context: Context, clickColor: Int): CharSequence {
    val regex = Regex("<b>(.*?)</b>")
    val color = String.format("#%08X", ContextCompat.getColor(context, clickColor) and 0x00FFFFFF)
    val message = replace(regex) { "<b><font color=\"$color\">${it.groupValues[1]}</font></b>" }
    val newMessage = message.replace("\n", "<br>")
    return  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(newMessage, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(newMessage)
    }
}

fun String.toSpannableStringClickable(context: Context, clickableString: String, clickCall: () -> Unit, clickColor: Int): CharSequence {
    val boldStart = this.indexOf("<b>")
    // 因为处理的文本是去除了 “<b>” 和 "</b>" 标签的所以 boldEnd 位置需要减去 "<b>".length
    val boldEnd = this.indexOf("</b>") - "<b>".length
    val realString = this.replace("<b>", "").replace("</b>", "")
    val spannableString = SpannableString(realString)
    val clickableSpan = object: ClickableSpan(){
        override fun onClick(widget: View) {
            clickCall()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }
    // 对应位置设置点击监听
    val clickIndexStart = realString.indexOf(clickableString)
    if (clickIndexStart != -1) {
        spannableString.setSpan(clickableSpan, boldStart, boldEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    // 加粗和设置白色显示
    val boldSpan = StyleSpan(Typeface.BOLD)
    val whiteColorSpan = ForegroundColorSpan(ContextCompat.getColor(context, clickColor))
    spannableString.setSpan(boldSpan, boldStart, boldEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    spannableString.setSpan(whiteColorSpan, boldStart, boldEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    return spannableString
}
