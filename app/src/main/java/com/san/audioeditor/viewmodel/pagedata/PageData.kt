package com.san.audioeditor.viewmodel.pagedata

import dev.android.player.framework.data.model.Song

data class PageData(var id: String)

data class AudioListPageState(
    var songs: List<Song>? = null,
    var notifyItemChangedState: NotifyItemChangedState? = null,
)

data class NotifyItemChangedState(var position: Int)
