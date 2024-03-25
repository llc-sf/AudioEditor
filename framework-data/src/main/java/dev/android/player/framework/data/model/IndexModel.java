package dev.android.player.framework.data.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 2017/4/25.
 */

public abstract class IndexModel implements IndexOperator {
    /**
     * 索引标记
     * key 代表使用的那个字段
     * value 代表索引的具体值
     */
    private final Map<String, String> mIndexerMapping = new HashMap<>();

    /**
     * 设置索引标记
     *
     * @param key
     * @param value
     */
    public void setIndexer(String key, String value) {
        if (key != null) {
            mIndexerMapping.put(key, value);
        }
    }

    /**
     * 获取索引标记
     *
     * @param key
     * @return
     */
    public String getIndexer(String key) {
        if (key == null) {
            return null;
        } else {
            return mIndexerMapping.get(key);
        }
    }
}
