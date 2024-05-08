package dev.android.player.app.business.data

import androidx.annotation.IntDef

@IntDef(OrderType.ASC, OrderType.DESC)
annotation class OrderType {
    companion object {
        const val ASC = 1//正序
        const val DESC = -1//倒序
    }
}