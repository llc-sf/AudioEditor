package com.music.font

import android.graphics.Typeface
import androidx.annotation.StringDef

/**
 * 字体名称
 */
class PoppinsSemiBold {

    @StringDef(PoppinsSemiBold)
    annotation class FontFamily

    companion object {

        private val Cache = mutableMapOf<String, Typeface>()


        const val PoppinsSemiBold = "PoppinsSemiBold"


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