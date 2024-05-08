package dev.android.player.app.business

import android.content.Context
import android.util.Log
import com.android.app.AppProvider
import com.zjsoft.simplecache.SharedPreferenceV2
import dev.android.player.framework.data.model.*
import dev.android.player.app.business.data.OrderType
import dev.android.player.app.business.data.SortStatus
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject

/**
 * 排序的规则记录
 */
object SortBusiness {

    private val TAG = "SortBusiness"

    private val mPreference by lazy { SharedPreferenceV2(AppProvider.get().getSharedPreferences("Sort_Order", Context.MODE_PRIVATE)) }

    private const val KEY_ALL_SONGS = "All_Song"//全部歌曲列表的排序

    private const val KEY_ALL_ALBUMS = "All_Album"//全部专辑列表的排序

    private const val KEY_ALL_ARTISTS = "All_Artist"//全部艺术家列表的排序

    private const val KEY_FOLDER_SONGS = "Folder_Song"//文件夹中的歌曲列表的排序

    //专辑下歌曲列表的排序
    private const val KEY_ALBUM_SONGS = "Album_Song"

    //艺术家下歌曲列表的排序
    private const val KEY_ARTIST_SONGS = "Artist_Song"

    //曲风下歌曲列表的排序
    private const val KEY_GENRE_SONGS = "Genre_Song"

    //video列表的排序
    private const val KEY_ALL_VIDEOS = "All_Video"

    private const val KEY_PLAY_LIST_SONGS = "PlayList(%d)"//播放列表的排序(%d)播放列表的Id

    init {
//        //注册播放列表数据变化监听
//        //播放列表数据改变，清除播放列表的排序状态
//        PlayListStoreObserver.register(object : IPlayListChanged {
//            override fun onDelete(id: Long) {
//                removePlayListSortStatus(id)
//            }
//
//            override fun onAdded(id: Long) {
//                removePlayListSortStatus(id)
//            }
//
//            override fun onContentAdded(id: Long, paths: List<String>) {
//                removePlayListSortStatus(id)
//            }
//
//            override fun onContentDeleted(id: Long, paths: List<String>) {
//                removePlayListSortStatus(id)
//            }
//
//            override fun onContentChanged(id: Long, count: Int) {
//                removePlayListSortStatus(id)
//            }
//        })
    }


    /**
     * 获取全部歌曲列表的排序依据
     */
    @JvmStatic
    fun getAllSongsSortStatus(): SortStatus {
        val status = mPreference.getString(KEY_ALL_SONGS, onEncode(SortStatus(Song.TITLE, OrderType.ASC)))
        return onDecode(status)
    }

    @JvmStatic
    fun getAllSongsSortByAddTimeStatus(): SortStatus {
        val status = mPreference.getString(KEY_ALL_SONGS, onEncode(SortStatus(Song.TIME_ADD, OrderType.DESC)))
        return onDecode(status)
    }

    /**
     * 默认排序方式
     */
    @JvmStatic
    fun getDefaultSongsSortStatus(): SortStatus {
        return SortStatus(Song.TITLE, OrderType.ASC)
    }


    @JvmStatic
    fun setAllSongsSortStatus(status: SortStatus) {
        onSaveDisk {
            mPreference.edit().putString(KEY_ALL_SONGS, onEncode(status)).apply()
        }
    }


    @JvmStatic
    fun setAllSongsSortStatusSync(status: SortStatus) {
        mPreference.edit().putString(KEY_ALL_SONGS, onEncode(status)).apply()
    }

    /**
     * 获取文件夹详情页的歌曲列表排序方式
     */
    @JvmStatic
    fun getFolderSongsSortStatus(): SortStatus {
        val status = mPreference.getString(KEY_FOLDER_SONGS, onEncode(SortStatus(Song.TITLE, OrderType.ASC)))
        return onDecode(status)
    }

    @JvmStatic
    fun setFolderSongsSortStatus(status: SortStatus) {
        onSaveDisk {
            mPreference.edit().putString(KEY_FOLDER_SONGS, onEncode(status)).apply()
        }
    }


    /**
     * 获取文件排序依据
     */
    @JvmStatic
    fun getDirectorySortStatus(): SortStatus {
        return SortStatus(Directory.NAME, OrderType.ASC)
    }

    /**
     * 专辑的排序依据
     */
    @JvmStatic
    fun getAlbumsSortStatus(): SortStatus {
        val status = mPreference.getString(KEY_ALL_ALBUMS, onEncode(SortStatus(Album.TITLE, OrderType.ASC)))
        return onDecode(status)
    }

    /**
     * 设置专辑排序方式
     */
    @JvmStatic
    fun setAlbumsSortStatus(status: SortStatus) {
        onSaveDisk {
            mPreference.edit().putString(KEY_ALL_ALBUMS, onEncode(status)).apply()
        }
    }

    /**
     * 获取默认的专辑排序方式
     */
    @JvmStatic
    fun getDefaultAlbumsSortStatus(): SortStatus {
        return SortStatus(Album.TITLE, OrderType.ASC)
    }

    /**
     * 专辑下歌曲排序
     */
    @JvmStatic
    fun getAlbumSongsSortStatus(): SortStatus {
        val status = mPreference.getString(KEY_ALBUM_SONGS, onEncode(SortStatus(Song.TRACK, OrderType.ASC)))
        return onDecode(status)
    }

