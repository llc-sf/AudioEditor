package com.san.audioeditor.view.tips

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.san.audioeditor.R
import dev.android.player.framework.utils.ScreenUtil
import dev.android.player.framework.utils.dp

class CutPipsView @JvmOverloads constructor(context: Context,
                                            attrs: AttributeSet? = null,
                                            defStyleAttr: Int = 0,
                                            isTopArrow: Boolean = false,
                                            isBottomArrow: Boolean = false,
                                            content: String,
                                            actionMsg: String) :
    LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.tips_view, this, true)
        findViewById<ImageView>(R.id.arrow_top).isVisible = isTopArrow
        findViewById<ImageView>(R.id.arrow_bottom).isVisible = isBottomArrow
        val titleTextView = findViewById<TextView>(R.id.title)
        titleTextView.text = content

        // 获取屏幕宽度
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val maxWidth = screenWidth - 32.dp - 20.dp - 40.dp

        // 测量文字宽度
        titleTextView.post {
            val textWidth = titleTextView.paint.measureText(content)
            val params = titleTextView.layoutParams
            params.width = when {
                textWidth < 146.dp -> 146.dp
                textWidth <= 206.dp && textWidth >= 146.dp -> 206.dp
                else -> maxWidth
            }
            titleTextView.layoutParams = params
        }

        findViewById<TextView>(R.id.action).text = actionMsg
    }

    fun topArrow(): ImageView {
        return findViewById<ImageView>(R.id.arrow_top)
    }

    fun bottomArrow(): ImageView {
        return findViewById<ImageView>(R.id.arrow_bottom)
    }

    fun setAction(listener: OnClickListener) {
        findViewById<View>(R.id.action).setOnClickListener(listener)
    }

}