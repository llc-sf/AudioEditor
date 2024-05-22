package com.music.font

import android.graphics.Typeface
import androidx.annotation.StringDef

/**
 * 字体名称
 */
class PoppinsLight {

    @StringDef(PoppinsLight)
    annotation class FontFamily

    companion object {

        private val Cache = mutableMapOf<String, Typeface>()


        const val PoppinsLight = "PoppinsLight"


        @JvmStatic
        fun getTypefaceFromCache(@FontFamily name: String): Typeface? {
            return Cache[name]
        }

        @JvmStatic
        fun putTypefaceToCache(@FontFamily name: String, typeface: Typeface) {
            Cache[name] = typeface
        }
    }

}