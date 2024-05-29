package com.san.audioeditor.sort

import android.content.Context
import com.san.audioeditor.R
import dev.android.player.framework.data.model.Album
import dev.android.player.framework.data.model.Artist
import dev.android.player.framework.data.model.Song

/**
 * 排序选项
 */
interface SortType {
    fun getTitle(context: Context): String

    fun getKey(): String

    /**
     * 标记是是反向操作，比方说，按照添加时间排序
     * 希望默认的操作是反向操作，就将这个字段设置为 true
     */
    fun isReverse() = false

    fun getOrderOptionASC(context: Context): CharSequence

    fun getOrderOptionDESC(context: Context): CharSequence
}

abstract class SortByName : SortType {
    override fun getOrderOptionASC(context: Context): CharSequence {
        return context.getString(R.string.sort_order_a_z)
    }

    override fun getOrderOptionDESC(context: Context): CharSequence {
        return context.getString(R.string.sort_order_z_a)
    }
}

abstract class SortBySize : SortType {
    override fun getOrderOptionASC(context: Context): CharSequence {
        return context.getString(R.string.sort_order_small_big)
    }

    override fun getOrderOptionDESC(context: Context): CharSequence {
        return context.getString(R.string.sort_order_big_small)
    }
}

/**
 * 歌曲排序
 */
interface SongSorts {
    /**
     * 标题排序
     */
    class SongTitleOrder : SortByName() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.song_name)
        }

        override fun getKey(): String {
            return Song.TITLE
        }
    }

    /**
     * 专辑名排序
     */
    class SongAlbumNameSort : SortByName() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_album_name)
        }

        override fun getKey(): String {
            return Song.ALBUM_NAME
        }
    }

    /**
     * 歌手名称排序
     */
    class SongArtistSort : SortByName() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.artist_name)
        }

        override fun getKey(): String {
            return Song.ARTIST_NAME
        }
    }

    /**
     * 添加时间
     */
    class SongAddTimeSort : SortType {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_time_added)
        }

        override fun getKey(): String {
            return Song.TIME_ADD
        }

        override fun isReverse(): Boolean {
            return true
        }

        override fun getOrderOptionASC(context: Context): CharSequence {
            return context.getString(R.string.sort_order_new_old)
        }

        override fun getOrderOptionDESC(context: Context): CharSequence {
            return context.getString(R.string.sort_order_old_new)
        }
    }

    /**
     * 歌曲时长
     */
    class SongDurationSort : SortType {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_duration)
        }

        override fun getKey(): String {
            return Song.DURATION
        }

        override fun getOrderOptionASC(context: Context): CharSequence {
            return context.getString(R.string.sort_order_short_long)
        }

        override fun getOrderOptionDESC(context: Context): CharSequence {
            return context.getString(R.string.sort_order_long_short)
        }
    }

    /**
     * 歌曲大小
     */
    class SongSizeSort : SortBySize() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_size)
        }

        override fun getKey(): String {
            return Song.SIZE
        }
    }

    /**
     * 专辑中的歌曲顺序
     */
    class SongTrack : SortBySize() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_track)
        }

        override fun getKey(): String {
            return Song.TRACK
        }
    }

    class SongAddToPlayListTime : SortType {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_add_playlist)
        }

        override fun getKey(): String {
            return Song.TIME_ADD_PLAYLIST
        }

        override fun isReverse(): Boolean {
            return true
        }

        override fun getOrderOptionASC(context: Context): CharSequence {
            return context.getString(R.string.sort_order_new_old)
        }

        override fun getOrderOptionDESC(context: Context): CharSequence {
            return context.getString(R.string.sort_order_old_new)
        }
    }
}

/**
 * 专辑排序
 */
interface AlbumSorts {
    /**
     * 专辑名称
     */
    class AlbumNameSort : SortByName() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_album_name)
        }

        override fun getKey(): String {
            return Album.TITLE
        }
    }

    /**
     * 专辑歌手名称
     */
    class AlbumArtistSort : SortByName() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_artist_name)
        }

        override fun getKey(): String {
            return Album.ARTIST
        }
    }
}

/**
 * 歌手排序
 */
interface ArtistSorts {
    /**
     * 歌手名称
     */
    class ArtistNameSort : SortByName() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_artist_name)
        }

        override fun getKey(): String {
            return Artist.NAME
        }
    }

    /**
     * 歌手歌曲数量
     */
    class ArtistSongCountSort : SortBySize() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_order_entry_number_of_songs)
        }

        override fun getKey(): String {
            return Artist.NUMBER_OF_SONGS
        }
    }

    /**
     * 歌手专辑数量
     */
    class ArtistAlbumCountSort : SortBySize() {
        override fun getTitle(context: Context): String {
            return context.getString(R.string.sort_key_number_of_albums)
        }

        override fun getKey(): String {
            return Artist.NUMBER_OF_ALBUMS
        }
    }
}