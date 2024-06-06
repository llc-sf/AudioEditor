package com.san.audioeditor.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.san.audioeditor.viewmodel.AudioPickViewModel
import dev.android.player.app.business.DataSortHelper
import dev.android.player.app.business.SortBusiness
import dev.android.player.framework.data.model.Song
import dev.audio.ffmpeglib.tool.ContentUtil
import dev.audio.timeruler.utils.AudioFileUtils
import java.io.File
import java.util.*

object AudioSyncUtil {

    internal const val TAG = "MediaSyncUtil"

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    fun sync(context: Context) {
        val newSongs = getSystemSongs(context)
        _songs.postValue(newSongs)
    }

    fun getOutputSongs(songs: List<Song>?): List<Song> {
        return songs?.filter { TextUtils.equals(File(it.path).parent, AudioFileUtils.OUTPUT_FOLDER) }
            ?: emptyList()
    }

    fun getSongsByPath(songs: List<Song>?, path: String): List<Song> {
        if (songs.isNullOrEmpty()) return emptyList()
        if (path == AudioPickViewModel.ALL_AUDIO) return songs
        return songs.filter { TextUtils.equals(File(it.path).parent, path) }
    }

    fun getSongFromUri(context: Context, uri: Uri): Song? {
        return runCatching {
            val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = getSelection(uri)
            val selectionArgs = getSelectionArgs(context, uri)
            context.applicationContext.contentResolver
                .query(contentUri, null, selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.convertSong()
                    } else {
                        null
                    }
                }
        }.onSuccess {
            return it
        }.getOrNull()
    }

    private fun getSelection(uri: Uri): String? {
        return if (ContentUtil.isMediaDocument(uri)) {
            "${MediaStore.Audio.Media._ID}=?"
        } else if (ContentUtil.isExternalStorageDocument(uri)) {
            "${MediaStore.Audio.Media.DATA} = ?"
        } else {
            null
        }
    }

    private fun getSelectionArgs(context: Context, uri: Uri): Array<String>? {
        return if (ContentUtil.isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return arrayOf(split[1])
        } else if (ContentUtil.isExternalStorageDocument(uri)) {
            return arrayOf(ContentUtil.getPath(context, uri) ?: "")
        } else {
            null
        }
    }

    private fun AudioSyncUtil.getSystemSongs(context: Context): List<Song> {
        val result: MutableList<Song> = ArrayList()
        try {
            context.contentResolver.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null)
                ?.use { cursor ->
                    result.addAll(cursor.convertSongs())
                    DataSortHelper.sort(result, Song::class.java, SortBusiness.getAllSongsSortStatus(), SortBusiness.getAllSongsSortByAddTimeStatus())
                }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e(TAG, "getSystemSongs: " + android.util.Log.getStackTraceString(e))
        }

        //todo
        return result.filter { it.duration > 0 }
            .filter { (File(it.path).exists()) }.apply {
            Log.i(TAG, "getSystemSongs: size = $size")
        }
    }

}