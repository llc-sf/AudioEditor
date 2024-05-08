package dev.android.player.app.business.sort

import android.text.TextUtils
import android.util.LruCache
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import java.text.Collator
import java.text.Normalizer
import java.util.Locale


object Chars {

    /**
     * 拼音的输出格式
     */
    private val mFormat by lazy {
        HanyuPinyinOutputFormat().apply {
            caseType = HanyuPinyinCaseType.UPPERCASE//拼音转换为大写
            toneType = HanyuPinyinToneType.WITHOUT_TONE//不区分声调
            vCharType = HanyuPinyinVCharType.WITH_V//绿->LV
        }
    }

    private val mUnicodeCollator = Collator.getInstance(Locale.US)

    private const val SpecicalChinesePinyin = "ZZZZ"

    /**
     * 判断是否是中文字符
     * @param c
     * @return
     */
    fun isChinese(c: Char): Boolean {
        //0x4E00..0x9FA5 == 一-龥 所有的中文字符
        //Unicode 12295 = 〇
        return c.code in 0x4E00..0x9FA5 || c.code == 12295
    }

    /**
     * 是否是英文大小写
     */
    fun isEnglish(c: Char): Boolean {
        //0x41..0x5A == A-Z
        //0x61..0x7A == a-z
        return c.code in 0x41..0x5A || c.code in 0x61..0x7A//A-Z a-z
    }

    /**
     * 是否是数字
     */
    fun isNumber(c: Char): Boolean {
        //0x30..0x39 == 0-9
        return c.code in 0x30..0x39
    }


    /**
     * 判断是标点符号
     */
    fun isSymbol(c: Char): Boolean {//排序在A之前，并且不是数组字母的情况下，就认为是符号
        //0x21..0x2F == !"#$%&'()*+,-./
        //0x3A..0x40 == :;<=>?@
        //0x5B..0x60 == [\]^_`
        //0x7B..0x7E == {|}~
        return c.code in 0x21..0x2F || c.code in 0x3A..0x40 || c.code in 0x5B..0x60 || c.code in 0x7B..0x7E
    }

    /**
     * 在英语之后
     */
    fun onAfterZ(str: String): Boolean {
        return mUnicodeCollator.compare(str, "Z") > 0
    }

    /**
     * 获取A-Z距离最近的索引值
     */
    tailrec fun getCompatIndexer(str: String, left: Int = 'A'.code, right: Int = 'Z'.code): String {
        val mid = (left + right) / 2
        val s = "${mid.toChar()}".toUpperCase()
        return when {
            left > right || mUnicodeCollator.compare(s, str) == 0 -> s
            mUnicodeCollator.compare(s, str) < 0 -> getCompatIndexer(str, mid + 1, right)
            else -> getCompatIndexer(str, left, mid - 1)
        }
    }


    /**
     * 获取中文拼音字符串
     */
    fun getChineseIndex(c: Char): String {
        val pinyin = getChinesePinyin(c)
        return if (TextUtils.equals(pinyin, SpecicalChinesePinyin)) {
            "#"
        } else {
            pinyin[0].toString().uppercase()
        }
    }

    /**
     * 获取当前字节的拼音
     */
    fun getChinesePinyin(c: Char): String {
        return ChineseCache[c.code] ?: getChinesePinyinInternal(c).apply {
            ChineseCache.put(c.code, this)
        }
    }

    private fun getChinesePinyinInternal(c: Char): String {
        return Pinyin.toPinyin(c)
    }

    /**
     * 获取格式化输出的标准字符串形式
     */
    fun getCompatString(sequence: String): String {
        return CompatStringCache[sequence] ?: Normalizer.normalize(sequence, Normalizer.Form.NFKD)
                .apply { CompatStringCache.put(sequence, this) }
    }

    /**
     * 获取标准字符串
     */
    internal fun getStandardString(str: String): String {
        return StandardStringCache.get(str) ?: run {
            val builder = StringBuilder()
            for (c in str) {
                builder.append(getCompatString(getChinesePinyin(c)))
            }
            builder.toString().uppercase().apply {
                StandardStringCache.put(str, this)
            }
        }
    }

    /**
     * 获取标准字符串
     */
    private val CompatStringCache = LruCache<String, String>(5000)

    private val StandardStringCache = LruCache<String, String>(5000)

    private val ChineseCache = LruCache<Int, String>(5000)


}
