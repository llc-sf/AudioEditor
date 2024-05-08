package dev.android.player.app.business

import dev.android.player.app.business.data.SortStatus

/**
 * 排序后回调接口
 */
fun interface ISortSelectionCallback {
    /**
     * @param status 选择的排序内容
     *
     */
    fun onSortSelection(status: SortStatus)
}