package dev.android.player.app.business.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 排序选项的状态
 * @param key 按照某个字段排序
 * @param order 正序还是倒序
 */
@Parcelize
class SortStatus(var key: String, @OrderType var order: Int) : Parcelable