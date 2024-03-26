package dev.android.player.framework.data.model

/**
 * Created by root on 2017/4/24.
 */
interface IndexOperator {
    fun setIndexer(key: String?, value: String?)
    fun getIndexer(key: String?): String?
}
