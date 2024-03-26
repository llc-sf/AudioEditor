package dev.android.player.framework.data.model

/**
 * Created by root on 2017/4/25.
 */
abstract class IndexModel : IndexOperator {
    /**
     * 索引标记
     * key 代表使用的那个字段
     * value 代表索引的具体值
     */
    private val mIndexerMapping: MutableMap<String, String?> = HashMap()

    /**
     * 设置索引标记
     *
     * @param key
     * @param value
     */
    override fun setIndexer(key: String?, value: String?) {
        if (key != null) {
            mIndexerMapping[key] = value
        }
    }

    /**
     * 获取索引标记
     *
     * @param key
     * @return
     */
    override fun getIndexer(key: String?): String? {
        return if (key == null) {
            null
        } else {
            mIndexerMapping[key]
        }
    }
}
