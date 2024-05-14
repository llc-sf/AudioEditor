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
                                            content: String) :
    LinearLayout(context, attrs, defStyleAttr) {

    init { // 从XML布局文件中加载布局
        LayoutInflater.from(context).inflate(R.layout.tips_view, this, true)
        findViewById<ImageView>(R.id.arrow_top).isVisible = isTopArrow
        findViewById<ImageView>(R.id.arrow_bottom).isVisible = isBottomArrow
        findViewById<TextView>(R.id.title).text = content
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