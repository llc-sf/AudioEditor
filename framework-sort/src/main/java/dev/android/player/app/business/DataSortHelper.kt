package dev.android.player.app.business

import android.text.TextUtils
import com.android.app.AppProvider
import dev.android.player.app.business.sort.Chars
import dev.android.player.app.business.sort.SortComparator
import dev.android.player.framework.data.model.IndexModel
import dev.android.player.app.business.data.SortStatus
import dev.android.player.framework.utils.LogUtils
import java.lang.reflect.Field
import java.util.*


/**
 * Created by lisao on 3/3/21
 */
object DataSortHelper {


    private const val isSupportMultiLanguage = true//设置是否支持多语言数据排序 有需要了改这个值即可

    /**
     * @param source 待排序数据源
     * @param clz 待排序数据
     * @param major 主要的的排序方式
     * @param seconds 次要的排序方式
     */
    @JvmStatic
    fun <T> sort(source: List<T>, clz: Class<T>, major: SortStatus, vararg seconds: SortStatus): List<T> {
        try {
            synchronized(source) {
                val master = SortComparator(clz, major.key, major.order, isSupportMultiLanguage)//主要排序字段
                var second: Comparator<T> = master
                var comparator: Comparator<T> = master
                if (seconds.isNotEmpty()) {
                    for (sort in seconds) {
                        val then = SortComparator(clz, sort.key, sort.order, isSupportMultiLanguage)
                        comparator = second.thenComparing(then)
                        second = then
                    }
                }
                Collections.sort(source, comparator)
                //已主要排序字段为主，其他排序字段为次要排序字段
                onInjectIndexer(source, clz, major, isSupportMultiLanguage) //注入索引值
            }
        } catch (e: Exception) {
            LogUtils.getInstance(AppProvider.get()).logException(e, false)
            e.printStackTrace()
        }
        return source
    }

    /**
     * 注入源数据索引，当失败时，不设置
     */
    private fun <T> onInjectIndexer(source: List<T>, clz: Class<T>, action: SortStatus, isMultiLanguageMode: Boolean) {
        try {
            val key = getField(clz, action.key) //反射获取需要排序的字段
            key.isAccessible = true
            source.forEach {
                val value = key.get(it)
                setIndexer(it, action.key, value, isMultiLanguageMode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            source.forEach { setIndexer(it, action.key, null, isMultiLanguageMode) }
            LogUtils.getInstance(AppProvider.get()).logException(e, false)
        }
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
     *
     * 设置数据源索引值 针对多语言处理
     *
     *  @param isMultiLanguageMode 是否支持多语言 true 可以针对多语言进行标准化解析 false 只支持英文，其他情况下按照Unicode排序
     *
     */
    private fun <T> setIndexer(source: T, key: String, value: Any?, isMultiLanguageMode: Boolean) {
        if (isMultiLanguageMode) {//支持多语言适配
            if (value is String && !TextUtils.isEmpty(value) && source is IndexModel) {
                val Normal = Chars.getCompatString(value)//获取标准文字
                when {
                    TextUtils.isEmpty(Normal) -> {
                        source.setIndexer(key, null)
                    }
                    Chars.isChinese(Normal[0]) -> {//中文用用第一个字符串拼音大写
                        val C = Chars.getChineseIndex(Normal[0])
                        source.setIndexer(key, "$C")
                    }
                    Chars.isEnglish(Normal[0]) -> {//英语进行大写转换
                        source.setIndexer(key, Normal[0].toString().toUpperCase())
                    }
                    Chars.isNumber(Normal[0]) -> {//数字用.来分组
                        source.setIndexer(key, ".")
                    }
                    Chars.isSymbol(Normal[0]) -> {//符号用@来分组
                        source.setIndexer(key, "@")
                    }
                    else -> {//如果排序状态在Z之后，也是说在最后一个，设置为#。其他情况下不设置索引
                        if (Chars.onAfterZ(Normal)) {
                            source.setIndexer(key, "#")
                        } else {
                            val Indexer = Chars.getCompatIndexer(Normal)
                            source.setIndexer(key, Indexer)
                        }
                    }
                }
            } else if (source is IndexModel) {//非字符串情况下没有索引值
                source.setIndexer(key, null)
            }
        } else {
            if (value is String && !TextUtils.isEmpty(value) && source is IndexModel) {
                when {
                    Chars.isEnglish(value[0]) -> {//英语进行大写转换
                        source.setIndexer(key, value[0].toString().toUpperCase())
                    }
                    else -> {//其他情况还是为 #
                        source.setIndexer(key, "#")
                    }
                }
            } else if (source is IndexModel) {//非字符串情况下没有索引值
                source.setIndexer(key, null)
            }
        }


    }

}



