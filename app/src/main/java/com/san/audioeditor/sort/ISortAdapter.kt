package com.san.audioeditor.sort

/**
 * Created by lisao on 3/5/21
 * 排序中所需字段
 */
interface ISortAdapter {
    fun getSortItems(): List<SortType>
}

/**
 * 空的列表啥样没有
 */
class EmptyAdapter : ISortAdapter {
    override fun getSortItems(): List<SortType> {
        return emptyList()
    }
}

/**
 * 歌曲排序所需数据
 */
open class SongSortAdapter : ISortAdapter {
    override fun getSortItems(): List<SortType> {
        return listOf(SongSorts.SongTitleOrder(), SongSorts.SongArtistSort(), SongSorts.SongAlbumNameSort(), SongSorts.SongAddTimeSort(), SongSorts.SongDurationSort(), SongSorts.SongSizeSort())
    }
}

open class AudioSortAdapter : ISortAdapter {
    override fun getSortItems(): List<SortType> {
        return listOf(SongSorts.SongTitleOrder(), SongSorts.SongAddTimeSort(), SongSorts.SongDurationSort(), SongSorts.SongSizeSort())
    }
}

/**
 * 专辑中歌曲排序所需数据
 * 比普通列表中多了一个按专辑中顺序排序,
 * 不需要按专辑名称，因为专辑名称一样，顺序也一样
 * 不需要按歌手名称，因为歌手一样，顺序也一样
 */
class AlbumSongSortAdapter : ISortAdapter {
    override fun getSortItems(): List<SortType> {
        return listOf(SongSorts.SongTrack(), SongSorts.SongTitleOrder(), SongSorts.SongAddTimeSort(), SongSorts.SongDurationSort(), SongSorts.SongSizeSort())
    }
}

/**
 * 歌手中歌曲排序所需数据
 * 不需要按歌手名称，因为歌手一样，顺序也一样
 */
class ArtistSongSortAdapter : ISortAdapter {
    override fun getSortItems(): List<SortType> {
        return listOf(SongSorts.SongTitleOrder(), SongSorts.SongAlbumNameSort(), SongSorts.SongAddTimeSort(), SongSorts.SongDurationSort(), SongSorts.SongSizeSort())
    }
}


/**
 * 播放列表中歌曲的排序
 * 比普通列表中多了一个添加到播放列表中的时间
 */
class PlayListSongSortAdapter : ISortAdapter {
    override fun getSortItems(): List<SortType> {
        return listOf(SongSorts.SongAddToPlayListTime(), SongSorts.SongTitleOrder(), SongSorts.SongArtistSort(), SongSorts.SongAlbumNameSort(), SongSorts.SongAddTimeSort(), SongSorts.SongDurationSort(), SongSorts.SongSizeSort())
    }
}

/**
 * 专辑列表排序
 */
class AlbumSortAdapter : ISortAdapter {
    override fun getSortItems(): List<SortType> {
        return listOf(AlbumSorts.AlbumNameSort(), AlbumSorts.AlbumArtistSort())
    }
}


/**
 * 歌手列表排序
 */
class ArtistSortAdapter : ISortAdapter {
    override fun getSortItems(): List<SortType> {
        return listOf(ArtistSorts.ArtistNameSort(), ArtistSorts.ArtistAlbumCountSort(), ArtistSorts.ArtistSongCountSort())
    }
}

