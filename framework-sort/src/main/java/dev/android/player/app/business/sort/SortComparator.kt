package dev.android.player.app.business.sort

import android.text.TextUtils
import android.util.LruCache
import java.lang.reflect.Field
import java.text.Collator
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * 排序核心算法
 */
class SortComparator<T>(
        private val clz: Class<T>,
        private val name: String,
        private val order: Int,
        private val isMultiLanguageMode: Boolean//是否支持多语言的文字排序
) : Comparator<T> {

    companion object {
        private const val LESS = -1

        private const val MORE = 1

        private const val EQUAL = 0
    }

    /**
     * Android 系统中对于不同的国家语言，那么对于排序规则处理不一样
     * 如果是英文，那么优先级顺序 [0-9]->[A-z]->[中文]
     * 如果是中文环境 那么优先级  [0-9]->[中文]->[A-z]
     */
    private val mUnicodeCollator = Collator.getInstance(Locale.getDefault())

    /**
     * 反射字段需要通过这个字段拿数据
     */
    private val mFiled by lazy {
        getField(clz, name).apply {
            isAccessible = true
        }
    }

    private val mFiledValueCache = LruCache<T, Any?>(200)

    override fun compare(o1: T, o2: T): Int {
        val C1 = getObjectValue(o1)
        val C2 = getObjectValue(o2)

        //按照所选字段进行排序
        val result = when {
            C1 == null && C2 == null -> EQUAL
            C1 == null -> LESS
            C2 == null -> MORE
            C1 is Number && C2 is Number -> {//当比较的双方同时为数字时
                val number1 = C1.toDouble()
                val number2 = C2.toDouble()
                number1.compareTo(number2)
            }

            else -> {//全部转化为大写字母进行比较
                if (isMultiLanguageMode) {
                    onCompareMultiLanguage("$C1", "$C2")
                } else {
                    onCompareEnglish("$C1".uppercase(), "$C2".uppercase())
                }
            }
        }
        return result * order
    }


    /**
     * 获取反射字段中的值，通过缓存方式
     */
    private fun getObjectValue(obj: T): Any? {
        if (mFiledValueCache.get(obj) == null) {
            mFiledValueCache.put(obj, mFiled.get(obj) ?: "")
        }
        return mFiledValueCache.get(obj)
    }

    /**
     * 反射获取排序的字段
     */
    private fun <T> getField(clz: Class<T>, name: String): Field {
        return try {
            clz.getDeclaredField(name)
        } catch (e: Exception) {
            clz.getField(name)
        }
    }


    /**
     * 比较两个字符串 支持多语言
     * @param Str1
     * @param Str2
     * @return 比较的结果
     */
    private fun onCompareMultiLanguage(Str1: String, Str2: String): Int {
        val result = when {
            TextUtils.equals(Str1, Str2) || TextUtils.isEmpty(Str1) && TextUtils.isEmpty(Str2) -> EQUAL
            TextUtils.isEmpty(Str1) -> LESS
            TextUtils.isEmpty(Str2) -> MORE
            else -> {
                val V1 = Chars.getStandardString(Str1)
                val V2 = Chars.getStandardString(Str2)
                //优化排序算法 出现数字，并且数字前方字符相同
                if (isNumberLeftEquals(V1, V2)) {
                    onCompareLesson(V1, V2)
                } else {
                    //进行归一化处理
                    mUnicodeCollator.compare(V1, V2)
                }
            }
        }
//        Log.d("SortComparator", "Origin=$Str1:Standard=${getStandardString(Str1)},Origin=$Str2:Standard=${getStandardString(Str2)},Result=$result")
        return result
    }


    /**
     * 一定要出现数字，并且出现数字的前方字符完全相同
     */
    private fun isNumberLeftEquals(Str1: String, Str2: String): Boolean {
        return if (Str1[0] == Str2[0] && !Chars.isNumber(Str1[0])) {
            val mStr1NumberPosition = getFirstNumberPosition(Str1)
            val mStr2NumberPosition = getFirstNumberPosition(Str2)
            mStr1NumberPosition > 0 && mStr2NumberPosition > 0 &&
                    TextUtils.equals(Str1.substring(0, mStr1NumberPosition), Str2.substring(0, mStr2NumberPosition))
        } else {
            false
        }
    }

    /**
     * 排序课程类相关资源
     */
    private fun onCompareLesson(Str1: String, Str2: String): Int {
        return try {
            //提取数字
            val d1 = Str1.filter { it.isDigit() }
            val d2 = Str2.filter { it.isDigit() }
            //获取两个字符串最大长度
            val max = Math.max(d1.length, d2.length)
            //以前方为0 对齐到相同的字符长度
            val number1 = d1.padStart(max, '0')
            val number2 = d2.padStart(max, '0')
            val result = mUnicodeCollator.compare(number1, number2)
            if (result == EQUAL && d1.length != d2.length) {//如果结果相同
                //哪个字段串短，哪个字符串在前
                (d1.length - d2.length) / abs(d1.length - d2.length) //1 or -1
            } else {
                result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            EQUAL
        }
    }


    private val patternNumber = Pattern.compile("[0-9]")

    /**
     * 获取第一个数字出现的位置
     */
    private fun getFirstNumberPosition(Str: String): Int {
        val matcher = patternNumber.matcher(Str)
        return if (matcher.find()) {
            matcher.start()
        } else {
            -1
        }
    }


    /**
     * 比较两个字符串 只支持英文
     * @param Str1
     * @param Str2
     * @return 比较的结果
     */
    private fun onCompareEnglish(Str1: String, Str2: String): Int {

        val compare = fun(Str1: String, Str2: String): Int {
            return when {
                Chars.isEnglish(Str1[0]) && Chars.isEnglish(Str2[0]) -> {
                    mUnicodeCollator.compare(Str1, Str2)
                }

                Chars.isEnglish(Str1[0]) -> {
                    LESS
                }

                Chars.isEnglish(Str2[0]) -> {
                    MORE
                }

                else -> {
                    mUnicodeCollator.compare(Str1, Str2)
                }
            }
        }

        val result = when {
            TextUtils.equals(Str1, Str2) || TextUtils.isEmpty(Str1) && TextUtils.isEmpty(Str2) -> EQUAL
            TextUtils.isEmpty(Str1) -> LESS
            TextUtils.isEmpty(Str2) -> MORE
            else -> {
                if (isNumberLeftEquals(Str1, Str2)) {
                    onCompareLesson(Str1, Str2)
                } else {
                    compare(Str1, Str2)
                }
            }
        }
//        Log.d("SortComparator", "Value1 = $Str1  Value2 = $Str2  Result = $result")
        return result
    }
}