    @JvmStatic
    fun setAlbumSongsSortStatus(status: SortStatus) {
        onSaveDisk {
            mPreference.edit().putString(KEY_ALBUM_SONGS, onEncode(status)).apply()
        }
    }


    /**
     * 获取歌手的排序依据
     */
    @JvmStatic
    fun getArtistsSortStatus(): SortStatus {
        val status = mPreference.getString(KEY_ALL_ARTISTS, onEncode(SortStatus(Artist.NAME, OrderType.ASC)))
        return onDecode(status)
    }

    @JvmStatic
    fun setArtistsSortStatus(status: SortStatus) {
        onSaveDisk {
            mPreference.edit().putString(KEY_ALL_ARTISTS, onEncode(status)).apply()
        }
    }

    /**
     * 获取默认的歌手排序方式
     */
    @JvmStatic
    fun getDefaultArtistsSortStatus(): SortStatus {
        return SortStatus(Artist.NAME, OrderType.ASC)
    }

    /**
     * 获取歌手下歌曲排序
     */
    @JvmStatic
    fun getArtistSongsSortStatus(): SortStatus {
        val status = mPreference.getString(KEY_ARTIST_SONGS, onEncode(SortStatus(Song.TITLE, OrderType.ASC)))
        return onDecode(status)
    }

    @JvmStatic
    fun setArtistSongsSortStatus(status: SortStatus) {
        onSaveDisk {
            mPreference.edit().putString(KEY_ARTIST_SONGS, onEncode(status)).apply()
        }
    }


    /**
     * 获取曲风的排序依据
     */
    @JvmStatic
    fun getGenresSortStatus(): SortStatus {
        return SortStatus(Genre.NAME, OrderType.ASC)
    }

    /**
     * 获取默认的曲风排序方式
     */
    @JvmStatic
    fun getDefaultGenresSortStatus(): SortStatus {
        return SortStatus(Genre.NAME, OrderType.ASC)
    }

    /**
     * 获取曲风下歌曲排序
     */
    @JvmStatic
    fun getGenreSongsSortStatus(): SortStatus {
        val status = mPreference.getString(KEY_GENRE_SONGS, onEncode(SortStatus(Song.TITLE, OrderType.ASC)))
        return onDecode(status)
    }

    @JvmStatic
    fun setGenreSongsSortStatus(status: SortStatus) {
        onSaveDisk {
            mPreference.edit().putString(KEY_GENRE_SONGS, onEncode(status)).apply()
        }
    }


    /**
     * 默认排序方式
     * todo
     */
    @JvmStatic
    fun getDefaultVideosSortStatus(key: String): SortStatus {
        return SortStatus(key, OrderType.ASC)
    }

    /**
     * 获取曲风下歌曲排序
     */
    @JvmStatic
    fun getAllVideosSortStatus(): SortStatus {
        val status = mPreference.getString(KEY_ALL_VIDEOS, onEncode(SortStatus(MediaFileInfo.ADDTIEM, OrderType.DESC)))
        return onDecode(status)
    }

    @JvmStatic
    fun setAllVideosSortStatus(status: SortStatus) {
        onSaveDisk {
            mPreference.edit().putString(KEY_ALL_VIDEOS, onEncode(status)).apply()
        }
    }


    /**
     * 获取播放列表中的歌曲排序状态
     * @param id 播放列表id
     */
    @JvmStatic
    fun getPlayListSortStatus(id: Long): SortStatus {
        val key = String.format(KEY_PLAY_LIST_SONGS, id)
        Log.d(TAG, "PlayList Key =$key")
        val status = if (id == PlayList.SPECIAL_ID_FAVORITE) {
            mPreference.getString(key, onEncode(SortStatus(Song.ORDER, OrderType.DESC)))
        } else {
            mPreference.getString(key, onEncode(SortStatus(Song.TIME_ADD_PLAYLIST, OrderType.DESC)))
        }
        return onDecode(status)
    }

    /**
     * 设置播放列表的排序状态
     */
    @JvmStatic
    fun setPlayListSortStatus(id: Long, status: SortStatus) {
        onSaveDisk {
            val key = String.format(KEY_PLAY_LIST_SONGS, id)
            Log.d(TAG, "PlayList Key =$key")
            mPreference.edit().putString(key, onEncode(status)).apply()
        }
    }

    /**
     * 移除播放列表的排序桩体
     */
    @JvmStatic
    fun removePlayListSortStatus(id: Long) {
        onSaveDisk {
            val key = String.format(KEY_PLAY_LIST_SONGS, id)
            Log.d(TAG, "PlayList Key =$key")
            mPreference.edit().remove(key).apply()
        }
    }


    /**
     * 堆排序状态进行解码
     */
    private fun onDecode(str: String?): SortStatus {
        val status = SortStatus("", OrderType.ASC)
        try {
            val obj = JSONObject(str)
            status.key = obj.optString("key", "")
            status.order = obj.optInt("order", OrderType.ASC)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return status
    }

    /**
     * 堆排序状态进行
     */
    private fun onEncode(status: SortStatus): String {
        val obj = JSONObject()
        try {
            obj.put("key", status.key)
            obj.put("order", status.order)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj.toString()
    }

    /**
     * 存储磁盘
     */
    private fun onSaveDisk(action: () -> Unit) {
        Completable.fromAction(action).observeOn(Schedulers.io()).subscribe()
    }

}