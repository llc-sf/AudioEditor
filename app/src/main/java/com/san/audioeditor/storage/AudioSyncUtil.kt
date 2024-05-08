package com.san.audioeditor.storage

import android.content.Context
import dev.android.player.app.business.DataSortHelper
import dev.android.player.app.business.SortBusiness
import dev.android.player.framework.data.model.Song
import dev.audio.recorder.utils.Log
import java.util.*

object AudioSyncUtil {

    internal const val TAG = "MediaSyncUtil"

    var songs: List<Song> = ArrayList()

    fun sync(context: Context) {
        songs = getSystemSongs(context)
    }


    private fun AudioSyncUtil.getSystemSongs(context: Context): List<Song> {
        val result: MutableList<Song> = ArrayList()
        try {
            context.contentResolver.query(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null
            )?.use { cursor ->
                result.addAll(cursor.convertSongs())
                DataSortHelper.sort(result, Song::class.java, SortBusiness.getAllSongsSortByAddTimeStatus(), SortBusiness.getAllSongsSortByAddTimeStatus())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e(TAG, "getSystemSongs: " + android.util.Log.getStackTraceString(e))
        }
        return result.apply {
            Log.i(TAG, "getSystemSongs: size = $size")
        }
    }

}