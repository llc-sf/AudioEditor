/*
 * Copyright (c) 2016-present. Drakeet Xu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.android.player.widget.cell

import android.text.TextUtils
import android.widget.SectionIndexer
import dev.android.player.framework.data.model.IndexModel

/**
 * 侧边栏快速定位
 */
open class MultiTypeFastScrollAdapter @JvmOverloads constructor(
        items: List<Any> = emptyList(),
        initialCapacity: Int = 0,
        types: Types = MutableTypes(initialCapacity)
) : MultiTypeAdapter(items, initialCapacity, types), SectionIndexer {

    private val mSectionPositions = mutableListOf<Int>()

    /**
     * 标记查找索引值的Key
     */
    private var mIndexerKey: String? = null

    open fun getIndexerKey(): String? {
        return mIndexerKey
    }

    /**
     * 这里值得注意，为什么不将排序的Key设置在数据源中，
     * 现在项目中用的歌曲列表统一为一个对象，如果切换列表，排序方式发生变化，
     * 那么设置的key就会被其他列表的数据污染，导致返回时数据错乱，这里一定要根据每个页面的不同
     * 设置不同的标记来记录
     *
     * @param key
     */
    open fun setIndexerKey(key: String?) {
        mIndexerKey = key
    }

    protected open fun isIndexerData(): Boolean {
        return true
    }


    override fun getSections(): Array<String> {
        return if (isIndexerData()) {
            val sections: MutableList<String> = ArrayList()
            mSectionPositions.clear()
            for (i in 0 until itemCount) {
                val data = items[i]
                if (data != null && data is IndexModel) {
                    val section: String = data.getIndexer(mIndexerKey)
                    if (!TextUtils.isEmpty(section) && !sections.contains(section)) {
                        sections.add(section)
                        mSectionPositions.add(i)
                    }
                }
            }
            sections.toTypedArray()
        } else {
            arrayOf()
        }
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return if (sectionIndex >= 0 && sectionIndex < mSectionPositions.size) {
            mSectionPositions[sectionIndex]
        } else {
            -1
        }
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }

}
