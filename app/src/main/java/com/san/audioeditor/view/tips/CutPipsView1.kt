package com.san.audioeditor.view.tips

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.san.audioeditor.R
import dev.android.player.framework.utils.ScreenUtil
import dev.android.player.framework.utils.dp

class CutPipsView1 @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    init { // 从XML布局文件中加载布局
        LayoutInflater.from(context).inflate(R.layout.cutpipsview1, this, true)
    }

}