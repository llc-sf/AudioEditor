package dev.android.player.framework.data.model

data class SettingsCommonItemData(
    val icon: Int,
    val title: String,
    val desc: String,
    var clickable: Boolean = true
)